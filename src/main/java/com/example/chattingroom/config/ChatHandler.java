package com.example.chattingroom.config;

import com.example.chattingroom.dto.MessageDTO;
import com.example.chattingroom.service.ChatService;
import com.example.chattingroom.service.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;

    private final GeminiService geminiService;

    // 1. 로그 기록용 로거
    private static final Logger log =
            LoggerFactory.getLogger(ChatHandler.class);

    // 2. 세션과 닉네임을 짝지어 기억하는 명부 (동시성 안전)
    private static final Map<WebSocketSession, String>
            sessionMap = new ConcurrentHashMap<>();

    // 자동 포장 기계
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature
                    .WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) throws Exception {

        log.info("[접속 성공] 세션 ID: {}",
                session.getId());

        // HTTP 세션에서 사용자 이름(user) 꺼내기 (WebSocketConfig의 HandshakeInterceptor 덕분)
        Map<String, Object> attributes = session.getAttributes();
        String name = (String) attributes.get("user");
        
        // 로그인 없이 비정상 접근된 경우 방어 로직
        if (name == null || name.trim().isEmpty()) {
            name = "익명_" + session.getId().substring(0, 4);
        }

        // 명부에 세션과 닉네임 등록
        sessionMap.put(session, name);

        // 전원에게 갱신된 접속자 명단 쏘기
        broadcastUserList();

        // [수정됨] 기존 이 자리에 있던 "과거 50개 메시지 강제 전송" 로직을 삭제했습니다.
        // 프론트엔드가 REST API(/api/chat/history)를 통해 직접 호출하므로 중복 방지.
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        String payload = message.getPayload();
        // 로그 출력 (내용은 DEBUG, 행위는 INFO)
        log.debug("[메시지 수신] 내용: {}", payload);

        log.info("[전송 처리] 세션 ID: {}, 길이: {} Byte",
                session.getId(), payload.length());

        // 1. 시스템 메시지 (명단 업데이트 등) 처리
        // 프론트에서 단순 문자열로 보내고 있다면 이 조건을 탑니다.
        if (payload.startsWith("[USERS]")) {
            for (WebSocketSession s : sessionMap.keySet()) {
                s.sendMessage(new TextMessage(payload));
            }
            // 시스템 메시지 처리가 끝났으니 여기서 메서드 종료!
            return;
        }

        // 2. 수신된 JSON 텍스트 해체 (역직렬화)

        // JsonNode : JSON 데이터의 트리 구조를 표현
        JsonNode node = mapper.readTree(payload);
        // 보안상 클라이언트가 보낸 sender(조작 가능성) 대신, 서버의 명부(인증된 정보)에서 꺼냅니다.
        String sender = sessionMap.get(session);
        String content = node.get("content").asText();

        /**
         *  AI 요약봇 개입 (가로채기 & 유니캐스트)
         */
        if ("!요약".equals(content.trim())) {
            log.info("[AI 요약 요청] 세션: {}", session.getId());

            // DB에서 최근 대화 내역 가져와서 하나의 문자열로 합치기.
            List<MessageDTO> history = chatService.getRecentMessages();
            StringBuilder chatData = new StringBuilder();
            for (MessageDTO msg : history) {
                chatData.append(msg.getSender()).append(": ").append(msg.getContent()).append("\n");
            }

            // 비동기 요청 전홍 (subscribe로 백그라운드 위임)
            geminiService.summarizeChat(chatData.toString())
                    .subscribe(
                            summary -> {
                                // 성공 시 실행될 로직
                                try {
                                    MessageDTO botMsg = new MessageDTO();
                                    botMsg.setSender("🤖 AI 요약봇");
                                    botMsg.setContent(summary);
                                    botMsg.setSendTime(LocalDateTime.now());

                                    // 방 전체가 아닌, 요청을 했던 그 세션에만 귓속말
                                    String botJson = mapper.writeValueAsString(botMsg);
                                    session.sendMessage(new TextMessage(botJson));
                                } catch (Exception e) {
                                    log.error("AI 요청 응답 실패", e);
                                }
                            },
                            error -> {// 에러 발생 시 실행 로직
                                log.error("[Gemini API 에러 원인 추적]: ", error);
                                try {
                                    MessageDTO errMSg = new MessageDTO();
                                    errMSg.setSender("🤖 AI 요약봇");
                                    errMSg.setContent("요약 중 서버 오류가 발생했습니다.");
                                    errMSg.setSendTime(LocalDateTime.now());

                                    session.sendMessage(new TextMessage(mapper.writeValueAsString(errMSg)));
                                } catch (Exception e) {
                                    log.error("에러 메시지 전송 실패", e);
                                }
                            }
                    );
            // 요약 명령어는 다른 사람에게 보일 필요 없으므로, 전송을 보냈다면, 여기서 종료.
            return;
        }


        // 3. 백그라운드 스레드에 DB 저장 지시 (비동기)
        chatService.saveMessageAsync(sender, content);

        // 4. 남들에게 방송할 새로운 DTO 상자 조립
        MessageDTO broadcastDto = new MessageDTO();
        broadcastDto.setSender(sender);
        broadcastDto.setContent(content);

        // ★ 핵심: 방송하는 시점의 서버 시간을 정확히 찍어줍니다!
        // (이 덕분에 DB 시간과 화면 시간이 일치하게 됩니다)
        broadcastDto.setSendTime(LocalDateTime.now());

        // 5. 조립된 상자를 다시 JSON 문자열로 포장 (직렬화)
        String broadcastJson =
                mapper.writeValueAsString(broadcastDto);

        // 6. 채팅방의 모든 사람에게 예쁘게 발송
        for (WebSocketSession s : sessionMap.keySet()) {
            s.sendMessage(new TextMessage(broadcastJson));
        }


    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) throws Exception {

        log.info("[접속 종료] 세션 ID: {}",
                session.getId());

        // 나간 사람은 명부에서 지우기
        sessionMap.remove(session);

        // 전원에게 갱신된 접속자 명단 다시 쏘기
        broadcastUserList();
    }

    // 접속자 명단 문자열을 만들어 방송하는 전용 메서드
    private void broadcastUserList() throws Exception {

        // 접속자 이름들을 쉼표로 예쁘게 연결
        String users = String.join(", ",
                sessionMap.values());

        // 클라이언트가 명단임을 알 수 있게 [USERS] 꼬리표 부착
        TextMessage msg = new TextMessage(
                "[USERS]" + users);

        // 모든 세션에 전송
        for (WebSocketSession s : sessionMap.keySet()) {
            s.sendMessage(msg);
        }
    }
}
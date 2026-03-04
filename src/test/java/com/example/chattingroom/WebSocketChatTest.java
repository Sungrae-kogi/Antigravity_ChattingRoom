package com.example.chattingroom;

import com.example.chattingroom.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketChatTest extends BaseIntegrationTest{

    @LocalServerPort
    private int port;   // 켜진 서버의 포트 번호를 가져옴

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void chatMessageTest() throws Exception{
        // 1. 서버가 돌려줄 메시지를 기다리는 우체통
        CompletableFuture<String> resultKeeper =
                new CompletableFuture<>();

        StandardWebSocketClient client =
                new StandardWebSocketClient();

        // 2. 서버 주소 (방금 확인한 /chat)
        String nickname = "테스트유저";
        String url = "ws://localhost:" + port + "/chat?name=" + nickname;

        // 3. 서버에 접속하고, 메시지가 오면 우체통에 넣는 핸들러 정의
        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session,
                                             TextMessage message) {
                String payload = message.getPayload();
                // 입장 알람 [USERS]테스트유저 는 무시하고 서버에서 메시지가 오면 우체통에 쏙!
                if(payload.contains("안녕!")){
                    resultKeeper.complete(message.getPayload());
                }
            }
        }, url).get(1, TimeUnit.SECONDS);

        MessageDTO sendMsg = new MessageDTO();
        sendMsg.setSender(nickname);
        sendMsg.setContent("안녕!");

        // 4. 메시지 발송 (JSON 형태의 문자열로 보냅니다)
        String jsonMessage = objectMapper.writeValueAsString(sendMsg);

        session.sendMessage(new TextMessage(jsonMessage));

        // 5. 서버의 응답을 최대 3초 대기
        String receivedJson =
                resultKeeper.get(3, TimeUnit.SECONDS);

        // 6. 검증 (받은 메시지에 보낸 사람 이름이 들어있는지 확인)
        MessageDTO responseMsg = objectMapper.readValue(receivedJson, MessageDTO.class);

        assertThat(responseMsg.getSender()).contains(nickname);
        assertThat(responseMsg.getContent()).contains("안녕!");

        session.close();
    }
}

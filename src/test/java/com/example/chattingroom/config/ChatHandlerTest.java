package com.example.chattingroom.config;

import com.example.chattingroom.service.ChatService;
import com.example.chattingroom.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class ChatHandlerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private GeminiService geminiService;

    // JSON 파싱은 하는 척이 아니라 실제로 이루어 져야 하므로, SPY 객체 사용.
    @Spy
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 테스트 본체 (생성한 Mock과 Spy 객체를 받음)
    @InjectMocks
    private ChatHandler chatHandler;

    // 브라우저 역할을 할 가짜 웹소켓 세션
    @Mock
    private WebSocketSession session;

    @Test
    @DisplayName("JSON 메시지를 받으면 DB에 저장하고 방송해야 한다.")
    void handleTextMessageTest() throws Exception {

        // given
        // 가짜 세션의 아이디를 요청 받으면 ,"session-1"으로 응답.
        given(session.getId()).willReturn("session-1");
        given(session.getUri()).willReturn(URI.create("ws://localhost/chat?name=테스트유저"));

        // 가짜 과거 채팅 내역 (빈 배열) 준비
        given(chatService.getRecentMessages()).willReturn(new ArrayList<>());

        // Handler의 입장 메서드를 실행시켜 sessionMap에 가짜 세션을 등록
        chatHandler.afterConnectionEstablished(session);

        String inputJson = "{\"sender\":\"테스트유저\", " + "\"content\":\"반갑습니다!\"}";
        TextMessage message = new TextMessage(inputJson);

        // when -> 메시지 수신
        chatHandler.handleTextMessage(session, message);

        // then
        // chatService에게 비동기 저장 지시를 내렸는가?
        // JSON에서 '테스트유저'와 '반갑습니다!' 를 정확히 뜯어냈는지 확인.
        verify(chatService, times(1)).saveMessageAsync("테스트유저", "반갑습니다!");

        // sendMessage가 잘 되었는가?
        // 입장하며 과거 메시지 전송 (1번) -> 방금 받은 채팅 전송 (1번)
        verify(session, times(2)).sendMessage(any(TextMessage.class));

    }
}
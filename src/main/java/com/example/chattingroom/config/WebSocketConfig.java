package com.example.chattingroom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/chat")
                // 💡 변경 포인트: 세션 기반 인터셉터를 버리고, JWT/시큐리티 기반 커스텀 인터셉터 장착!
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        
                        // 1. 이미 JwtAuthenticationFilter가 채워놓은 '신분증(Authentication)'을 꺼냅니다.
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        
                        // 2. 신분증이 제대로 있고, 가짜 '익명 유저'가 아니라면
                        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                            // 3. 챗 핸들러가 쓸 수 있도록 방금 꺼낸 아이디를 "user"라는 이름으로 통에 담아줍니다.
                            attributes.put("user", auth.getName());
                            return true; // 웹소켓 문을 열어줍니다!
                        }
                        
                        // 4. 신분증(JWT 쿠키)이 없으면 웹소켓 연결 자체를 즉시 거부(차단)합니다.
                        return false; 
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                               WebSocketHandler wsHandler, Exception exception) {
                        // 통과된 이후의 작업 (여기서는 필요 없음)
                    }
                })
                // ★ CORS 수정: 모든 출처(*) 허용에서 프론트엔드 서버(3000번)만 허용하도록 변경
                .setAllowedOrigins("http://localhost:3000");
    }
}

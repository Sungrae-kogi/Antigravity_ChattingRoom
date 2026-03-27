package com.example.chattingroom.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    // Interceptor는 DispatcherServlet 과 Controller 사이에 존재하는 검문소 역할, preHandle 메소드는
    // 클라이언트 요청이 Controller에 닿기 전 실행.
    // return true : 통과, return false : 차단

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("Unauthenticated access attempt to: {}", request.getRequestURI());
            response.sendRedirect("/signin");
            return false;
        }
        return true;
    }
}

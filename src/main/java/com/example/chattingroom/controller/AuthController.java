package com.example.chattingroom.controller;

import com.example.chattingroom.dto.LoginRequest;
import com.example.chattingroom.dto.SignupRequest;
import com.example.chattingroom.security.JwtUtil;
import com.example.chattingroom.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @GetMapping("/")
    public String Index() {
        return "redirect:/signin";
    }

    @GetMapping("/signin")
    public String showSigninPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "signin";
    }

    /**
     * 💡 변경 포인트: JWT 전용 로그인 로직
     * 사용자가 전송한 ID/PW를 검증하고 성공 시 쿠키에 JWT를 담아줍니다.
     */
    @PostMapping("/api/auth/login")
    public String login(@ModelAttribute LoginRequest request, HttpServletResponse response, Model model) {
        try {
            // 1. 스프링 시큐리티의 인증 매니저에게 인증을 맡깁니다. (ID/PW 체크)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. 인증에 성공했다면, JWT 토큰을 발행합니다.
            String token = jwtUtil.generateToken(authentication.getName());

            // 3. 발행된 토큰을 쿠키(Cookie)에 정성껏 포장하여 담습니다.
            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true);       // 자바스크립트가 못 건드리게 보안 설정
            jwtCookie.setPath("/");             // 모든 경로에서 쿠키 사용 가능
            jwtCookie.setMaxAge(3600);          // 1시간 동안 유효 (초 단위)
            // jwtCookie.setSecure(true);       // HTTPS 환경이라면 활성화하세요!

            // 4. 응답(Response)에 쿠키를 추가합니다.
            response.addCookie(jwtCookie);

            log.info("사용자 {} 로그인 성공 (JWT 발급 완료)", request.getUsername());

            // 5. 로그인 성공 시 채팅방으로 입장!
            return "redirect:/room";

        } catch (AuthenticationException e) {
            // 인증에 실패했을 때 (ID/PW가 틀렸을 때)
            log.warn("로그인 실패: {}", e.getMessage());
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "signin";
        }
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@ModelAttribute SignupRequest request, Model model) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Username and password are required");
            return "signup";
        }

        boolean success = userService.register(username, password);
        if (success) {
            log.info("New user {} registered", username);
            model.addAttribute("message", "Registration successful. Please sign in.");
            return "signin";
        } else {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }
    }

    /**
     * 💡 변경 포인트: 로그아웃 시 쿠키 삭제
     */
    @GetMapping("/api/auth/logout")
    public String logout(HttpServletResponse response) {
        // 동일한 이름의 쿠키를 만들고 시간을 0으로 설정하면 브라우저가 즉시 삭제합니다.
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return "redirect:/signin";
    }
}

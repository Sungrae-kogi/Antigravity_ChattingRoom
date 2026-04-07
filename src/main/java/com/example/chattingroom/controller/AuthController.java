package com.example.chattingroom.controller;

import com.example.chattingroom.dto.LoginRequest;
import com.example.chattingroom.dto.SignupRequest;
import com.example.chattingroom.security.JwtUtil;
import com.example.chattingroom.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 💡 핵심: @Controller -> @RestController 로 변경! (더 이상 HTML 화면을 찾지 않고, '데이터(JSON)'만 주는 클래스로 변신합니다)
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // 💡 옛날 코드: @GetMapping("/signin") 같이 화면 띄워주던 코드들은 이제 미련 없이 싹 지웠습니다!
    // (화면은 전부 프론트엔드인 3000번 포트에서 알아서 그릴 거니까요!)

    // 💡 기존의 폼 데이터(@ModelAttribute) 대신 -> 프론트에서 보낼 JSON 데이터(@RequestBody)를 받도록 변경
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtUtil.generateToken(authentication.getName());

            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true);       // 자바스크립트가 못 건드리게 보안 설정
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            
            response.addCookie(jwtCookie);
            log.info("사용자 {} 로그인 성공 (JWT 발급 완료)", request.getUsername());

            // 💡 옛날엔 return "redirect:/room" 이었지만, 이제는 백엔드가 "나 성공했어!" 하고 소식(JSON)만 알려줍니다.
            // 이동할지 안할지는 프론트엔드가 이 데이터를 받고 결정합니다.
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "로그인 성공"));

        } catch (AuthenticationException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            
            // 💡 에러 발생 시 에러 메시지(JSP)를 그릴 필요 없이, 에러 코드(401)와 내용만 JSON으로 발송해 프론트가 처리하게 합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("status", "error", "message", "아이디 또는 비밀번호가 일치하지 않습니다."));
        }
    }

    // 주소 통일성을 위해 /signup -> /api/auth/signup 으로 변경
    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> register(@RequestBody SignupRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                                 .body(Map.of("status", "error", "message", "이름과 비밀번호를 입력해주세요."));
        }

        boolean success = userService.register(username, password);
        if (success) {
            log.info("New user {} registered", username);
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "회원가입이 완료되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Map.of("status", "error", "message", "이미 존재하는 아이디입니다."));
        }
    }

    @GetMapping("/api/auth/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok().body(Map.of("status", "success", "message", "로그아웃 되었습니다."));
    }
}

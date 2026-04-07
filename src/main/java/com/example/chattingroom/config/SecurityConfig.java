package com.example.chattingroom.config;

import com.example.chattingroom.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Provides the BCryptPasswordEncoder bean used across the app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 💡 변경 포인트: 직접 인증을 수행할 Manager를 Bean으로 등록합니다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 💡 변경 포인트: 메서드 파라미터로 우리가 만든 jwtAuthenticationFilter를 주입받습니다!
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                // ★ 추가: 방금 아래에서 만든 CORS 설정(corsConfigurationSource)을 시큐리티가 사용하도록 적용합니다!
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. CSRF 보호 기능 끄기 (JWT를 쿠키에 담더라도 SameSite 제어를 쓴다고 가정하고 일단 끕니다)
                .csrf(csrf -> csrf.disable())

                // 2. 세션(Session) 끄기! "서버야 이제 누구 로그인했는지 뇌(메모리)에 저장하지 마!" (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 요청별 접근 권한 (기존과 거의 동일합니다)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/uploads/**", "/error").permitAll()

                        .requestMatchers("/room", "/api/chat/**").authenticated()
                        .anyRequest().permitAll())

                // 4. 기존 방식(Form Login, Logout) 시원하게 날려버리기!
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // 💡 변경: 권한 없는 사용자가 접근 시 화면 이동(리다이렉트)이 아닌 프론트엔드가 알 수 있게 401 에러코드와 JSON 데이터를 응답합니다.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\":\"error\", \"message\":\"로그인이 필요하거나 권한이 없습니다.\"}");
                        }))

                // 5. 핵심: JWT 문지기 세우기!
                // "스프링의 기본 아이디/비번 검사기(UsernamePasswordAuthenticationFilter)가 돌기 전에,
                // 우리가 만든 JWT 검사기(jwtAuthenticationFilter)부터 통과하도록 배치해!"
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ★ 추가: Next.js(3000번)가 안전하게 접속할 수 있도록 출입증을 발급해주는 CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 허용할 프론트엔드 출처(도메인) 설정 (Next.js가 구동되는 3000번 포트)
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 2. 허용할 HTTP 메서드 설정 (모든 방식 허용)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. 허용할 헤더 설정
        config.setAllowedHeaders(List.of("*"));

        // 4. ★ 가장 중요: 프론트와 백엔드가 서버 간에 '쿠키(JWT 토큰)'를 주고받으려면 반드시 true로 설정해야 합니다!
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1시간 동안 브라우저가 이 CORS 설정을 기억하게 합니다.

        // 5. 서버의 모든 경로("/**")에 대해 위의 규칙을 적용합니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

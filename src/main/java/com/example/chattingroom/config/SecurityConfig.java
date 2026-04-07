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
                // 1. CSRF 보호 기능 끄기 (JWT를 쿠키에 담더라도 SameSite 제어를 쓴다고 가정하고 일단 끕니다)
                .csrf(csrf -> csrf.disable())

                // 2. 세션(Session) 끄기! "서버야 이제 누구 로그인했는지 뇌(메모리)에 저장하지 마!" (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 요청별 접근 권한 (기존과 거의 동일합니다)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signin", "/signup", "/css/**", "/js/**", "/error", "/uploads/**").permitAll()

                        // 나중에 브라우저가 아이디/비번을 가지고 "나 토큰(JWT) 줘!"라고 요청할 API 주소입니다.
                        .requestMatchers("/api/auth/login").permitAll()

                        .requestMatchers("/room", "/api/chat/**").authenticated()
                        .anyRequest().permitAll())

                // 4. 기존 방식(Form Login, Logout) 시원하게 날려버리기!
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // 💡 Check 2: 권한 없는 사용자가 접근 시 하얀 에러창 대신 /signin으로 돌려보내기
                // 람다식으로 Config 내부에 직접 작성한 방식 -> 로직이 간단한 경우. 복잡하면 별도 클래스 생성 권장.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/signin");
                        }))

                // 5. 핵심: JWT 문지기 세우기!
                // "스프링의 기본 아이디/비번 검사기(UsernamePasswordAuthenticationFilter)가 돌기 전에,
                // 우리가 만든 JWT 검사기(jwtAuthenticationFilter)부터 통과하도록 배치해!"
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

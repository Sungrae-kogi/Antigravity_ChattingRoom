package com.example.chattingroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Provides the BCryptPasswordEncoder bean used across the app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signin", "/signup", "/css/**", "/js/**", "/error", "/uploads/**").permitAll()
                        .requestMatchers("/room", "/api/chat/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form // Spring Security 기본제공 로그인 창 대신 우리가 만든 로그인 페이지 사용하도록 연결
                        .loginPage("/signin") // 로그인 페이지 URL
                        .loginProcessingUrl("/signin") // 로그인 처리 URL
                        .defaultSuccessUrl("/room", true) // 로그인 성공 시 이동할 URL
                        .permitAll())
                .csrf(csrf -> csrf.disable()) // Disable for now to test websockets cleanly
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/signin")
                        .permitAll());

        return http.build();
    }
}

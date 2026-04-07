package com.example.chattingroom.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 HTTP 요청이 들어올 때마다 가장 먼저 거쳐가는 필터입니다.
 * 쿠키에 들어있는 JWT를 확인하고, 정상 사용자라면 SecurityContext에 등록 해주는 역할을 합니다.
 * 
 * 
 * 필터를 만들었고 security의 기본 filter chain에 끼워넣지않으면 무용지물.
 * security의 기본 filter chain에 끼워넣는 방법은 securityConfig에서 설정하면됨.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 💡 Check 3: 이런 주소들은 굳이 쿠키를 열어보고 DB를 검사하는 '필터 작업'을 할 필요가 없습니다!
        return path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/error") ||
                path.equals("/signin") ||
                path.equals("/signup") ||
                path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. 요청(Request)의 쿠키 목록에서 "JWT_TOKEN"이라는 이름의 쿠키 값을 통째로 꺼내옵니다.
            String jwt = getJwtFromCookies(request);

            // 2. 토큰이 존재하고, 유효성 검사(위조/만료 기간 등)를 무사히 통과했다면?
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {

                // 3. 토큰의 배를 갈라서 진짜 주인공의 ID(username)를 꺼냅니다.
                String username = jwtUtil.getUsernameFromToken(jwt);

                // 4. 꺼낸 ID로 DB를 뒤져서 해당 유저의 권한(Roles)과 정보(UserDetails)를 불러옵니다. -> 모든 요청시 DB 조회가
                // 들어간상황.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 스프링 시큐리티 전용 '인증 완료 도장(Authentication 객체)'을 만듭니다.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 부가 정보(누가, 어느 IP에서 요청했는가 등) 추가
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Security Context (보안 안전 지대)에 인증 완료된 도장을 쾅! 찍어 보관합니다.
                // 👉 이 순간부터 이 사용자는 '로그인 된 합법적 유저'로 서버 어디서든 대우받습니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Security Context에 사용자 인증 정보를 설정할 수 없습니다", ex);
        }

        // 7. 내 할 일(검문)은 끝났으니, 다음 필터나 최종 목적지(컨트롤러)로 요청을 들여보냅니다.
        // 토큰이 아예 없거나 가짜(위조/만료)였다면, 위 6번 과정을 못 거쳤을 테니
        // 뒤에 기다리고 있는 스프링 시큐리티의 권한 검사기가 "넌 무단 침입이야!"라며 쫓아낼 것입니다.
        filterChain.doFilter(request, response);
    }

    /**
     * 클라이언트가 보낸 쿠키 더미 속에서 "JWT_TOKEN"만 쏙 골라내는 도우미 메서드입니다.
     */
    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null; // 못 찾으면 조용히 null 리턴 (어차피 로그인 페이지로 튕길 테니까요!)
    }
}

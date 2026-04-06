package com.example.chattingroom.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT(JSON Web Token)를 생성하고 검증하는 자판기(유틸리티) 클래스입니다.
 */
@Slf4j
@Component
public class JwtUtil {

    // application.properties에서 값을 가져옵니다.
    // (값이 없을 경우를 대비해 기본값도 설정해 줍니다.)
    @Value("${jwt.secret:default_secret_key_which_must_be_very_long_for_hs256_at_least_32_chars}")
    private String secretKeyString;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;

    private Key key;

    // 빈(Bean)이 생성된 직후에 비밀키(Key) 객체를 세팅합니다.
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 1. 출입증(JWT 토큰) 발급 기능
     * 사용자의 아이디(username)를 넣어서 암호화된 토큰을 만들어냅니다.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username) // 토큰 제목에 사용자 아이디 저장
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(expiryDate) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 비밀키와 알고리즘으로 서명(도장 쾅!)
                .compact();
    }

    /**
     * 2. 출입증에서 이름 확인 기능
     * 토큰을 뜯어서 그 안에 적힌 사용자 아이디(username)를 꺼내줍니다.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // 우리가 가진 비밀키로 봉인 해제
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // 처음에 넣었던 username 꺼내기
    }

    /**
     * 3. 출입증 위조/만료 검사 기능
     * 진짜 우리가 발급한 게 맞는지, 기한이 지나진 않았는지 확인합니다.
     */
    public boolean validateToken(String token) {
        try {
            // 이 코드가 에러 없이 넘어가면 정상적인 토큰입니다.
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. 다시 로그인해주세요.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 형태입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰 내부가 비어있거나 잘못되었습니다.");
        }
        return false;
    }
}

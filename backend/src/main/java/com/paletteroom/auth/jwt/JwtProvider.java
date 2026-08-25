package com.paletteroom.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component // 이 클래스를 Spring Bin으로 등록 @Service와 비슷 
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenValidity;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidity) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
    } // Keys.hmacShaKeyFor : 문자열 비밀키 → 서명키 객체
    
    //@Value yam 파일 값을 생성자 파라미터로 주입   access-token-validity: 1800000    refresh-token-validity: 1209600000

    
    // JWT 구조 : 헤더, 페이로드, 서명 
    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact(); 
    }
    
    
    // 토큰 검증: 서명이 맞고 만료 안 됐으면 true 
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)          // 우리 비밀키로 서명 확인
                    .build()
                    .parseSignedClaims(token); // 파싱 — 위조·만료·형식오류면 예외
            return true;
        } catch (Exception e) {
            return false;                      // 어떤 이유든 무효 토큰
        }
    }

    // 토큰에서 userId 꺼내기 (subject에 넣었던 값)
    public Long getUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }
}
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
}
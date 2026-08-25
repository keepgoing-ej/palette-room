package com.paletteroom.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // ① Authorization 헤더에서 토큰 꺼내기 ("Bearer eyJ..." 형식)
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);   // "Bearer " 7글자 뒤가 토큰

            // ② 유효하면 SecurityContext에 인증 정보 등록
            if (jwtProvider.validateToken(token)) {
                boolean valid = jwtProvider.validateToken(token);    // 먼저 검증
                System.out.println("[JWT] token valid? " + valid);    
                if (valid) {
                	Long userId = jwtProvider.getUserId(token);       // 여기서만 꺼냄 삭제 (Long userId = jwtProvider.getUserId(token); )  
                    System.out.println("[JWT] userId = " + userId);   // 임시
                // "이 요청은 userId님의 것" — principal에 userId를 넣음
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        // ③ 다음 필터로 진행 (토큰 없거나 무효면 "익명인 채로" 통과
        //    → permitAll 경로는 OK, 보호 경로는 Security가 401 처리)
        } filterChain.doFilter(request, response);
    
    }
}



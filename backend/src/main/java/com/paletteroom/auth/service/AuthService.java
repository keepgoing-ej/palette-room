package com.paletteroom.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paletteroom.auth.domain.RefreshToken;
import com.paletteroom.auth.jwt.JwtProvider;
import com.paletteroom.auth.repository.RefreshTokenRepository;
import com.paletteroom.common.exception.BusinessException;
import com.paletteroom.common.exception.ErrorCode;
import com.paletteroom.user.domain.User;
import com.paletteroom.user.dto.LoginRequest;
import com.paletteroom.user.dto.LoginResponse;
import com.paletteroom.user.dto.RefreshRequest;
import com.paletteroom.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회 (없어도 비번 틀려도 같은 에러 — 02 문서 UC-01-02)
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. Access Token 발급
        String accessToken = jwtProvider.createAccessToken(user.getId());

        // 4. Refresh Token: 랜덤값 생성 → SHA-256 해시만 DB 저장 (ADR-16)
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(refreshTokenValue))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        // 5. 클라이언트에는 원문 반환 (해시는 DB에만)
        return new LoginResponse(accessToken, refreshTokenValue);
    } 

 // Refresh Token 재발급 (회전 방식, ADR-16)
    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        // 1. 클라이언트가 보낸 원문을 해싱해서 DB 조회
        //    (DB엔 해시만 있으므로 같은 방식으로 해싱해야 비교 가능)
        //    없으면 = 위조됐거나 이미 회전/로그아웃으로 폐기된 토큰 → 401
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(sha256(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        // 2. 만료 검사: 14일 지난 토큰이면 DB에서 지우고 401
        //    (지우는 이유: 죽은 토큰을 남겨두면 쓰레기 행만 쌓임)
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 3. 회전: 방금 사용된 토큰은 즉시 폐기
        //    → 모든 refresh 토큰은 1회용이 됨
        //    → 탈취범과 본인이 같은 토큰을 쓰면 두 번째 시도가 401 = 탈취 감지 가능
        refreshTokenRepository.delete(refreshToken);

        // 4. 토큰 주인 꺼내기 (@ManyToOne LAZY — 이 시점에 실제 user 조회 쿼리 발생)
        User user = refreshToken.getUser();

        // 5. 새 Access Token 발급 (30분)
        String accessToken = jwtProvider.createAccessToken(user.getId());

        // 6. 새 Refresh Token 생성: 원문은 UUID 랜덤값, DB엔 SHA-256 해시만 저장
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(refreshTokenValue))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        // 7. 클라이언트에는 원문 반환 (해시는 DB에만 — 유출돼도 원문 복원 불가)
        return new LoginResponse(accessToken, refreshTokenValue);
    }
    
    
    // 로그아웃
    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByTokenHash(sha256(request.refreshToken()))
                .ifPresent(refreshTokenRepository::delete);
    }
        
    

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
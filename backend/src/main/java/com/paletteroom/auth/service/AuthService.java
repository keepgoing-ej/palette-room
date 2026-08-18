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
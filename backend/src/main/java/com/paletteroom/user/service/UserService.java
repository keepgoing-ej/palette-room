package com.paletteroom.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paletteroom.common.exception.BusinessException;
import com.paletteroom.common.exception.ErrorCode;
import com.paletteroom.user.domain.User;
import com.paletteroom.user.dto.SignupRequest;
import com.paletteroom.user.dto.SignupResponse;
import com.paletteroom.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        User saved = userRepository.save(user);
        return SignupResponse.from(saved);
    }
}
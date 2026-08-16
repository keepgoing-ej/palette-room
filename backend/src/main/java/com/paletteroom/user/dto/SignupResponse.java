package com.paletteroom.user.dto;

import com.paletteroom.user.domain.User;

public record SignupResponse(Long id, String email, String nickname) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
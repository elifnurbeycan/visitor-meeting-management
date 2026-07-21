package com.yasarbilgi.visitormeetingmanagment.auth.dto.response;

import lombok.Builder;

@Builder
public record LoginResponseDto(


        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean mustChangePassword


) {
}
package com.yasarbilgi.visitormeetingmanagment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshTokenRequestDto(

        @NotBlank(message = "{auth.refreshToken.notBlank}")
        String refreshToken

) {
}
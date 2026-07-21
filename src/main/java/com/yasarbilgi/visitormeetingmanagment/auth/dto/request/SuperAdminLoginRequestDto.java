package com.yasarbilgi.visitormeetingmanagment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SuperAdminLoginRequestDto(

        @NotBlank(message = "{auth.email.notBlank}")
        String email,

        @NotBlank(message = "{auth.password.notBlank}")
        String password

) {
}
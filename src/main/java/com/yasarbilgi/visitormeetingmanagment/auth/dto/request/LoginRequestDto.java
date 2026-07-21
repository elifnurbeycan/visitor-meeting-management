package com.yasarbilgi.visitormeetingmanagment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequestDto(

        @NotBlank(message = "{auth.companySlug.notBlank}")
        String companySlug,

        @NotBlank(message = "{auth.email.notBlank}")
        String email,

        @NotBlank(message = "{auth.password.notBlank}")
        String password

) {
}
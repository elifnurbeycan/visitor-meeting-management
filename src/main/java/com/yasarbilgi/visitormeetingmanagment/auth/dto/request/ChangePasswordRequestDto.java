package com.yasarbilgi.visitormeetingmanagment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChangePasswordRequestDto(

        @NotBlank(message = "{auth.currentPassword.notBlank}")
        String currentPassword,

        @NotBlank(message = "{auth.newPassword.notBlank}")
        @Size(min = 8, max = 100, message = "{auth.newPassword.size}")
        String newPassword

) {
}
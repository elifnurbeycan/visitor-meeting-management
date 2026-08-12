package com.yasarbilgi.visitormeetingmanagment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDto(
        @NotBlank(message = "{user.firstName.notBlank}")
        @Size(max = 100, message = "{user.firstName.size}")
        String firstName,

        @NotBlank(message = "{user.lastName.notBlank}")
        @Size(max = 100, message = "{user.lastName.size}")
        String lastName,

        @NotBlank(message = "{user.email.notBlank}")
        @Email(message = "{user.email.invalid}")
        @Size(max = 150, message = "{user.email.size}")
        String email,

        Long jobTitleId
) {
}

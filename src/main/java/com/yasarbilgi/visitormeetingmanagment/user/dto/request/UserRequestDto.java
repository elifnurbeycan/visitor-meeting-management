package com.yasarbilgi.visitormeetingmanagment.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserRequestDto(

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

        @NotBlank(message = "{user.password.notBlank}")
        @Size(min = 8, max = 100, message = "{user.password.size}")
        String password,

        Long jobTitleId,

        @NotEmpty(message = "{user.roleIds.notEmpty}")
        Set<Long> roleIds

) {
}
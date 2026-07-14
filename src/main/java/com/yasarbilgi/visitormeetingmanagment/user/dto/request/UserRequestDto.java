package com.yasarbilgi.visitormeetingmanagment.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequestDto(

        @NotBlank(message = "Ad boş olamaz")
        @Size(max = 100, message = "Ad en fazla 100 karakter olabilir")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir")
        String lastName,

        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        @Size(max = 150, message = "Email en fazla 150 karakter olabilir")
        String email,

        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 8, max = 100, message = "Şifre en az 8 karakter olmalıdır")
        String password,

        Long jobTitleId,

        @NotEmpty(message = "Kullanıcıya en az bir rol atanmalıdır")
        Set<Long> roleIds

) {
}
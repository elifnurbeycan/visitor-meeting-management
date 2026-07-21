package com.yasarbilgi.visitormeetingmanagment.company.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CompanyRequestDto(

        @NotBlank(message = "{company.name.notBlank}")
        @Size(max = 150, message = "{company.name.size}")
        String name,

        @NotBlank(message = "{company.slug.notBlank}")
        @Size(max = 100, message = "{company.slug.size}")
        @Pattern(
                regexp = "^[a-z0-9-]+$",
                message = "{company.slug.pattern}"
        )
        String slug,

        @Size(max = 1000, message = "{company.description.size}")
        String description,

        @Size(max = 20, message = "{company.taxNumber.size}")
        String taxNumber,

        @NotBlank(message = "{company.contactEmail.notBlank}")
        @Email(message = "{company.contactEmail.invalid}")
        @Size(max = 150, message = "{company.contactEmail.size}")
        String contactEmail,

        @Size(max = 20, message = "{company.contactPhone.size}")
        String contactPhone,

        @Size(max = 500, message = "{company.address.size}")
        String address,

        @Size(max = 100, message = "{company.industry.size}")
        String industry,

        // ----- Owner (ilk kullanıcı) bilgileri -----

        @NotBlank(message = "{user.firstName.notBlank}")
        @Size(max = 100, message = "{user.firstName.size}")
        String ownerFirstName,

        @NotBlank(message = "{user.lastName.notBlank}")
        @Size(max = 100, message = "{user.lastName.size}")
        String ownerLastName,

        @NotBlank(message = "{user.email.notBlank}")
        @Email(message = "{user.email.invalid}")
        @Size(max = 150, message = "{user.email.size}")
        String ownerEmail,

        @NotBlank(message = "{user.usernameRequired}")
        @Size(max = 50, message = "{user.username.size}")
        String ownerUsername,

        @NotBlank(message = "{user.password.notBlank}")
        @Size(min = 8, max = 100, message = "{user.password.size}")
        String ownerPassword

) {
}
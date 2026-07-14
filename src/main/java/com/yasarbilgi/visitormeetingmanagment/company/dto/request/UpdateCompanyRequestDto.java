package com.yasarbilgi.visitormeetingmanagment.company.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateCompanyRequestDto(

        @NotBlank(message = "Company name cannot be blank.")
        @Size(max = 150, message = "Company name cannot exceed 150 characters.")
        String name,

        @NotBlank(message = "Slug cannot be blank.")
        @Size(max = 100, message = "Slug cannot exceed 100 characters.")
        @Pattern(
                regexp = "^[a-z0-9-]+$",
                message = "Slug can only contain lowercase letters, numbers and hyphens."
        )
        String slug,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
        String description,

        @Size(max = 20, message = "Tax number cannot exceed 20 characters.")
        String taxNumber,

        @NotBlank(message = "Contact email cannot be blank.")
        @Email(message = "Please enter a valid email address.")
        @Size(max = 150, message = "Contact email cannot exceed 150 characters.")
        String contactEmail,

        @Size(max = 20, message = "Contact phone cannot exceed 20 characters.")
        String contactPhone,

        @Size(max = 500, message = "Address cannot exceed 500 characters.")
        String address,

        @Size(max = 100, message = "Industry cannot exceed 100 characters.")
        String industry

) {
}
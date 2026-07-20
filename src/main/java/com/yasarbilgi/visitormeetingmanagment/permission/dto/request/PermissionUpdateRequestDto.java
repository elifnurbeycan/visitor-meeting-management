package com.yasarbilgi.visitormeetingmanagment.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PermissionUpdateRequestDto(

        @NotBlank(message = "{permission.name.notBlank}")
        @Size(max = 150, message = "{permission.name.size}")
        String name,

        @Size(max = 500, message = "{permission.description.size}")
        String description

) {
}

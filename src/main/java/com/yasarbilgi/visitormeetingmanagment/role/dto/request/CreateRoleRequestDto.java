package com.yasarbilgi.visitormeetingmanagment.role.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateRoleRequestDto(

        @NotBlank(message = "{role.name.notBlank}")
        @Size(max = 100, message = "{role.name.size}")
        String name,

        @Size(max = 500, message = "{role.description.size}")
        String description,

        Set<Long> permissionIds

) {
}
package com.yasarbilgi.visitormeetingmanagment.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record DepartmentRequestDto(

        @NotBlank(message = "{department.name.notBlank}")
        @Size(max = 100, message = "{department.name.size}")
        String name,

        @Size(max = 500, message = "{department.description.size}")
        String description

) {
}
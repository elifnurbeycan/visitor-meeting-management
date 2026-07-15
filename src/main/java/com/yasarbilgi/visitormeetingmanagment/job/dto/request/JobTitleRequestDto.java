package com.yasarbilgi.visitormeetingmanagment.job.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record JobTitleRequestDto(

        @NotBlank(message = "{jobTitle.name.notBlank}")
        @Size(max = 100, message = "{jobTitle.name.size}")
        String name,

        @Size(max = 500, message = "{jobTitle.description.size}")
        String description,

        Set<Long> defaultRoleIds

) {
}
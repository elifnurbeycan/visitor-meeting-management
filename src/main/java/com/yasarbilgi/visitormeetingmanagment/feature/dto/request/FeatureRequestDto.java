package com.yasarbilgi.visitormeetingmanagment.feature.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FeatureRequestDto(

        @NotBlank(message = "{feature.name.notBlank}")
        @Size(max = 100, message = "{feature.name.size}")
        String name,

        @Size(max = 500, message = "{feature.description.size}")
        String description

) {
}

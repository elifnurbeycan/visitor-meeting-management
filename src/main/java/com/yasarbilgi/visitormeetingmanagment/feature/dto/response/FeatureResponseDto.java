package com.yasarbilgi.visitormeetingmanagment.feature.dto.response;

import lombok.Builder;

@Builder
public record FeatureResponseDto(

        Long id,
        String name,
        String description,
        boolean active

) {
}

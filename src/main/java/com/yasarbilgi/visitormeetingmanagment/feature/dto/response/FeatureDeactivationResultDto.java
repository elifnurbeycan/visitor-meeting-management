package com.yasarbilgi.visitormeetingmanagment.feature.dto.response;

import lombok.Builder;

@Builder
public record FeatureDeactivationResultDto(
        Long featureId,
        int affectedRoomCount
) {
}
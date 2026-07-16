package com.yasarbilgi.visitormeetingmanagment.room.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record RoomResponseDto(

        Long id,
        String name,
        String location,
        int capacity,
        String description,
        Set<FeatureSummary> features,
        boolean active

) {

    @Builder
    public record FeatureSummary(
            Long id,
            String name
    ) {
    }
}
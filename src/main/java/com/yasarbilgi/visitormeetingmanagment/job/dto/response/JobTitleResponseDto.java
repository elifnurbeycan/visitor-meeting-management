package com.yasarbilgi.visitormeetingmanagment.job.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record JobTitleResponseDto(

        Long id,
        String name,
        String description,
        Set<RoleSummary> defaultRoles,
        boolean active

) {

    @Builder
    public record RoleSummary(
            Long id,
            String name
    ) {
    }
}
package com.yasarbilgi.visitormeetingmanagment.user.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
public record UserResponseDto(

        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        JobTitleSummary jobTitle,
        boolean owner,
        boolean active,
        Set<RoleSummary> roles,
        Instant createdAt

) {

    @Builder
    public record JobTitleSummary(
            Long id,
            String name
    ) {
    }

    @Builder
    public record RoleSummary(
            Long id,
            String name
    ) {
    }
}
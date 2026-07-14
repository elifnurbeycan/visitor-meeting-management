package com.yasarbilgi.visitormeetingmanagment.user.dto.response;

import java.time.Instant;
import java.util.Set;

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

    public record JobTitleSummary(
            Long id,
            String name
    ) {
    }

    public record RoleSummary(
            Long id,
            String name
    ) {
    }
}
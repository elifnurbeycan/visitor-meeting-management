package com.yasarbilgi.visitormeetingmanagment.platform.dto.response;

import lombok.Builder;

@Builder
public record SuperAdminResponseDto(
        Long id,
        String email,
        String fullName,
        boolean active
) {
}
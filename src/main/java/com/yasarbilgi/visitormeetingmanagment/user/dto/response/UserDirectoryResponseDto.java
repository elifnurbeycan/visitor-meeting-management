package com.yasarbilgi.visitormeetingmanagment.user.dto.response;

import lombok.Builder;

@Builder
public record UserDirectoryResponseDto(
        Long id,
        String fullName,
        String email,
        String username
) {
}
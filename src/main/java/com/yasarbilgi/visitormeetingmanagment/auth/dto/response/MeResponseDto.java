package com.yasarbilgi.visitormeetingmanagment.auth.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record MeResponseDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String username,
        boolean owner,
        Long companyId,
        String companyName,
        Set<String> roleNames,
        Set<String> permissions
) {
}
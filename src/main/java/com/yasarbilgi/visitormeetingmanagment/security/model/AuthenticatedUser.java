package com.yasarbilgi.visitormeetingmanagment.security.model;

import lombok.Builder;

import java.util.Set;

@Builder
public record AuthenticatedUser(
        Long userId,
        Long companyId,
        Set<String> permissions,
        boolean superAdmin
) {
}
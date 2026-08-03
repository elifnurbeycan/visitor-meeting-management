package com.yasarbilgi.visitormeetingmanagment.audit.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AuditLogResponseDto(
        Long id,
        Long companyId,
        String companyName,
        Long actorUserId,
        String actorName,
        String action,
        String targetType,
        Long targetId,
        String details,
        Instant createdAt
) {
}
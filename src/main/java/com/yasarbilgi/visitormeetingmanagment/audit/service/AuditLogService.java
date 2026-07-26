package com.yasarbilgi.visitormeetingmanagment.audit.service;

public interface AuditLogService {

    void log(
            Long companyId,
            Long actorUserId,
            String action,
            String targetType,
            Long targetId,
            String details
    );
}
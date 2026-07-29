package com.yasarbilgi.visitormeetingmanagment.audit.service;

public interface AuditLogExportService {

    byte[] exportForCompany(Long companyId, String targetType);

    byte[] exportForSuperAdmin(Long companyId, String targetType);
}
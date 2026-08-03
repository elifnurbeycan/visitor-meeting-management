package com.yasarbilgi.visitormeetingmanagment.audit.service;

import java.util.List;

public interface AuditLogExportService {

    byte[] exportForCompany(Long companyId, List<String> targetTypes);

    byte[] exportForSuperAdmin(Long companyId, List<String> targetTypes);
}
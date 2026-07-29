package com.yasarbilgi.visitormeetingmanagment.audit.controller;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogExportService;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Şirket içi audit log dışa aktarma (export) endpoint'i.
 * URL şeması: /api/v1/audit-logs/export

 * companyId her zaman JWT'den gelir — bir şirketin kullanıcısı,
 * sadece kendi şirketinin loglarını indirebilir.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogExportService auditLogExportService;

    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String targetType
    ) {
        byte[] excelBytes = auditLogExportService.exportForCompany(currentUser.companyId(), targetType);

        String filename = "audit-log-" + currentUser.companyId() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}
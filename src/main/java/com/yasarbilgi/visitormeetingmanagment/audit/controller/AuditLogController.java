package com.yasarbilgi.visitormeetingmanagment.audit.controller;

import com.yasarbilgi.visitormeetingmanagment.audit.dto.response.AuditLogResponseDto;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogExportService;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Şirket içi audit log görüntüleme (JSON) ve dışa aktarma (Excel) endpoint'leri.
 * URL şeması: /api/v1/audit-logs, /api/v1/audit-logs/export
 * <p>
 * companyId her zaman JWT'den gelir — bir şirketin kullanıcısı,
 * sadece kendi şirketinin loglarını görebilir/indirebilir. targetTypes
 * verilmezse (ya da boş bırakılırsa) tüm kategoriler dahil edilir.
 * <p>
 * Geçerli kategoriler: USER, USER_PERMISSION, ROOM, RESERVATION, COMPANY,
 * ROLE, FEATURE, DEPARTMENT, JOB_TITLE, AUTH.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogExportService auditLogExportService;
    private final AuditLogService auditLogService;

    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> getLogs(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) List<String> targetTypes,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AuditLogResponseDto> logs = PageResponse.of(
                auditLogService.getLogsForCompany(currentUser.companyId(), targetTypes, pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) List<String> targetTypes
    ) {
        byte[] excelBytes = auditLogExportService.exportForCompany(currentUser.companyId(), targetTypes);

        String filename = "audit-log-" + currentUser.companyId() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}
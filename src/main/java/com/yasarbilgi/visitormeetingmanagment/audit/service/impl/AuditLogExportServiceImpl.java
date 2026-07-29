package com.yasarbilgi.visitormeetingmanagment.audit.service.impl;

import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import com.yasarbilgi.visitormeetingmanagment.audit.repository.AuditLogRepository;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogExportService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogExportServiceImpl implements AuditLogExportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final String[] HEADERS = {
            "ID", "Şirket", "Kullanıcı ID", "İşlem", "Hedef Tip", "Hedef ID", "Detay", "Tarih"
    };

    private final AuditLogRepository auditLogRepository;

    @Override
    public byte[] exportForCompany(Long companyId, String targetType) {
        log.info("Exporting audit logs for company: {}, targetType: {}", companyId, targetType);

        List<AuditLog> logs = (targetType == null || targetType.isBlank())
                ? fetchAll(auditLogRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId, Pageable.unpaged()))
                : fetchAll(auditLogRepository.findAllByCompanyIdAndTargetTypeOrderByCreatedAtDesc(companyId, targetType, Pageable.unpaged()));

        return buildExcel(logs);
    }

    @Override
    public byte[] exportForSuperAdmin(Long companyId, String targetType) {
        log.info("SuperAdmin exporting audit logs. companyId: {}, targetType: {}", companyId, targetType);

        List<AuditLog> logs;
        if (companyId != null && (targetType == null || targetType.isBlank())) {
            logs = fetchAll(auditLogRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId, Pageable.unpaged()));
        } else if (companyId != null) {
            logs = fetchAll(auditLogRepository.findAllByCompanyIdAndTargetTypeOrderByCreatedAtDesc(companyId, targetType, Pageable.unpaged()));
        } else if (targetType == null || targetType.isBlank()) {
            logs = fetchAll(auditLogRepository.findAllByOrderByCreatedAtDesc(Pageable.unpaged()));
        } else {
            logs = fetchAll(auditLogRepository.findAllByTargetTypeOrderByCreatedAtDesc(targetType, Pageable.unpaged()));
        }

        return buildExcel(logs);
    }

    private List<AuditLog> fetchAll(Page<AuditLog> page) {
        return page.getContent();
    }

    private byte[] buildExcel(List<AuditLog> logs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Audit Loglari");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (AuditLog logEntry : logs) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(logEntry.getId());
                row.createCell(1).setCellValue(logEntry.getCompany().getName());
                row.createCell(2).setCellValue(logEntry.getActorUserId() != null ? logEntry.getActorUserId() : 0);
                row.createCell(3).setCellValue(logEntry.getAction());
                row.createCell(4).setCellValue(logEntry.getTargetType());
                row.createCell(5).setCellValue(logEntry.getTargetId() != null ? logEntry.getTargetId() : 0);
                row.createCell(6).setCellValue(logEntry.getDetails());
                row.createCell(7).setCellValue(DATE_FORMATTER.format(logEntry.getCreatedAt()));
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate audit log Excel export", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
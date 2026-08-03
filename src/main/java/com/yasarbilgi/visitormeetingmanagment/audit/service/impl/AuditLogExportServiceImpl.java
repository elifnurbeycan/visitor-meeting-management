package com.yasarbilgi.visitormeetingmanagment.audit.service.impl;

import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import com.yasarbilgi.visitormeetingmanagment.audit.repository.AuditLogRepository;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogExportService;
import com.yasarbilgi.visitormeetingmanagment.common.constant.AppConstants;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.platform.repository.SuperAdminRepository;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Audit log kayıtlarını Excel'e dışa aktarır. Birden fazla kategori
 * (targetType) seçilirse, her kategori KENDİ SHEET'İNE yazılır — böylece
 * "User + Reservation" gibi bir seçimde, iki ayrı sekme (tab) halinde,
 * karışmadan görüntülenebilir.

 * "Kullanıcı" sütunu, ham actorUserId yerine okunaklı bir isim gösterir.
 * Aktör bir şirket kullanıcısı (User) ya da bir SuperAdmin olabilir — ikisi
 * de tek seferde, toplu (N+1 sorgu problemine düşmeden) çözülür.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogExportServiceImpl implements AuditLogExportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(AppConstants.ISTANBUL_ZONE);

    private static final String[] HEADERS = {
            "ID", "Şirket", "Kullanıcı", "İşlem", "Hedef Tip", "Hedef ID", "Detay", "Tarih"
    };

    private static final String UNKNOWN_ACTOR_LABEL = "Sistem";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;

    @Override
    public byte[] exportForCompany(Long companyId, List<String> targetTypes) {
        log.info("Exporting audit logs for company: {}, targetTypes: {}", companyId, targetTypes);

        List<AuditLog> logs = hasSelection(targetTypes)
                ? fetchAll(auditLogRepository.findAllByCompanyIdAndTargetTypeInOrderByCreatedAtDesc(
                companyId, targetTypes, Pageable.unpaged()))
                : fetchAll(auditLogRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId, Pageable.unpaged()));

        return buildExcel(logs);
    }

    @Override
    public byte[] exportForSuperAdmin(Long companyId, List<String> targetTypes) {
        log.info("SuperAdmin exporting audit logs. companyId: {}, targetTypes: {}", companyId, targetTypes);

        List<AuditLog> logs;
        if (companyId != null && hasSelection(targetTypes)) {
            logs = fetchAll(auditLogRepository.findAllByCompanyIdAndTargetTypeInOrderByCreatedAtDesc(
                    companyId, targetTypes, Pageable.unpaged()));
        } else if (companyId != null) {
            logs = fetchAll(auditLogRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId, Pageable.unpaged()));
        } else if (hasSelection(targetTypes)) {
            logs = fetchAll(auditLogRepository.findAllByTargetTypeInOrderByCreatedAtDesc(targetTypes, Pageable.unpaged()));
        } else {
            logs = fetchAll(auditLogRepository.findAllByOrderByCreatedAtDesc(Pageable.unpaged()));
        }

        return buildExcel(logs);
    }

    private boolean hasSelection(List<String> targetTypes) {
        return targetTypes != null && !targetTypes.isEmpty();
    }

    private List<AuditLog> fetchAll(Page<AuditLog> page) {
        return page.getContent();
    }

    /**
     * Kayıtları targetType'a göre gruplar, her grup için ayrı bir sheet
     * oluşturur. Aktör isimlerini (bkz. resolveActorNames) tüm sheet'ler
     * için TEK SEFERDE çözer, her sheet için tekrar sorgu atmaz.
     */
    private byte[] buildExcel(List<AuditLog> logs) {
        Map<Long, String> actorNames = resolveActorNames(logs);

        Map<String, List<AuditLog>> byCategory = logs.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getTargetType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (byCategory.isEmpty()) {
                writeCategorySheet(workbook, "Audit Loglari", List.of(), actorNames);
            } else {
                for (Map.Entry<String, List<AuditLog>> entry : byCategory.entrySet()) {
                    writeCategorySheet(workbook, entry.getKey(), entry.getValue(), actorNames);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate audit log Excel export", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * actorUserId, ya bir User'a ya da (login/logout gibi AUTH olaylarında)
     * bir SuperAdmin'e ait olabilir — ikisi de tek birer toplu sorguyla
     * (findAllById) çözülüyor, log sayısı kaç olursa olsun 2 sorgu yeterli.
     */
    private Map<Long, String> resolveActorNames(List<AuditLog> logs) {
        Set<Long> actorIds = logs.stream()
                .map(AuditLog::getActorUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (actorIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> names = new HashMap<>();
        userRepository.findAllById(actorIds)
                .forEach(user -> names.put(user.getId(), user.getFullName() + " (ID: " + user.getId() + ")"));

        Set<Long> unresolved = new HashSet<>(actorIds);
        unresolved.removeAll(names.keySet());

        if (!unresolved.isEmpty()) {
            superAdminRepository.findAllById(unresolved).forEach(superAdmin ->
                    names.put(superAdmin.getId(), superAdmin.getFullName() + " (SuperAdmin, ID: " + superAdmin.getId() + ")")
            );
        }

        return names;
    }

    private void writeCategorySheet(
            Workbook workbook,
            String category,
            List<AuditLog> logs,
            Map<Long, String> actorNames
    ) {
        // Excel sheet adları 31 karakteri geçemez; targetType değerlerimiz
        // zaten kısa/güvenli (USER, RESERVATION, USER_PERMISSION vb.) ama
        // yine de kısaltarak güvenceye alıyoruz.
        String sheetName = category.length() > 31 ? category.substring(0, 31) : category;
        Sheet sheet = workbook.createSheet(sheetName);

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
            row.createCell(1).setCellValue(
                    logEntry.getCompany() != null ? logEntry.getCompany().getName() : "Platform (SuperAdmin)"
            );
            row.createCell(2).setCellValue(resolveActorLabel(logEntry, actorNames));
            row.createCell(3).setCellValue(logEntry.getAction());
            row.createCell(4).setCellValue(logEntry.getTargetType());
            row.createCell(5).setCellValue(logEntry.getTargetId() != null ? logEntry.getTargetId() : 0);
            row.createCell(6).setCellValue(logEntry.getDetails());
            row.createCell(7).setCellValue(DATE_FORMATTER.format(logEntry.getCreatedAt()));
        }

        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String resolveActorLabel(AuditLog logEntry, Map<Long, String> actorNames) {
        if (logEntry.getActorUserId() == null) {
            return UNKNOWN_ACTOR_LABEL;
        }
        return actorNames.getOrDefault(logEntry.getActorUserId(), "Bilinmeyen (ID: " + logEntry.getActorUserId() + ")");
    }
}
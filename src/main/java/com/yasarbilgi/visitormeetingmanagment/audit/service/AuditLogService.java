package com.yasarbilgi.visitormeetingmanagment.audit.service;

import com.yasarbilgi.visitormeetingmanagment.audit.dto.response.AuditLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    void log(
            Long companyId,
            Long actorUserId,
            String action,
            String targetType,
            Long targetId,
            String details
    );

    /**
     * Bir kullanıcının KENDİ login/logout geçmişi — herhangi bir admin
     * izni gerektirmez, sadece "bu benim kendi verim" mantığıyla çalışır.
     */
    Page<AuditLogResponseDto> getMyLoginHistory(Long companyId, Long actorUserId, Pageable pageable);

    /**
     * Şirket admininin panelde göreceği audit log listesi (JSON) — Excel
     * export'a mahkum kalmadan, aynı çoklu kategori filtresiyle.
     */
    Page<AuditLogResponseDto> getLogsForCompany(Long companyId, List<String> targetTypes, Pageable pageable);

    /**
     * SuperAdmin'in panelde göreceği audit log listesi (JSON) — companyId
     * null ise tüm şirketler, verilirse tek şirket.
     */
    Page<AuditLogResponseDto> getLogsForSuperAdmin(Long companyId, List<String> targetTypes, Pageable pageable);
}
package com.yasarbilgi.visitormeetingmanagment.audit.service.impl;

import com.yasarbilgi.visitormeetingmanagment.audit.dto.response.AuditLogResponseDto;
import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import com.yasarbilgi.visitormeetingmanagment.audit.repository.AuditLogRepository;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.platform.repository.SuperAdminRepository;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final String UNKNOWN_ACTOR_LABEL = "Sistem";

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            Long companyId,
            Long actorUserId,
            String action,
            String targetType,
            Long targetId,
            String details
    ) {
        Company company = null;
        if (companyId != null) {
            company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        }

        AuditLog auditLog = AuditLog.builder()
                .company(company)
                .actorUserId(actorUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);

        log.debug("Audit log created: action={}, targetType={}, targetId={}, actorUserId={}, companyId={}",
                action, targetType, targetId, actorUserId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getMyLoginHistory(Long companyId, Long actorUserId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository
                .findAllByCompanyIdAndActorUserIdAndTargetTypeOrderByCreatedAtDesc(
                        companyId, actorUserId, "AUTH", pageable
                );
        return mapToDtoPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getLogsForCompany(Long companyId, List<String> targetTypes, Pageable pageable) {
        Page<AuditLog> page = hasSelection(targetTypes)
                ? auditLogRepository.findAllByCompanyIdAndTargetTypeInOrderByCreatedAtDesc(companyId, targetTypes, pageable)
                : auditLogRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
        return mapToDtoPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getLogsForSuperAdmin(Long companyId, List<String> targetTypes, Pageable pageable) {
        Page<AuditLog> page;
        if (companyId != null && hasSelection(targetTypes)) {
            page = auditLogRepository.findAllByCompanyIdAndTargetTypeInOrderByCreatedAtDesc(companyId, targetTypes, pageable);
        } else if (companyId != null) {
            page = auditLogRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
        } else if (hasSelection(targetTypes)) {
            page = auditLogRepository.findAllByTargetTypeInOrderByCreatedAtDesc(targetTypes, pageable);
        } else {
            page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return mapToDtoPage(page);
    }

    private boolean hasSelection(List<String> targetTypes) {
        return targetTypes != null && !targetTypes.isEmpty();
    }

    /**
     * Ham AuditLog sayfasını, actorUserId'yi okunaklı bir isme çevirerek
     * DTO'ya dönüştürür. Aktör isimleri (User ya da SuperAdmin) TEK SEFERDE,
     * toplu olarak çözülür (N+1 sorgu problemine düşmeden).
     */
    private Page<AuditLogResponseDto> mapToDtoPage(Page<AuditLog> page) {
        Map<Long, String> actorNames = resolveActorNames(page.getContent());

        return page.map(entry -> AuditLogResponseDto.builder()
                .id(entry.getId())
                .companyId(entry.getCompany() != null ? entry.getCompany().getId() : null)
                .companyName(entry.getCompany() != null ? entry.getCompany().getName() : "Platform (SuperAdmin)")
                .actorUserId(entry.getActorUserId())
                .actorName(resolveActorLabel(entry, actorNames))
                .action(entry.getAction())
                .targetType(entry.getTargetType())
                .targetId(entry.getTargetId())
                .details(entry.getDetails())
                .createdAt(entry.getCreatedAt())
                .build());
    }

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
                .forEach(user -> names.put(user.getId(), user.getFullName()));

        Set<Long> unresolved = new HashSet<>(actorIds);
        unresolved.removeAll(names.keySet());

        if (!unresolved.isEmpty()) {
            superAdminRepository.findAllById(unresolved)
                    .forEach(superAdmin -> names.put(superAdmin.getId(), superAdmin.getFullName() + " (SuperAdmin)"));
        }

        return names;
    }

    private String resolveActorLabel(AuditLog entry, Map<Long, String> actorNames) {
        if (entry.getActorUserId() == null) {
            return UNKNOWN_ACTOR_LABEL;
        }
        return actorNames.getOrDefault(entry.getActorUserId(), "Bilinmeyen (ID: " + entry.getActorUserId() + ")");
    }
}
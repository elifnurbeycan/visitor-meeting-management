package com.yasarbilgi.visitormeetingmanagment.audit.service.impl;

import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import com.yasarbilgi.visitormeetingmanagment.audit.repository.AuditLogRepository;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void log(
            Long companyId,
            Long actorUserId,
            String action,
            String targetType,
            Long targetId,
            String details
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        AuditLog auditLog = AuditLog.builder()
                .company(company)
                .actorUserId(actorUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);

        log.debug("Audit log created: action={}, targetType={}, targetId={}, actorUserId={}",
                action, targetType, targetId, actorUserId);
    }
}
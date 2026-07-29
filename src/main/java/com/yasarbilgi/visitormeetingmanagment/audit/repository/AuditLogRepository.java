package com.yasarbilgi.visitormeetingmanagment.audit.repository;

import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    Page<AuditLog> findAllByCompanyIdAndTargetTypeOrderByCreatedAtDesc(
            Long companyId, String targetType, Pageable pageable
    );

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findAllByTargetTypeOrderByCreatedAtDesc(String targetType, Pageable pageable);
}
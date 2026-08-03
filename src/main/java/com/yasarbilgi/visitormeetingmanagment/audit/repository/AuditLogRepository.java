package com.yasarbilgi.visitormeetingmanagment.audit.repository;

import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    Page<AuditLog> findAllByCompanyIdAndTargetTypeOrderByCreatedAtDesc(
            Long companyId, String targetType, Pageable pageable
    );

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findAllByTargetTypeOrderByCreatedAtDesc(String targetType, Pageable pageable);

    Page<AuditLog> findAllByCompanyIdAndTargetTypeInOrderByCreatedAtDesc(
            Long companyId, List<String> targetTypes, Pageable pageable
    );

    Page<AuditLog> findAllByTargetTypeInOrderByCreatedAtDesc(
            List<String> targetTypes, Pageable pageable
    );

    /**
     * Bir kullanıcının KENDİ login/logout geçmişini görebilmesi için —
     * herhangi bir admin izni gerektirmez, sadece "bu benim kendi verim"
     * mantığıyla çalışır. companyId de filtreye dahil edilir çünkü User
     * ve SuperAdmin ayrı ID dizileri kullanıyor (ikisi de 1'den başlıyor) —
     * companyId olmadan, bir kullanıcı teorik olarak aynı ID'ye sahip bir
     * SuperAdmin'in AUTH kayıtlarını da görebilirdi. SuperAdmin'in kendi
     * AUTH kayıtlarında companyId her zaman null olduğu için, bu filtre
     * çakışmayı tamamen engelliyor.
     */
    Page<AuditLog> findAllByCompanyIdAndActorUserIdAndTargetTypeOrderByCreatedAtDesc(
            Long companyId, Long actorUserId, String targetType, Pageable pageable
    );
}
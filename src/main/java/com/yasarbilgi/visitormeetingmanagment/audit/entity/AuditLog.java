package com.yasarbilgi.visitormeetingmanagment.audit.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Şirket içindeki kritik iş olaylarının (rol atama, oda oluşturma,
 * rezervasyon onaylama vb.) kalıcı kaydı. Kim, ne zaman, neye, ne yaptı
 * sorusuna cevap verir. createdAt (BaseEntity'den) zaten "ne zaman"
 * bilgisini taşıdığı için ayrı bir zaman alanı tutulmaz.

 * actorUserId null olabilir — sistem tarafından otomatik tetiklenen
 * olaylarda (örn. scheduled expire job) bir insan aktör yoktur.
 */
@Getter
@SuperBuilder
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_company_id", columnList = "company_id"),
                @Index(name = "idx_audit_logs_actor_user_id", columnList = "actor_user_id"),
                @Index(name = "idx_audit_logs_target", columnList = "target_type, target_id"),
                @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog extends TenantBaseEntity {

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "details", length = 1000)
    private String details;
}
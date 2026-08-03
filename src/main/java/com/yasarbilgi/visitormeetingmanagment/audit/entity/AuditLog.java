package com.yasarbilgi.visitormeetingmanagment.audit.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

 * NOT: Diğer şirket-içi entity'lerin aksine (Room, Reservation, User vb.),
 * bu entity TenantBaseEntity'den DEĞİL, doğrudan BaseEntity'den türüyor —
 * çünkü company alanının burada NULL olabilmesi gerekiyor (SuperAdmin'in
 * kendi login/logout gibi hiçbir şirkete bağlı olmayan olayları için).
 * TenantBaseEntity'deki company alanı ise kasıtlı olarak "nullable=false"
 * (bkz. TenantBaseEntity) — bu davranışı SADECE AuditLog için gevşetmek üzere
 * ayrı bir @ManyToOne tanımlandı, diğer entity'ler etkilenmedi.
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
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", updatable = false)
    private Company company;

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
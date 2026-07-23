package com.yasarbilgi.visitormeetingmanagment.userpermission.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

@Entity
@Filter(name = "tenantFilter")
@Table(
        name = "user_permission_overrides",
        uniqueConstraints = {
                // Bir kullanıcı bir yetki için sadece bir adet override kuralına sahip olabilir (ya GRANT ya REVOKE).
                // Aynı kombinasyondan ikinci bir satır eklenmesini veritabanı seviyesinde önler.
                @UniqueConstraint(
                        name = "uk_user_permission_overrides_user_permission",
                        columnNames = {"user_id", "permission_id"}
                )
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPermissionOverride extends TenantBaseEntity {

    /** Override işleminin uygulanacağı kullanıcı */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /** Override edilecek yetki tanımı */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false, updatable = false)
    private Permission permission;

    /** Override tipi (GRANT veya REVOKE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OverrideType type;

    /**
     * Override kuralının ek yetki tanımlama amaçlı (GRANT) olup olmadığını sorgular.
     */
    public boolean isGrant() {
        return this.type == OverrideType.GRANT;
    }

    /**
     * Override kuralının yetki kısıtlama amaçlı (REVOKE) olup olmadığını sorgular.
     */
    public boolean isRevoke() {
        return this.type == OverrideType.REVOKE;
    }

    /**
     * Override tipini günceller.
     */
    public void updateType(OverrideType type) {
        if (type == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        this.type = type;
    }
}

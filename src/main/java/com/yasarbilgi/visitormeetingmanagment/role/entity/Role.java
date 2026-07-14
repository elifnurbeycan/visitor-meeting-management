package com.yasarbilgi.visitormeetingmanagment.role.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_company_name", columnNames = {"company_id", "name"})
        },
        indexes = {
                @Index(name = "idx_roles_company_id", columnList = "company_id")
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends TenantBaseEntity {

    @Column(name = "role_name", nullable = false, length = 100)
    private String name;

    @Column(name = "role_description", length = 500)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    private boolean systemRole;

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException("Rol adı boş olamaz");
        }
        this.name = newName;
    }

    public void deactivateIfAllowed() {
        if (this.systemRole) {
            throw new BusinessException("Sistem rolü pasife alınamaz");
        }
        deactivate();
    }
}

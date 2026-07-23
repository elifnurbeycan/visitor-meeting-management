package com.yasarbilgi.visitormeetingmanagment.role.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Filter(name = "tenantFilter")
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

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    private boolean systemRole;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permissions_role_permission",
                    columnNames = {"role_id", "permission_id"}
            )
    )
    private Set<Permission> permissions = new HashSet<>();

    public void assignPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void revokePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.ROLE_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void deactivateIfAllowed() {
        if (this.systemRole) {
            throw new BusinessException(ErrorCode.ROLE_SYSTEM_ROLE_CANNOT_BE_DEACTIVATED);
        }
        deactivate();
    }
}
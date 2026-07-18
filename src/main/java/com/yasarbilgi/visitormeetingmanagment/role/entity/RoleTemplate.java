package com.yasarbilgi.visitormeetingmanagment.role.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
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

import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "role_templates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_templates_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_role_templates_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleTemplate extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_template_permissions",
            joinColumns = @JoinColumn(name = "role_template_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_template_permissions_template_permission",
                    columnNames = {"role_template_id", "permission_id"}
            )
    )
    private Set<Permission> permissions = new HashSet<>();

    public void assignPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void revokePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.ROLE_TEMPLATE_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
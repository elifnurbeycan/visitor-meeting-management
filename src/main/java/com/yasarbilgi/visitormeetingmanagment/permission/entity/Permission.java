package com.yasarbilgi.visitormeetingmanagment.permission.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCategory;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permissions_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_permissions_category", columnList = "category"),
                @Index(name = "idx_permissions_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 100, updatable = false)
    private PermissionCode code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private PermissionCategory category;

    @Column(name = "is_system_permission", nullable = false)
    private boolean systemPermission;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.PERMISSION_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void deactivateIfAllowed() {
        if (this.systemPermission) {
            throw new BusinessException(ErrorCode.SYSTEM_PERMISSION_CANNOT_BE_DEACTIVATED);
        }
        deactivate();
    }
}
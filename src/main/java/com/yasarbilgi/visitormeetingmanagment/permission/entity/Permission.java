package com.yasarbilgi.visitormeetingmanagment.permission.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
                @Index(name = "idx_permissions_category_id", columnList = "category_id"),
                @Index(name = "idx_permissions_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private PermissionCategory category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}

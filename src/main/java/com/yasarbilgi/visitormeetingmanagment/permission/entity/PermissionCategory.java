package com.yasarbilgi.visitormeetingmanagment.permission.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "permission_categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_categories_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_permission_categories_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PermissionCategory extends BaseEntity {

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}

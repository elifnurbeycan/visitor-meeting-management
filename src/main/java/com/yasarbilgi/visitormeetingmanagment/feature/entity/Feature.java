package com.yasarbilgi.visitormeetingmanagment.feature.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
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
        name = "features",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_features_company_name", columnNames = {"company_id", "name"})
        },
        indexes = {
                @Index(name = "idx_features_company_id", columnList = "company_id"),
                @Index(name = "idx_features_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feature extends TenantBaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.FEATURE_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}

package com.yasarbilgi.visitormeetingmanagment.department.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@Entity
@Filter(name = "tenantFilter")
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_departments_company_name", columnNames = {"company_id", "name"})
        },
        indexes = {
                @Index(name = "idx_departments_company_id", columnList = "company_id"),
                @Index(name = "idx_departments_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends TenantBaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public int getUserCount() {
        return this.users.size();
    }
}
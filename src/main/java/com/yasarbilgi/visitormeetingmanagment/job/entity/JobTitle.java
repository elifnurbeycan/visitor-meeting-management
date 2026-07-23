package com.yasarbilgi.visitormeetingmanagment.job.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role; // paket yolunu kendi projene göre düzelt
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

@Getter
@SuperBuilder
@Entity
@Filter(name = "tenantFilter")
@Table(
        name = "job_titles",
        indexes = {
                @Index(name = "idx_job_titles_name", columnList = "name"),
                @Index(name = "idx_job_titles_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobTitle extends TenantBaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_title_default_roles",
            joinColumns = @JoinColumn(name = "job_title_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_job_title_default_roles",
                    columnNames = {"job_title_id", "role_id"}
            )
    )
    private Set<Role> defaultRoles = new HashSet<>();

    public void addDefaultRole(Role role) {
        this.defaultRoles.add(role);
    }

    public void removeDefaultRole(Role role) {
        this.defaultRoles.remove(role);
    }

    public boolean hasDefaultRole(Role role) {
        return this.defaultRoles.contains(role);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.JOB_TITLE_NAME_REQUIRED
            );
        }
        this.name = newName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
package com.yasarbilgi.visitormeetingmanagment.job.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "job_titles",
        indexes = {
                @Index(name = "idx_job_titles_name", columnList = "name"),
                @Index(name = "idx_job_titles_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobTitle extends TenantBaseEntity {

    @Column(name = "job_name", nullable = false, length = 100)
    private String name;

    @Column(name = "job_description", length = 500)
    private String description;

    @Column(name = "job_has_default_role", nullable = false)
    private boolean hasDefaultRole;
}
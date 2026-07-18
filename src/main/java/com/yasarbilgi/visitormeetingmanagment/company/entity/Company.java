package com.yasarbilgi.visitormeetingmanagment.company.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_companies_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_companies_tax_number", columnNames = "tax_number")
        },
        indexes = {
                @Index(name = "idx_companies_active", columnList = "active"),
                @Index(name = "idx_companies_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    private static final String SLUG_PATTERN = "^[a-z0-9-]+$";

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "tax_number", length = 20)
    private String taxNumber;

    @Column(name = "contact_email", nullable = false, length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "industry", length = 100)
    private String industry;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CompanyStatus status = CompanyStatus.PENDING_APPROVAL;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public void approve() {
        validatePendingStatus();
        this.status = CompanyStatus.ACTIVE;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        validatePendingStatus();
        this.status = CompanyStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public boolean isApproved() {
        return this.status == CompanyStatus.ACTIVE;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.COMPANY_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void changeSlug(String newSlug) {
        validateSlug(newSlug);
        this.slug = newSlug;
    }

    public void updateContactInfo(String email, String phone, String address) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.COMPANY_EMAIL_REQUIRED);
        }
        this.contactEmail = email;
        this.contactPhone = phone;
        this.address = address;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public void updateIndustry(String industry) {
        this.industry = industry;
    }

    private void validatePendingStatus() {
        if (this.status != CompanyStatus.PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_PENDING_APPROVAL);
        }
    }

    private static void validateSlug(String candidateSlug) {
        if (candidateSlug == null || !candidateSlug.matches(SLUG_PATTERN)) {
            throw new BusinessException(ErrorCode.COMPANY_INVALID_SLUG);
        }
    }
}
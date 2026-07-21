package com.yasarbilgi.visitormeetingmanagment.user.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_company_email", columnNames = {"company_id", "email"})
        },
        indexes = {
                @Index(name = "idx_users_company_id", columnList = "company_id"),
                @Index(name = "idx_users_email", columnList = "email")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TenantBaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", updatable = true)
    private JobTitle jobTitle;

    @Builder.Default
    @Column(name = "is_owner", nullable = false)
    private boolean owner = false;

    @Builder.Default
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = true;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_user_roles_user_role",
                    columnNames = {"user_id", "role_id"}
            )
    )
    private Set<Role> roles = new HashSet<>();

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public void updateName(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessException(ErrorCode.USER_FIRST_NAME_REQUIRED);
        }
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessException(ErrorCode.USER_LAST_NAME_REQUIRED);
        }
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void changeEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_REQUIRED);
        }
        this.email = newEmail;
    }

    public void changePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_REQUIRED);
        }
        this.passwordHash = newPasswordHash;
    }

    public void assignRole(Role role) {
        this.roles.add(role);
    }

    public void revokeRole(Role role) {
        if (this.owner) {
            throw new BusinessException(ErrorCode.USER_OWNER_ROLE_MODIFICATION_FORBIDDEN);
        }
        this.roles.remove(role);
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    public void changeJobTitle(JobTitle newJobTitle) {
        this.jobTitle = newJobTitle;
    }

    public void promoteToOwner() {
        this.owner = true;
    }

    public void demoteFromOwner() {
        this.owner = false;
    }

    public void deactivateIfAllowed() {
        if (this.owner) {
            throw new BusinessException(ErrorCode.USER_OWNER_CANNOT_BE_DEACTIVATED);
        }
        this.deactivate();
    }

    public void clearMustChangePasswordFlag() {
        this.mustChangePassword = false;
    }

    public void forcePasswordChangeOnNextLogin() {
        this.mustChangePassword = true;
    }
}
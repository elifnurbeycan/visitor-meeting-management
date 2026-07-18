package com.yasarbilgi.visitormeetingmanagment.platform.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.BaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "super_admins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_super_admins_email", columnNames = "email")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuperAdmin extends BaseEntity {

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new BusinessException(ErrorCode.SUPER_ADMIN_PASSWORD_REQUIRED);
        }
        this.passwordHash = newPasswordHash;
    }

    public void rename(String newFullName) {
        if (newFullName == null || newFullName.isBlank()) {
            throw new BusinessException(ErrorCode.SUPER_ADMIN_NAME_REQUIRED);
        }
        this.fullName = newFullName;
    }
}
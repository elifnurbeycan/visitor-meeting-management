package com.yasarbilgi.visitormeetingmanagment.notification.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

@Getter
@SuperBuilder
@Entity
@Filter(name = "tenantFilter")
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_user_id", columnList = "recipient_user_id"),
                @Index(name = "idx_notifications_company_id", columnList = "company_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends TenantBaseEntity {

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Builder.Default
    @Column(name = "read", nullable = false)
    private boolean read = false;

    public void markAsRead() {
        this.read = true;
    }
}
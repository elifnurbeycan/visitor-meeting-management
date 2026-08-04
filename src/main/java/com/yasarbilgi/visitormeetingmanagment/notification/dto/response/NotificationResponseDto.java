package com.yasarbilgi.visitormeetingmanagment.notification.dto.response;

import com.yasarbilgi.visitormeetingmanagment.notification.entity.Notification;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        Long recipientUserId,
        String title,
        String message,
        Long reservationId,
        boolean read,
        Instant createdAt
) {

    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReservationId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
package com.yasarbilgi.visitormeetingmanagment.notification.service;

import com.yasarbilgi.visitormeetingmanagment.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void notifyUser(Long companyId, Long recipientUserId, String title, String message, Long reservationId);

    Page<Notification> listForUser(Long recipientUserId, Pageable pageable);

    long countUnread(Long recipientUserId);

    void markAsRead(Long notificationId, Long recipientUserId);
}
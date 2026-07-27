package com.yasarbilgi.visitormeetingmanagment.notification.service;

public interface NotificationService {

    void notifyUser(Long companyId, Long recipientUserId, String title, String message, Long reservationId);
}
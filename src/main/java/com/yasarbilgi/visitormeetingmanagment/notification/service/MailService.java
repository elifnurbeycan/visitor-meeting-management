package com.yasarbilgi.visitormeetingmanagment.notification.service;

public interface MailService {

    void sendMail(String to, String subject, String body);
}
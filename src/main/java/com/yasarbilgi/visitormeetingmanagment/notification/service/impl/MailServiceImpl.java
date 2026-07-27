package com.yasarbilgi.visitormeetingmanagment.notification.service.impl;

import com.yasarbilgi.visitormeetingmanagment.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Mail gönderimini JavaMailSender üzerinden yapar. Hata durumunda
 * (SMTP sunucusuna ulaşılamaması gibi) exception'ı yutar, sadece loglar —
 * mail gönderiminin başarısız olması, ana iş akışını (örn. rezervasyon
 * onayı) asla bozmamalı.

 * İleride yüksek hacimli/güvenilir teslimat gerekirse, bu sınıfın içi
 * bir mesaj kuyruğuna (RabbitMQ vb.) yazacak şekilde değiştirilebilir —
 * arayüz (MailService) ve çağıran kodlar hiç değişmeden kalır.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendMail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Mail sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send mail to {}: {}", to, e.getMessage());
        }
    }
}
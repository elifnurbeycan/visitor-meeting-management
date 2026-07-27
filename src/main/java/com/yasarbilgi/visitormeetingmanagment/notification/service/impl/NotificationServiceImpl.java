package com.yasarbilgi.visitormeetingmanagment.notification.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.notification.entity.Notification;
import com.yasarbilgi.visitormeetingmanagment.notification.repository.NotificationRepository;
import com.yasarbilgi.visitormeetingmanagment.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void notifyUser(Long companyId, Long recipientUserId, String title, String message, Long reservationId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Notification notification = Notification.builder()
                .company(company)
                .recipientUserId(recipientUserId)
                .title(title)
                .message(message)
                .reservationId(reservationId)
                .build();

        notificationRepository.save(notification);
        log.debug("Notification created for user {}: {}", recipientUserId, title);
    }
}
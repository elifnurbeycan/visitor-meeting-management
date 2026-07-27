package com.yasarbilgi.visitormeetingmanagment.reservation.scheduler;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Onay bekleyen (PENDING_APPROVAL) rezervasyonlardan, onay süresi
 * (approvalDeadline) geçmiş olanları periyodik olarak EXPIRED durumuna
 * çevirir. Her 15 dakikada bir çalışır.

 * Pessimistic lock kullanılır — bir yönetici tam bu sırada aynı
 * rezervasyonu onaylamaya/reddetmeye çalışırsa, iki işlem birbirini
 * bekler, veri tutarsızlığı oluşmaz.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;
    private final AuditLogService auditLogService;

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void expireOverdueReservations() {
        List<Reservation> expired = reservationRepository.findExpiredPendingForUpdate(Instant.now());

        for (Reservation reservation : expired) {
            reservation.expire();

            auditLogService.log(
                    reservation.getCompany().getId(),
                    null,
                    "RESERVATION_EXPIRED",
                    "RESERVATION",
                    reservation.getId(),
                    "Reservation '" + reservation.getTitle() + "' expired due to no approval action"
            );

            log.info("Reservation {} expired due to approval deadline", reservation.getId());
        }

        if (!expired.isEmpty()) {
            log.info("Expired {} overdue pending reservation(s)", expired.size());
        }
    }
}
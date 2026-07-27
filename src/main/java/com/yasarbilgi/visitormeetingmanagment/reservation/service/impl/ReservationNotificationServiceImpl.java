package com.yasarbilgi.visitormeetingmanagment.reservation.service.impl;

import com.yasarbilgi.visitormeetingmanagment.notification.service.MailService;
import com.yasarbilgi.visitormeetingmanagment.notification.service.NotificationService;
import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationNotificationService;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationNotificationServiceImpl implements ReservationNotificationService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationService notificationService;
    private final MailService mailService;

    @Override
    public void notifyApproval(Reservation reservation) {
        String title = "Toplantı Onaylandı";
        String message = buildApprovalMessage(reservation);

        reservation.getParticipants().forEach(participant ->
                sendToUser(reservation, participant, title, message)
        );

        log.debug("Approval notifications sent to {} participant(s) for reservation {}",
                reservation.getParticipants().size(), reservation.getId());
    }

    @Override
    public void notifyParticipantAdded(Reservation reservation, User addedParticipant) {
        String title = "Toplantıya Davet Edildiniz";
        String message = buildApprovalMessage(reservation);
        sendToUser(reservation, addedParticipant, title, message);
    }

    @Override
    public void notifyParticipantRemoved(Reservation reservation, User removedParticipant) {
        String title = "Toplantı Davetiniz İptal Edildi";
        String message = "'" + reservation.getTitle() + "' başlıklı toplantıya artık davetli değilsiniz.";
        sendToUser(reservation, removedParticipant, title, message);
    }

    @Override
    public void notifyCancellation(Reservation reservation) {
        String title = "Toplantı İptal Edildi";
        String message = "'" + reservation.getTitle() + "' başlıklı, "
                + reservation.getStartTime().format(DATE_FORMATTER) + " tarihli toplantı iptal edildi.";

        reservation.getParticipants().forEach(participant ->
                sendToUser(reservation, participant, title, message)
        );

        log.debug("Cancellation notifications sent to {} participant(s) for reservation {}",
                reservation.getParticipants().size(), reservation.getId());
    }

    @Override
    public void notifyOrganizerOnly(Reservation reservation, String title, String message) {
        sendToUser(reservation, reservation.getOrganizer(), title, message);
    }

    private void sendToUser(Reservation reservation, User user, String title, String message) {
        notificationService.notifyUser(
                reservation.getCompany().getId(), user.getId(), title, message, reservation.getId()
        );
        mailService.sendMail(user.getEmail(), title, message);
    }

    private String buildApprovalMessage(Reservation reservation) {
        return "'" + reservation.getTitle() + "' başlıklı toplantınız "
                + reservation.getStartTime().format(DATE_FORMATTER) + " - "
                + reservation.getEndTime().format(DATE_FORMATTER) + " saatleri arasında, "
                + reservation.getRoom().getName() + " odasında onaylandı.";
    }
}
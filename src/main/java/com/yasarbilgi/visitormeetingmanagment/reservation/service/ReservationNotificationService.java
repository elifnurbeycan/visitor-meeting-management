package com.yasarbilgi.visitormeetingmanagment.reservation.service;

import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;

/**
 * Rezervasyon yaşam döngüsündeki tüm bildirim/mail senaryolarını
 * merkezi olarak yönetir. Her metod hem bir Notification kaydı
 * oluşturur hem de gerçek bir mail gönderir.
 */
public interface ReservationNotificationService {

    /** Rezervasyon onaylandığında, o anki tüm katılımcılara toplu bildirim. */
    void notifyApproval(Reservation reservation);

    /** ACTIVE bir rezervasyona yeni katılımcı eklendiğinde, sadece o kişiye. */
    void notifyParticipantAdded(Reservation reservation, User addedParticipant);

    /** ACTIVE bir rezervasyondan katılımcı çıkarıldığında, sadece o kişiye. */
    void notifyParticipantRemoved(Reservation reservation, User removedParticipant);

    /** ACTIVE bir rezervasyon iptal edildiğinde, o anki tüm katılımcılara toplu. */
    void notifyCancellation(Reservation reservation);

    /** Reddedilme, onaylanmadan iptal, ya da süre dolması — sadece organizer'a. */
    void notifyOrganizerOnly(Reservation reservation, String title, String message);
}
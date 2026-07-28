package com.yasarbilgi.visitormeetingmanagment.reservation.service;

import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.notification.service.MailService;
import com.yasarbilgi.visitormeetingmanagment.notification.service.NotificationService;
import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.impl.ReservationNotificationServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ReservationNotificationServiceImpl için JUnit 5 ve Mockito birim testleri.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class ReservationNotificationServiceImplTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ReservationNotificationServiceImpl reservationNotificationService;

    private static final Long COMPANY_ID = 1L;
    private static final Long RESERVATION_ID = 100L;

    private Company company;
    private Room room;
    private User organizer;
    private User participant1;
    private User participant2;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Atlas Teknoloji")
                .build();

        room = Room.builder()
                .id(5L)
                .name("Açelya Odası")
                .company(company)
                .build();

        organizer = User.builder()
                .id(2L)
                .email("organizer@test.com")
                .company(company)
                .build();

        participant1 = User.builder()
                .id(10L)
                .email("participant1@test.com")
                .company(company)
                .build();

        participant2 = User.builder()
                .id(11L)
                .email("participant2@test.com")
                .company(company)
                .build();

        // 2 katılımcılı bir rezervasyon nesnesi hazırlıyoruz
        reservation = Reservation.builder()
                .id(RESERVATION_ID)
                .title("Aylık Değerlendirme")
                .startTime(LocalDateTime.of(2026, 7, 28, 10, 0))
                .endTime(LocalDateTime.of(2026, 7, 28, 11, 0))
                .room(room)
                .organizer(organizer)
                .company(company)
                .participants(Set.of(participant1, participant2))
                .build();
    }

    // ----- notifyApproval() -----

    @Test
    void notifyApproval_shouldSendNotificationsToAllParticipants() {
        // Act
        reservationNotificationService.notifyApproval(reservation);

        // Assert: 2 katılımcının her birine hem bildirim hem de e-posta gittiğini doğruluyoruz
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(10L), eq("Toplantı Onaylandı"), anyString(), eq(RESERVATION_ID)
        );
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(11L), eq("Toplantı Onaylandı"), anyString(), eq(RESERVATION_ID)
        );

        verify(mailService, times(1)).sendMail(eq("participant1@test.com"), eq("Toplantı Onaylandı"), anyString());
        verify(mailService, times(1)).sendMail(eq("participant2@test.com"), eq("Toplantı Onaylandı"), anyString());
    }

    // ----- notifyParticipantAdded() -----

    @Test
    void notifyParticipantAdded_shouldSendNotificationToAddedParticipant() {
        User addedUser = User.builder().id(20L).email("added@test.com").company(company).build();

        // Act
        reservationNotificationService.notifyParticipantAdded(reservation, addedUser);

        // Assert: Sadece eklenen kullanıcıya bildirim ve mail gitmeli
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(20L), eq("Toplantıya Davet Edildiniz"), anyString(), eq(RESERVATION_ID)
        );
        verify(mailService, times(1)).sendMail(eq("added@test.com"), eq("Toplantıya Davet Edildiniz"), anyString());
    }

    // ----- notifyParticipantRemoved() -----

    @Test
    void notifyParticipantRemoved_shouldSendNotificationToRemovedParticipant() {
        User removedUser = User.builder().id(20L).email("removed@test.com").company(company).build();

        // Act
        reservationNotificationService.notifyParticipantRemoved(reservation, removedUser);

        // Assert: Sadece çıkarılan kullanıcıya bildirim ve mail gitmeli
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(20L), eq("Toplantı Davetiniz İptal Edildi"), anyString(), eq(RESERVATION_ID)
        );
        verify(mailService, times(1)).sendMail(eq("removed@test.com"), eq("Toplantı Davetiniz İptal Edildi"), anyString());
    }

    // ----- notifyCancellation() -----

    @Test
    void notifyCancellation_shouldSendNotificationsToAllParticipants() {
        // Act
        reservationNotificationService.notifyCancellation(reservation);

        // Assert: Tüm katılımcılara iptal bildirimlerinin gittiğini doğruluyoruz
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(10L), eq("Toplantı İptal Edildi"), anyString(), eq(RESERVATION_ID)
        );
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(11L), eq("Toplantı İptal Edildi"), anyString(), eq(RESERVATION_ID)
        );

        verify(mailService, times(1)).sendMail(eq("participant1@test.com"), eq("Toplantı İptal Edildi"), anyString());
        verify(mailService, times(1)).sendMail(eq("participant2@test.com"), eq("Toplantı İptal Edildi"), anyString());
    }

    // ----- notifyOrganizerOnly() -----

    @Test
    void notifyOrganizerOnly_shouldSendNotificationToOrganizer() {
        String customTitle = "Talep Reddedildi";
        String customMessage = "Rezervasyon talebiniz yönetici tarafından reddedildi.";

        // Act
        reservationNotificationService.notifyOrganizerOnly(reservation, customTitle, customMessage);

        // Assert: Sadece rezervasyon sahibine bildirim gitmelidir
        verify(notificationService, times(1)).notifyUser(
                eq(COMPANY_ID), eq(organizer.getId()), eq(customTitle), eq(customMessage), eq(RESERVATION_ID)
        );
        verify(mailService, times(1)).sendMail(eq("organizer@test.com"), eq(customTitle), eq(customMessage));
    }
}

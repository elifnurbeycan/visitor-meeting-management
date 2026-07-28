package com.yasarbilgi.visitormeetingmanagment.notification.service;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.notification.entity.Notification;
import com.yasarbilgi.visitormeetingmanagment.notification.repository.NotificationRepository;
import com.yasarbilgi.visitormeetingmanagment.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NotificationServiceImpl için kapsamlı birim (unit) testler.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private static final Long COMPANY_ID = 7L;
    private static final Long RECIPIENT_USER_ID = 12L;
    private static final Long RESERVATION_ID = 100L;

    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .build();
    }

    // ----- notifyUser() -----

    @Test
    void notifyUser_shouldSaveNotification_whenCompanyExists() {
        // Arrange
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        // Act
        notificationService.notifyUser(
                COMPANY_ID,
                RECIPIENT_USER_ID,
                "Toplantı Onayı",
                "Toplantı rezervasyonunuz onaylanmıştır.",
                RESERVATION_ID
        );

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification).isNotNull();
        assertThat(savedNotification.getCompany()).isEqualTo(company);
        assertThat(savedNotification.getRecipientUserId()).isEqualTo(RECIPIENT_USER_ID);
        assertThat(savedNotification.getTitle()).isEqualTo("Toplantı Onayı");
        assertThat(savedNotification.getMessage()).isEqualTo("Toplantı rezervasyonunuz onaylanmıştır.");
        assertThat(savedNotification.getReservationId()).isEqualTo(RESERVATION_ID);
        assertThat(savedNotification.isRead()).isFalse();
    }

    @Test
    void notifyUser_shouldSaveNotification_whenReservationIdIsNull() {
        // Arrange
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        // Act
        notificationService.notifyUser(
                COMPANY_ID,
                RECIPIENT_USER_ID,
                "Genel Duyuru",
                "Sistem bakım çalışması yapılacaktır.",
                null
        );

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification).isNotNull();
        assertThat(savedNotification.getCompany()).isEqualTo(company);
        assertThat(savedNotification.getRecipientUserId()).isEqualTo(RECIPIENT_USER_ID);
        assertThat(savedNotification.getTitle()).isEqualTo("Genel Duyuru");
        assertThat(savedNotification.getMessage()).isEqualTo("Sistem bakım çalışması yapılacaktır.");
        assertThat(savedNotification.getReservationId()).isNull();
        assertThat(savedNotification.isRead()).isFalse();
    }

    @Test
    void notifyUser_shouldThrowException_whenCompanyNotFound() {
        // Arrange
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.notifyUser(
                COMPANY_ID,
                RECIPIENT_USER_ID,
                "Toplantı Onayı",
                "Mesaj içeriği",
                RESERVATION_ID
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPANY_NOT_FOUND);

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}

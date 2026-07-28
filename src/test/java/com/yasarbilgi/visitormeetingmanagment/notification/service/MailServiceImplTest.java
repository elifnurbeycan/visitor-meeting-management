package com.yasarbilgi.visitormeetingmanagment.notification.service;

import com.yasarbilgi.visitormeetingmanagment.notification.service.impl.MailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * MailServiceImpl için kapsamlı birim (unit) testler.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailServiceImpl mailService;

    private static final String TO = "user@example.com";
    private static final String SUBJECT = "Toplantı Hatırlatması";
    private static final String BODY = "Toplantınız 10 dakika içinde başlayacaktır.";

    // ----- sendMail() -----

    @Test
    void sendMail_shouldSendMessage_whenValidInput() {
        // Act
        mailService.sendMail(TO, SUBJECT, BODY);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getTo()).containsExactly(TO);
        assertThat(capturedMessage.getSubject()).isEqualTo(SUBJECT);
        assertThat(capturedMessage.getText()).isEqualTo(BODY);
    }

    @Test
    void sendMail_shouldNotThrowException_whenMailSenderFails() {
        // Arrange
        doThrow(new MailSendException("SMTP sunucusuna erişilemedi"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert (Mail gönderiminin başarısız olması ana iş akışını bozmamalı, exception yutulmalı)
        assertDoesNotThrow(() -> mailService.sendMail(TO, SUBJECT, BODY));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}

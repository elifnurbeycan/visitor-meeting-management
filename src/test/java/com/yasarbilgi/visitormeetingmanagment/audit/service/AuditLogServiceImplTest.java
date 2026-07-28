package com.yasarbilgi.visitormeetingmanagment.audit.service;

import com.yasarbilgi.visitormeetingmanagment.audit.entity.AuditLog;
import com.yasarbilgi.visitormeetingmanagment.audit.repository.AuditLogRepository;
import com.yasarbilgi.visitormeetingmanagment.audit.service.impl.AuditLogServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
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
 * AuditLogServiceImpl için kapsamlı birim (unit) testler.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private static final Long COMPANY_ID = 7L;
    private static final Long ACTOR_USER_ID = 12L;
    private static final Long TARGET_ID = 100L;

    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .build();
    }

    // ----- log() -----

    @Test
    void log_shouldSaveAuditLog_whenCompanyExists() {
        // Arrange
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        // Act
        auditLogService.log(
                COMPANY_ID,
                ACTOR_USER_ID,
                "ROOM_CREATED",
                "ROOM",
                TARGET_ID,
                "Room 'A Toplantı Odası' created"
        );

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertThat(savedAuditLog).isNotNull();
        assertThat(savedAuditLog.getCompany()).isEqualTo(company);
        assertThat(savedAuditLog.getActorUserId()).isEqualTo(ACTOR_USER_ID);
        assertThat(savedAuditLog.getAction()).isEqualTo("ROOM_CREATED");
        assertThat(savedAuditLog.getTargetType()).isEqualTo("ROOM");
        assertThat(savedAuditLog.getTargetId()).isEqualTo(TARGET_ID);
        assertThat(savedAuditLog.getDetails()).isEqualTo("Room 'A Toplantı Odası' created");
    }

    @Test
    void log_shouldSaveAuditLog_whenActorUserIdIsNull() {
        // Arrange (Sistem tarafından tetiklenen olaylarda actorUserId null olabilir)
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        // Act
        auditLogService.log(
                COMPANY_ID,
                null,
                "RESERVATION_EXPIRED",
                "RESERVATION",
                TARGET_ID,
                "Reservation expired automatically"
        );

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertThat(savedAuditLog).isNotNull();
        assertThat(savedAuditLog.getCompany()).isEqualTo(company);
        assertThat(savedAuditLog.getActorUserId()).isNull();
        assertThat(savedAuditLog.getAction()).isEqualTo("RESERVATION_EXPIRED");
        assertThat(savedAuditLog.getTargetType()).isEqualTo("RESERVATION");
        assertThat(savedAuditLog.getTargetId()).isEqualTo(TARGET_ID);
        assertThat(savedAuditLog.getDetails()).isEqualTo("Reservation expired automatically");
    }

    @Test
    void log_shouldThrowException_whenCompanyNotFound() {
        // Arrange
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> auditLogService.log(
                COMPANY_ID,
                ACTOR_USER_ID,
                "ROOM_CREATED",
                "ROOM",
                TARGET_ID,
                "Room created"
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPANY_NOT_FOUND);

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }
}

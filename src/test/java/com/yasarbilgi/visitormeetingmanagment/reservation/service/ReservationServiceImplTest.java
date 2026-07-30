package com.yasarbilgi.visitormeetingmanagment.reservation.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.UpdateReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import com.yasarbilgi.visitormeetingmanagment.reservation.mapper.ReservationMapper;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.ReservationRepository;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.impl.ReservationServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.room.repository.RoomRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ReservationServiceImpl iş mantığı için JUnit 5 ve Mockito birim testleri.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @Mock
    private ReservationNotificationService reservationNotificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private static final Long COMPANY_ID = 1L;
    private static final Long ORGANIZER_ID = 2L;
    private static final Long RESERVATION_ID = 100L;
    private static final Long ROOM_ID = 5L;

    private Company company;
    private Room room;
    private User organizer;
    private Reservation reservation;
    private ReservationResponseDto responseDto;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Atlas Teknoloji")
                .build();

        room = Room.builder()
                .id(ROOM_ID)
                .name("Açelya Odası")
                .capacity(10)
                .company(company)
                .build();

        organizer = User.builder()
                .id(ORGANIZER_ID)
                .firstName("Elif")
                .lastName("Beycan")
                .company(company)
                .build();

        reservation = Reservation.builder()
                .id(RESERVATION_ID)
                .title("Senkron Toplantısı")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .room(room)
                .organizer(organizer)
                .company(company)
                .participants(new HashSet<>())
                .status(ReservationStatus.PENDING_APPROVAL)
                .build();

        responseDto = ReservationResponseDto.builder()
                .id(RESERVATION_ID)
                .title("Senkron Toplantısı")
                .status(ReservationStatus.PENDING_APPROVAL)
                .build();

        authenticatedUser = new AuthenticatedUser(
                ORGANIZER_ID, COMPANY_ID, Collections.emptySet(), false
        );
    }

    // ----- create() -----

    @Test
    void create_shouldSucceed_whenNoConflicts() {
        ReservationRequestDto dto = ReservationRequestDto.builder()
                .title("Senkron Toplantısı")
                .description("Açıklama")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .roomId(ROOM_ID)
                .participantIds(Collections.emptySet())
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(userRepository.findById(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(reservationRepository.existsByRoomIdAndCompanyIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(ROOM_ID), eq(COMPANY_ID), anyList(), any(), any()
        )).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.toResponseDto(any(Reservation.class))).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.create(COMPANY_ID, ORGANIZER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Senkron Toplantısı");
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(auditLogService, times(1)).log(any(), any(), eq("RESERVATION_CREATED"), any(), any(), any());
    }

    @Test
    void create_shouldThrowException_whenConflictExists() {
        ReservationRequestDto dto = ReservationRequestDto.builder()
                .title("Çakışan Toplantı")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .roomId(ROOM_ID)
                .participantIds(Collections.emptySet())
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(userRepository.findById(ORGANIZER_ID)).thenReturn(Optional.of(organizer));
        when(reservationRepository.existsByRoomIdAndCompanyIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(ROOM_ID), eq(COMPANY_ID), anyList(), any(), any()
        )).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(COMPANY_ID, ORGANIZER_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_CONFLICT);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenCallerIsOrganizer() {
        Reservation activeReservation = Reservation.builder()
                .id(RESERVATION_ID)
                .title("Senkron Toplantısı")
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .room(room)
                .organizer(organizer)
                .company(company)
                .status(ReservationStatus.ACTIVE)
                .build();

        UpdateReservationRequestDto dto = UpdateReservationRequestDto.builder()
                .title("Yeni Başlık")
                .description("Yeni Açıklama")
                .startTime(LocalDateTime.now().plusHours(3))
                .endTime(LocalDateTime.now().plusHours(4))
                .roomId(ROOM_ID)
                .build();

        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(activeReservation));
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(reservationMapper.toResponseDto(any(Reservation.class))).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.update(COMPANY_ID, ORGANIZER_ID, RESERVATION_ID, dto);

        assertThat(result).isNotNull();
        verify(auditLogService, times(1)).log(any(), any(), eq("RESERVATION_UPDATED"), any(), any(), any());
    }

    @Test
    void update_shouldThrowException_whenUserIsNotOrganizer() {
        UpdateReservationRequestDto dto = UpdateReservationRequestDto.builder()
                .title("Yeni Başlık")
                .roomId(ROOM_ID)
                .build();
        Long otherUserId = 999L;

        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.update(COMPANY_ID, otherUserId, RESERVATION_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    // ----- approve() -----

    @Test
    void approve_shouldSucceed_andAutoRejectConflicts() {
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));

        AuthenticatedUser approvingAdmin = new AuthenticatedUser(
                99L, COMPANY_ID, Collections.emptySet(), true
        );
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(approvingAdmin));

        List<Reservation> mockConflicts = List.of(reservation);
        when(reservationRepository.findPendingConflictsForUpdate(any(), any(), any(), any()))
                .thenReturn(mockConflicts);
        when(reservationMapper.toResponseDto(any(Reservation.class))).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.approve(COMPANY_ID, RESERVATION_ID);

        assertThat(result).isNotNull();
        verify(reservationNotificationService, times(1)).notifyApproval(any());
    }

    // ----- cancel() -----

    @Test
    void cancel_shouldSucceed_whenUserIsOrganizer() {
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));

        reservationService.cancel(COMPANY_ID, ORGANIZER_ID, RESERVATION_ID, "Gerek kalmadı");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(auditLogService, times(1)).log(any(), any(), eq("RESERVATION_CANCELLED"), any(), any(), any());
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnDto_whenFound() {
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponseDto(reservation)).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.getById(COMPANY_ID, RESERVATION_ID);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Senkron Toplantısı");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getById(COMPANY_ID, RESERVATION_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_FOUND);
    }

    // ----- getAll() / getAllByRoom() / getAllByOrganizer() / getAllByDateRange() -----

    @Test
    void getAll_shouldReturnPagedReservations() {
        Pageable pageable = Pageable.unpaged();
        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAllByCompanyId(COMPANY_ID, pageable)).thenReturn(page);
        when(reservationMapper.toResponseDto(reservation)).thenReturn(responseDto);

        Page<ReservationResponseDto> result = reservationService.getAll(COMPANY_ID, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByRoom_shouldReturnPagedReservations() {
        Pageable pageable = Pageable.unpaged();
        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAllByRoomIdAndCompanyId(ROOM_ID, COMPANY_ID, pageable)).thenReturn(page);
        when(reservationMapper.toResponseDto(reservation)).thenReturn(responseDto);

        Page<ReservationResponseDto> result = reservationService.getAllByRoom(COMPANY_ID, ROOM_ID, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByOrganizer_shouldReturnPagedReservations() {
        Pageable pageable = Pageable.unpaged();
        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAllByOrganizerIdAndCompanyId(ORGANIZER_ID, COMPANY_ID, pageable)).thenReturn(page);
        when(reservationMapper.toResponseDto(reservation)).thenReturn(responseDto);

        Page<ReservationResponseDto> result = reservationService.getAllByOrganizer(COMPANY_ID, ORGANIZER_ID, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByDateRange_shouldReturnPagedReservations() {
        Pageable pageable = Pageable.unpaged();
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAllByCompanyIdAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(COMPANY_ID), eq(to), eq(from), eq(pageable)
        )).thenReturn(page);
        when(reservationMapper.toResponseDto(reservation)).thenReturn(responseDto);

        Page<ReservationResponseDto> result = reservationService.getAllByDateRange(COMPANY_ID, from, to, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    // ----- addParticipant() & removeParticipant() -----

    @Test
    void addParticipant_shouldSucceed() {
        User newParticipant = User.builder().id(200L).company(company).build();

        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(200L)).thenReturn(Optional.of(newParticipant));
        when(reservationMapper.toResponseDto(any(Reservation.class))).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.addParticipant(COMPANY_ID, ORGANIZER_ID, RESERVATION_ID, 200L);

        assertThat(result).isNotNull();
        verify(auditLogService, times(1)).log(any(), any(), eq("PARTICIPANT_ADDED"), any(), any(), any());
    }

    @Test
    void removeParticipant_shouldSucceed() {
        User participant = User.builder().id(200L).company(company).build();
        reservation.getParticipants().add(participant);

        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(200L)).thenReturn(Optional.of(participant));
        when(reservationMapper.toResponseDto(any(Reservation.class))).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.removeParticipant(COMPANY_ID, ORGANIZER_ID, RESERVATION_ID, 200L);

        assertThat(result).isNotNull();
        verify(auditLogService, times(1)).log(any(), any(), eq("PARTICIPANT_REMOVED"), any(), any(), any());
    }

    // ----- reject() -----

    @Test
    void reject_shouldSucceed() {
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));

        AuthenticatedUser approvingAdmin = new AuthenticatedUser(99L, COMPANY_ID, Collections.emptySet(), true);
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(approvingAdmin));
        when(reservationMapper.toResponseDto(any(Reservation.class))).thenReturn(responseDto);

        ReservationResponseDto result = reservationService.reject(COMPANY_ID, RESERVATION_ID, "Oda bakımda");

        assertThat(result).isNotNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);
        verify(auditLogService, times(1)).log(any(), any(), eq("RESERVATION_REJECTED"), any(), any(), any());
    }

    // ----- cancel() Ek Testler -----

    @Test
    void cancel_shouldSucceed_whenCallerIsNotOrganizer_butHasCancelAllPermission() {
        Long adminUserId = 88L;
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));
        when(permissionResolutionService.resolveEffectivePermissions(adminUserId))
                .thenReturn(Set.of("RESERVATION_CANCEL_ALL")); // İptal yetkisi var

        reservationService.cancel(COMPANY_ID, adminUserId, RESERVATION_ID, "Yönetici İptali");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(auditLogService, times(1)).log(any(), any(), eq("RESERVATION_CANCELLED"), any(), any(), any());
    }

    @Test
    void cancel_shouldThrowException_whenCallerIsNotOrganizer_andHasNoPermission() {
        Long unauthorizedUserId = 99L;
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));
        when(permissionResolutionService.resolveEffectivePermissions(unauthorizedUserId))
                .thenReturn(Collections.emptySet()); // Yetki yok

        assertThatThrownBy(() -> reservationService.cancel(COMPANY_ID, unauthorizedUserId, RESERVATION_ID, "İptal"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    // ----- addParticipant() & removeParticipant() Ek Testler -----

    @Test
    void addParticipant_shouldThrowException_whenCallerIsNotOrganizer() {
        Long unauthorizedUserId = 99L;
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.addParticipant(COMPANY_ID, unauthorizedUserId, RESERVATION_ID, 200L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    @Test
    void removeParticipant_shouldThrowException_whenCallerIsNotOrganizer() {
        Long unauthorizedUserId = 99L;
        when(reservationRepository.findByIdAndCompanyId(RESERVATION_ID, COMPANY_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.removeParticipant(COMPANY_ID, unauthorizedUserId, RESERVATION_ID, 200L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ACCESS_DENIED);
    }

    // ----- getMyReservations() -----

    @Test
    void getMyReservations_shouldReturnPagedReservations() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAllByOrganizerIdAndCompanyId(ORGANIZER_ID, COMPANY_ID, pageable))
                .thenReturn(page);
        when(reservationMapper.toResponseDto(reservation))
                .thenReturn(responseDto);

        // Act
        Page<ReservationResponseDto> result = reservationService.getMyReservations(COMPANY_ID, ORGANIZER_ID, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(reservationRepository, times(1))
                .findAllByOrganizerIdAndCompanyId(ORGANIZER_ID, COMPANY_ID, pageable);
    }


}

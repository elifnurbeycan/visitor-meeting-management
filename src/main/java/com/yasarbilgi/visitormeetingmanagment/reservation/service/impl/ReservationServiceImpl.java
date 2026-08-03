package com.yasarbilgi.visitormeetingmanagment.reservation.service.impl;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.constant.AppConstants;
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
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationNotificationService;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationService;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.room.repository.RoomRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ReservationService arayüzünün iş mantığı implementasyon sınıfı.
 * Odaların çakışma durumları, kapasite sınırları ve kiracı (Tenant) doğrulamaları burada yapılır.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final CurrentUserProvider currentUserProvider;
    private final PermissionResolutionService permissionResolutionService;
    private final ReservationNotificationService reservationNotificationService;
    private final AuditLogService auditLogService;

    private static final List<ReservationStatus> ACTIVE_CONFLICT_STATUSES =
            List.of(ReservationStatus.PENDING_APPROVAL, ReservationStatus.ACTIVE);

    /**
     * Yeni bir toplantı odası rezervasyonu oluşturur.
     * Tarih aralığı, oda kapasitesi, kiracı uyumluluğu ve oda doluluk durumu doğrulanır.
     */
    @Override
    @Transactional
    public ReservationResponseDto create(Long companyId, Long organizerId, ReservationRequestDto dto) {
        log.info("Creating reservation request for room: {} by organizer: {}", dto.roomId(), organizerId);

        Company company = findCompanyOrThrow(companyId);
        Room room = findRoomAndValidateTenant(dto.roomId(), companyId);
        User organizer = findUserAndValidateTenant(organizerId, companyId);

        validateTimeRange(dto.startTime(), dto.endTime());

        Set<User> participants = resolveParticipants(companyId, dto.participantIds());

        boolean hasConflict = reservationRepository
                .existsByRoomIdAndCompanyIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        dto.roomId(), companyId, ACTIVE_CONFLICT_STATUSES, dto.endTime(), dto.startTime()
                );

        if (hasConflict) {
            throw new BusinessException(ErrorCode.RESERVATION_CONFLICT);
        }

        Instant deadlineCandidate = Instant.now().plus(24, ChronoUnit.HOURS);
        Instant startTimeAsInstant = dto.startTime().atZone(AppConstants.ISTANBUL_ZONE).toInstant();
        Instant approvalDeadline = deadlineCandidate.isBefore(startTimeAsInstant)
                ? deadlineCandidate
                : startTimeAsInstant;

        Reservation reservation = Reservation.builder()
                .title(dto.title())
                .description(dto.description())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .participants(participants)
                .status(ReservationStatus.PENDING_APPROVAL)
                .approvalDeadline(approvalDeadline)
                .room(room)
                .organizer(organizer)
                .company(company)
                .active(true)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created with id: {}", saved.getId());

        if (saved.exceedsRoomCapacity()) {
            log.warn("Reservation {} exceeds room capacity (participants: {}, capacity: {})",
                    saved.getId(), saved.getParticipants().size(), room.getCapacity());
        }

        auditLogService.log(
                companyId,
                organizerId,
                "RESERVATION_CREATED",
                "RESERVATION",
                saved.getId(),
                "Reservation '" + saved.getTitle() + "' requested for room '" + room.getName() + "'"
        );

        return reservationMapper.toResponseDto(saved);
    }

    /**
     * Rezervasyon detaylarını günceller.
     * Sadece toplantıyı düzenleyen kişi güncelleme yapabilir.
     */
    @Override
    @Transactional
    public ReservationResponseDto update(Long companyId, Long organizerId, Long id, UpdateReservationRequestDto dto) {
        log.info("Updating reservation id: {} by user: {}", id, organizerId);

        Reservation reservation = findReservationOrThrow(id, companyId);

        if (!reservation.getOrganizer().getId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        Room newRoom = findRoomAndValidateTenant(dto.roomId(), companyId);

        boolean hasConflict = reservationRepository
                .existsByRoomIdAndCompanyIdAndIdNotAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        dto.roomId(), companyId, id, ACTIVE_CONFLICT_STATUSES, dto.endTime(), dto.startTime()
                );

        if (hasConflict) {
            throw new BusinessException(ErrorCode.RESERVATION_CONFLICT);
        }

        reservation.updateDetails(
                dto.title(), dto.description(), dto.startTime(), dto.endTime(), newRoom
        );

        auditLogService.log(
                companyId, organizerId, "RESERVATION_UPDATED", "RESERVATION", id,
                "Reservation '" + reservation.getTitle() + "' updated"
        );

        if (reservation.exceedsRoomCapacity()) {
            log.warn("Reservation {} exceeds room capacity after update", id);
        }

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Rezervasyonu onaylar.
     * Çakışan diğer bekleyen (PENDING_APPROVAL) istekleri otomatik olarak REDDEDER.
     */
    @Override
    @Transactional
    public ReservationResponseDto approve(Long companyId, Long id) {
        log.info("Approving reservation id: {}", id);

        Reservation target = findReservationOrThrow(id, companyId);

        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        if (!currentUser.superAdmin() && target.belongsTo(findUserAndValidateTenant(currentUser.userId(), companyId))) {
            log.warn("User {} attempted to approve their own reservation {}", currentUser.userId(), id);
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        List<Reservation> lockedConflicts = reservationRepository.findPendingConflictsForUpdate(
                target.getRoom().getId(), companyId, target.getStartTime(), target.getEndTime()
        );

        Reservation lockedTarget = lockedConflicts.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_PENDING_APPROVAL));

        lockedTarget.approve();

        List<Reservation> autoRejected = lockedConflicts.stream()
                .filter(r -> !r.getId().equals(id))
                .toList();

        autoRejected.forEach(Reservation::autoRejectDueToConflict);

        Long actorId = currentUser.superAdmin() ? null : currentUser.userId();

        auditLogService.log(
                companyId, actorId, "RESERVATION_APPROVED", "RESERVATION", id,
                "Reservation '" + lockedTarget.getTitle() + "' approved"
        );

        autoRejected.forEach(rejected ->
                auditLogService.log(
                        companyId, actorId, "RESERVATION_AUTO_REJECTED", "RESERVATION", rejected.getId(),
                        "Reservation '" + rejected.getTitle() + "' auto-rejected due to conflict with approved reservation " + id
                )
        );

        log.info("Reservation approved successfully with id: {}", id);
        if (!autoRejected.isEmpty()) {
            log.info("Auto-rejected {} conflicting pending reservation(s) for room {}",
                    autoRejected.size(), target.getRoom().getId());
        }

        reservationNotificationService.notifyApproval(lockedTarget);

        autoRejected.forEach(rejected ->
                reservationNotificationService.notifyOrganizerOnly(
                        rejected,
                        "Rezervasyon Talebiniz Reddedildi",
                        "'" + rejected.getTitle() + "' başlıklı talebiniz, aynı saatte başka bir talep onaylandığı için otomatik olarak reddedildi."
                )
        );

        return reservationMapper.toResponseDto(lockedTarget);
    }

    /**
     * Rezervasyon başvurusunu gerekçe belirterek reddeder.
     */
    @Override
    @Transactional
    public ReservationResponseDto reject(Long companyId, Long id, String reason) {
        log.info("Rejecting reservation id: {}, reason: {}", id, reason);
        Reservation reservation = findReservationOrThrow(id, companyId);
        reservation.reject(reason);

        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        auditLogService.log(
                companyId,
                currentUser.superAdmin() ? null : currentUser.userId(),
                "RESERVATION_REJECTED",
                "RESERVATION",
                id,
                "Reservation '" + reservation.getTitle() + "' rejected: " + reason
        );

        reservationNotificationService.notifyOrganizerOnly(
                reservation,
                "Rezervasyon Talebiniz Reddedildi",
                "'" + reservation.getTitle() + "' başlıklı talebiniz reddedildi. Sebep: " + reason
        );

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Aktif bir rezervasyonu gerekçe belirterek iptal eder.
     */
    @Override
    @Transactional
    public void cancel(Long companyId, Long userId, Long id, String reason) {
        log.info("Cancelling reservation id: {} by user: {}", id, userId);
        Reservation reservation = findReservationOrThrow(id, companyId);

        boolean isOrganizer = reservation.getOrganizer().getId().equals(userId);
        boolean hasCancelAllPermission = permissionResolutionService
                .resolveEffectivePermissions(userId)
                .contains("RESERVATION_CANCEL_ALL");

        if (!isOrganizer && !hasCancelAllPermission) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        boolean wasActive = reservation.getStatus() == ReservationStatus.ACTIVE;

        reservation.cancel(reason);

        auditLogService.log(
                companyId, userId, "RESERVATION_CANCELLED", "RESERVATION", id,
                "Reservation '" + reservation.getTitle() + "' cancelled: " + reason
        );

        if (wasActive) {
            reservationNotificationService.notifyCancellation(reservation);
        } else {
            reservationNotificationService.notifyOrganizerOnly(
                    reservation,
                    "Rezervasyon Talebiniz İptal Edildi",
                    "'" + reservation.getTitle() + "' başlıklı talebiniz, onaylanmadan önce iptal edildi."
            );
        }
    }

    @Override
    public ReservationResponseDto getById(Long companyId, Long id) {
        return reservationMapper.toResponseDto(findReservationOrThrow(id, companyId));
    }

    @Override
    public Page<ReservationResponseDto> getAll(Long companyId, Pageable pageable) {
        return reservationRepository.findAllByCompanyId(companyId, pageable)
                .map(reservationMapper::toResponseDto);
    }

    @Override
    public Page<ReservationResponseDto> getAllByRoom(Long companyId, Long roomId, Pageable pageable) {
        return reservationRepository.findAllByRoomIdAndCompanyId(roomId, companyId, pageable)
                .map(reservationMapper::toResponseDto);
    }

    @Override
    public Page<ReservationResponseDto> getAllByOrganizer(Long companyId, Long organizerId, Pageable pageable) {
        return reservationRepository.findAllByOrganizerIdAndCompanyId(organizerId, companyId, pageable)
                .map(reservationMapper::toResponseDto);
    }

    @Override
    public Page<ReservationResponseDto> getAllByDateRange(
            Long companyId, LocalDateTime from, LocalDateTime to, Pageable pageable
    ) {
        log.debug("Fetching reservations for company: {} between {} and {}", companyId, from, to);
        return reservationRepository
                .findAllByCompanyIdAndStartTimeLessThanAndEndTimeGreaterThan(companyId, to, from, pageable)
                .map(reservationMapper::toResponseDto);
    }

    @Override
    public Page<ReservationResponseDto> getMyReservations(Long companyId, Long userId, Pageable pageable) {
        return reservationRepository.findAllByOrganizerIdAndCompanyId(userId, companyId, pageable)
                .map(reservationMapper::toResponseDto);
    }

    @Override
    @Transactional
    public ReservationResponseDto addParticipant(Long companyId, Long organizerId, Long reservationId, Long userId) {
        log.info("Adding participant {} to reservation {} by user {}", userId, reservationId, organizerId);

        Reservation reservation = findReservationOrThrow(reservationId, companyId);

        if (!reservation.getOrganizer().getId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        User newParticipant = findUserAndValidateTenant(userId, companyId);
        reservation.addParticipant(newParticipant);

        auditLogService.log(
                companyId, organizerId, "PARTICIPANT_ADDED", "RESERVATION", reservationId,
                "User '" + newParticipant.getFullName() + "' added as participant to reservation '" + reservation.getTitle() + "'"
        );

        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            reservationNotificationService.notifyParticipantAdded(reservation, newParticipant);
        }

        if (reservation.exceedsRoomCapacity()) {
            log.warn("Reservation {} exceeds room capacity after adding participant", reservationId);
        }

        return reservationMapper.toResponseDto(reservation);
    }

    @Override
    @Transactional
    public ReservationResponseDto removeParticipant(Long companyId, Long organizerId, Long reservationId, Long userId) {
        log.info("Removing participant {} from reservation {} by user {}", userId, reservationId, organizerId);

        Reservation reservation = findReservationOrThrow(reservationId, companyId);

        if (!reservation.getOrganizer().getId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        User participant = findUserAndValidateTenant(userId, companyId);
        reservation.removeParticipant(participant);

        auditLogService.log(
                companyId, organizerId, "PARTICIPANT_REMOVED", "RESERVATION", reservationId,
                "User '" + participant.getFullName() + "' removed as participant from reservation '" + reservation.getTitle() + "'"
        );

        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            reservationNotificationService.notifyParticipantRemoved(reservation, participant);
        }

        return reservationMapper.toResponseDto(reservation);
    }

    // ----- Yardımcı Metotlar -----

    private Company findCompanyOrThrow(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
    }

    private Reservation findReservationOrThrow(Long id, Long companyId) {
        return reservationRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private Room findRoomAndValidateTenant(Long roomId, Long companyId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_ROOM_NOT_FOUND));
        if (!room.getCompany().getId().equals(companyId)) {
            throw new BusinessException(ErrorCode.MEETING_ROOM_NOT_FOUND);
        }
        return room;
    }

    private User findUserAndValidateTenant(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.getCompany().getId().equals(companyId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RESERVATION_IN_PAST);
        }
    }

    private Set<User> resolveParticipants(Long companyId, Set<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return new HashSet<>();
        }
        return participantIds.stream()
                .map(id -> findUserAndValidateTenant(id, companyId))
                .collect(java.util.stream.Collectors.toSet());
    }
}
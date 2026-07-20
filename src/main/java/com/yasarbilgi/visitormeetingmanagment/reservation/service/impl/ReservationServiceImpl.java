package com.yasarbilgi.visitormeetingmanagment.reservation.service.impl;

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
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationService;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.room.repository.RoomRepository;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final UserRepository userRepository; // Arkadaşının yazacağı repository
    private final ReservationMapper reservationMapper;

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

        if (dto.participantCount() > room.getCapacity()) {
            throw new BusinessException(ErrorCode.RESERVATION_EXCEEDS_ROOM_CAPACITY);
        }

        // Çakışma Kontrolü: Aynı saatte reddedilmemiş (Rejected) veya iptal edilmemiş başka aktif rezervasyon var mı?
        boolean hasConflict = reservationRepository
                .existsByRoomIdAndCompanyIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        dto.roomId(), companyId, ReservationStatus.REJECTED, dto.endTime(), dto.startTime()
                );

        if (hasConflict) {
            throw new BusinessException(ErrorCode.RESERVATION_CONFLICT);
        }

        Reservation reservation = Reservation.builder()
                .title(dto.title())
                .description(dto.description())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .participantCount(dto.participantCount())
                .status(ReservationStatus.PENDING_APPROVAL)
                .room(room)
                .organizer(organizer)
                .company(company)
                .active(true)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created with id: {}", saved.getId());
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
        Room newRoom = findRoomAndValidateTenant(dto.roomId(), companyId);

        if (!reservation.getOrganizer().getId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        reservation.updateDetails(
                dto.title(), dto.description(), dto.startTime(),
                dto.endTime(), dto.participantCount(), newRoom
        );

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
        Reservation reservation = findReservationOrThrow(id, companyId);

        reservation.approve();

        log.info("Reservation approved successfully with id: {}", id);
        return reservationMapper.toResponseDto(reservation);
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
        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Aktif bir rezervasyonu gerekçe belirterek iptal eder.
     * Sadece toplantı sahibi iptal edebilir.
     */
    @Override
    @Transactional
    public void cancel(Long companyId, Long userId, Long id, String reason) {
        log.info("Cancelling reservation id: {} by user: {}", id, userId);
        Reservation reservation = findReservationOrThrow(id, companyId);

        if (!reservation.getOrganizer().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }

        reservation.cancel(reason);
    }

    @Override
    public ReservationResponseDto getById(Long companyId, Long id) {
        return reservationMapper.toResponseDto(findReservationOrThrow(id, companyId));
    }

    @Override
    public List<ReservationResponseDto> getAll(Long companyId) {
        return reservationRepository.findAllByCompanyId(companyId).stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ReservationResponseDto> getAllByRoom(Long companyId, Long roomId) {
        return reservationRepository.findAllByRoomIdAndCompanyId(roomId, companyId).stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ReservationResponseDto> getAllByOrganizer(Long companyId, Long organizerId) {
        return reservationRepository.findAllByOrganizerIdAndCompanyId(organizerId, companyId).stream()
                .map(reservationMapper::toResponseDto)
                .toList();
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
}

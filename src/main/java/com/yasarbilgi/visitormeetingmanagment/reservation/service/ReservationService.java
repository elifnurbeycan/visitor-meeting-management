package com.yasarbilgi.visitormeetingmanagment.reservation.service;

import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.UpdateReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Toplantı odası rezervasyonları (Reservation) ile ilgili iş mantığı kurallarını tanımlayan servis arayüzü.
 */
public interface ReservationService {

    /** Yeni bir rezervasyon talebi oluşturur. */
    ReservationResponseDto create(Long companyId, Long organizerId, ReservationRequestDto dto);

    /** Rezervasyon detaylarını günceller. */
    ReservationResponseDto update(Long companyId, Long organizerId, Long id, UpdateReservationRequestDto dto);

    /** Rezervasyon talebini onaylar ve çakışan diğer bekleyen talepleri otomatik reddeder. */
    ReservationResponseDto approve(Long companyId, Long id);

    /** Rezervasyon talebini reddeder. */
    ReservationResponseDto reject(Long companyId, Long id, String reason);

    /** Rezervasyonu iptal eder. */
    void cancel(Long companyId, Long userId, Long id, String reason);

    /** ID bazlı tekil rezervasyon sorgular. */
    ReservationResponseDto getById(Long companyId, Long id);

    /** Şirkete ait tüm rezervasyonları sayfalanmış şekilde listeler. */
    Page<ReservationResponseDto> getAll(Long companyId, Pageable pageable);

    /** Belirli bir odaya ait rezervasyonları sayfalanmış şekilde listeler. */
    Page<ReservationResponseDto> getAllByRoom(Long companyId, Long roomId, Pageable pageable);

    /** Toplantıyı düzenleyen kişiye göre rezervasyonları sayfalanmış şekilde listeler. */
    Page<ReservationResponseDto> getAllByOrganizer(Long companyId, Long organizerId, Pageable pageable);

    /** Verilen tarih aralığıyla kesişen rezervasyonları sayfalanmış şekilde listeler (takvim görünümü için). */
    Page<ReservationResponseDto> getAllByDateRange(Long companyId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<ReservationResponseDto> getMyReservations(Long companyId, Long userId, Pageable pageable);

    ReservationResponseDto addParticipant(Long companyId, Long organizerId, Long reservationId, Long userId);

    ReservationResponseDto removeParticipant(Long companyId, Long organizerId, Long reservationId, Long userId);
}
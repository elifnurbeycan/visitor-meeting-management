package com.yasarbilgi.visitormeetingmanagment.reservation.service;

import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.UpdateReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;

import java.util.List;

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

    /** Şirkete ait tüm rezervasyonları listeler. */
    List<ReservationResponseDto> getAll(Long companyId);

    /** Belirli bir odaya ait rezervasyonları listeler. */
    List<ReservationResponseDto> getAllByRoom(Long companyId, Long roomId);

    /** Toplantıyı düzenleyen kişiye göre rezervasyonları listeler. */
    List<ReservationResponseDto> getAllByOrganizer(Long companyId, Long organizerId);
}

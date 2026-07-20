package com.yasarbilgi.visitormeetingmanagment.reservation.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.UpdateReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Toplantı Odası Rezervasyonları REST API denetleyici sınıfı.
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /** Yeni bir rezervasyon talebi oluşturur. */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponseDto>> create(
            @RequestHeader("X-Company-Id") Long companyId,
            @RequestHeader("X-User-Id") Long organizerId,
            @Valid @RequestBody ReservationRequestDto dto
    ) {
        ReservationResponseDto created = reservationService.create(companyId, organizerId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation request created successfully", created));
    }

    /** Rezervasyon detaylarını günceller. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> update(
            @RequestHeader("X-Company-Id") Long companyId,
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequestDto dto
    ) {
        ReservationResponseDto updated = reservationService.update(companyId, organizerId, id, dto);
        return ResponseEntity.ok(ApiResponse.success("Reservation updated successfully", updated));
    }

    /** Rezervasyon başvurusunu onaylar. */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> approve(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id
    ) {
        ReservationResponseDto approved = reservationService.approve(companyId, id);
        return ResponseEntity.ok(ApiResponse.success("Reservation approved successfully", approved));
    }

    /** Rezervasyon başvurusunu reddeder. */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> reject(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        ReservationResponseDto rejected = reservationService.reject(companyId, id, reason);
        return ResponseEntity.ok(ApiResponse.success("Reservation request rejected", rejected));
    }

    /** Rezervasyonu iptal eder. */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @RequestHeader("X-Company-Id") Long companyId,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        reservationService.cancel(companyId, userId, id, reason);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully"));
    }

    /** ID'ye göre tekil rezervasyon sorgular. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> getById(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id
    ) {
        ReservationResponseDto reservation = reservationService.getById(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    /** Şirkete ait tüm rezervasyonları listeler. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getAll(
            @RequestHeader("X-Company-Id") Long companyId
    ) {
        List<ReservationResponseDto> list = reservationService.getAll(companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /** Odaya göre filtrelenmiş rezervasyonları listeler. */
    @GetMapping("/by-room/{roomId}")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getAllByRoom(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long roomId
    ) {
        List<ReservationResponseDto> list = reservationService.getAllByRoom(companyId, roomId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /** Organizatöre göre filtrelenmiş rezervasyonları listeler. */
    @GetMapping("/by-organizer/{organizerId}")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getAllByOrganizer(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long organizerId
    ) {
        List<ReservationResponseDto> list = reservationService.getAllByOrganizer(companyId, organizerId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}

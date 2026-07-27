package com.yasarbilgi.visitormeetingmanagment.reservation.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.UpdateReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationService;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Toplantı Odası Rezervasyonları REST API denetleyici sınıfı.
 *
 * companyId ve organizerId/userId artık istemciden (header'dan) alınmıyor —
 * her zaman JWT'den (AuthenticatedUser) çözülüyor. Böylece kimse başka bir
 * kullanıcı ya da şirket adına işlem yapamaz.
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponseDto>> create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ReservationRequestDto dto
    ) {
        ReservationResponseDto created = reservationService.create(
                currentUser.companyId(), currentUser.userId(), dto
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation request created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequestDto dto
    ) {
        ReservationResponseDto updated = reservationService.update(
                currentUser.companyId(), currentUser.userId(), id, dto
        );
        return ResponseEntity.ok(ApiResponse.success("Reservation updated successfully", updated));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> approve(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        ReservationResponseDto approved = reservationService.approve(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success("Reservation approved successfully", approved));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> reject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        ReservationResponseDto rejected = reservationService.reject(currentUser.companyId(), id, reason);
        return ResponseEntity.ok(ApiResponse.success("Reservation request rejected", rejected));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        reservationService.cancel(currentUser.companyId(), currentUser.userId(), id, reason);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        ReservationResponseDto reservation = reservationService.getById(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getAll(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        List<ReservationResponseDto> list = reservationService.getAll(currentUser.companyId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/by-room/{roomId}")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getAllByRoom(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long roomId
    ) {
        List<ReservationResponseDto> list = reservationService.getAllByRoom(currentUser.companyId(), roomId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/by-organizer/{organizerId}")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getAllByOrganizer(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long organizerId
    ) {
        List<ReservationResponseDto> list = reservationService.getAllByOrganizer(currentUser.companyId(), organizerId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
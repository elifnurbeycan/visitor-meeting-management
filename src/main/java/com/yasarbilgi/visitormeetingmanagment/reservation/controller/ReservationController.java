package com.yasarbilgi.visitormeetingmanagment.reservation.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.UpdateReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.service.ReservationService;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @PreAuthorize("hasAuthority('RESERVATION_CREATE')")
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

    @PreAuthorize("hasAuthority('RESERVATION_UPDATE_OWN')")
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

    @PreAuthorize("hasAuthority('RESERVATION_APPROVE')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> approve(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        ReservationResponseDto approved = reservationService.approve(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success("Reservation approved successfully", approved));
    }

    @PreAuthorize("hasAuthority('RESERVATION_REJECT')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> reject(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        ReservationResponseDto rejected = reservationService.reject(currentUser.companyId(), id, reason);
        return ResponseEntity.ok(ApiResponse.success("Reservation request rejected", rejected));
    }

    @PreAuthorize("hasAnyAuthority('RESERVATION_CANCEL_OWN', 'RESERVATION_CANCEL_ALL')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        reservationService.cancel(currentUser.companyId(), currentUser.userId(), id, reason);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully"));
    }

    @PreAuthorize("hasAuthority('RESERVATION_VIEW_DETAILS')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        ReservationResponseDto reservation = reservationService.getById(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    @PreAuthorize("hasAuthority('RESERVATION_VIEW_ALL')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponseDto>>> getAll(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        PageResponse<ReservationResponseDto> page = PageResponse.of(
                reservationService.getAll(currentUser.companyId(), pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PreAuthorize("hasAuthority('RESERVATION_FILTER_BY_ROOM')")
    @GetMapping("/by-room/{roomId}")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponseDto>>> getAllByRoom(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long roomId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        PageResponse<ReservationResponseDto> page = PageResponse.of(
                reservationService.getAllByRoom(currentUser.companyId(), roomId, pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PreAuthorize("hasAuthority('RESERVATION_VIEW_ALL')")
    @GetMapping("/by-organizer/{organizerId}")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponseDto>>> getAllByOrganizer(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long organizerId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        PageResponse<ReservationResponseDto> page = PageResponse.of(
                reservationService.getAllByOrganizer(currentUser.companyId(), organizerId, pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PreAuthorize("hasAuthority('RESERVATION_FILTER_BY_DATE')")
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponseDto>>> getAllByDateRange(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 50, sort = "startTime") Pageable pageable
    ) {
        PageResponse<ReservationResponseDto> page = PageResponse.of(
                reservationService.getAllByDateRange(currentUser.companyId(), from, to, pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PreAuthorize("hasAuthority('RESERVATION_UPDATE_OWN')")
    @PatchMapping("/{id}/participants/{userId}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> addParticipant(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        ReservationResponseDto updated = reservationService.addParticipant(
                currentUser.companyId(), currentUser.userId(), id, userId
        );
        return ResponseEntity.ok(ApiResponse.success("Participant added successfully", updated));
    }

    @PreAuthorize("hasAuthority('RESERVATION_UPDATE_OWN')")
    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<ApiResponse<ReservationResponseDto>> removeParticipant(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        ReservationResponseDto updated = reservationService.removeParticipant(
                currentUser.companyId(), currentUser.userId(), id, userId
        );
        return ResponseEntity.ok(ApiResponse.success("Participant removed successfully", updated));
    }

    @PreAuthorize("hasAnyAuthority('RESERVATION_VIEW_OWN', 'RESERVATION_VIEW_ALL')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponseDto>>> getMyReservations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        PageResponse<ReservationResponseDto> page = PageResponse.of(
                reservationService.getMyReservations(currentUser.companyId(), currentUser.userId(), pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
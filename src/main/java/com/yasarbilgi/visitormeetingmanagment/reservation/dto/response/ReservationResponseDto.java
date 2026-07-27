package com.yasarbilgi.visitormeetingmanagment.reservation.dto.response;

import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ReservationResponseDto(

        Long id,

        String title,

        String description,

        LocalDateTime startTime,

        LocalDateTime endTime,

        Set<UserSummary> participants,

        boolean capacityWarning,

        ReservationStatus status,

        Instant approvalDeadline,

        String rejectionReason,

        String cancelReason,

        RoomSummary room,

        UserSummary organizer,

        boolean active,

        Instant createdAt,

        Instant updatedAt

) {

    @Builder
    public record RoomSummary(
            Long id,
            String name,
            String location,
            Integer capacity
    ) {
    }

    @Builder
    public record UserSummary(
            Long id,
            String fullName,
            String email
    ) {
    }
}
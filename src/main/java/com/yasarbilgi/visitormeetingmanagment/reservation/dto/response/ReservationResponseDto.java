package com.yasarbilgi.visitormeetingmanagment.reservation.dto.response;

import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
public record ReservationResponseDto(

        Long id,

        String title,

        String description,

        LocalDateTime startTime,

        LocalDateTime endTime,

        Integer participantCount,

        ReservationStatus status,

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
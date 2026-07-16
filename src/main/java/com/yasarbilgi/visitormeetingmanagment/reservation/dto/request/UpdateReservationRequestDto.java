package com.yasarbilgi.visitormeetingmanagment.reservation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UpdateReservationRequestDto(

        @NotBlank(message = "{reservation.title.notBlank}")
        @Size(max = 200, message = "{reservation.title.size}")
        String title,

        @Size(max = 1000, message = "{reservation.description.size}")
        String description,

        @NotNull(message = "{reservation.startTime.notNull}")
        @FutureOrPresent(message = "{reservation.startTime.futureOrPresent}")
        LocalDateTime startTime,

        @NotNull(message = "{reservation.endTime.notNull}")
        @FutureOrPresent(message = "{reservation.endTime.futureOrPresent}")
        LocalDateTime endTime,

        @NotNull(message = "{reservation.roomId.notNull}")
        Long roomId,

        @NotNull(message = "{reservation.participantCount.notNull}")
        @Min(value = 1, message = "{reservation.participantCount.min}")
        Integer participantCount

) {
}
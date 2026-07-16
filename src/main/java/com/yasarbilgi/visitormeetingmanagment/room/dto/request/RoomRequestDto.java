package com.yasarbilgi.visitormeetingmanagment.room.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record RoomRequestDto(

        @NotBlank(message = "{meetingRoom.name.notBlank}")
        @Size(max = 150, message = "{meetingRoom.name.size}")
        String name,

        @Size(max = 150, message = "{meetingRoom.location.size}")
        String location,

        @NotNull(message = "{meetingRoom.capacity.min}")
        @Min(value = 1, message = "{meetingRoom.capacity.min}")
        Integer capacity,

        @Size(max = 1000, message = "{meetingRoom.description.size}")
        String description,

        Set<Long> featureIds

) {
}
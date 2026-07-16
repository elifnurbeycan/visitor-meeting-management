package com.yasarbilgi.visitormeetingmanagment.reservation.mapper;

import com.yasarbilgi.visitormeetingmanagment.reservation.dto.request.ReservationRequestDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.dto.response.ReservationResponseDto;
import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "room", source = "room")
    @Mapping(target = "organizer", source = "organizer")
    ReservationResponseDto toResponseDto(
            Reservation reservation
    );

    ReservationResponseDto.RoomSummary toRoomSummary(
            Room room
    );

    @Mapping(
            target = "fullName",
            expression = "java(user.getFullName())"
    )
    ReservationResponseDto.UserSummary toUserSummary(
            User user
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deactivatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Reservation toEntity(
            ReservationRequestDto dto
    );
}
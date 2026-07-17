package com.yasarbilgi.visitormeetingmanagment.reservation.mapper;

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
    ReservationResponseDto toResponseDto(Reservation reservation);

    ReservationResponseDto.RoomSummary toRoomSummary(Room room);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    ReservationResponseDto.UserSummary toUserSummary(User user);

}
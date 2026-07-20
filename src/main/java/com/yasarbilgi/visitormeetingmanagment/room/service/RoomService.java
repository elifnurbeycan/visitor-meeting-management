package com.yasarbilgi.visitormeetingmanagment.room.service;

import com.yasarbilgi.visitormeetingmanagment.room.dto.request.RoomRequestDto;
import com.yasarbilgi.visitormeetingmanagment.room.dto.request.UpdateRoomRequestDto;
import com.yasarbilgi.visitormeetingmanagment.room.dto.response.RoomResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {

    RoomResponseDto create(
            Long companyId,
            RoomRequestDto dto
    );

    RoomResponseDto update(
            Long companyId,
            Long id,
            UpdateRoomRequestDto dto
    );

    RoomResponseDto getById(
            Long companyId,
            Long id
    );

    Page<RoomResponseDto> getAll(
            Long companyId,
            Pageable pageable
    );

    Page<RoomResponseDto> getAllByActive(
            Long companyId,
            boolean active,
            Pageable pageable
    );

    Page<RoomResponseDto> getAllByMinimumCapacity(
            Long companyId,
            boolean active,
            int capacity,
            Pageable pageable
    );

    Page<RoomResponseDto> search(
            Long companyId,
            boolean active,
            String keyword,
            Pageable pageable
    );

    RoomResponseDto addFeature(
            Long companyId,
            Long roomId,
            Long featureId
    );

    RoomResponseDto removeFeature(
            Long companyId,
            Long roomId,
            Long featureId
    );

    void deactivate(
            Long companyId,
            Long id
    );

    void activate(
            Long companyId,
            Long id
    );
}
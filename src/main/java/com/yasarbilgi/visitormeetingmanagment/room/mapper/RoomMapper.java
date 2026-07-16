package com.yasarbilgi.visitormeetingmanagment.room.mapper;

import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import com.yasarbilgi.visitormeetingmanagment.room.dto.response.RoomResponseDto;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "features", source = "features")
    RoomResponseDto toResponseDto(Room room);

    RoomResponseDto.FeatureSummary toFeatureSummary(Feature feature);

    Set<RoomResponseDto.FeatureSummary> toFeatureSummarySet(Set<Feature> features);

}
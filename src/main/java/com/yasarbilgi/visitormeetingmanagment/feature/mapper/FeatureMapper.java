package com.yasarbilgi.visitormeetingmanagment.feature.mapper;

import com.yasarbilgi.visitormeetingmanagment.feature.dto.request.FeatureRequestDto;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.response.FeatureResponseDto;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    FeatureResponseDto toResponseDto(Feature feature);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deactivatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    Feature toEntity(FeatureRequestDto dto);

}

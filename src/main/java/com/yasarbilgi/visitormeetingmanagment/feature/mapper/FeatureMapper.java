package com.yasarbilgi.visitormeetingmanagment.feature.mapper;

import com.yasarbilgi.visitormeetingmanagment.feature.dto.response.FeatureResponseDto;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    FeatureResponseDto toResponseDto(Feature feature);

}

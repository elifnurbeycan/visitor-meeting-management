package com.yasarbilgi.visitormeetingmanagment.platform.mapper;

import com.yasarbilgi.visitormeetingmanagment.platform.dto.response.SuperAdminResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.entity.SuperAdmin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SuperAdminMapper {

    SuperAdminResponseDto toResponseDto(SuperAdmin superAdmin);

}
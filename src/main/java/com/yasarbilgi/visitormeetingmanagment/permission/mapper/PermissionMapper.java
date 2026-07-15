package com.yasarbilgi.visitormeetingmanagment.permission.mapper;

import com.yasarbilgi.visitormeetingmanagment.permission.dto.response.PermissionResponseDto;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @Mapping(target = "code", expression = "java(permission.getCode().name())")
    PermissionResponseDto toResponseDto(Permission permission);

}
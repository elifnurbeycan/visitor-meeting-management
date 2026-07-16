package com.yasarbilgi.visitormeetingmanagment.userpermission.mapper;

import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPermissionOverrideMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "permission.id", source = "permission.id")
    @Mapping(target = "permission.code", expression = "java(override.getPermission().getCode().name())")
    @Mapping(target = "permission.name", source = "permission.name")
    UserPermissionOverrideResponseDto toResponseDto(UserPermissionOverride override);
}

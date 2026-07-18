package com.yasarbilgi.visitormeetingmanagment.role.mapper;

import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.role.dto.response.RoleTemplateResponseDto;
import com.yasarbilgi.visitormeetingmanagment.role.entity.RoleTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleTemplateMapper {

    @Mapping(target = "permissions", source = "permissions")
    RoleTemplateResponseDto toResponseDto(RoleTemplate roleTemplate);

    @Mapping(target = "code", expression = "java(permission.getCode().name())")
    RoleTemplateResponseDto.PermissionSummary toPermissionSummary(Permission permission);

    Set<RoleTemplateResponseDto.PermissionSummary> toPermissionSummarySet(Set<Permission> permissions);

}
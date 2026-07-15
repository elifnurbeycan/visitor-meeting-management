package com.yasarbilgi.visitormeetingmanagment.role.mapper;

import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission; // Permission yazılınca gerçek paket yolu teyit edilecek
import com.yasarbilgi.visitormeetingmanagment.role.dto.response.RoleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", source = "permissions")
    RoleResponseDto toResponseDto(Role role);

    RoleResponseDto.PermissionSummary toPermissionSummary(Permission permission);

    Set<RoleResponseDto.PermissionSummary> toPermissionSummarySet(Set<Permission> permissions);

}
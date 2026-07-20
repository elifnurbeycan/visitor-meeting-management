package com.yasarbilgi.visitormeetingmanagment.role.service;

import com.yasarbilgi.visitormeetingmanagment.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.response.RoleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleResponseDto create(
            Long companyId,
            CreateRoleRequestDto dto
    );

    RoleResponseDto update(
            Long companyId,
            Long id,
            UpdateRoleRequestDto dto
    );

    RoleResponseDto getById(
            Long companyId,
            Long id
    );

    Page<RoleResponseDto> getAll(
            Long companyId,
            Pageable pageable
    );

    Page<RoleResponseDto> getAllByActive(
            Long companyId,
            boolean active,
            Pageable pageable
    );

    Page<RoleResponseDto> getAllBySystemRole(
            Long companyId,
            boolean systemRole,
            Pageable pageable
    );

    Page<RoleResponseDto> search(
            Long companyId,
            boolean active,
            String keyword,
            Pageable pageable
    );

    RoleResponseDto assignPermission(
            Long companyId,
            Long roleId,
            Long permissionId
    );

    RoleResponseDto revokePermission(
            Long companyId,
            Long roleId,
            Long permissionId
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
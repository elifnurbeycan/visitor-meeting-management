package com.yasarbilgi.visitormeetingmanagment.userpermission.service;

import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request.UserPermissionOverrideRequestDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPermissionOverrideService {

    UserPermissionOverrideResponseDto create(Long companyId, UserPermissionOverrideRequestDto dto);

    UserPermissionOverrideResponseDto update(Long companyId, Long id, UserPermissionOverrideRequestDto dto);

    UserPermissionOverrideResponseDto getById(Long companyId, Long id);

    Page<UserPermissionOverrideResponseDto> getAll(Long companyId, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> getAllByUserId(Long companyId, Long userId, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> getAllByUserIdAndActive(Long companyId, Long userId, boolean active, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable);

    void deactivate(Long companyId, Long id);

    void activate(Long companyId, Long id);
}
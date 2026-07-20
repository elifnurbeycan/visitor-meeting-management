package com.yasarbilgi.visitormeetingmanagment.userpermission.service;

import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request.UserPermissionOverrideRequestDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPermissionOverrideService {

    UserPermissionOverrideResponseDto create(UserPermissionOverrideRequestDto dto);

    UserPermissionOverrideResponseDto update(Long id, UserPermissionOverrideRequestDto dto);

    UserPermissionOverrideResponseDto getById(Long id);

    Page<UserPermissionOverrideResponseDto> getAll(Pageable pageable);

    Page<UserPermissionOverrideResponseDto> getAllByActive(boolean active, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> getAllByUserId(Long userId, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> getAllByUserIdAndActive(Long userId, boolean active, Pageable pageable);

    Page<UserPermissionOverrideResponseDto> search(boolean active, String keyword, Pageable pageable);

    void deactivate(Long id);

    void activate(Long id);
}

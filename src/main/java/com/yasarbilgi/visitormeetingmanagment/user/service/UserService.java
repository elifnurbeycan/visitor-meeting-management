package com.yasarbilgi.visitormeetingmanagment.user.service;

import com.yasarbilgi.visitormeetingmanagment.user.dto.request.UserRequestDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    // ----- CRUD -----

    UserResponseDto create(Long companyId, UserRequestDto dto);

    UserResponseDto update(Long companyId, Long userId, UserRequestDto dto);

    UserResponseDto getById(Long companyId, Long userId);

    UserResponseDto getByEmail(Long companyId, String email);

    UserResponseDto getOwner(Long companyId);

    // ----- Listeleme -----

    Page<UserResponseDto> getAll(Long companyId, Pageable pageable);

    Page<UserResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable);

    Page<UserResponseDto> getAllByJobTitle(Long companyId, Long jobTitleId, Pageable pageable);

    Page<UserResponseDto> getAllByRole(Long companyId, Long roleId, Pageable pageable);

    Page<UserResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable);

    // ----- Durum yönetimi -----

    void deactivate(Long companyId, Long userId);

    void activate(Long companyId, Long userId);

    // ----- Rol yönetimi -----

    UserResponseDto assignRole(Long companyId, Long userId, Long roleId);

    UserResponseDto revokeRole(Long companyId, Long userId, Long roleId);

    // ----- Unvan yönetimi -----

    UserResponseDto changeJobTitle(Long companyId, Long userId, Long jobTitleId);

    // ----- Owner yönetimi -----

    UserResponseDto promoteToOwner(Long companyId, Long userId);

    UserResponseDto transferOwnership(Long companyId, Long currentOwnerId, Long newOwnerId);

    // ----- İstatistik -----

    long countUsers(Long companyId);

    long countActiveUsers(Long companyId);

}
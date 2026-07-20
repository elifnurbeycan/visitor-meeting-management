package com.yasarbilgi.visitormeetingmanagment.permission.service;

import com.yasarbilgi.visitormeetingmanagment.permission.dto.request.PermissionUpdateRequestDto;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.response.PermissionResponseDto;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCategory;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionService {

    PermissionResponseDto update(Long id, PermissionUpdateRequestDto dto);

    PermissionResponseDto getById(Long id);

    PermissionResponseDto getByCode(PermissionCode code);

    Page<PermissionResponseDto> getAll(Pageable pageable);

    Page<PermissionResponseDto> getAllByActive(boolean active, Pageable pageable);

    Page<PermissionResponseDto> getAllByCategory(PermissionCategory category, Pageable pageable);

    Page<PermissionResponseDto> getAllByActiveAndCategory(boolean active, PermissionCategory category, Pageable pageable);

    Page<PermissionResponseDto> search(boolean active, String keyword, Pageable pageable);

    Page<PermissionResponseDto> getAllByCategoryOrdered(PermissionCategory category, Pageable pageable);

    long countByCategory(PermissionCategory category);

    long countBySystemPermission(boolean systemPermission);

    void deactivate(Long id);

    void activate(Long id);

}

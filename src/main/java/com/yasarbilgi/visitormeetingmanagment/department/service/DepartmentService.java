package com.yasarbilgi.visitormeetingmanagment.department.service;

import com.yasarbilgi.visitormeetingmanagment.department.dto.request.DepartmentRequestDto;
import com.yasarbilgi.visitormeetingmanagment.department.dto.response.DepartmentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    DepartmentResponseDto create(Long companyId, DepartmentRequestDto dto);

    DepartmentResponseDto update(Long companyId, Long departmentId, DepartmentRequestDto dto);

    DepartmentResponseDto getById(Long companyId, Long departmentId);

    Page<DepartmentResponseDto> getAll(Long companyId, Pageable pageable);

    Page<DepartmentResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable);

    Page<DepartmentResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable);

    void deactivate(Long companyId, Long departmentId);

    void activate(Long companyId, Long departmentId);

}
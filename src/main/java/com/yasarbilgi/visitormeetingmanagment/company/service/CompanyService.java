package com.yasarbilgi.visitormeetingmanagment.company.service;

import com.yasarbilgi.visitormeetingmanagment.company.dto.request.CompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.UpdateCompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {

    CompanyResponseDto create(CompanyRequestDto dto);

    CompanyResponseDto update(Long id, UpdateCompanyRequestDto dto);

    CompanyResponseDto getById(Long id);

    CompanyResponseDto getBySlug(String slug);

    Page<CompanyResponseDto> getAll(Pageable pageable);

    Page<CompanyResponseDto> getAllByActive(boolean active, Pageable pageable);

    Page<CompanyResponseDto> getAllByStatus(CompanyStatus status, Pageable pageable);

    Page<CompanyResponseDto> search(boolean active, String keyword, Pageable pageable);

    Page<CompanyResponseDto> getPendingApprovals(Pageable pageable);

    long countPendingApprovals();

    CompanyResponseDto approve(Long id);

    CompanyResponseDto reject(Long id, String reason);

    void hardDelete(Long id);

    void deactivate(Long id);

    void activate(Long id);


}

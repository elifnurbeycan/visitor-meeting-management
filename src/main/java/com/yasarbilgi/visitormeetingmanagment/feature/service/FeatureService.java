package com.yasarbilgi.visitormeetingmanagment.feature.service;

import com.yasarbilgi.visitormeetingmanagment.feature.dto.request.FeatureRequestDto;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.response.FeatureResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeatureService {

    FeatureResponseDto create(Long companyId, FeatureRequestDto dto);

    FeatureResponseDto update(Long companyId, Long id, FeatureRequestDto dto);

    FeatureResponseDto getById(Long companyId, Long id);

    Page<FeatureResponseDto> getAll(Long companyId, Pageable pageable);

    Page<FeatureResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable);

    Page<FeatureResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable);

    long countAll(Long companyId);

    void deactivate(Long companyId, Long id);

    void activate(Long companyId, Long id);

}

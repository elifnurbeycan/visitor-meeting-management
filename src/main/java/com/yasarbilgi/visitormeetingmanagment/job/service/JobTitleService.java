package com.yasarbilgi.visitormeetingmanagment.job.service;

import com.yasarbilgi.visitormeetingmanagment.job.dto.request.JobTitleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.job.dto.response.JobTitleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface JobTitleService {

    /** Yeni bir iş unvanı oluşturur. */
    JobTitleResponseDto create(Long companyId, JobTitleRequestDto dto);

    /** Var olan bir iş unvanını günceller. */
    JobTitleResponseDto update(Long companyId, Long id, JobTitleRequestDto dto);

    /** ID'ye göre iş unvanı getirir. */
    JobTitleResponseDto getById(Long companyId, Long id);

    /** Tüm iş unvanlarını sayfalanmış olarak listeler. */
    Page<JobTitleResponseDto> getAll(Long companyId, Pageable pageable);

    /** Aktiflik durumuna göre sayfalanmış listeleme yapar. */
    Page<JobTitleResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable);

    /** İsim ve açıklamaya göre arama yapar. */
    Page<JobTitleResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable);

    /** İş unvanını pasif duruma getirir (soft-delete). */
    void deactivate(Long companyId, Long id);

    /** Pasif iş unvanını tekrar aktif duruma getirir. */
    void activate(Long companyId, Long id);
}

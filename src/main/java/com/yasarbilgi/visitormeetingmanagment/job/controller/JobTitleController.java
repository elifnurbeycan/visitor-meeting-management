package com.yasarbilgi.visitormeetingmanagment.job.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.job.dto.request.JobTitleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.job.dto.response.JobTitleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.job.service.JobTitleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * JobTitle (İş Unvanı) kaynağı için dış dünyaya sunulan REST denetleyici sınıfı.
 * Güvenlik ve JWT aşamasına geçildiğinde X-Company-Id başlığı kolayca filtrelerle değiştirilebilir.
 */
@RestController
@RequestMapping("/api/v1/job-titles")
@RequiredArgsConstructor
public class JobTitleController {

    private final JobTitleService jobTitleService;

    /**
     * Şirket için yeni bir iş unvanı oluşturur. Başarılı olursa 201 Created döner.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<JobTitleResponseDto>> create(
            @RequestHeader("X-Company-Id") Long companyId,
            @Valid @RequestBody JobTitleRequestDto dto
    ) {
        JobTitleResponseDto created = jobTitleService.create(companyId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job title created successfully", created));
    }

    /**
     * Belirtilen şirketteki iş unvanını günceller.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTitleResponseDto>> update(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id,
            @Valid @RequestBody JobTitleRequestDto dto
    ) {
        JobTitleResponseDto updated = jobTitleService.update(companyId, id, dto);
        return ResponseEntity.ok(ApiResponse.success("Job title updated successfully", updated));
    }

    /**
     * Şirkete ait iş unvanını ID'sine göre getirir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTitleResponseDto>> getById(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id
    ) {
        JobTitleResponseDto jobTitle = jobTitleService.getById(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(jobTitle));
    }

    /**
     * Şirkete ait tüm iş unvanlarını sayfalanmış olarak listeler.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobTitleResponseDto>>> getAll(
            @RequestHeader("X-Company-Id") Long companyId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<JobTitleResponseDto> jobTitles = PageResponse.of(jobTitleService.getAll(companyId, pageable));
        return ResponseEntity.ok(ApiResponse.success(jobTitles));
    }

    /**
     * Şirkete ait aktif/pasif durumuna göre filtrelenmiş iş unvanlarını sayfalanmış olarak listeler.
     */
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<JobTitleResponseDto>>> getAllByActive(
            @RequestHeader("X-Company-Id") Long companyId,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<JobTitleResponseDto> jobTitles =
                PageResponse.of(jobTitleService.getAllByActive(companyId, active, pageable));
        return ResponseEntity.ok(ApiResponse.success(jobTitles));
    }

    /**
     * Şirket içinde arama kelimesine göre iş unvanı araması yapar.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<JobTitleResponseDto>>> search(
            @RequestHeader("X-Company-Id") Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<JobTitleResponseDto> jobTitles =
                PageResponse.of(jobTitleService.search(companyId, active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(jobTitles));
    }

    /**
     * İş unvanını pasif duruma getirir (Soft Delete).
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id
    ) {
        jobTitleService.deactivate(companyId, id);
        return ResponseEntity.ok(ApiResponse.success("Job title deactivated successfully"));
    }

    /**
     * Pasif durumdaki iş unvanını tekrar aktif eder.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @RequestHeader("X-Company-Id") Long companyId,
            @PathVariable Long id
    ) {
        jobTitleService.activate(companyId, id);
        return ResponseEntity.ok(ApiResponse.success("Job title activated successfully"));
    }
}

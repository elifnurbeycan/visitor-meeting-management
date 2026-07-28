package com.yasarbilgi.visitormeetingmanagment.job.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.job.dto.request.JobTitleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.job.dto.response.JobTitleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.job.service.JobTitleService;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * JobTitle (İş Unvanı) kaynağı için dış dünyaya sunulan REST denetleyici sınıfı.

 * companyId artık istemciden (header'dan) alınmıyor — her zaman JWT'den
 * (AuthenticatedUser) çözülüyor. Böylece kimse başka bir şirket adına
 * işlem yapamaz.
 */
@RestController
@RequestMapping("/api/v1/job-titles")
@RequiredArgsConstructor
public class JobTitleController {

    private final JobTitleService jobTitleService;

    @PreAuthorize("hasAuthority('JOB_TITLE_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<JobTitleResponseDto>> create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody JobTitleRequestDto dto
    ) {
        JobTitleResponseDto created = jobTitleService.create(currentUser.companyId(), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job title created successfully", created));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTitleResponseDto>> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody JobTitleRequestDto dto
    ) {
        JobTitleResponseDto updated = jobTitleService.update(currentUser.companyId(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Job title updated successfully", updated));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTitleResponseDto>> getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        JobTitleResponseDto jobTitle = jobTitleService.getById(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success(jobTitle));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_VIEW')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobTitleResponseDto>>> getAll(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<JobTitleResponseDto> jobTitles =
                PageResponse.of(jobTitleService.getAll(currentUser.companyId(), pageable));
        return ResponseEntity.ok(ApiResponse.success(jobTitles));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_VIEW')")
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<JobTitleResponseDto>>> getAllByActive(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<JobTitleResponseDto> jobTitles =
                PageResponse.of(jobTitleService.getAllByActive(currentUser.companyId(), active, pageable));
        return ResponseEntity.ok(ApiResponse.success(jobTitles));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_VIEW')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<JobTitleResponseDto>>> search(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<JobTitleResponseDto> jobTitles =
                PageResponse.of(jobTitleService.search(currentUser.companyId(), active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(jobTitles));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_DEACTIVATE')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        jobTitleService.deactivate(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success("Job title deactivated successfully"));
    }

    @PreAuthorize("hasAuthority('JOB_TITLE_ACTIVATE')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        jobTitleService.activate(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success("Job title activated successfully"));
    }
}
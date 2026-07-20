package com.yasarbilgi.visitormeetingmanagment.userpermission.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request.UserPermissionOverrideRequestDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.service.UserPermissionOverrideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user-permission-overrides")
@RequiredArgsConstructor
public class UserPermissionOverrideController {

    private final UserPermissionOverrideService overrideService;


    @PostMapping
    public ResponseEntity<ApiResponse<UserPermissionOverrideResponseDto>> create(
            @Valid @RequestBody UserPermissionOverrideRequestDto dto
    ) {
        UserPermissionOverrideResponseDto created = overrideService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User permission override created successfully", created));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserPermissionOverrideResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserPermissionOverrideRequestDto dto
    ) {
        UserPermissionOverrideResponseDto updated = overrideService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("User permission override updated successfully", updated));
    }

    /**
     * ID'ye göre tekil bir kullanıcı yetki override kaydı getirir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserPermissionOverrideResponseDto>> getById(@PathVariable Long id) {
        UserPermissionOverrideResponseDto override = overrideService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(override));
    }

    /**
     * Tüm kullanıcı yetki override kayıtlarını sayfalanmış şekilde listeler.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAll(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides = PageResponse.of(overrideService.getAll(pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    /**
     * Aktif/pasif durumuna göre filtrelenmiş kullanıcı yetki override kayıtlarını sayfalanmış şekilde listeler.
     */
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAllByActive(
            @RequestParam boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAllByActive(active, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    /**
     * Belirli bir kullanıcıya ait yetki override kayıtlarını sayfalanmış şekilde listeler.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAllByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAllByUserId(userId, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    /**
     * Belirli bir kullanıcıya ait aktiflik durumuna göre filtrelenmiş yetki override kayıtlarını sayfalanmış şekilde listeler.
     */
    @GetMapping("/user/{userId}/by-active")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAllByUserIdAndActive(
            @PathVariable Long userId,
            @RequestParam boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAllByUserIdAndActive(userId, active, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    /**
     * Kullanıcı adı, soyadı, e-postası veya yetki ismi üzerinde anahtar kelime araması yapar.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> search(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.search(active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    /**
     * Bir kullanıcı yetki override kaydını pasif hale getirir (soft-delete).
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        overrideService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("User permission override deactivated successfully"));
    }

    /**
     * Pasif bir kullanıcı yetki override kaydını tekrar aktif eder.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        overrideService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("User permission override activated successfully"));
    }
}

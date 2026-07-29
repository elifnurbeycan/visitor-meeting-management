package com.yasarbilgi.visitormeetingmanagment.userpermission.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request.UserPermissionOverrideRequestDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.service.UserPermissionOverrideService;
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
 * UserPermissionOverride kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/user-permission-overrides
 *
 * companyId artık istemciden alınmıyor, her zaman JWT'den (AuthenticatedUser)
 * çözülüyor. Yazma işlemleri (create/update/deactivate/activate), servis
 * katmanındaki enforceFullAdminPrivilege() ile ek olarak korunuyor —
 * sadece owner/SuperAdmin/tam yetkili admin bir override oluşturabilir,
 * çünkü bu mekanizma rol sisteminden bağımsız, doğrudan izin verme yolu.
 */
@RestController
@RequestMapping("/api/v1/user-permission-overrides")
@RequiredArgsConstructor
public class UserPermissionOverrideController {

    private final UserPermissionOverrideService overrideService;

    @PreAuthorize("hasAuthority('USER_GRANT_PERMISSION_OVERRIDE')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserPermissionOverrideResponseDto>> create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody UserPermissionOverrideRequestDto dto
    ) {
        UserPermissionOverrideResponseDto created = overrideService.create(currentUser.companyId(), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User permission override created successfully", created));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserPermissionOverrideResponseDto>> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UserPermissionOverrideRequestDto dto
    ) {
        UserPermissionOverrideResponseDto updated = overrideService.update(currentUser.companyId(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("User permission override updated successfully", updated));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserPermissionOverrideResponseDto>> getById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        UserPermissionOverrideResponseDto override = overrideService.getById(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success(override));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAll(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAll(currentUser.companyId(), pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAllByActive(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAllByActive(currentUser.companyId(), active, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAllByUserId(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAllByUserId(currentUser.companyId(), userId, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @GetMapping("/user/{userId}/by-active")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> getAllByUserIdAndActive(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long userId,
            @RequestParam boolean active,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.getAllByUserIdAndActive(currentUser.companyId(), userId, active, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    @PreAuthorize("hasAnyAuthority('USER_GRANT_PERMISSION_OVERRIDE', 'USER_REVOKE_PERMISSION_OVERRIDE')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserPermissionOverrideResponseDto>>> search(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<UserPermissionOverrideResponseDto> overrides =
                PageResponse.of(overrideService.search(currentUser.companyId(), active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    @PreAuthorize("hasAuthority('USER_REVOKE_PERMISSION_OVERRIDE')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        overrideService.deactivate(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success("User permission override deactivated successfully"));
    }

    @PreAuthorize("hasAuthority('USER_GRANT_PERMISSION_OVERRIDE')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id
    ) {
        overrideService.activate(currentUser.companyId(), id);
        return ResponseEntity.ok(ApiResponse.success("User permission override activated successfully"));
    }
}
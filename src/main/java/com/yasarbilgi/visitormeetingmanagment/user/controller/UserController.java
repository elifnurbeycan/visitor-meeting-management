package com.yasarbilgi.visitormeetingmanagment.user.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.user.dto.request.UserRequestDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserDirectoryResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/companies/{companyId}/users

 * companyId path'teki değer, TenantPathGuardInterceptor tarafından otomatik
 * olarak isteği yapan kullanıcının kendi companyId'siyle karşılaştırılır.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody UserRequestDto dto
    ) {
        UserResponseDto created = userService.create(companyId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto dto
    ) {
        UserResponseDto updated = userService.update(companyId, userId, dto);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        UserResponseDto user = userService.getById(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserResponseDto>> getByEmail(
            @PathVariable Long companyId,
            @RequestParam String email
    ) {
        UserResponseDto user = userService.getByEmail(companyId, email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<UserResponseDto>> getOwner(
            @PathVariable Long companyId
    ) {
        UserResponseDto owner = userService.getOwner(companyId);
        return ResponseEntity.ok(ApiResponse.success(owner));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users = PageResponse.of(userService.getAll(companyId, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAllByActive(
            @PathVariable Long companyId,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users =
                PageResponse.of(userService.getAllByActive(companyId, active, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/by-job-title/{jobTitleId}")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAllByJobTitle(
            @PathVariable Long companyId,
            @PathVariable Long jobTitleId,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users =
                PageResponse.of(userService.getAllByJobTitle(companyId, jobTitleId, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAllByRole(
            @PathVariable Long companyId,
            @PathVariable Long roleId,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users =
                PageResponse.of(userService.getAllByRole(companyId, roleId, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> search(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users =
                PageResponse.of(userService.search(companyId, active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        userService.deactivate(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }

    @PreAuthorize("hasAuthority('USER_ACTIVATE')")
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        userService.activate(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
    }

    @PreAuthorize("hasAuthority('USER_ASSIGN_ROLE')")
    @PatchMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> assignRole(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        UserResponseDto updated = userService.assignRole(companyId, userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", updated));
    }

    @PreAuthorize("hasAuthority('USER_REVOKE_ROLE')")
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> revokeRole(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        UserResponseDto updated = userService.revokeRole(companyId, userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role revoked successfully", updated));
    }

    @PreAuthorize("hasAuthority('USER_ASSIGN_JOB_TITLE')")
    @PatchMapping("/{userId}/job-title/{jobTitleId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> changeJobTitle(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @PathVariable Long jobTitleId
    ) {
        UserResponseDto updated = userService.changeJobTitle(companyId, userId, jobTitleId);
        return ResponseEntity.ok(ApiResponse.success("Job title changed successfully", updated));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{userId}/promote-to-owner")
    public ResponseEntity<ApiResponse<UserResponseDto>> promoteToOwner(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        UserResponseDto updated = userService.promoteToOwner(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User promoted to owner successfully", updated));
    }

    @PreAuthorize("#currentOwnerId == authentication.principal.userId or hasRole('SUPER_ADMIN')")
    @PatchMapping("/transfer-ownership")
    public ResponseEntity<ApiResponse<UserResponseDto>> transferOwnership(
            @PathVariable Long companyId,
            @RequestParam Long currentOwnerId,
            @RequestParam Long newOwnerId
    ) {
        UserResponseDto updated = userService.transferOwnership(companyId, currentOwnerId, newOwnerId);
        return ResponseEntity.ok(ApiResponse.success("Ownership transferred successfully", updated));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countUsers(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(userService.countUsers(companyId)));
    }

    @PreAuthorize("hasAuthority('USER_VIEW_ALL')")
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> countActiveUsers(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(userService.countActiveUsers(companyId)));
    }

    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/directory")
    public ResponseEntity<ApiResponse<PageResponse<UserDirectoryResponseDto>>> searchDirectory(
            @PathVariable Long companyId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserDirectoryResponseDto> users =
                PageResponse.of(userService.searchDirectory(companyId, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PatchMapping("/{userId}/force-password-reset")
    public ResponseEntity<ApiResponse<Void>> forcePasswordReset(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        userService.forcePasswordReset(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User will be required to change password on next login"));
    }
}
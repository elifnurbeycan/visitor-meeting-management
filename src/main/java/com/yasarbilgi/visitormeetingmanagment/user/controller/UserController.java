package com.yasarbilgi.visitormeetingmanagment.user.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.user.dto.request.UserRequestDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/companies/{companyId}/users
 *
 * companyId path'te taşınıyor çünkü Security henüz kurulmadı; JWT/SecurityContext
 * geldiğinde bu, current user'ın company'sinden otomatik çözülecek ve path'ten
 * kaldırılabilir.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto dto
    ) {
        UserResponseDto updated = userService.update(companyId, userId, dto);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        UserResponseDto user = userService.getById(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserResponseDto>> getByEmail(
            @PathVariable Long companyId,
            @RequestParam String email
    ) {
        UserResponseDto user = userService.getByEmail(companyId, email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<UserResponseDto>> getOwner(
            @PathVariable Long companyId
    ) {
        UserResponseDto owner = userService.getOwner(companyId);
        return ResponseEntity.ok(ApiResponse.success(owner));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users = PageResponse.of(userService.getAll(companyId, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

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

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        userService.deactivate(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        userService.activate(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
    }

    @PatchMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> assignRole(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        UserResponseDto updated = userService.assignRole(companyId, userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", updated));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> revokeRole(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        UserResponseDto updated = userService.revokeRole(companyId, userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role revoked successfully", updated));
    }

    @PatchMapping("/{userId}/job-title/{jobTitleId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> changeJobTitle(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @PathVariable Long jobTitleId
    ) {
        UserResponseDto updated = userService.changeJobTitle(companyId, userId, jobTitleId);
        return ResponseEntity.ok(ApiResponse.success("Job title changed successfully", updated));
    }

    @PatchMapping("/{userId}/promote-to-owner")
    public ResponseEntity<ApiResponse<UserResponseDto>> promoteToOwner(
            @PathVariable Long companyId,
            @PathVariable Long userId
    ) {
        UserResponseDto updated = userService.promoteToOwner(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User promoted to owner successfully", updated));
    }

    @PatchMapping("/transfer-ownership")
    public ResponseEntity<ApiResponse<UserResponseDto>> transferOwnership(
            @PathVariable Long companyId,
            @RequestParam Long currentOwnerId,
            @RequestParam Long newOwnerId
    ) {
        UserResponseDto updated = userService.transferOwnership(companyId, currentOwnerId, newOwnerId);
        return ResponseEntity.ok(ApiResponse.success("Ownership transferred successfully", updated));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countUsers(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(userService.countUsers(companyId)));
    }

    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> countActiveUsers(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(userService.countActiveUsers(companyId)));
    }
}
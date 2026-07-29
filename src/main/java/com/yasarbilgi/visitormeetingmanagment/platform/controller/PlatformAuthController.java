package com.yasarbilgi.visitormeetingmanagment.platform.controller;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.ChangePasswordRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.SuperAdminLoginRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.service.AuthService;
import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SuperAdmin authentication endpoint'i. Normal user login'inden
 * tamamen ayrı — companySlug gerektirmez, çünkü SuperAdmin hiçbir
 * şirkete ait değildir.
 * URL şeması: /api/v1/platform/auth/...
 */
@RestController
@RequestMapping("/api/v1/platform/auth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody SuperAdminLoginRequestDto dto
    ) {
        LoginResponseDto response = authService.loginSuperAdmin(dto.email(), dto.password());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<LoginResponseDto>> changePassword(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        LoginResponseDto result = authService.changeSuperAdminPassword(
                currentUser.userId(), dto.currentPassword(), dto.newPassword()
        );
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", result));
    }
}
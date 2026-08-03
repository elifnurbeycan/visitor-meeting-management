package com.yasarbilgi.visitormeetingmanagment.auth.controller;

import com.yasarbilgi.visitormeetingmanagment.audit.dto.response.AuditLogResponseDto;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.ChangePasswordRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.LoginRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.RefreshTokenRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.MeResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.service.AuthService;
import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Normal kullanıcı (User) authentication endpoint'leri.
 * URL şeması: /api/v1/auth/...
 * Bu path'ler SecurityConfig'te permitAll() ile herkese açık bırakılmıştır —
 * TEK İSTİSNA: /api/v1/auth/my-login-history, authentication gerektirir
 * (bkz. SecurityConfig'teki özel kural).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto
    ) {
        LoginResponseDto response = authService.login(dto.companySlug(), dto.email(), dto.password());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponseDto>> getCurrentUser(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        MeResponseDto me = authService.getCurrentUser(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success(me));
    }

    /**
     * Kullanıcının KENDİ login/logout geçmişi. Herhangi bir admin izni
     * gerektirmez — sadece giriş yapmış olmak yeterli (bkz. SecurityConfig).
     */
    @GetMapping("/my-login-history")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> getMyLoginHistory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AuditLogResponseDto> history = PageResponse.of(
                auditLogService.getMyLoginHistory(currentUser.companyId(), currentUser.userId(), pageable)
        );
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto dto
    ) {
        LoginResponseDto response = authService.refresh(dto.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto dto
    ) {
        authService.logout(dto.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<LoginResponseDto>> changePassword(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        LoginResponseDto result = authService.changePassword(
                currentUser.userId(), dto.currentPassword(), dto.newPassword()
        );
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", result));
    }
}
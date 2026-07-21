package com.yasarbilgi.visitormeetingmanagment.auth.controller;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.ChangePasswordRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.LoginRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.RefreshTokenRequestDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.service.AuthService;
import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Normal kullanıcı (User) authentication endpoint'leri.
 * URL şeması: /api/v1/auth/...
 * Bu path'ler SecurityConfig'te permitAll() ile herkese açık bırakılmıştır.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto
    ) {
        LoginResponseDto response = authService.login(dto.companySlug(), dto.email(), dto.password());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
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
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        authService.changePassword(currentUser.userId(), dto.currentPassword(), dto.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
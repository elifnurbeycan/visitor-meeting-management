package com.yasarbilgi.visitormeetingmanagment.auth.service;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.MeResponseDto;

public interface AuthService {

    LoginResponseDto login(String companySlug, String identifier, String password);

    LoginResponseDto loginSuperAdmin(String email, String password);

    LoginResponseDto refresh(String refreshToken);

    MeResponseDto getCurrentUser(Long userId);

    void logout(String refreshToken);

    LoginResponseDto changePassword(Long userId, String currentPassword, String newPassword);

    LoginResponseDto changeSuperAdminPassword(Long superAdminId, String currentPassword, String newPassword);

}
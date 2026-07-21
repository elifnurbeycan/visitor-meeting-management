package com.yasarbilgi.visitormeetingmanagment.auth.service;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;

public interface AuthService {

    LoginResponseDto login(String companySlug, String email, String password);

    LoginResponseDto loginSuperAdmin(String email, String password);

    LoginResponseDto refresh(String refreshToken);

    void logout(String refreshToken);

    void changePassword(Long userId, String currentPassword, String newPassword);

}
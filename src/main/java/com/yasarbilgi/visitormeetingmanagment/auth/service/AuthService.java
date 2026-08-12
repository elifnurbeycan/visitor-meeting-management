package com.yasarbilgi.visitormeetingmanagment.auth.service;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.MeResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.ProfileJobTitleResponseDto;
import java.util.List;
import com.yasarbilgi.visitormeetingmanagment.auth.dto.request.UpdateProfileRequestDto;

public interface AuthService {

    LoginResponseDto login(String companySlug, String identifier, String password);

    LoginResponseDto loginSuperAdmin(String email, String password);

    LoginResponseDto refresh(String refreshToken);

    MeResponseDto getCurrentUser(Long userId);

    MeResponseDto updateCurrentUser(Long userId, UpdateProfileRequestDto dto);

    List<ProfileJobTitleResponseDto> getProfileJobTitles(Long userId);

    void logout(String refreshToken);

    LoginResponseDto changePassword(Long userId, String currentPassword, String newPassword);

    LoginResponseDto changeSuperAdminPassword(Long superAdminId, String currentPassword, String newPassword);

}

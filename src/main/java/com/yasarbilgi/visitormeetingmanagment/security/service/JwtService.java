package com.yasarbilgi.visitormeetingmanagment.security.service;

import java.util.Set;

public interface JwtService {

    String generateAccessToken(Long userId, Long companyId, Set<String> permissions);

    String generateSuperAdminAccessToken(Long superAdminId);

    String generateRefreshToken();

    boolean isTokenValid(String token);

    Long extractUserId(String token);

    Long extractCompanyId(String token);

    Set<String> extractPermissions(String token);

    boolean isSuperAdminToken(String token);

}
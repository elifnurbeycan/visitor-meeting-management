package com.yasarbilgi.visitormeetingmanagment.auth.service.impl;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.service.AuthService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.platform.entity.SuperAdmin;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import com.yasarbilgi.visitormeetingmanagment.platform.repository.SuperAdminRepository;
import com.yasarbilgi.visitormeetingmanagment.security.entity.RefreshToken;
import com.yasarbilgi.visitormeetingmanagment.security.repository.RefreshTokenRepository;
import com.yasarbilgi.visitormeetingmanagment.security.service.JwtService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final SuperAdminRepository superAdminRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PermissionResolutionService permissionResolutionService;
    private final PermissionCacheService permissionCacheService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public LoginResponseDto login(String companySlug, String identifier, String password) {
        log.info("Login attempt for identifier: {} in company: {}", identifier, companySlug);

        Company company = companyRepository.findBySlug(companySlug)
                .orElseThrow(() -> {
                    log.warn("Login failed: company not found for slug: {}", companySlug);
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!company.isActive()) {
            log.warn("Login failed: company is deactivated: {}", companySlug);
            throw new BusinessException(ErrorCode.COMPANY_INACTIVE);
        }

        if (company.getStatus() == CompanyStatus.PENDING_APPROVAL) {
            log.warn("Login failed: company approval pending: {}", companySlug);
            throw new BusinessException(ErrorCode.COMPANY_APPROVAL_PENDING);
        }

        if (company.getStatus() == CompanyStatus.REJECTED) {
            log.warn("Login failed: company rejected: {}", companySlug);
            throw new BusinessException(ErrorCode.COMPANY_REJECTED);
        }

        User user = resolveUserByIdentifier(company.getId(), identifier);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed: invalid password for identifier: {}", identifier);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            log.warn("Login failed: user is inactive: {}", identifier);
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        Set<String> permissions = permissionResolutionService.resolveEffectivePermissions(user.getId());
        permissionCacheService.cachePermissions(user.getId(), permissions);

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getCompany().getId(), permissions
        );
        String refreshToken = issueRefreshToken(user, null);

        log.info("Login successful for user: {}", user.getId());
        return buildLoginResponse(accessToken, refreshToken, user.isMustChangePassword());
    }


    @Override
    @Transactional
    public LoginResponseDto loginSuperAdmin(String email, String password) {
        log.info("SuperAdmin login attempt for email: {}", email);

        SuperAdmin superAdmin = superAdminRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("SuperAdmin login failed: not found for email: {}", email);
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(password, superAdmin.getPasswordHash())) {
            log.warn("SuperAdmin login failed: invalid password for email: {}", email);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!superAdmin.isActive()) {
            log.warn("SuperAdmin login failed: account not approved yet: {}", email);
            throw new BusinessException(ErrorCode.SUPER_ADMIN_NOT_ACTIVE);
        }

        String accessToken = jwtService.generateSuperAdminAccessToken(superAdmin.getId());
        String refreshToken = issueRefreshToken(null, superAdmin);

        log.info("SuperAdmin login successful: {}", superAdmin.getId());
        return buildLoginResponse(accessToken, refreshToken, false);
    }

    @Override
    @Transactional
    public LoginResponseDto refresh(String refreshToken) {
        log.info("Refresh token requested");

        String hash = hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> {
                    log.warn("Refresh failed: token not found");
                    return new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (!storedToken.isValid()) {
            log.warn("Refresh failed: token expired or revoked");
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        storedToken.revoke();

        String newAccessToken;
        String newRefreshToken;
        boolean mustChangePassword = false;

        if (storedToken.getUser() != null) {
            User user = storedToken.getUser();
            Set<String> permissions = permissionResolutionService.resolveEffectivePermissions(user.getId());
            permissionCacheService.cachePermissions(user.getId(), permissions);

            newAccessToken = jwtService.generateAccessToken(
                    user.getId(), user.getCompany().getId(), permissions
            );
            newRefreshToken = issueRefreshToken(user, null);
            mustChangePassword = user.isMustChangePassword();
        } else {
            SuperAdmin superAdmin = storedToken.getSuperAdmin();
            newAccessToken = jwtService.generateSuperAdminAccessToken(superAdmin.getId());
            newRefreshToken = issueRefreshToken(null, superAdmin);
        }

        log.info("Token refreshed successfully");
        return buildLoginResponse(newAccessToken, newRefreshToken, mustChangePassword);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout requested");

        String hash = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(RefreshToken::revoke);
    }

    /**
     * Kullanıcının kendi şifresini değiştirmesi. Mevcut şifre doğrulanır,
     * yeni şifre hashlenir, mustChangePassword bayrağı temizlenir.
     * Şifre değiştiğinde tüm mevcut refresh token'lar iptal edilir —
     * güvenlik amaçlı, diğer cihazlardaki oturumlar sonlandırılır.
     */
    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Password change requested for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("Password change failed: current password mismatch for user: {}", userId);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.changePasswordHash(passwordEncoder.encode(newPassword));
        user.clearMustChangePasswordFlag();

        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());

        log.info("Password changed successfully for user: {}", userId);
    }

    // ----- Private helpers -----

    private String issueRefreshToken(User user, SuperAdmin superAdmin) {
        String rawToken = jwtService.generateRefreshToken();
        String hash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hash)
                .user(user)
                .superAdmin(superAdmin)
                .expiresAt(Instant.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private LoginResponseDto buildLoginResponse(String accessToken, String refreshToken, boolean mustChangePassword) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(accessTokenExpirationMs / 1000)
                .mustChangePassword(mustChangePassword)
                .build();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verilen identifier'ın email mi yoksa username mi olduğunu, @ karakterinin
     * varlığına bakarak belirler (DB'ye çift sorgu atmamak için). Username
     * global unique olduğu için şirket bağımsız aranır; email ise şirket
     * kapsamında (company_id + email) aranır.
     */
    private User resolveUserByIdentifier(Long companyId, String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByCompanyIdAndEmail(companyId, identifier)
                    .orElseThrow(() -> {
                        log.warn("Login failed: user not found for email: {}", identifier);
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                    });
        }

        User user = userRepository.findByUsername(identifier)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for username: {}", identifier);
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!user.getCompany().getId().equals(companyId)) {
            log.warn("Login failed: username {} does not belong to company {}", identifier, companyId);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return user;
    }
}
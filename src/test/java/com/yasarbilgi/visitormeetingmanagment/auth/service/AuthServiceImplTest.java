package com.yasarbilgi.visitormeetingmanagment.auth.service;

import com.yasarbilgi.visitormeetingmanagment.auth.dto.response.LoginResponseDto;
import com.yasarbilgi.visitormeetingmanagment.auth.service.impl.AuthServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SuperAdminRepository superAdminRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @Mock
    private PermissionCacheService permissionCacheService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final Long COMPANY_ID = 7L;
    private static final Long USER_ID = 11L;
    private static final Long SUPER_ADMIN_ID = 99L;

    private static final String COMPANY_SLUG = "test-sirketi";
    private static final String EMAIL = "eylul@example.com";
    private static final String USERNAME = "eylul";
    private static final String PASSWORD = "Password123";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    private Company company;
    private User user;
    private SuperAdmin superAdmin;

    @BeforeEach
    void setUp() {
        company = org.mockito.Mockito.mock(Company.class);
        user = org.mockito.Mockito.mock(User.class);
        superAdmin = org.mockito.Mockito.mock(SuperAdmin.class);

        ReflectionTestUtils.setField(
                authService,
                "accessTokenExpirationMs",
                900_000L
        );

        ReflectionTestUtils.setField(
                authService,
                "refreshTokenExpirationMs",
                604_800_000L
        );
    }

    // ----- login() -----

    @Test
    void login_shouldSucceed_withEmail() {
        mockActiveCompany();

        when(userRepository.findByCompanyIdAndEmail(
                COMPANY_ID,
                EMAIL
        )).thenReturn(Optional.of(user));

        mockActiveUser();

        Set<String> permissions = Set.of(
                "ROOM_VIEW",
                "RESERVATION_CREATE"
        );

        when(permissionResolutionService
                .resolveEffectivePermissions(USER_ID))
                .thenReturn(permissions);

        when(jwtService.generateAccessToken(
                USER_ID,
                COMPANY_ID,
                permissions
        )).thenReturn(ACCESS_TOKEN);

        when(jwtService.generateRefreshToken())
                .thenReturn(REFRESH_TOKEN);

        LoginResponseDto result =
                authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                );

        assertThat(result).isNotNull();
        assertThat(result.accessToken())
                .isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken())
                .isEqualTo(REFRESH_TOKEN);
        assertThat(result.tokenType())
                .isEqualTo("Bearer");
        assertThat(result.expiresIn())
                .isEqualTo(900L);

        verify(permissionCacheService)
                .cachePermissions(USER_ID, permissions);

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    @Test
    void login_shouldSucceed_withUsername() {
        mockActiveCompany();

        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));

        mockActiveUser();

        Set<String> permissions = Set.of("ROOM_VIEW");

        when(permissionResolutionService
                .resolveEffectivePermissions(USER_ID))
                .thenReturn(permissions);

        when(jwtService.generateAccessToken(
                USER_ID,
                COMPANY_ID,
                permissions
        )).thenReturn(ACCESS_TOKEN);

        when(jwtService.generateRefreshToken())
                .thenReturn(REFRESH_TOKEN);

        LoginResponseDto result =
                authService.login(
                        COMPANY_SLUG,
                        USERNAME,
                        PASSWORD
                );

        assertThat(result).isNotNull();
        assertThat(result.accessToken())
                .isEqualTo(ACCESS_TOKEN);

        verify(userRepository)
                .findByUsername(USERNAME);

        verify(userRepository, never())
                .findByCompanyIdAndEmail(
                        any(),
                        anyString()
                );
    }

    @Test
    void login_shouldThrowException_whenCompanyNotFound() {
        when(companyRepository.findBySlug(COMPANY_SLUG))
                .thenReturn(Optional.empty());

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.INVALID_CREDENTIALS
        );
    }

    @Test
    void login_shouldThrowException_whenCompanyInactive() {
        when(companyRepository.findBySlug(COMPANY_SLUG))
                .thenReturn(Optional.of(company));

        when(company.isActive()).thenReturn(false);

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.COMPANY_INACTIVE
        );

        verify(userRepository, never())
                .findByCompanyIdAndEmail(
                        any(),
                        anyString()
                );
    }

    @Test
    void login_shouldThrowException_whenCompanyApprovalPending() {
        when(companyRepository.findBySlug(COMPANY_SLUG))
                .thenReturn(Optional.of(company));

        when(company.isActive()).thenReturn(true);
        when(company.getStatus())
                .thenReturn(CompanyStatus.PENDING_APPROVAL);

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.COMPANY_APPROVAL_PENDING
        );
    }

    @Test
    void login_shouldThrowException_whenCompanyRejected() {
        when(companyRepository.findBySlug(COMPANY_SLUG))
                .thenReturn(Optional.of(company));

        when(company.isActive()).thenReturn(true);
        when(company.getStatus())
                .thenReturn(CompanyStatus.REJECTED);

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.COMPANY_REJECTED
        );
    }

    @Test
    void login_shouldThrowException_whenEmailNotFound() {
        mockActiveCompany();

        when(userRepository.findByCompanyIdAndEmail(
                COMPANY_ID,
                EMAIL
        )).thenReturn(Optional.empty());

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.INVALID_CREDENTIALS
        );
    }

    @Test
    void login_shouldThrowException_whenUsernameBelongsToDifferentCompany() {
        Company differentCompany =
                org.mockito.Mockito.mock(Company.class);

        mockActiveCompany();

        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));

        when(user.getCompany())
                .thenReturn(differentCompany);

        when(differentCompany.getId())
                .thenReturn(100L);

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        USERNAME,
                        PASSWORD
                ),
                ErrorCode.INVALID_CREDENTIALS
        );
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {
        mockActiveCompany();

        when(userRepository.findByCompanyIdAndEmail(
                COMPANY_ID,
                EMAIL
        )).thenReturn(Optional.of(user));

        when(user.getPasswordHash())
                .thenReturn("hashed-password");

        when(passwordEncoder.matches(
                PASSWORD,
                "hashed-password"
        )).thenReturn(false);

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.INVALID_CREDENTIALS
        );

        verify(jwtService, never())
                .generateAccessToken(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void login_shouldThrowException_whenUserInactive() {
        mockActiveCompany();

        when(userRepository.findByCompanyIdAndEmail(
                COMPANY_ID,
                EMAIL
        )).thenReturn(Optional.of(user));

        when(user.getPasswordHash())
                .thenReturn("hashed-password");

        when(passwordEncoder.matches(
                PASSWORD,
                "hashed-password"
        )).thenReturn(true);

        when(user.isActive())
                .thenReturn(false);

        assertBusinessException(
                () -> authService.login(
                        COMPANY_SLUG,
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.USER_INACTIVE
        );
    }

    // ----- loginSuperAdmin() -----

    @Test
    void loginSuperAdmin_shouldSucceed() {
        when(superAdminRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(superAdmin));

        when(superAdmin.getId())
                .thenReturn(SUPER_ADMIN_ID);

        when(superAdmin.getPasswordHash())
                .thenReturn("admin-hash");

        when(passwordEncoder.matches(
                PASSWORD,
                "admin-hash"
        )).thenReturn(true);

        when(superAdmin.isActive())
                .thenReturn(true);

        when(jwtService.generateSuperAdminAccessToken(
                SUPER_ADMIN_ID
        )).thenReturn(ACCESS_TOKEN);

        when(jwtService.generateRefreshToken())
                .thenReturn(REFRESH_TOKEN);

        LoginResponseDto result =
                authService.loginSuperAdmin(
                        EMAIL,
                        PASSWORD
                );

        assertThat(result.accessToken())
                .isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken())
                .isEqualTo(REFRESH_TOKEN);
        assertThat(result.mustChangePassword())
                .isFalse();

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    @Test
    void loginSuperAdmin_shouldThrowException_whenAdminNotFound() {
        when(superAdminRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertBusinessException(
                () -> authService.loginSuperAdmin(
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.INVALID_CREDENTIALS
        );
    }

    @Test
    void loginSuperAdmin_shouldThrowException_whenPasswordIsWrong() {
        when(superAdminRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(superAdmin));

        when(superAdmin.getPasswordHash())
                .thenReturn("admin-hash");

        when(passwordEncoder.matches(
                PASSWORD,
                "admin-hash"
        )).thenReturn(false);

        assertBusinessException(
                () -> authService.loginSuperAdmin(
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.INVALID_CREDENTIALS
        );
    }

    @Test
    void loginSuperAdmin_shouldThrowException_whenAdminInactive() {
        when(superAdminRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(superAdmin));

        when(superAdmin.getPasswordHash())
                .thenReturn("admin-hash");

        when(passwordEncoder.matches(
                PASSWORD,
                "admin-hash"
        )).thenReturn(true);

        when(superAdmin.isActive())
                .thenReturn(false);

        assertBusinessException(
                () -> authService.loginSuperAdmin(
                        EMAIL,
                        PASSWORD
                ),
                ErrorCode.SUPER_ADMIN_NOT_ACTIVE
        );
    }

    // ----- refresh() -----

    @Test
    void refresh_shouldSucceed_forUser() {
        RefreshToken storedToken =
                org.mockito.Mockito.mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenHash(
                anyString()
        )).thenReturn(Optional.of(storedToken));

        when(storedToken.isValid())
                .thenReturn(true);

        when(storedToken.getUser())
                .thenReturn(user);

        mockUserWithoutPasswordCheck();

        Set<String> permissions = Set.of("ROOM_VIEW");

        when(permissionResolutionService
                .resolveEffectivePermissions(USER_ID))
                .thenReturn(permissions);

        when(jwtService.generateAccessToken(
                USER_ID,
                COMPANY_ID,
                permissions
        )).thenReturn("new-access-token");

        when(jwtService.generateRefreshToken())
                .thenReturn("new-refresh-token");

        LoginResponseDto result =
                authService.refresh(REFRESH_TOKEN);

        assertThat(result.accessToken())
                .isEqualTo("new-access-token");

        assertThat(result.refreshToken())
                .isEqualTo("new-refresh-token");

        verify(storedToken).revoke();

        verify(permissionCacheService)
                .cachePermissions(USER_ID, permissions);
    }

    @Test
    void refresh_shouldSucceed_forSuperAdmin() {
        RefreshToken storedToken =
                org.mockito.Mockito.mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenHash(
                anyString()
        )).thenReturn(Optional.of(storedToken));

        when(storedToken.isValid())
                .thenReturn(true);

        when(storedToken.getUser())
                .thenReturn(null);

        when(storedToken.getSuperAdmin())
                .thenReturn(superAdmin);

        when(superAdmin.getId())
                .thenReturn(SUPER_ADMIN_ID);

        when(jwtService.generateSuperAdminAccessToken(
                SUPER_ADMIN_ID
        )).thenReturn("new-admin-token");

        when(jwtService.generateRefreshToken())
                .thenReturn("new-refresh-token");

        LoginResponseDto result =
                authService.refresh(REFRESH_TOKEN);

        assertThat(result.accessToken())
                .isEqualTo("new-admin-token");

        assertThat(result.mustChangePassword())
                .isFalse();

        verify(storedToken).revoke();
    }

    @Test
    void refresh_shouldThrowException_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHash(
                anyString()
        )).thenReturn(Optional.empty());

        assertBusinessException(
                () -> authService.refresh(REFRESH_TOKEN),
                ErrorCode.INVALID_REFRESH_TOKEN
        );
    }

    @Test
    void refresh_shouldThrowException_whenTokenInvalid() {
        RefreshToken storedToken =
                org.mockito.Mockito.mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenHash(
                anyString()
        )).thenReturn(Optional.of(storedToken));

        when(storedToken.isValid())
                .thenReturn(false);

        assertBusinessException(
                () -> authService.refresh(REFRESH_TOKEN),
                ErrorCode.INVALID_REFRESH_TOKEN
        );

        verify(storedToken, never()).revoke();
    }

    // ----- logout() -----

    @Test
    void logout_shouldRevokeToken_whenTokenExists() {
        RefreshToken storedToken =
                org.mockito.Mockito.mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenHash(
                anyString()
        )).thenReturn(Optional.of(storedToken));

        authService.logout(REFRESH_TOKEN);

        verify(storedToken).revoke();
    }

    @Test
    void logout_shouldNotThrowException_whenTokenDoesNotExist() {
        when(refreshTokenRepository.findByTokenHash(
                anyString()
        )).thenReturn(Optional.empty());

        assertThatCode(() ->
                authService.logout(REFRESH_TOKEN)
        ).doesNotThrowAnyException();
    }

    // ----- changePassword() -----

    @Test
    void changePassword_shouldSucceed() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(user.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(COMPANY_ID);

        when(user.getId())
                .thenReturn(USER_ID);

        when(user.getPasswordHash())
                .thenReturn("old-password-hash");

        when(passwordEncoder.matches(
                "old-password",
                "old-password-hash"
        )).thenReturn(true);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-password-hash");

        when(permissionResolutionService.resolveEffectivePermissions(USER_ID))
                .thenReturn(Set.of());

        when(jwtService.generateAccessToken(
                org.mockito.ArgumentMatchers.eq(USER_ID),
                org.mockito.ArgumentMatchers.eq(COMPANY_ID),
                any()
        )).thenReturn(ACCESS_TOKEN);

        when(jwtService.generateRefreshToken())
                .thenReturn(REFRESH_TOKEN);

        authService.changePassword(
                USER_ID,
                "old-password",
                "new-password"
        );

        verify(user)
                .changePasswordHash(
                        "new-password-hash"
                );

        verify(user)
                .clearMustChangePasswordFlag();

        verify(refreshTokenRepository)
                .revokeAllByUserId(
                        org.mockito.ArgumentMatchers.eq(USER_ID),
                        any(Instant.class)
                );
    }

    @Test
    void changePassword_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertBusinessException(
                () -> authService.changePassword(
                        USER_ID,
                        "old-password",
                        "new-password"
                ),
                ErrorCode.USER_NOT_FOUND
        );
    }

    @Test
    void changePassword_shouldThrowException_whenCurrentPasswordWrong() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(user.getPasswordHash())
                .thenReturn("old-password-hash");

        when(passwordEncoder.matches(
                "wrong-password",
                "old-password-hash"
        )).thenReturn(false);

        assertBusinessException(
                () -> authService.changePassword(
                        USER_ID,
                        "wrong-password",
                        "new-password"
                ),
                ErrorCode.INVALID_CREDENTIALS
        );

        verify(user, never())
                .changePasswordHash(anyString());

        verify(refreshTokenRepository, never())
                .revokeAllByUserId(
                        any(),
                        any()
                );
    }

    // ----- Helpers -----

    private void mockActiveCompany() {
        when(companyRepository.findBySlug(COMPANY_SLUG))
                .thenReturn(Optional.of(company));

        when(company.getId())
                .thenReturn(COMPANY_ID);

        when(company.isActive())
                .thenReturn(true);

        when(company.getStatus())
                .thenReturn(CompanyStatus.ACTIVE);
    }

    private void mockActiveUser() {
        mockUserWithoutPasswordCheck();

        when(user.getPasswordHash())
                .thenReturn("hashed-password");

        when(passwordEncoder.matches(
                PASSWORD,
                "hashed-password"
        )).thenReturn(true);

        when(user.isActive())
                .thenReturn(true);
    }

    private void mockUserWithoutPasswordCheck() {
        when(user.getId())
                .thenReturn(USER_ID);

        when(user.getCompany())
                .thenReturn(company);

        when(company.getId())
                .thenReturn(COMPANY_ID);

        when(user.isMustChangePassword())
                .thenReturn(false);
    }

    private void assertBusinessException(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        expectedErrorCode
                );
    }
}
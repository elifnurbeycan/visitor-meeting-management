package com.yasarbilgi.visitormeetingmanagment.userpermission.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.repository.PermissionRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request.UserPermissionOverrideRequestDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.OverrideType;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import com.yasarbilgi.visitormeetingmanagment.userpermission.mapper.UserPermissionOverrideMapper;
import com.yasarbilgi.visitormeetingmanagment.userpermission.repository.UserPermissionOverrideRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.service.impl.UserPermissionOverrideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserPermissionOverrideServiceImpl için JUnit 5 ve Mockito birim testleri.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class UserPermissionOverrideServiceImplTest {

    @Mock
    private UserPermissionOverrideRepository overrideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserPermissionOverrideMapper overrideMapper;

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @Mock
    private PermissionCacheService permissionCacheService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserPermissionOverrideServiceImpl overrideService;

    private static final Long COMPANY_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long PERMISSION_ID = 5L;
    private static final Long OVERRIDE_ID = 100L;

    private Company company;
    private User user;
    private Permission permission;
    private UserPermissionOverride override;
    private UserPermissionOverrideResponseDto responseDto;
    private AuthenticatedUser superAdmin;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Atlas Teknoloji")
                .slug("atlas-teknoloji")
                .build();

        user = User.builder()
                .id(USER_ID)
                .firstName("Elif")
                .company(company)
                .build();

        permission = mock(Permission.class);

        override = UserPermissionOverride.builder()
                .id(OVERRIDE_ID)
                .user(user)
                .permission(permission)
                .type(OverrideType.GRANT)
                .company(company)
                .active(true)
                .build();

        // Altan'ın güncellediği PermissionSummary nested record yapısını kullanıyoruz
        responseDto = UserPermissionOverrideResponseDto.builder()
                .id(OVERRIDE_ID)
                .userId(USER_ID)
                .permission(UserPermissionOverrideResponseDto.PermissionSummary.builder()
                        .id(PERMISSION_ID)
                        .code("MEETING_ROOM_CREATE")
                        .name("Toplantı Odası Oluşturma")
                        .build())
                .type(OverrideType.GRANT)
                .active(true)
                .build();

        superAdmin = AuthenticatedUser.builder()
                .userId(99L)
                .companyId(COMPANY_ID)
                .permissions(java.util.Set.of())
                .superAdmin(true)
                .build();
    }

    // ----- create() Testleri -----

    @Test
    void create_shouldSucceed_whenDataIsValid() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        mockSuperAdmin();

        when(overrideRepository.findByCompanyIdAndUserIdAndPermissionId(COMPANY_ID, USER_ID, PERMISSION_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.of(permission));
        when(overrideRepository.save(any(UserPermissionOverride.class))).thenReturn(override);
        when(overrideMapper.toResponseDto(any(UserPermissionOverride.class))).thenReturn(responseDto);

        UserPermissionOverrideResponseDto result = overrideService.create(COMPANY_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(OverrideType.GRANT);
        verify(overrideRepository, times(1)).save(any(UserPermissionOverride.class));
        verify(permissionCacheService, times(1)).invalidate(USER_ID);
    }

    @Test
    void create_shouldThrowException_whenOverrideAlreadyExists() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        mockSuperAdmin();

        when(overrideRepository.findByCompanyIdAndUserIdAndPermissionId(COMPANY_ID, USER_ID, PERMISSION_ID))
                .thenReturn(Optional.of(override));

        assertThatThrownBy(() -> overrideService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PERMISSION_OVERRIDE);

        verify(overrideRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenUserNotFound() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        mockSuperAdmin();

        when(overrideRepository.findByCompanyIdAndUserIdAndPermissionId(COMPANY_ID, USER_ID, PERMISSION_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void create_shouldThrowException_whenPermissionNotFound() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        mockSuperAdmin();

        when(overrideRepository.findByCompanyIdAndUserIdAndPermissionId(COMPANY_ID, USER_ID, PERMISSION_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permissionRepository.findById(PERMISSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    void create_shouldThrowException_whenActorLacksFullAdminPrivilege() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        AuthenticatedUser regularUser = AuthenticatedUser.builder()
                .userId(USER_ID)
                .companyId(COMPANY_ID)
                .permissions(java.util.Set.of())
                .superAdmin(false)
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permissionResolutionService.hasAllPermissions(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> overrideService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROLE_PERMISSION_MANAGEMENT_FORBIDDEN);

        verify(overrideRepository, never()).save(any());
    }

    // ----- update() Testleri -----

    @Test
    void update_shouldSucceed_whenTypeUpdated() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.REVOKE) // Grant olan tipi Revoke yapıyoruz
                .build();

        mockSuperAdmin();
        when(permission.getId()).thenReturn(PERMISSION_ID);

        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.of(override));
        when(overrideMapper.toResponseDto(any(UserPermissionOverride.class))).thenReturn(responseDto);

        UserPermissionOverrideResponseDto result = overrideService.update(COMPANY_ID, OVERRIDE_ID, dto);

        assertThat(result).isNotNull();
        assertThat(override.getType()).isEqualTo(OverrideType.REVOKE);
        verify(permissionCacheService, times(1)).invalidate(USER_ID);
    }

    @Test
    void update_shouldThrowException_whenAttemptToChangeUserOrPermission() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(999L) // Farklı kullanıcı değiştirilmeye çalışılıyor
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        mockSuperAdmin();

        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.of(override));

        assertThatThrownBy(() -> overrideService.update(COMPANY_ID, OVERRIDE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    @Test
    void update_shouldThrowException_whenOverrideNotFound() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(OverrideType.GRANT)
                .build();

        mockSuperAdmin();

        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.update(COMPANY_ID, OVERRIDE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_OVERRIDE_NOT_FOUND);
    }

    @Test
    void update_shouldThrowException_whenTypeIsNull() {
        UserPermissionOverrideRequestDto dto = UserPermissionOverrideRequestDto.builder()
                .userId(USER_ID)
                .permissionId(PERMISSION_ID)
                .type(null) // Tipi null yapıyoruz
                .build();

        mockSuperAdmin();
        when(permission.getId()).thenReturn(PERMISSION_ID);

        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.of(override));

        assertThatThrownBy(() -> overrideService.update(COMPANY_ID, OVERRIDE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    // ----- getById() Testleri -----

    @Test
    void getById_shouldReturnOverride_whenFound() {
        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.of(override));
        when(overrideMapper.toResponseDto(override)).thenReturn(responseDto);

        UserPermissionOverrideResponseDto result = overrideService.getById(COMPANY_ID, OVERRIDE_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.getById(COMPANY_ID, OVERRIDE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_OVERRIDE_NOT_FOUND);
    }

    // ----- Sayfalama Sorgu Testleri -----

    @Test
    void getAll_shouldReturnPagedOverrides() {
        Pageable pageable = Pageable.unpaged();
        Page<UserPermissionOverride> page = new PageImpl<>(List.of(override));

        when(overrideRepository.findAllByCompanyId(COMPANY_ID, pageable)).thenReturn(page);
        when(overrideMapper.toResponseDto(override)).thenReturn(responseDto);

        Page<UserPermissionOverrideResponseDto> result = overrideService.getAll(COMPANY_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByActive_shouldReturnFilteredOverrides() {
        Pageable pageable = Pageable.unpaged();
        Page<UserPermissionOverride> page = new PageImpl<>(List.of(override));

        when(overrideRepository.findAllByCompanyIdAndActive(COMPANY_ID, true, pageable)).thenReturn(page);
        when(overrideMapper.toResponseDto(override)).thenReturn(responseDto);

        Page<UserPermissionOverrideResponseDto> result = overrideService.getAllByActive(COMPANY_ID, true, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByUserId_shouldReturnUserOverrides() {
        Pageable pageable = Pageable.unpaged();
        Page<UserPermissionOverride> page = new PageImpl<>(List.of(override));

        when(overrideRepository.findAllByCompanyIdAndUserId(COMPANY_ID, USER_ID, pageable)).thenReturn(page);
        when(overrideMapper.toResponseDto(override)).thenReturn(responseDto);

        Page<UserPermissionOverrideResponseDto> result = overrideService.getAllByUserId(COMPANY_ID, USER_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByUserIdAndActive_shouldReturnFilteredUserOverrides() {
        Pageable pageable = Pageable.unpaged();
        Page<UserPermissionOverride> page = new PageImpl<>(List.of(override));

        when(overrideRepository.findAllByCompanyIdAndUserIdAndActive(COMPANY_ID, USER_ID, true, pageable)).thenReturn(page);
        when(overrideMapper.toResponseDto(override)).thenReturn(responseDto);

        Page<UserPermissionOverrideResponseDto> result =
                overrideService.getAllByUserIdAndActive(COMPANY_ID, USER_ID, true, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_shouldReturnMatchingOverrides() {
        Pageable pageable = Pageable.unpaged();
        Page<UserPermissionOverride> page = new PageImpl<>(List.of(override));

        when(overrideRepository.searchByKeyword(COMPANY_ID, true, "Elif", pageable)).thenReturn(page);
        when(overrideMapper.toResponseDto(override)).thenReturn(responseDto);

        Page<UserPermissionOverrideResponseDto> result = overrideService.search(COMPANY_ID, true, "Elif", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ----- activate() / deactivate() Testleri -----

    @Test
    void deactivate_shouldDeactivateOverride() {
        mockSuperAdmin();
        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.of(override));

        overrideService.deactivate(COMPANY_ID, OVERRIDE_ID);

        assertThat(override.isActive()).isFalse();
        verify(permissionCacheService, times(1)).invalidate(USER_ID);
    }

    @Test
    void deactivate_shouldThrowException_whenNotFound() {
        mockSuperAdmin();
        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.deactivate(COMPANY_ID, OVERRIDE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_OVERRIDE_NOT_FOUND);
    }

    @Test
    void activate_shouldActivateOverride() {
        override.deactivate();
        mockSuperAdmin();
        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.of(override));

        overrideService.activate(COMPANY_ID, OVERRIDE_ID);

        assertThat(override.isActive()).isTrue();
        verify(permissionCacheService, times(1)).invalidate(USER_ID);
    }

    @Test
    void activate_shouldThrowException_whenNotFound() {
        mockSuperAdmin();
        when(overrideRepository.findByIdAndCompanyId(OVERRIDE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> overrideService.activate(COMPANY_ID, OVERRIDE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_OVERRIDE_NOT_FOUND);
    }

    // ----- Helper -----

    private void mockSuperAdmin() {
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(superAdmin));
    }
}

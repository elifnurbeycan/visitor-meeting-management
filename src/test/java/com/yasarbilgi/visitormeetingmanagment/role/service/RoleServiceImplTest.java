package com.yasarbilgi.visitormeetingmanagment.role.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.repository.PermissionRepository;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.response.RoleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.mapper.RoleMapper;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.role.service.impl.RoleServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private PermissionCacheService permissionCacheService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RoleServiceImpl roleService;

    private static final Long COMPANY_ID = 7L;
    private static final Long ROLE_ID = 1L;
    private static final Long PERMISSION_ID = 10L;

    private Company company;
    private Role role;
    private Permission permission;
    private RoleResponseDto responseDto;
    private AuthenticatedUser superAdmin;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .name("Test Şirketi")
                .slug("test-sirketi")
                .build();

        role = Role.builder()
                .company(company)
                .name("Yönetici")
                .description("Yönetici rolü")
                .build();

        permission = mock(Permission.class);

        responseDto = RoleResponseDto.builder()
                .id(ROLE_ID)
                .name("Yönetici")
                .description("Yönetici rolü")
                .permissions(Set.of())
                .build();

        superAdmin = AuthenticatedUser.builder()
                .userId(99L)
                .companyId(COMPANY_ID)
                .permissions(Set.of())
                .superAdmin(true)
                .build();
    }

    // ----- create() -----

    @Test
    void create_shouldSucceed_whenRoleNameIsNotTaken() {
        CreateRoleRequestDto dto = CreateRoleRequestDto.builder()
                .name("Yönetici")
                .description("Yönetici rolü")
                .permissionIds(Set.of())
                .build();

        mockSuperAdmin();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(roleRepository.existsByCompanyIdAndNameIgnoreCase(
                COMPANY_ID,
                dto.name()
        )).thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        RoleResponseDto result =
                roleService.create(COMPANY_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Yönetici");

        verify(roleRepository, times(1))
                .save(any(Role.class));
    }

    @Test
    void create_shouldThrowException_whenRoleNameAlreadyExists() {
        CreateRoleRequestDto dto = CreateRoleRequestDto.builder()
                .name("Yönetici")
                .description("Yönetici rolü")
                .permissionIds(Set.of())
                .build();

        mockSuperAdmin();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(roleRepository.existsByCompanyIdAndNameIgnoreCase(
                COMPANY_ID,
                dto.name()
        )).thenReturn(true);

        assertThatThrownBy(() ->
                roleService.create(COMPANY_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_ALREADY_EXISTS
                );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void create_shouldThrowException_whenCompanyNotFound() {
        CreateRoleRequestDto dto = CreateRoleRequestDto.builder()
                .name("Yönetici")
                .permissionIds(Set.of())
                .build();

        mockSuperAdmin();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roleService.create(COMPANY_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void create_shouldThrowException_whenPermissionNotFound() {
        CreateRoleRequestDto dto = CreateRoleRequestDto.builder()
                .name("Yönetici")
                .permissionIds(Set.of(PERMISSION_ID))
                .build();

        mockSuperAdmin();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(roleRepository.existsByCompanyIdAndNameIgnoreCase(
                COMPANY_ID,
                dto.name()
        )).thenReturn(false);

        when(permissionRepository.findAllById(dto.permissionIds()))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                roleService.create(COMPANY_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenRoleExists() {
        UpdateRoleRequestDto dto = UpdateRoleRequestDto.builder()
                .name("Yönetici")
                .description("Güncel açıklama")
                .permissionIds(Set.of())
                .build();

        mockSuperAdmin();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(userRepository.findUserIdsByRoleId(any()))
                .thenReturn(List.of());

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        RoleResponseDto result =
                roleService.update(COMPANY_ID, ROLE_ID, dto);

        assertThat(result).isNotNull();

        verify(
                roleRepository,
                never()
        ).existsByCompanyIdAndNameIgnoreCaseAndIdNot(
                any(),
                any(),
                any()
        );
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyExists() {
        UpdateRoleRequestDto dto = UpdateRoleRequestDto.builder()
                .name("Başka Yönetici")
                .description("Açıklama")
                .permissionIds(Set.of())
                .build();

        mockSuperAdmin();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(roleRepository
                .existsByCompanyIdAndNameIgnoreCaseAndIdNot(
                        COMPANY_ID,
                        dto.name(),
                        ROLE_ID
                )).thenReturn(true);

        assertThatThrownBy(() ->
                roleService.update(COMPANY_ID, ROLE_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_ALREADY_EXISTS
                );
    }

    @Test
    void update_shouldThrowException_whenRoleNotFound() {
        UpdateRoleRequestDto dto = UpdateRoleRequestDto.builder()
                .name("Yönetici")
                .permissionIds(Set.of())
                .build();

        mockSuperAdmin();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roleService.update(COMPANY_ID, ROLE_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_NOT_FOUND
                );
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnRole_whenRoleExists() {
        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        RoleResponseDto result =
                roleService.getById(COMPANY_ID, ROLE_ID);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Yönetici");
    }

    @Test
    void getById_shouldThrowException_whenRoleNotFound() {
        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roleService.getById(COMPANY_ID, ROLE_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_NOT_FOUND
                );
    }

    // ----- getAll() / getAllByActive() / search() -----

    @Test
    void getAll_shouldReturnPagedRoles() {
        Pageable pageable = Pageable.unpaged();
        Page<Role> page = new PageImpl<>(List.of(role));

        when(roleRepository.findAllByCompanyId(
                COMPANY_ID,
                pageable
        )).thenReturn(page);

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        Page<RoleResponseDto> result =
                roleService.getAll(COMPANY_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name())
                .isEqualTo("Yönetici");
    }

    @Test
    void getAllByActive_shouldReturnFilteredRoles() {
        Pageable pageable = Pageable.unpaged();
        Page<Role> page = new PageImpl<>(List.of(role));

        when(roleRepository.findAllByCompanyIdAndActive(
                COMPANY_ID,
                true,
                pageable
        )).thenReturn(page);

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        Page<RoleResponseDto> result =
                roleService.getAllByActive(
                        COMPANY_ID,
                        true,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_shouldReturnMatchingRoles() {
        Pageable pageable = Pageable.unpaged();
        Page<Role> page = new PageImpl<>(List.of(role));

        when(roleRepository.searchByKeyword(
                COMPANY_ID,
                true,
                "Yönetici",
                pageable
        )).thenReturn(page);

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        Page<RoleResponseDto> result =
                roleService.search(
                        COMPANY_ID,
                        true,
                        " Yönetici ",
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    // ----- assignPermission() / revokePermission() -----

    @Test
    void assignPermission_shouldSucceed_whenRoleAndPermissionExist() {
        mockSuperAdmin();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        when(userRepository.findUserIdsByRoleId(ROLE_ID))
                .thenReturn(List.of());

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        RoleResponseDto result =
                roleService.assignPermission(
                        COMPANY_ID,
                        ROLE_ID,
                        PERMISSION_ID
                );

        assertThat(result).isNotNull();
        assertThat(role.hasPermission(permission)).isTrue();
    }

    @Test
    void assignPermission_shouldThrowException_whenPermissionNotFound() {
        mockSuperAdmin();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roleService.assignPermission(
                        COMPANY_ID,
                        ROLE_ID,
                        PERMISSION_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );
    }

    @Test
    void revokePermission_shouldSucceed_whenPermissionAssigned() {
        role.assignPermission(permission);
        mockSuperAdmin();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        when(userRepository.findUserIdsByRoleId(ROLE_ID))
                .thenReturn(List.of());

        when(roleMapper.toResponseDto(role))
                .thenReturn(responseDto);

        RoleResponseDto result =
                roleService.revokePermission(
                        COMPANY_ID,
                        ROLE_ID,
                        PERMISSION_ID
                );

        assertThat(result).isNotNull();
        assertThat(role.hasPermission(permission)).isFalse();
    }

    // ----- deactivate() / activate() -----

    @Test
    void deactivate_shouldSucceed_whenRoleExists() {
        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(userRepository.findUserIdsByRoleId(ROLE_ID))
                .thenReturn(List.of());

        roleService.deactivate(COMPANY_ID, ROLE_ID);

        assertThat(role.isActive()).isFalse();
    }

    @Test
    void activate_shouldSucceed_whenRoleExists() {
        role.deactivate();

        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.of(role));

        when(userRepository.findUserIdsByRoleId(ROLE_ID))
                .thenReturn(List.of());

        roleService.activate(COMPANY_ID, ROLE_ID);

        assertThat(role.isActive()).isTrue();
    }

    @Test
    void deactivate_shouldThrowException_whenRoleNotFound() {
        when(roleRepository.findByIdAndCompanyId(
                ROLE_ID,
                COMPANY_ID
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roleService.deactivate(COMPANY_ID, ROLE_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_NOT_FOUND
                );
    }

    // ----- Helper -----

    private void mockSuperAdmin() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.of(superAdmin));
    }
}
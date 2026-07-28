package com.yasarbilgi.visitormeetingmanagment.user.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.department.entity.Department;
import com.yasarbilgi.visitormeetingmanagment.department.repository.DepartmentRepository;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.job.repository.JobTitleRepository;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.dto.request.UserRequestDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.mapper.UserMapper;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import com.yasarbilgi.visitormeetingmanagment.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long COMPANY_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long SECOND_USER_ID = 20L;
    private static final Long ROLE_ID = 30L;
    private static final Long JOB_TITLE_ID = 40L;
    private static final Long DEPARTMENT_ID = 50L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JobTitleRepository jobTitleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private PermissionCacheService permissionCacheService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    private Company company;
    private User user;
    private Role role;
    private JobTitle jobTitle;
    private Department department;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .contactEmail("company@test.com")
                .active(true)
                .build();

        role = Role.builder()
                .id(ROLE_ID)
                .company(company)
                .name("Employee")
                .description("Employee role")
                .active(true)
                .build();

        jobTitle = JobTitle.builder()
                .id(JOB_TITLE_ID)
                .company(company)
                .name("Software Developer")
                .active(true)
                .build();

        department = Department.builder()
                .id(DEPARTMENT_ID)
                .company(company)
                .name("IT")
                .active(true)
                .build();

        user = User.builder()
                .id(USER_ID)
                .company(company)
                .firstName("Emir")
                .lastName("Doğruer")
                .email("emir@test.com")
                .username("emird")
                .passwordHash("encodedPassword")
                .jobTitle(jobTitle)
                .department(department)
                .roles(new HashSet<>(Set.of(role)))
                .owner(false)
                .active(true)
                .build();

        responseDto = UserResponseDto.builder()
                .id(USER_ID)
                .firstName("Emir")
                .lastName("Doğruer")
                .fullName("Emir Doğruer")
                .email("emir@test.com")
                .username("emird")
                .owner(false)
                .active(true)
                .mustChangePassword(true)
                .roles(new HashSet<>())
                .build();
    }

    // ----- create() -----

    @Test
    void create_shouldSucceed_whenRequestIsValid() {
        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(jobTitleRepository.findById(JOB_TITLE_ID))
                .thenReturn(Optional.of(jobTitle));

        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(department));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(responseDto);

        UserResponseDto result = userService.create(COMPANY_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.email()).isEqualTo("emir@test.com");

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getCompany()).isEqualTo(company);
        assertThat(savedUser.getFirstName()).isEqualTo("Emir");
        assertThat(savedUser.getLastName()).isEqualTo("Doğruer");
        assertThat(savedUser.getEmail()).isEqualTo("emir@test.com");
        assertThat(savedUser.getUsername()).isEqualTo("emird");
        assertThat(savedUser.getPasswordHash())
                .isEqualTo("encodedPassword");
        assertThat(savedUser.getJobTitle()).isEqualTo(jobTitle);
        assertThat(savedUser.getDepartment()).isEqualTo(department);
        assertThat(savedUser.getRoles()).contains(role);

        verify(passwordEncoder).encode("12345678");
    }

    @Test
    void create_shouldSucceed_whenOptionalRelationsAreNull() {
        UserRequestDto dto = UserRequestDto.builder()
                .firstName("Emir")
                .lastName("Doğruer")
                .email("emir@test.com")
                .username("emird")
                .password("12345678")
                .jobTitleId(null)
                .departmentId(null)
                .roleIds(Set.of())
                .build();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toResponseDto(any(User.class)))
                .thenReturn(responseDto);

        UserResponseDto result = userService.create(COMPANY_ID, dto);

        assertThat(result).isNotNull();

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getJobTitle()).isNull();
        assertThat(captor.getValue().getDepartment()).isNull();
        assertThat(captor.getValue().getRoles()).isEmpty();

        verify(jobTitleRepository, never()).findById(anyLong());
        verify(departmentRepository, never()).findById(anyLong());
        verify(roleRepository, never()).findById(anyLong());
    }

    @Test
    void create_shouldThrowException_whenEmailAlreadyExists() {
        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(true);

        assertThatThrownBy(() -> userService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_ALREADY_EXISTS
                );

        verify(companyRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenUsernameAlreadyExists() {
        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_USERNAME_ALREADY_EXISTS
                );

        verify(companyRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenCompanyNotFound() {
        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenJobTitleBelongsToAnotherCompany() {
        Company otherCompany = createOtherCompany();

        JobTitle foreignJobTitle = JobTitle.builder()
                .id(JOB_TITLE_ID)
                .company(otherCompany)
                .name("Foreign title")
                .active(true)
                .build();

        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(jobTitleRepository.findById(JOB_TITLE_ID))
                .thenReturn(Optional.of(foreignJobTitle));

        assertThatThrownBy(() -> userService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.JOB_TITLE_NOT_FOUND
                );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenDepartmentBelongsToAnotherCompany() {
        Company otherCompany = createOtherCompany();

        Department foreignDepartment = Department.builder()
                .id(DEPARTMENT_ID)
                .company(otherCompany)
                .name("Foreign department")
                .active(true)
                .build();

        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(jobTitleRepository.findById(JOB_TITLE_ID))
                .thenReturn(Optional.of(jobTitle));

        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(foreignDepartment));

        assertThatThrownBy(() -> userService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.DEPARTMENT_NOT_FOUND
                );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenRoleBelongsToAnotherCompany() {
        Company otherCompany = createOtherCompany();

        Role foreignRole = Role.builder()
                .id(ROLE_ID)
                .company(otherCompany)
                .name("Foreign role")
                .active(true)
                .build();

        UserRequestDto dto = createRequestDto();

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        when(jobTitleRepository.findById(JOB_TITLE_ID))
                .thenReturn(Optional.of(jobTitle));

        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(department));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(foreignRole));

        assertThatThrownBy(() -> userService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_NOT_FOUND
                );

        verify(userRepository, never()).save(any(User.class));
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenRequestIsValid() {
        UserRequestDto dto = UserRequestDto.builder()
                .firstName("Emir Can")
                .lastName("Doğruer")
                .email("new@test.com")
                .username("newusername")
                .password("87654321")
                .jobTitleId(JOB_TITLE_ID)
                .departmentId(DEPARTMENT_ID)
                .roleIds(Set.of(ROLE_ID))
                .build();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByCompanyIdAndEmail(
                COMPANY_ID,
                dto.email()
        )).thenReturn(false);

        when(userRepository.existsByUsername(dto.username()))
                .thenReturn(false);

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("newEncodedPassword");

        when(jobTitleRepository.findById(JOB_TITLE_ID))
                .thenReturn(Optional.of(jobTitle));

        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(department));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.update(COMPANY_ID, USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Emir Can");
        assertThat(user.getLastName()).isEqualTo("Doğruer");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getUsername()).isEqualTo("newusername");
        assertThat(user.getPasswordHash())
                .isEqualTo("newEncodedPassword");
        assertThat(user.getJobTitle()).isEqualTo(jobTitle);
        assertThat(user.getDepartment()).isEqualTo(department);

        verify(passwordEncoder).encode("87654321");
    }

    @Test
    void update_shouldNotCheckUniqueness_whenEmailAndUsernameUnchanged() {
        UserRequestDto dto = UserRequestDto.builder()
                .firstName("Updated")
                .lastName("User")
                .email(user.getEmail())
                .username(user.getUsername())
                .password("12345678")
                .jobTitleId(null)
                .departmentId(null)
                .roleIds(Set.of())
                .build();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("updatedPassword");

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.update(COMPANY_ID, USER_ID, dto);

        assertThat(result).isNotNull();

        verify(userRepository, never())
                .existsByCompanyIdAndEmail(anyLong(), anyString());

        verify(userRepository, never())
                .existsByUsername(anyString());

        verify(jobTitleRepository, never()).findById(anyLong());
        verify(departmentRepository, never()).findById(anyLong());
    }

    @Test
    void update_shouldThrowException_whenUserNotFound() {
        UserRequestDto dto = createRequestDto();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.update(COMPANY_ID, USER_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_FOUND
                );

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void update_shouldThrowException_whenUserBelongsToAnotherCompany() {
        Company otherCompany = createOtherCompany();

        User foreignUser = createUser(
                USER_ID,
                otherCompany,
                false,
                true
        );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(foreignUser));

        assertThatThrownBy(
                () -> userService.update(
                        COMPANY_ID,
                        USER_ID,
                        createRequestDto()
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_FOUND
                );
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnUser_whenFound() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.getById(COMPANY_ID, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.fullName()).isEqualTo("Emir Doğruer");
    }

    @Test
    void getById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.getById(COMPANY_ID, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_FOUND
                );
    }

    // ----- getByEmail() -----

    @Test
    void getByEmail_shouldReturnUser_whenFound() {
        when(userRepository.findByCompanyIdAndEmail(
                COMPANY_ID,
                "emir@test.com"
        )).thenReturn(Optional.of(user));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.getByEmail(COMPANY_ID, "emir@test.com");

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("emir@test.com");
    }

    @Test
    void getByEmail_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByCompanyIdAndEmail(
                COMPANY_ID,
                "missing@test.com"
        )).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.getByEmail(
                        COMPANY_ID,
                        "missing@test.com"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_FOUND
                );
    }

    // ----- getOwner() -----

    @Test
    void getOwner_shouldReturnOwner_whenFound() {
        User owner = createUser(USER_ID, company, true, true);

        UserResponseDto ownerResponse = UserResponseDto.builder()
                .id(USER_ID)
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .email("user10@test.com")
                .username("user10")
                .owner(true)
                .active(true)
                .roles(Set.of())
                .build();

        when(userRepository.findByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(Optional.of(owner));

        when(userMapper.toResponseDto(owner))
                .thenReturn(ownerResponse);

        UserResponseDto result = userService.getOwner(COMPANY_ID);

        assertThat(result.owner()).isTrue();
    }

    @Test
    void getOwner_shouldThrowException_whenOwnerNotFound() {
        when(userRepository.findByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOwner(COMPANY_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_FOUND
                );
    }

    // ----- Page methods -----

    @Test
    void getAll_shouldReturnPagedUsers() {
        Pageable pageable = Pageable.unpaged();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByCompanyId(COMPANY_ID, pageable))
                .thenReturn(page);

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result =
                userService.getAll(COMPANY_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id())
                .isEqualTo(USER_ID);
    }

    @Test
    void getAllByActive_shouldReturnActiveUsers() {
        Pageable pageable = Pageable.unpaged();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByCompanyIdAndActive(
                COMPANY_ID,
                true,
                pageable
        )).thenReturn(page);

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result =
                userService.getAllByActive(
                        COMPANY_ID,
                        true,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);

        verify(userRepository).findAllByCompanyIdAndActive(
                COMPANY_ID,
                true,
                pageable
        );
    }

    @Test
    void getAllByJobTitle_shouldReturnMatchingUsers() {
        Pageable pageable = Pageable.unpaged();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByCompanyIdAndJobTitleId(
                COMPANY_ID,
                JOB_TITLE_ID,
                pageable
        )).thenReturn(page);

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result =
                userService.getAllByJobTitle(
                        COMPANY_ID,
                        JOB_TITLE_ID,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByDepartment_shouldReturnMatchingUsers() {
        Pageable pageable = Pageable.unpaged();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByCompanyIdAndDepartmentId(
                COMPANY_ID,
                DEPARTMENT_ID,
                pageable
        )).thenReturn(page);

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result =
                userService.getAllByDepartment(
                        COMPANY_ID,
                        DEPARTMENT_ID,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByRole_shouldReturnMatchingUsers() {
        Pageable pageable = Pageable.unpaged();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAllByCompanyIdAndRoleId(
                COMPANY_ID,
                ROLE_ID,
                pageable
        )).thenReturn(page);

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result =
                userService.getAllByRole(
                        COMPANY_ID,
                        ROLE_ID,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_shouldReturnMatchingUsers() {
        Pageable pageable = Pageable.unpaged();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.searchByKeyword(
                COMPANY_ID,
                true,
                "Emir",
                pageable
        )).thenReturn(page);

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result =
                userService.search(
                        COMPANY_ID,
                        true,
                        "Emir",
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).fullName())
                .isEqualTo("Emir Doğruer");
    }

    // ----- deactivate() / activate() -----

    @Test
    void deactivate_shouldDeactivateUserAndInvalidateCache() {
        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        userService.deactivate(COMPANY_ID, USER_ID);

        assertThat(user.isActive()).isFalse();

        verify(permissionCacheService).invalidate(USER_ID);
    }

    @Test
    void deactivate_shouldThrowException_whenUserIsOwner() {
        User owner = createUser(USER_ID, company, true, true);

        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(owner));

        assertThatThrownBy(
                () -> userService.deactivate(COMPANY_ID, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_OWNER_CANNOT_BE_DEACTIVATED
                );

        verify(permissionCacheService, never()).invalidate(USER_ID);
    }

    @Test
    void activate_shouldActivateUserAndInvalidateCache() {
        user.deactivate();

        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        userService.activate(COMPANY_ID, USER_ID);

        assertThat(user.isActive()).isTrue();

        verify(permissionCacheService).invalidate(USER_ID);
    }

    @Test
    void deactivate_shouldThrowException_whenCurrentUserIsMissing() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.deactivate(COMPANY_ID, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.FORBIDDEN
                );

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void deactivate_shouldThrowException_whenAdminModifiesAnotherAdmin() {
        Long actorId = 99L;

        User actor = createUser(actorId, company, false, true);
        User target = createUser(USER_ID, company, false, true);

        mockCurrentUser(actorId);

        when(userRepository.findById(actorId))
                .thenReturn(Optional.of(actor));

        when(permissionResolutionService.hasAllPermissions(actorId))
                .thenReturn(true);

        when(permissionResolutionService.hasAllPermissions(USER_ID))
                .thenReturn(true);

        assertThatThrownBy(
                () -> userService.deactivate(COMPANY_ID, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ADMIN_CANNOT_MODIFY_ANOTHER_ADMIN
                );

        verify(userRepository, never()).findById(USER_ID);
        assertThat(target.isActive()).isTrue();
    }

    // ----- assignRole() -----

    @Test
    void assignRole_shouldAssignRoleAndWriteAuditLog() {
        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(role));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.assignRole(COMPANY_ID, USER_ID, ROLE_ID);

        assertThat(result).isNotNull();
        assertThat(user.getRoles()).contains(role);

        verify(permissionCacheService).invalidate(USER_ID);

        verify(auditLogService).log(
                eq(COMPANY_ID),
                eq(USER_ID),
                eq("ROLE_ASSIGNED"),
                eq("USER"),
                eq(USER_ID),
                anyString()
        );
    }

    @Test
    void assignRole_shouldThrowException_whenRoleNotFound() {
        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.assignRole(
                        COMPANY_ID,
                        USER_ID,
                        ROLE_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ROLE_NOT_FOUND
                );

        verify(permissionCacheService, never()).invalidate(USER_ID);
    }

    // ----- revokeRole() -----

    @Test
    void revokeRole_shouldRevokeRoleAndWriteAuditLog() {
        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(role));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.revokeRole(COMPANY_ID, USER_ID, ROLE_ID);

        assertThat(result).isNotNull();
        assertThat(user.getRoles()).doesNotContain(role);

        verify(permissionCacheService).invalidate(USER_ID);

        verify(auditLogService).log(
                eq(COMPANY_ID),
                eq(USER_ID),
                eq("ROLE_REVOKED"),
                eq("USER"),
                eq(USER_ID),
                anyString()
        );
    }

    @Test
    void revokeRole_shouldThrowException_whenUserIsOwner() {
        User owner = User.builder()
                .id(USER_ID)
                .company(company)
                .firstName("Test")
                .lastName("User")
                .email("owner@test.com")
                .username("owner")
                .passwordHash("encodedPassword")
                .owner(true)
                .active(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();

        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(owner));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(role));

        assertThatThrownBy(
                () -> userService.revokeRole(
                        COMPANY_ID,
                        USER_ID,
                        ROLE_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_OWNER_ROLE_MODIFICATION_FORBIDDEN
                );

        verify(permissionCacheService, never()).invalidate(USER_ID);
    }

    // ----- changeJobTitle() -----

    @Test
    void changeJobTitle_shouldChangeJobTitle() {
        mockCurrentUser(USER_ID);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(jobTitleRepository.findById(JOB_TITLE_ID))
                .thenReturn(Optional.of(jobTitle));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.changeJobTitle(
                        COMPANY_ID,
                        USER_ID,
                        JOB_TITLE_ID
                );

        assertThat(result).isNotNull();
        assertThat(user.getJobTitle()).isEqualTo(jobTitle);
    }

    // ----- promoteToOwner() -----

    @Test
    void promoteToOwner_shouldPromoteActiveUser() {
        when(userRepository.existsByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(false);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.promoteToOwner(COMPANY_ID, USER_ID);

        assertThat(result).isNotNull();
        assertThat(user.isOwner()).isTrue();

        verify(permissionCacheService).invalidate(USER_ID);
    }

    @Test
    void promoteToOwner_shouldThrowException_whenCompanyAlreadyHasOwner() {
        when(userRepository.existsByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(true);

        assertThatThrownBy(
                () -> userService.promoteToOwner(COMPANY_ID, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_ALREADY_HAS_OWNER
                );

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void promoteToOwner_shouldThrowException_whenUserIsInactive() {
        User inactiveUser =
                createUser(USER_ID, company, false, false);

        when(userRepository.existsByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(false);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(
                () -> userService.promoteToOwner(COMPANY_ID, USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_INACTIVE
                );

        verify(permissionCacheService, never()).invalidate(USER_ID);
    }

    // ----- transferOwnership() -----

    @Test
    void transferOwnership_shouldTransferOwnership() {
        User currentOwner =
                createUser(USER_ID, company, true, true);

        User newOwner =
                createUser(SECOND_USER_ID, company, false, true);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(currentOwner));

        when(userRepository.findById(SECOND_USER_ID))
                .thenReturn(Optional.of(newOwner));

        when(userMapper.toResponseDto(newOwner))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.transferOwnership(
                        COMPANY_ID,
                        USER_ID,
                        SECOND_USER_ID
                );

        assertThat(result).isNotNull();
        assertThat(currentOwner.isOwner()).isFalse();
        assertThat(newOwner.isOwner()).isTrue();

        verify(permissionCacheService).invalidate(USER_ID);
        verify(permissionCacheService).invalidate(SECOND_USER_ID);
    }

    @Test
    void transferOwnership_shouldThrowException_whenCurrentUserIsNotOwner() {
        User currentUser =
                createUser(USER_ID, company, false, true);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(currentUser));

        assertThatThrownBy(
                () -> userService.transferOwnership(
                        COMPANY_ID,
                        USER_ID,
                        SECOND_USER_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_OWNER
                );

        verify(userRepository, never()).findById(SECOND_USER_ID);
    }

    @Test
    void transferOwnership_shouldThrowException_whenNewOwnerIsInactive() {
        User currentOwner =
                createUser(USER_ID, company, true, true);

        User inactiveNewOwner =
                createUser(SECOND_USER_ID, company, false, false);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(currentOwner));

        when(userRepository.findById(SECOND_USER_ID))
                .thenReturn(Optional.of(inactiveNewOwner));

        assertThatThrownBy(
                () -> userService.transferOwnership(
                        COMPANY_ID,
                        USER_ID,
                        SECOND_USER_ID
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_INACTIVE
                );

        assertThat(currentOwner.isOwner()).isTrue();
        assertThat(inactiveNewOwner.isOwner()).isFalse();
    }

    // ----- forceTransferOwnership() -----

    @Test
    void forceTransferOwnership_shouldDemoteExistingOwnerAndPromoteNewOwner() {
        User currentOwner =
                createUser(USER_ID, company, true, true);

        User newOwner =
                createUser(SECOND_USER_ID, company, false, true);

        when(userRepository.findByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(Optional.of(currentOwner));

        when(userRepository.findById(SECOND_USER_ID))
                .thenReturn(Optional.of(newOwner));

        when(userMapper.toResponseDto(newOwner))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.forceTransferOwnership(
                        COMPANY_ID,
                        SECOND_USER_ID
                );

        assertThat(result).isNotNull();
        assertThat(currentOwner.isOwner()).isFalse();
        assertThat(newOwner.isOwner()).isTrue();

        verify(permissionCacheService).invalidate(USER_ID);
        verify(permissionCacheService).invalidate(SECOND_USER_ID);
    }

    @Test
    void forceTransferOwnership_shouldPromoteUser_whenNoOwnerExists() {
        User newOwner =
                createUser(SECOND_USER_ID, company, false, true);

        when(userRepository.findByCompanyIdAndOwnerTrue(COMPANY_ID))
                .thenReturn(Optional.empty());

        when(userRepository.findById(SECOND_USER_ID))
                .thenReturn(Optional.of(newOwner));

        when(userMapper.toResponseDto(newOwner))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.forceTransferOwnership(
                        COMPANY_ID,
                        SECOND_USER_ID
                );

        assertThat(result).isNotNull();
        assertThat(newOwner.isOwner()).isTrue();

        verify(permissionCacheService, times(1))
                .invalidate(SECOND_USER_ID);
    }

    // ----- count methods -----

    @Test
    void countUsers_shouldReturnUserCount() {
        when(userRepository.countByCompanyId(COMPANY_ID))
                .thenReturn(12L);

        long result = userService.countUsers(COMPANY_ID);

        assertThat(result).isEqualTo(12L);
    }

    @Test
    void countActiveUsers_shouldReturnActiveUserCount() {
        when(userRepository.countByCompanyIdAndActive(
                COMPANY_ID,
                true
        )).thenReturn(8L);

        long result = userService.countActiveUsers(COMPANY_ID);

        assertThat(result).isEqualTo(8L);
    }

    // ----- Helpers -----

    private UserRequestDto createRequestDto() {
        return UserRequestDto.builder()
                .firstName("Emir")
                .lastName("Doğruer")
                .email("emir@test.com")
                .username("emird")
                .password("12345678")
                .jobTitleId(JOB_TITLE_ID)
                .departmentId(DEPARTMENT_ID)
                .roleIds(Set.of(ROLE_ID))
                .build();
    }

    private Company createOtherCompany() {
        return Company.builder()
                .id(999L)
                .name("Başka Şirket")
                .slug("baska-sirket")
                .contactEmail("other@test.com")
                .active(true)
                .build();
    }

    private User createUser(
            Long id,
            Company userCompany,
            boolean owner,
            boolean active
    ) {
        return User.builder()
                .id(id)
                .company(userCompany)
                .firstName("Test")
                .lastName("User")
                .email("user" + id + "@test.com")
                .username("user" + id)
                .passwordHash("encodedPassword")
                .owner(owner)
                .active(active)
                .roles(Set.of())
                .build();
    }

    private void mockCurrentUser(Long currentUserId) {
        AuthenticatedUser authenticatedUser =
                mock(AuthenticatedUser.class);

        when(authenticatedUser.userId())
                .thenReturn(currentUserId);

        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.of(authenticatedUser));
    }
}
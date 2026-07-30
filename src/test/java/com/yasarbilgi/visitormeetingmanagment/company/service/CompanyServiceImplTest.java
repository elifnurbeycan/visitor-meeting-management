package com.yasarbilgi.visitormeetingmanagment.company.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.CompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.UpdateCompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.mapper.CompanyMapper;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.company.service.impl.CompanyServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import com.yasarbilgi.visitormeetingmanagment.feature.repository.FeatureRepository;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.entity.RoleTemplate;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleTemplateRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CompanyServiceImpl için kapsamlı birim testleri.
 *
 * Desen: Arrange -> Act -> Assert
 * Başarılı senaryolar ve önemli hata senaryoları ayrı testlerle kapsanır.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleTemplateRepository roleTemplateRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private static final Long COMPANY_ID = 1L;
    private static final Long CURRENT_USER_ID = 10L;

    private Company company;
    private CompanyResponseDto responseDto;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .description("Test şirket açıklaması")
                .taxNumber("1234567890")
                .contactEmail("test@sirket.com")
                .contactPhone("05551234567")
                .address("İzmir")
                .industry("Yazılım")
                .status(CompanyStatus.PENDING_APPROVAL)
                .active(true)
                .build();

        responseDto = CompanyResponseDto.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .description("Test şirket açıklaması")
                .taxNumber("1234567890")
                .contactEmail("test@sirket.com")
                .contactPhone("05551234567")
                .address("İzmir")
                .industry("Yazılım")
                .status(CompanyStatus.PENDING_APPROVAL)
                .active(true)
                .build();
    }

    // ----- create() -----

    @Test
    void create_shouldSucceed_whenCompanyIsValid() {
        CompanyRequestDto dto = createCompanyRequestDto();

        when(companyRepository.existsBySlug(dto.slug())).thenReturn(false);
        when(companyRepository.existsByTaxNumber(dto.taxNumber())).thenReturn(false);
        when(userRepository.existsByUsername(dto.ownerUsername())).thenReturn(false);
        when(companyMapper.toEntity(dto)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(passwordEncoder.encode(dto.ownerPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyMapper.toResponseDto(company)).thenReturn(responseDto);

        CompanyResponseDto result = companyService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Şirketi");
        assertThat(result.slug()).isEqualTo("test-sirketi");

        verify(companyRepository, times(1)).save(company);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("12345678");
    }

    @Test
    void create_shouldThrowException_whenSlugAlreadyExists() {
        CompanyRequestDto dto = createCompanyRequestDto();

        when(companyRepository.existsBySlug(dto.slug())).thenReturn(true);

        assertThatThrownBy(() -> companyService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_ALREADY_EXISTS
                );

        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenTaxNumberAlreadyExists() {
        CompanyRequestDto dto = createCompanyRequestDto();

        when(companyRepository.existsBySlug(dto.slug())).thenReturn(false);
        when(companyRepository.existsByTaxNumber(dto.taxNumber())).thenReturn(true);

        assertThatThrownBy(() -> companyService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_ALREADY_EXISTS
                );

        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenOwnerUsernameAlreadyExists() {
        CompanyRequestDto dto = createCompanyRequestDto();

        when(companyRepository.existsBySlug(dto.slug())).thenReturn(false);
        when(companyRepository.existsByTaxNumber(dto.taxNumber())).thenReturn(false);
        when(userRepository.existsByUsername(dto.ownerUsername())).thenReturn(true);

        assertThatThrownBy(() -> companyService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_USERNAME_ALREADY_EXISTS
                );

        verify(companyRepository, never()).save(any(Company.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_shouldSkipTaxNumberCheck_whenTaxNumberIsNull() {
        CompanyRequestDto dto = CompanyRequestDto.builder()
                .name("Test Şirketi")
                .slug("test-sirketi")
                .description("Test açıklaması")
                .taxNumber(null)
                .contactEmail("test@sirket.com")
                .contactPhone("05551234567")
                .address("İzmir")
                .industry("Yazılım")
                .ownerFirstName("Emir")
                .ownerLastName("Doğruer")
                .ownerEmail("emir@test.com")
                .ownerUsername("emird")
                .ownerPassword("12345678")
                .build();

        when(companyRepository.existsBySlug(dto.slug())).thenReturn(false);
        when(userRepository.existsByUsername(dto.ownerUsername())).thenReturn(false);
        when(companyMapper.toEntity(dto)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(passwordEncoder.encode(dto.ownerPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyMapper.toResponseDto(company)).thenReturn(responseDto);

        CompanyResponseDto result = companyService.create(dto);

        assertThat(result).isNotNull();

        verify(companyRepository, never()).existsByTaxNumber(anyString());
        verify(companyRepository).save(company);
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenSlugAndTaxNumberAreUnchanged() {
        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .name("Güncel Test Şirketi")
                .slug("test-sirketi")
                .description("Yeni açıklama")
                .taxNumber("1234567890")
                .contactEmail("guncel@sirket.com")
                .contactPhone("05559876543")
                .address("Manisa")
                .industry("Teknoloji")
                .build();

        CompanyResponseDto updatedResponse = CompanyResponseDto.builder()
                .id(COMPANY_ID)
                .name("Güncel Test Şirketi")
                .slug("test-sirketi")
                .description("Yeni açıklama")
                .taxNumber("1234567890")
                .contactEmail("guncel@sirket.com")
                .contactPhone("05559876543")
                .address("Manisa")
                .industry("Teknoloji")
                .status(CompanyStatus.PENDING_APPROVAL)
                .active(true)
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(companyMapper.toResponseDto(company))
                .thenReturn(updatedResponse);

        CompanyResponseDto result = companyService.update(COMPANY_ID, dto);

        assertThat(result).isNotNull();
        assertThat(company.getName()).isEqualTo("Güncel Test Şirketi");
        assertThat(company.getDescription()).isEqualTo("Yeni açıklama");
        assertThat(company.getContactEmail()).isEqualTo("guncel@sirket.com");
        assertThat(company.getContactPhone()).isEqualTo("05559876543");
        assertThat(company.getAddress()).isEqualTo("Manisa");
        assertThat(company.getIndustry()).isEqualTo("Teknoloji");

        verify(companyRepository, never()).existsBySlug(anyString());
        verify(companyRepository, never()).existsByTaxNumber(anyString());
    }

    @Test
    void update_shouldValidateUniqueness_whenSlugAndTaxNumberChanged() {
        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .name("Yeni Şirket")
                .slug("yeni-sirket")
                .description("Yeni açıklama")
                .taxNumber("9999999999")
                .contactEmail("yeni@sirket.com")
                .contactPhone("05550000000")
                .address("İstanbul")
                .industry("Finans")
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(companyRepository.existsBySlug("yeni-sirket"))
                .thenReturn(false);
        when(companyRepository.existsByTaxNumber("9999999999"))
                .thenReturn(false);
        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        companyService.update(COMPANY_ID, dto);

        verify(companyRepository, times(1))
                .existsBySlug("yeni-sirket");
        verify(companyRepository, times(1))
                .existsByTaxNumber("9999999999");

        assertThat(company.getSlug()).isEqualTo("yeni-sirket");
        assertThat(company.getTaxNumber()).isEqualTo("9999999999");
    }

    @Test
    void update_shouldThrowException_whenNewSlugAlreadyExists() {
        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .name("Yeni Şirket")
                .slug("kullanilan-slug")
                .contactEmail("test@sirket.com")
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(companyRepository.existsBySlug("kullanilan-slug"))
                .thenReturn(true);

        assertThatThrownBy(() -> companyService.update(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_ALREADY_EXISTS
                );
    }

    @Test
    void update_shouldThrowException_whenNewTaxNumberAlreadyExists() {
        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .name("Test Şirketi")
                .slug("test-sirketi")
                .taxNumber("9999999999")
                .contactEmail("test@sirket.com")
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(companyRepository.existsByTaxNumber("9999999999"))
                .thenReturn(true);

        assertThatThrownBy(() -> companyService.update(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_ALREADY_EXISTS
                );
    }

    @Test
    void update_shouldThrowException_whenCompanyNotFound() {
        UpdateCompanyRequestDto dto = UpdateCompanyRequestDto.builder()
                .name("Test Şirketi")
                .slug("test-sirketi")
                .contactEmail("test@sirket.com")
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.update(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnCompany_whenFound() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        CompanyResponseDto result = companyService.getById(COMPANY_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(COMPANY_ID);
        assertThat(result.name()).isEqualTo("Test Şirketi");
    }

    @Test
    void getById_shouldThrowException_whenCompanyNotFound() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getById(COMPANY_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );
    }

    // ----- getBySlug() -----

    @Test
    void getBySlug_shouldReturnCompany_whenFound() {
        when(companyRepository.findBySlug("test-sirketi"))
                .thenReturn(Optional.of(company));
        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        CompanyResponseDto result =
                companyService.getBySlug("test-sirketi");

        assertThat(result).isNotNull();
        assertThat(result.slug()).isEqualTo("test-sirketi");
    }

    @Test
    void getBySlug_shouldThrowException_whenCompanyNotFound() {
        when(companyRepository.findBySlug("olmayan-sirket"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> companyService.getBySlug("olmayan-sirket")
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );
    }

    // ----- getAll() -----

    @Test
    void getAll_shouldReturnPagedCompanies() {
        Pageable pageable = Pageable.unpaged();
        Page<Company> companyPage =
                new PageImpl<>(List.of(company));

        when(companyRepository.findAll(pageable))
                .thenReturn(companyPage);
        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        Page<CompanyResponseDto> result =
                companyService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name())
                .isEqualTo("Test Şirketi");
    }

    // ----- getAllByActive() -----

    @Test
    void getAllByActive_shouldReturnFilteredCompanies() {
        Pageable pageable = Pageable.unpaged();
        Page<Company> companyPage =
                new PageImpl<>(List.of(company));

        when(companyRepository.findAllByActive(true, pageable))
                .thenReturn(companyPage);
        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        Page<CompanyResponseDto> result =
                companyService.getAllByActive(true, pageable);

        assertThat(result.getContent()).hasSize(1);

        verify(companyRepository)
                .findAllByActive(true, pageable);
    }

    // ----- getAllByStatus() -----

    @Test
    void getAllByStatus_shouldReturnFilteredCompanies() {
        Pageable pageable = Pageable.unpaged();
        Page<Company> companyPage =
                new PageImpl<>(List.of(company));

        when(companyRepository.findAllByStatus(
                CompanyStatus.PENDING_APPROVAL,
                pageable
        )).thenReturn(companyPage);

        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        Page<CompanyResponseDto> result =
                companyService.getAllByStatus(
                        CompanyStatus.PENDING_APPROVAL,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);

        verify(companyRepository).findAllByStatus(
                CompanyStatus.PENDING_APPROVAL,
                pageable
        );
    }

    // ----- search() -----

    @Test
    void search_shouldReturnMatchingCompanies() {
        Pageable pageable = Pageable.unpaged();
        Page<Company> companyPage =
                new PageImpl<>(List.of(company));

        when(companyRepository.searchByKeyword(
                true,
                "Test",
                pageable
        )).thenReturn(companyPage);

        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        Page<CompanyResponseDto> result =
                companyService.search(true, "Test", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).slug())
                .isEqualTo("test-sirketi");
    }

    // ----- getPendingApprovals() -----

    @Test
    void getPendingApprovals_shouldReturnPendingCompanies() {
        Pageable pageable = Pageable.unpaged();
        Page<Company> companyPage =
                new PageImpl<>(List.of(company));

        when(companyRepository
                .findAllByStatusOrderByCreatedAtAsc(
                        CompanyStatus.PENDING_APPROVAL,
                        pageable
                ))
                .thenReturn(companyPage);

        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        Page<CompanyResponseDto> result =
                companyService.getPendingApprovals(pageable);

        assertThat(result.getContent()).hasSize(1);

        verify(companyRepository)
                .findAllByStatusOrderByCreatedAtAsc(
                        CompanyStatus.PENDING_APPROVAL,
                        pageable
                );
    }

    // ----- countPendingApprovals() -----

    @Test
    void countPendingApprovals_shouldReturnPendingCompanyCount() {
        when(companyRepository.countByStatus(
                CompanyStatus.PENDING_APPROVAL
        )).thenReturn(3L);

        long result = companyService.countPendingApprovals();

        assertThat(result).isEqualTo(3L);

        verify(companyRepository).countByStatus(
                CompanyStatus.PENDING_APPROVAL
        );
    }

    // ----- approve() -----

    @Test
    void approve_shouldApproveCompanyAndCreateDefaultRoles() {
        RoleTemplate adminTemplate = RoleTemplate.builder()
                .name("Admin")
                .description("Admin rolü")
                .permissions(Set.of())
                .active(true)
                .build();

        RoleTemplate employeeTemplate = RoleTemplate.builder()
                .name("Employee")
                .description("Çalışan rolü")
                .permissions(Set.of())
                .active(true)
                .build();

        AuthenticatedUser authenticatedUser =
                AuthenticatedUser.builder()
                        .userId(CURRENT_USER_ID)
                        .companyId(null)
                        .permissions(Set.of())
                        .superAdmin(true)
                        .build();

        CompanyResponseDto approvedResponse =
                CompanyResponseDto.builder()
                        .id(COMPANY_ID)
                        .name("Test Şirketi")
                        .slug("test-sirketi")
                        .status(CompanyStatus.ACTIVE)
                        .active(true)
                        .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(roleTemplateRepository.findAllByActiveTrue())
                .thenReturn(List.of(
                        adminTemplate,
                        employeeTemplate
                ));
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(featureRepository.save(any(Feature.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.of(authenticatedUser));
        when(companyMapper.toResponseDto(company))
                .thenReturn(approvedResponse);

        CompanyResponseDto result =
                companyService.approve(COMPANY_ID);

        assertThat(result).isNotNull();
        assertThat(company.getStatus())
                .isEqualTo(CompanyStatus.ACTIVE);
        assertThat(company.isApproved()).isTrue();

        verify(roleRepository, times(2))
                .save(any(Role.class));
        verify(featureRepository, times(7))
                .save(any(Feature.class));

        verify(auditLogService).log(
                eq(COMPANY_ID),
                eq(CURRENT_USER_ID),
                eq("COMPANY_APPROVED"),
                eq("COMPANY"),
                eq(COMPANY_ID),
                anyString()
        );
    }

    @Test
    void approve_shouldUseNullActorId_whenCurrentUserNotFound() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(roleTemplateRepository.findAllByActiveTrue())
                .thenReturn(List.of());
        when(featureRepository.save(any(Feature.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.empty());
        when(companyMapper.toResponseDto(company))
                .thenReturn(responseDto);

        companyService.approve(COMPANY_ID);

        assertThat(company.getStatus())
                .isEqualTo(CompanyStatus.ACTIVE);

        verify(featureRepository, times(7))
                .save(any(Feature.class));

        verify(auditLogService).log(
                eq(COMPANY_ID),
                eq(null),
                eq("COMPANY_APPROVED"),
                eq("COMPANY"),
                eq(COMPANY_ID),
                anyString()
        );
    }

    @Test
    void approve_shouldThrowException_whenCompanyNotPendingApproval() {
        Company activeCompany = Company.builder()
                .id(COMPANY_ID)
                .name("Aktif Şirket")
                .slug("aktif-sirket")
                .contactEmail("aktif@sirket.com")
                .status(CompanyStatus.ACTIVE)
                .active(true)
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(activeCompany));

        assertThatThrownBy(
                () -> companyService.approve(COMPANY_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_PENDING_APPROVAL
                );

        verify(roleRepository, never())
                .save(any(Role.class));
        verify(auditLogService, never()).log(
                anyLong(),
                any(),
                anyString(),
                anyString(),
                anyLong(),
                anyString()
        );
    }

    // ----- reject() -----

    @Test
    void reject_shouldRejectCompanyAndWriteAuditLog() {
        String reason = "Belgeler eksik";

        AuthenticatedUser authenticatedUser =
                AuthenticatedUser.builder()
                        .userId(CURRENT_USER_ID)
                        .companyId(null)
                        .permissions(Set.of())
                        .superAdmin(true)
                        .build();

        CompanyResponseDto rejectedResponse =
                CompanyResponseDto.builder()
                        .id(COMPANY_ID)
                        .name("Test Şirketi")
                        .slug("test-sirketi")
                        .status(CompanyStatus.REJECTED)
                        .rejectionReason(reason)
                        .active(true)
                        .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));
        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.of(authenticatedUser));
        when(companyMapper.toResponseDto(company))
                .thenReturn(rejectedResponse);

        CompanyResponseDto result =
                companyService.reject(COMPANY_ID, reason);

        assertThat(result).isNotNull();
        assertThat(company.getStatus())
                .isEqualTo(CompanyStatus.REJECTED);
        assertThat(company.getRejectionReason())
                .isEqualTo(reason);

        verify(auditLogService).log(
                eq(COMPANY_ID),
                eq(CURRENT_USER_ID),
                eq("COMPANY_REJECTED"),
                eq("COMPANY"),
                eq(COMPANY_ID),
                anyString()
        );
    }

    @Test
    void reject_shouldThrowException_whenCompanyNotPendingApproval() {
        Company rejectedCompany = Company.builder()
                .id(COMPANY_ID)
                .name("Reddedilmiş Şirket")
                .slug("reddedilmis-sirket")
                .contactEmail("red@sirket.com")
                .status(CompanyStatus.REJECTED)
                .active(true)
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(rejectedCompany));

        assertThatThrownBy(
                () -> companyService.reject(
                        COMPANY_ID,
                        "Tekrar reddetme"
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_PENDING_APPROVAL
                );

        verify(auditLogService, never()).log(
                anyLong(),
                any(),
                anyString(),
                anyString(),
                anyLong(),
                anyString()
        );
    }

    // ----- hardDelete() -----

    @Test
    void hardDelete_shouldDeleteCompany_whenCompanyIsPendingApproval() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        companyService.hardDelete(COMPANY_ID);

        verify(companyRepository, times(1))
                .deleteById(COMPANY_ID);
    }

    @Test
    void hardDelete_shouldDeleteCompany_whenCompanyIsRejected() {
        Company rejectedCompany = Company.builder()
                .id(COMPANY_ID)
                .name("Reddedilmiş Şirket")
                .slug("reddedilmis-sirket")
                .contactEmail("red@sirket.com")
                .status(CompanyStatus.REJECTED)
                .active(true)
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(rejectedCompany));

        companyService.hardDelete(COMPANY_ID);

        verify(companyRepository)
                .deleteById(COMPANY_ID);
    }

    @Test
    void hardDelete_shouldThrowException_whenCompanyIsActive() {
        Company activeCompany = Company.builder()
                .id(COMPANY_ID)
                .name("Aktif Şirket")
                .slug("aktif-sirket")
                .contactEmail("aktif@sirket.com")
                .status(CompanyStatus.ACTIVE)
                .active(true)
                .build();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(activeCompany));

        assertThatThrownBy(
                () -> companyService.hardDelete(COMPANY_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_CANNOT_HARD_DELETE_ACTIVE
                );

        verify(companyRepository, never())
                .deleteById(COMPANY_ID);
    }

    @Test
    void hardDelete_shouldThrowException_whenCompanyNotFound() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> companyService.hardDelete(COMPANY_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );

        verify(companyRepository, never())
                .deleteById(COMPANY_ID);
    }

    // ----- deactivate() -----

    @Test
    void deactivate_shouldDeactivateCompany_whenCompanyExists() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        companyService.deactivate(COMPANY_ID);

        assertThat(company.isActive()).isFalse();
        assertThat(company.getDeactivatedAt()).isNotNull();
    }

    @Test
    void deactivate_shouldThrowException_whenCompanyNotFound() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> companyService.deactivate(COMPANY_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );
    }

    // ----- activate() -----

    @Test
    void activate_shouldActivateCompany_whenCompanyExists() {
        company.deactivate();

        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(company));

        companyService.activate(COMPANY_ID);

        assertThat(company.isActive()).isTrue();
        assertThat(company.getDeactivatedAt()).isNull();
    }

    @Test
    void activate_shouldThrowException_whenCompanyNotFound() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> companyService.activate(COMPANY_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.COMPANY_NOT_FOUND
                );
    }

    // ----- Test data helpers -----

    private CompanyRequestDto createCompanyRequestDto() {
        return CompanyRequestDto.builder()
                .name("Test Şirketi")
                .slug("test-sirketi")
                .description("Test şirket açıklaması")
                .taxNumber("1234567890")
                .contactEmail("test@sirket.com")
                .contactPhone("05551234567")
                .address("İzmir")
                .industry("Yazılım")
                .ownerFirstName("Emir")
                .ownerLastName("Doğruer")
                .ownerEmail("emir@test.com")
                .ownerUsername("emird")
                .ownerPassword("12345678")
                .build();
    }
}
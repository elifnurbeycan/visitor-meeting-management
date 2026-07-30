package com.yasarbilgi.visitormeetingmanagment.job.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.job.dto.request.JobTitleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.job.dto.response.JobTitleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.job.mapper.JobTitleMapper;
import com.yasarbilgi.visitormeetingmanagment.job.repository.JobTitleRepository;
import com.yasarbilgi.visitormeetingmanagment.job.service.impl.JobTitleServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * JobTitleServiceImpl için kapsamlı JUnit 5 ve Mockito birim testleri.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class JobTitleServiceImplTest {

    @Mock
    private JobTitleRepository jobTitleRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JobTitleMapper jobTitleMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private JobTitleServiceImpl jobTitleService;

    private static final Long COMPANY_ID = 1L;
    private static final Long JOB_TITLE_ID = 100L;
    private static final Long ROLE_ID = 5L;

    private Company company;
    private Role defaultRole;
    private JobTitle jobTitle;
    private JobTitleResponseDto responseDto;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Atlas Teknoloji")
                .build();

        defaultRole = Role.builder()
                .id(ROLE_ID)
                .name("Yazılım Geliştirici")
                .company(company)
                .build();

        jobTitle = JobTitle.builder()
                .id(JOB_TITLE_ID)
                .name("Yazılım Mühendisi")
                .description("Yazılım süreçlerini yöneten unvan")
                .company(company)
                .defaultRoles(new HashSet<>(Set.of(defaultRole)))
                .active(true)
                .build();

        responseDto = JobTitleResponseDto.builder()
                .id(JOB_TITLE_ID)
                .name("Yazılım Mühendisi")
                .description("Yazılım süreçlerini yöneten unvan")
                .active(true)
                .build();
    }

    // ----- create() Testleri -----

    @Test
    void create_shouldSucceed_whenNameIsUnique() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("Yazılım Mühendisi")
                .description("Açıklama")
                .defaultRoleIds(Set.of(ROLE_ID))
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(jobTitleRepository.existsByNameAndCompanyId(dto.name(), COMPANY_ID)).thenReturn(false);
        when(roleRepository.findAllById(dto.defaultRoleIds())).thenReturn(List.of(defaultRole));
        when(jobTitleRepository.save(any(JobTitle.class))).thenReturn(jobTitle);
        when(jobTitleMapper.toResponseDto(any(JobTitle.class))).thenReturn(responseDto);

        JobTitleResponseDto result = jobTitleService.create(COMPANY_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Yazılım Mühendisi");
        verify(jobTitleRepository, times(1)).save(any(JobTitle.class));
    }

    @Test
    void create_shouldThrowException_whenCompanyNotFound() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder().name("Mühendis").build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTitleService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPANY_NOT_FOUND);

        verify(jobTitleRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyExists() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder().name("Yazılım Mühendisi").build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(jobTitleRepository.existsByNameAndCompanyId(dto.name(), COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> jobTitleService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_ALREADY_EXISTS);

        verify(jobTitleRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenRoleBelongsToDifferentCompany() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("Yazılım Mühendisi")
                .defaultRoleIds(Set.of(ROLE_ID))
                .build();

        Company differentCompany = Company.builder().id(99L).name("Farklı Şirket").build();
        Role alienRole = Role.builder().id(ROLE_ID).company(differentCompany).build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(jobTitleRepository.existsByNameAndCompanyId(dto.name(), COMPANY_ID)).thenReturn(false);
        when(roleRepository.findAllById(dto.defaultRoleIds())).thenReturn(List.of(alienRole));

        assertThatThrownBy(() -> jobTitleService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROLE_NOT_FOUND);
    }

    // ----- update() Testleri -----

    @Test
    void update_shouldSucceed_whenNameIsUnchanged() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("Yazılım Mühendisi")
                .description("Güncel Açıklama")
                .defaultRoleIds(Collections.emptySet())
                .build();

        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));
        when(jobTitleMapper.toResponseDto(any(JobTitle.class))).thenReturn(responseDto);

        JobTitleResponseDto result = jobTitleService.update(COMPANY_ID, JOB_TITLE_ID, dto);

        assertThat(result).isNotNull();
        verify(jobTitleRepository, never()).existsByNameAndCompanyIdAndIdNot(anyString(), anyLong(), anyLong());
    }

    @Test
    void update_shouldSucceed_whenNameIsUnique() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("Yeni Ünvan İsmi")
                .description("Güncel Açıklama")
                .build();

        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));
        when(jobTitleRepository.existsByNameAndCompanyIdAndIdNot("Yeni Ünvan İsmi", COMPANY_ID, JOB_TITLE_ID)).thenReturn(false);
        when(jobTitleMapper.toResponseDto(any(JobTitle.class))).thenReturn(responseDto);

        JobTitleResponseDto result = jobTitleService.update(COMPANY_ID, JOB_TITLE_ID, dto);

        assertThat(result).isNotNull();
        verify(jobTitleRepository, times(1)).existsByNameAndCompanyIdAndIdNot("Yeni Ünvan İsmi", COMPANY_ID, JOB_TITLE_ID);
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyTaken() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("Başka Ünvan")
                .build();

        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));
        when(jobTitleRepository.existsByNameAndCompanyIdAndIdNot("Başka Ünvan", COMPANY_ID, JOB_TITLE_ID)).thenReturn(true);

        assertThatThrownBy(() -> jobTitleService.update(COMPANY_ID, JOB_TITLE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_ALREADY_EXISTS);
    }

    @Test
    void update_shouldThrowException_whenJobTitleNotFound() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder().name("Yeni Mühendis").build();
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTitleService.update(COMPANY_ID, JOB_TITLE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_NOT_FOUND);
    }

    // ----- getById() Testleri -----

    @Test
    void getById_shouldReturnJobTitle_whenFound() {
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));
        when(jobTitleMapper.toResponseDto(jobTitle)).thenReturn(responseDto);

        JobTitleResponseDto result = jobTitleService.getById(COMPANY_ID, JOB_TITLE_ID);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Yazılım Mühendisi");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTitleService.getById(COMPANY_ID, JOB_TITLE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_NOT_FOUND);
    }

    // ----- Paginated Query Testleri -----

    @Test
    void getAll_shouldReturnPagedJobTitles() {
        Pageable pageable = Pageable.unpaged();
        Page<JobTitle> page = new PageImpl<>(List.of(jobTitle));

        when(jobTitleRepository.findAllByCompanyId(COMPANY_ID, pageable)).thenReturn(page);
        when(jobTitleMapper.toResponseDto(jobTitle)).thenReturn(responseDto);

        Page<JobTitleResponseDto> result = jobTitleService.getAll(COMPANY_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllByActive_shouldReturnFilteredJobTitles() {
        Pageable pageable = Pageable.unpaged();
        Page<JobTitle> page = new PageImpl<>(List.of(jobTitle));

        when(jobTitleRepository.findAllByCompanyIdAndActive(COMPANY_ID, true, pageable)).thenReturn(page);
        when(jobTitleMapper.toResponseDto(jobTitle)).thenReturn(responseDto);

        Page<JobTitleResponseDto> result = jobTitleService.getAllByActive(COMPANY_ID, true, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_shouldReturnMatchingJobTitles() {
        Pageable pageable = Pageable.unpaged();
        Page<JobTitle> page = new PageImpl<>(List.of(jobTitle));

        when(jobTitleRepository.searchByKeyword(COMPANY_ID, true, "Yazılım", pageable)).thenReturn(page);
        when(jobTitleMapper.toResponseDto(jobTitle)).thenReturn(responseDto);

        Page<JobTitleResponseDto> result = jobTitleService.search(COMPANY_ID, true, "Yazılım", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ----- activate() / deactivate() Testleri -----

    @Test
    void deactivate_shouldDeactivateJobTitle() {
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));

        jobTitleService.deactivate(COMPANY_ID, JOB_TITLE_ID);

        assertThat(jobTitle.isActive()).isFalse();
    }

    @Test
    void deactivate_shouldThrowException_whenJobTitleNotFound() {
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTitleService.deactivate(COMPANY_ID, JOB_TITLE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_NOT_FOUND);
    }

    @Test
    void activate_shouldActivateJobTitle() {
        jobTitle.deactivate();
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));

        jobTitleService.activate(COMPANY_ID, JOB_TITLE_ID);

        assertThat(jobTitle.isActive()).isTrue();
    }

    @Test
    void activate_shouldThrowException_whenJobTitleNotFound() {
        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTitleService.activate(COMPANY_ID, JOB_TITLE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_NOT_FOUND);
    }

    @Test
    void create_shouldThrowException_whenSomeRolesDoNotExist() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("Yazılım Mühendisi")
                .defaultRoleIds(Set.of(ROLE_ID, 999L)) // 2 rol istiyoruz
                .build();

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(jobTitleRepository.existsByNameAndCompanyId(dto.name(), COMPANY_ID)).thenReturn(false);
        // Ama veritabanı sadece 1 rol dönüyor (rol yok):
        when(roleRepository.findAllById(dto.defaultRoleIds())).thenReturn(List.of(defaultRole));

        assertThatThrownBy(() -> jobTitleService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    void update_shouldThrowException_whenNameIsBlank() {
        JobTitleRequestDto dto = JobTitleRequestDto.builder()
                .name("") // Boş isim gönderiliyor
                .build();

        when(jobTitleRepository.findByIdAndCompanyId(JOB_TITLE_ID, COMPANY_ID)).thenReturn(Optional.of(jobTitle));

        assertThatThrownBy(() -> jobTitleService.update(COMPANY_ID, JOB_TITLE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_TITLE_NAME_REQUIRED);
    }

}

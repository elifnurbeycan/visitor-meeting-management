package com.yasarbilgi.visitormeetingmanagment.department.service;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.department.dto.request.DepartmentRequestDto;
import com.yasarbilgi.visitormeetingmanagment.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.visitormeetingmanagment.department.entity.Department;
import com.yasarbilgi.visitormeetingmanagment.department.mapper.DepartmentMapper;
import com.yasarbilgi.visitormeetingmanagment.department.repository.DepartmentRepository;
import com.yasarbilgi.visitormeetingmanagment.department.service.impl.DepartmentServiceImpl;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DepartmentServiceImpl için kapsamlı birim (unit) testler.

 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 * Her metod için hem "mutlu yol" (başarılı senaryo) hem de olası hata
 * senaryoları ayrı test metodlarıyla kapsanıyor.
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private static final Long COMPANY_ID = 7L;
    private static final Long DEPARTMENT_ID = 1L;

    private Company company;
    private Department department;
    private DepartmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .name("Test Şirketi")
                .slug("test-sirketi")
                .build();

        department = Department.builder()
                .company(company)
                .name("Yazılım Departmanı")
                .description("Test açıklaması")
                .build();

        responseDto = DepartmentResponseDto.builder()
                .id(DEPARTMENT_ID)
                .name("Yazılım Departmanı")
                .description("Test açıklaması")
                .build();
    }

    // ----- create() -----

    @Test
    void create_shouldSucceed_whenNameIsNotTaken() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Yazılım Departmanı")
                .description("Test açıklaması")
                .build();

        when(departmentRepository.existsByCompanyIdAndName(COMPANY_ID, dto.name())).thenReturn(false);
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(departmentMapper.toResponseDto(any(Department.class))).thenReturn(responseDto);

        DepartmentResponseDto result = departmentService.create(COMPANY_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Yazılım Departmanı");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyExists() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Yazılım Departmanı")
                .build();

        when(departmentRepository.existsByCompanyIdAndName(COMPANY_ID, dto.name())).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPARTMENT_ALREADY_EXISTS);

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void create_shouldThrowException_whenCompanyNotFound() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Yazılım Departmanı")
                .build();

        when(departmentRepository.existsByCompanyIdAndName(COMPANY_ID, dto.name())).thenReturn(false);
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPANY_NOT_FOUND);

        verify(departmentRepository, never()).save(any(Department.class));
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenNameUnchanged() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Yazılım Departmanı") // isim aynı kaldı
                .description("Yeni açıklama")
                .build();

        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.of(department));
        when(departmentMapper.toResponseDto(department)).thenReturn(responseDto);

        DepartmentResponseDto result = departmentService.update(COMPANY_ID, DEPARTMENT_ID, dto);

        assertThat(result).isNotNull();
        // İsim değişmediği için existsByCompanyIdAndName HİÇ çağrılmamalı
        verify(departmentRepository, never()).existsByCompanyIdAndName(any(), anyString());
    }

    @Test
    void update_shouldValidateUniqueness_whenNameChanged() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Yeni İsim")
                .description("Yeni açıklama")
                .build();

        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.of(department));
        when(departmentRepository.existsByCompanyIdAndName(COMPANY_ID, "Yeni İsim")).thenReturn(false);
        when(departmentMapper.toResponseDto(department)).thenReturn(responseDto);

        departmentService.update(COMPANY_ID, DEPARTMENT_ID, dto);

        verify(departmentRepository, times(1)).existsByCompanyIdAndName(COMPANY_ID, "Yeni İsim");
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyTaken() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Başka Departman")
                .build();

        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.of(department));
        when(departmentRepository.existsByCompanyIdAndName(COMPANY_ID, "Başka Departman")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.update(COMPANY_ID, DEPARTMENT_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPARTMENT_ALREADY_EXISTS);
    }

    @Test
    void update_shouldThrowException_whenDepartmentNotFound() {
        DepartmentRequestDto dto = DepartmentRequestDto.builder()
                .name("Herhangi Bir İsim")
                .build();

        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.update(COMPANY_ID, DEPARTMENT_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnDepartment_whenFound() {
        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.of(department));
        when(departmentMapper.toResponseDto(department)).thenReturn(responseDto);

        DepartmentResponseDto result = departmentService.getById(COMPANY_ID, DEPARTMENT_ID);

        assertThat(result.name()).isEqualTo("Yazılım Departmanı");
    }

    @Test
    void getById_shouldThrowException_whenDepartmentNotFound() {
        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(COMPANY_ID, DEPARTMENT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    // ----- getAll() / getAllByActive() / search() -----

    @Test
    void getAll_shouldReturnPagedDepartments() {
        Pageable pageable = Pageable.unpaged();
        Page<Department> page = new PageImpl<>(java.util.List.of(department));

        when(departmentRepository.findAllByCompanyId(COMPANY_ID, pageable)).thenReturn(page);
        when(departmentMapper.toResponseDto(department)).thenReturn(responseDto);

        Page<DepartmentResponseDto> result = departmentService.getAll(COMPANY_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Yazılım Departmanı");
    }

    @Test
    void getAllByActive_shouldReturnFilteredDepartments() {
        Pageable pageable = Pageable.unpaged();
        Page<Department> page = new PageImpl<>(java.util.List.of(department));

        when(departmentRepository.findAllByCompanyIdAndActive(COMPANY_ID, true, pageable)).thenReturn(page);
        when(departmentMapper.toResponseDto(department)).thenReturn(responseDto);

        Page<DepartmentResponseDto> result = departmentService.getAllByActive(COMPANY_ID, true, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_shouldReturnMatchingDepartments() {
        Pageable pageable = Pageable.unpaged();
        Page<Department> page = new PageImpl<>(java.util.List.of(department));

        when(departmentRepository.searchByKeyword(COMPANY_ID, true, "Yazılım", pageable)).thenReturn(page);
        when(departmentMapper.toResponseDto(department)).thenReturn(responseDto);

        Page<DepartmentResponseDto> result = departmentService.search(COMPANY_ID, true, "Yazılım", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ----- deactivate() / activate() -----

    @Test
    void deactivate_shouldSucceed_whenDepartmentExists() {
        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.of(department));

        departmentService.deactivate(COMPANY_ID, DEPARTMENT_ID);

        assertThat(department.isActive()).isFalse();
    }

    @Test
    void deactivate_shouldThrowException_whenDepartmentNotFound() {
        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deactivate(COMPANY_ID, DEPARTMENT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    @Test
    void activate_shouldSucceed_whenDepartmentExists() {
        department.deactivate(); // önce pasif duruma getirelim ki tekrar aktif etme anlamlı olsun

        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.of(department));

        departmentService.activate(COMPANY_ID, DEPARTMENT_ID);

        assertThat(department.isActive()).isTrue();
    }

    @Test
    void activate_shouldThrowException_whenDepartmentNotFound() {
        when(departmentRepository.findByCompanyIdAndId(COMPANY_ID, DEPARTMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.activate(COMPANY_ID, DEPARTMENT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEPARTMENT_NOT_FOUND);
    }
}
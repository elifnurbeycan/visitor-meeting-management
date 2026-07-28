package com.yasarbilgi.visitormeetingmanagment.platform.service;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.company.service.CompanyService;
import com.yasarbilgi.visitormeetingmanagment.platform.dto.response.SuperAdminResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.entity.SuperAdmin;
import com.yasarbilgi.visitormeetingmanagment.platform.mapper.SuperAdminMapper;
import com.yasarbilgi.visitormeetingmanagment.platform.repository.SuperAdminRepository;
import com.yasarbilgi.visitormeetingmanagment.platform.service.impl.SuperAdminServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.service.UserService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceImplTest {

    @Mock
    private SuperAdminRepository superAdminRepository;

    @Mock
    private SuperAdminMapper superAdminMapper;

    @Mock
    private CompanyService companyService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SuperAdminServiceImpl superAdminService;

    private static final Long SUPER_ADMIN_ID = 1L;
    private static final Long COMPANY_ID = 10L;
    private static final Long NEW_OWNER_ID = 20L;

    private SuperAdmin superAdmin;
    private SuperAdminResponseDto superAdminResponseDto;
    private CompanyResponseDto companyResponseDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        superAdmin = mock(SuperAdmin.class);
        superAdminResponseDto = mock(SuperAdminResponseDto.class);
        companyResponseDto = mock(CompanyResponseDto.class);
        userResponseDto = mock(UserResponseDto.class);
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnSuperAdmin_whenExists() {
        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.of(superAdmin));

        when(superAdminMapper.toResponseDto(superAdmin))
                .thenReturn(superAdminResponseDto);

        SuperAdminResponseDto result =
                superAdminService.getById(SUPER_ADMIN_ID);

        assertThat(result).isEqualTo(superAdminResponseDto);

        verify(superAdminRepository)
                .findById(SUPER_ADMIN_ID);

        verify(superAdminMapper)
                .toResponseDto(superAdmin);
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                superAdminService.getById(SUPER_ADMIN_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.SUPER_ADMIN_NOT_FOUND
                );

        verify(superAdminMapper, never())
                .toResponseDto(superAdmin);
    }

    // ----- getAll() -----

    @Test
    void getAll_shouldReturnPagedSuperAdmins() {
        Pageable pageable = Pageable.unpaged();

        Page<SuperAdmin> page =
                new PageImpl<>(List.of(superAdmin));

        when(superAdminRepository.findAll(pageable))
                .thenReturn(page);

        when(superAdminMapper.toResponseDto(superAdmin))
                .thenReturn(superAdminResponseDto);

        Page<SuperAdminResponseDto> result =
                superAdminService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0))
                .isEqualTo(superAdminResponseDto);
    }

    // ----- getAllByActive() -----

    @Test
    void getAllByActive_shouldReturnFilteredSuperAdmins() {
        Pageable pageable = Pageable.unpaged();

        Page<SuperAdmin> page =
                new PageImpl<>(List.of(superAdmin));

        when(superAdminRepository.findAllByActive(
                true,
                pageable
        )).thenReturn(page);

        when(superAdminMapper.toResponseDto(superAdmin))
                .thenReturn(superAdminResponseDto);

        Page<SuperAdminResponseDto> result =
                superAdminService.getAllByActive(
                        true,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);

        verify(superAdminRepository)
                .findAllByActive(true, pageable);
    }

    // ----- search() -----

    @Test
    void search_shouldReturnMatchingSuperAdmins() {
        Pageable pageable = Pageable.unpaged();

        Page<SuperAdmin> page =
                new PageImpl<>(List.of(superAdmin));

        when(superAdminRepository.searchByKeyword(
                "Eylül",
                pageable
        )).thenReturn(page);

        when(superAdminMapper.toResponseDto(superAdmin))
                .thenReturn(superAdminResponseDto);

        Page<SuperAdminResponseDto> result =
                superAdminService.search(
                        "Eylül",
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);

        verify(superAdminRepository)
                .searchByKeyword("Eylül", pageable);
    }

    // ----- countActive() -----

    @Test
    void countActive_shouldReturnActiveSuperAdminCount() {
        when(superAdminRepository.countByActive(true))
                .thenReturn(3L);

        long result = superAdminService.countActive();

        assertThat(result).isEqualTo(3L);

        verify(superAdminRepository)
                .countByActive(true);
    }

    // ----- approve() -----

    @Test
    void approve_shouldActivateAndReturnSuperAdmin() {
        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.of(superAdmin));

        when(superAdminMapper.toResponseDto(superAdmin))
                .thenReturn(superAdminResponseDto);

        SuperAdminResponseDto result =
                superAdminService.approve(SUPER_ADMIN_ID);

        assertThat(result).isEqualTo(superAdminResponseDto);

        verify(superAdmin).activate();

        verify(superAdminMapper)
                .toResponseDto(superAdmin);
    }

    @Test
    void approve_shouldThrowException_whenSuperAdminNotFound() {
        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                superAdminService.approve(SUPER_ADMIN_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.SUPER_ADMIN_NOT_FOUND
                );

        verify(superAdmin, never()).activate();
    }

    // ----- deactivate() -----

    @Test
    void deactivate_shouldSucceed_whenMoreThanOneAdminActive() {
        when(superAdminRepository.countByActive(true))
                .thenReturn(2L);

        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.of(superAdmin));

        superAdminService.deactivate(SUPER_ADMIN_ID);

        verify(superAdmin).deactivate();
    }

    @Test
    void deactivate_shouldThrowException_whenLastActiveAdmin() {
        when(superAdminRepository.countByActive(true))
                .thenReturn(1L);

        assertThatThrownBy(() ->
                superAdminService.deactivate(SUPER_ADMIN_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.LAST_SUPER_ADMIN_CANNOT_BE_DEACTIVATED
                );

        verify(superAdminRepository, never())
                .findById(SUPER_ADMIN_ID);

        verify(superAdmin, never())
                .deactivate();
    }

    @Test
    void deactivate_shouldThrowException_whenNoActiveAdminExists() {
        when(superAdminRepository.countByActive(true))
                .thenReturn(0L);

        assertThatThrownBy(() ->
                superAdminService.deactivate(SUPER_ADMIN_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.LAST_SUPER_ADMIN_CANNOT_BE_DEACTIVATED
                );
    }

    @Test
    void deactivate_shouldThrowException_whenSuperAdminNotFound() {
        when(superAdminRepository.countByActive(true))
                .thenReturn(2L);

        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                superAdminService.deactivate(SUPER_ADMIN_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.SUPER_ADMIN_NOT_FOUND
                );

        verify(superAdmin, never())
                .deactivate();
    }

    // ----- activate() -----

    @Test
    void activate_shouldSucceed_whenSuperAdminExists() {
        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.of(superAdmin));

        superAdminService.activate(SUPER_ADMIN_ID);

        verify(superAdmin).activate();
    }

    @Test
    void activate_shouldThrowException_whenSuperAdminNotFound() {
        when(superAdminRepository.findById(SUPER_ADMIN_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                superAdminService.activate(SUPER_ADMIN_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.SUPER_ADMIN_NOT_FOUND
                );

        verify(superAdmin, never()).activate();
    }

    // ----- getAllCompanies() -----

    @Test
    void getAllCompanies_shouldDelegateToCompanyService() {
        Pageable pageable = Pageable.unpaged();

        Page<CompanyResponseDto> page =
                new PageImpl<>(List.of(companyResponseDto));

        when(companyService.getAll(pageable))
                .thenReturn(page);

        Page<CompanyResponseDto> result =
                superAdminService.getAllCompanies(pageable);

        assertThat(result).isEqualTo(page);

        verify(companyService).getAll(pageable);
    }

    // ----- getPendingCompanies() -----

    @Test
    void getPendingCompanies_shouldDelegateToCompanyService() {
        Pageable pageable = Pageable.unpaged();

        Page<CompanyResponseDto> page =
                new PageImpl<>(List.of(companyResponseDto));

        when(companyService.getPendingApprovals(pageable))
                .thenReturn(page);

        Page<CompanyResponseDto> result =
                superAdminService.getPendingCompanies(pageable);

        assertThat(result).isEqualTo(page);

        verify(companyService)
                .getPendingApprovals(pageable);
    }

    // ----- approveCompany() -----

    @Test
    void approveCompany_shouldDelegateToCompanyService() {
        when(companyService.approve(COMPANY_ID))
                .thenReturn(companyResponseDto);

        CompanyResponseDto result =
                superAdminService.approveCompany(COMPANY_ID);

        assertThat(result).isEqualTo(companyResponseDto);

        verify(companyService).approve(COMPANY_ID);
    }

    // ----- rejectCompany() -----

    @Test
    void rejectCompany_shouldDelegateToCompanyService() {
        String reason = "Eksik şirket bilgileri";

        when(companyService.reject(COMPANY_ID, reason))
                .thenReturn(companyResponseDto);

        CompanyResponseDto result =
                superAdminService.rejectCompany(
                        COMPANY_ID,
                        reason
                );

        assertThat(result).isEqualTo(companyResponseDto);

        verify(companyService)
                .reject(COMPANY_ID, reason);
    }

    // ----- deactivateCompany() -----

    @Test
    void deactivateCompany_shouldDelegateToCompanyService() {
        superAdminService.deactivateCompany(COMPANY_ID);

        verify(companyService)
                .deactivate(COMPANY_ID);
    }

    // ----- activateCompany() -----

    @Test
    void activateCompany_shouldDelegateToCompanyService() {
        superAdminService.activateCompany(COMPANY_ID);

        verify(companyService)
                .activate(COMPANY_ID);
    }

    // ----- hardDeleteCompany() -----

    @Test
    void hardDeleteCompany_shouldDelegateToCompanyService() {
        superAdminService.hardDeleteCompany(COMPANY_ID);

        verify(companyService)
                .hardDelete(COMPANY_ID);
    }

    // ----- forceTransferCompanyOwnership() -----

    @Test
    void forceTransferCompanyOwnership_shouldDelegateToUserService() {
        when(userService.forceTransferOwnership(
                COMPANY_ID,
                NEW_OWNER_ID
        )).thenReturn(userResponseDto);

        UserResponseDto result =
                superAdminService.forceTransferCompanyOwnership(
                        COMPANY_ID,
                        NEW_OWNER_ID
                );

        assertThat(result).isEqualTo(userResponseDto);

        verify(userService)
                .forceTransferOwnership(
                        COMPANY_ID,
                        NEW_OWNER_ID
                );
    }
}
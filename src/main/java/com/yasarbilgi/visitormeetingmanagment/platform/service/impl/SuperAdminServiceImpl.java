package com.yasarbilgi.visitormeetingmanagment.platform.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.company.service.CompanyService;
import com.yasarbilgi.visitormeetingmanagment.platform.dto.response.SuperAdminResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.entity.SuperAdmin;
import com.yasarbilgi.visitormeetingmanagment.platform.mapper.SuperAdminMapper;
import com.yasarbilgi.visitormeetingmanagment.platform.repository.SuperAdminRepository;
import com.yasarbilgi.visitormeetingmanagment.platform.service.SuperAdminService;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SuperAdminService'in gerçek implementasyonu.
 * Şirket yönetimi metodları, kendi business logic'ini tekrar yazmak yerine
 * CompanyService/UserService'e delege eder — kod tekrarını önler ve
 * tüm iş kurallarının (validasyon, exception, transactional sınırlar)
 * tek bir yerden (asıl servisten) yönetilmesini sağlar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminServiceImpl implements SuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final SuperAdminMapper superAdminMapper;
    private final CompanyService companyService;
    private final UserService userService;

    // ----- SuperAdmin'in kendi yönetimi -----

    @Override
    public SuperAdminResponseDto getById(Long id) {
        log.debug("Fetching super admin with id: {}", id);
        SuperAdmin superAdmin = findSuperAdminOrThrow(id);
        return superAdminMapper.toResponseDto(superAdmin);
    }

    @Override
    public Page<SuperAdminResponseDto> getAll(Pageable pageable) {
        log.debug("Fetching all super admins, page: {}", pageable);
        return superAdminRepository.findAll(pageable)
                .map(superAdminMapper::toResponseDto);
    }

    @Override
    public Page<SuperAdminResponseDto> getAllByActive(boolean active, Pageable pageable) {
        log.debug("Fetching super admins by active={}", active);
        return superAdminRepository.findAllByActive(active, pageable)
                .map(superAdminMapper::toResponseDto);
    }

    @Override
    public Page<SuperAdminResponseDto> search(String keyword, Pageable pageable) {
        log.debug("Searching super admins with keyword='{}'", keyword);
        return superAdminRepository.searchByKeyword(keyword, pageable)
                .map(superAdminMapper::toResponseDto);
    }

    @Override
    public long countActive() {
        return superAdminRepository.countByActive(true);
    }

    /**
     * Bekleyen bir SuperAdmin başvurusunu onaylar (active=true).
     */
    @Override
    @Transactional
    public SuperAdminResponseDto approve(Long id) {
        log.info("Approving super admin with id: {}", id);
        SuperAdmin superAdmin = findSuperAdminOrThrow(id);
        superAdmin.activate();
        log.info("Super admin approved successfully: {}", id);
        return superAdminMapper.toResponseDto(superAdmin);
    }

    /**
     * Bir SuperAdmin'i pasif hale getirir. Sistemde en az 1 aktif
     * SuperAdmin kalması ZORUNLUDUR — son aktif admin pasife alınamaz,
     * kendini de imha edemez.
     */
    @Override
    @Transactional
    public void deactivate(Long id) {
        log.warn("Deactivation requested for super admin with id: {}", id);

        long activeCount = superAdminRepository.countByActive(true);
        if (activeCount <= 1) {
            log.warn("Deactivation rejected: only {} active super admin(s) remain", activeCount);
            throw new BusinessException(ErrorCode.LAST_SUPER_ADMIN_CANNOT_BE_DEACTIVATED);
        }

        SuperAdmin superAdmin = findSuperAdminOrThrow(id);
        superAdmin.deactivate();

        log.warn("Super admin with id: {} has been deactivated", id);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        log.info("Activating super admin with id: {}", id);
        SuperAdmin superAdmin = findSuperAdminOrThrow(id);
        superAdmin.activate();
    }

    // ----- Şirket yönetimi -----

    @Override
    public Page<CompanyResponseDto> getAllCompanies(Pageable pageable) {
        return companyService.getAll(pageable);
    }

    @Override
    public Page<CompanyResponseDto> getPendingCompanies(Pageable pageable) {
        return companyService.getPendingApprovals(pageable);
    }

    @Override
    @Transactional
    public CompanyResponseDto approveCompany(Long companyId) {
        log.info("SuperAdmin approving company: {}", companyId);
        return companyService.approve(companyId);
    }

    @Override
    @Transactional
    public CompanyResponseDto rejectCompany(Long companyId, String reason) {
        log.info("SuperAdmin rejecting company: {}, reason: {}", companyId, reason);
        return companyService.reject(companyId, reason);
    }

    @Override
    @Transactional
    public void deactivateCompany(Long companyId) {
        log.warn("SuperAdmin deactivating company: {}", companyId);
        companyService.deactivate(companyId);
    }

    @Override
    @Transactional
    public void activateCompany(Long companyId) {
        log.info("SuperAdmin activating company: {}", companyId);
        companyService.activate(companyId);
    }

    @Override
    @Transactional
    public void hardDeleteCompany(Long companyId) {
        log.warn("SuperAdmin HARD DELETING company: {}", companyId);
        companyService.hardDelete(companyId);
    }

    // ----- Kullanıcı/Owner yönetimi -----

    @Override
    @Transactional
    public UserResponseDto forceTransferCompanyOwnership(Long companyId, Long newOwnerId) {
        log.warn("SuperAdmin forcing ownership transfer in company: {} to user: {}",
                companyId, newOwnerId);
        return userService.forceTransferOwnership(companyId, newOwnerId);
    }

    // ----- Private helpers -----

    private SuperAdmin findSuperAdminOrThrow(Long id) {
        return superAdminRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Super admin not found with id: {}", id);
                    return new BusinessException(ErrorCode.SUPER_ADMIN_NOT_FOUND);
                });
    }
}
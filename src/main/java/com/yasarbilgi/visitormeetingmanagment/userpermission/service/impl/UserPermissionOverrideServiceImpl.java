package com.yasarbilgi.visitormeetingmanagment.userpermission.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
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
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import com.yasarbilgi.visitormeetingmanagment.userpermission.mapper.UserPermissionOverrideMapper;
import com.yasarbilgi.visitormeetingmanagment.userpermission.repository.UserPermissionOverrideRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.service.UserPermissionOverrideService;
import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPermissionOverrideServiceImpl implements UserPermissionOverrideService {

    private final UserPermissionOverrideRepository overrideRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionOverrideMapper overrideMapper;
    private final PermissionResolutionService permissionResolutionService;
    private final PermissionCacheService permissionCacheService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserPermissionOverrideResponseDto create(Long companyId, UserPermissionOverrideRequestDto dto) {
        log.info("Creating user permission override for user: {}, permission: {} in company: {}",
                dto.userId(), dto.permissionId(), companyId);

        enforceFullAdminPrivilege(companyId);

        validateOverrideNotExists(companyId, dto.userId(), dto.permissionId());

        User user = findUserAndValidateTenant(dto.userId(), companyId);
        Permission permission = findPermissionOrThrow(dto.permissionId());

        UserPermissionOverride override = UserPermissionOverride.builder()
                .user(user)
                .permission(permission)
                .type(dto.type())
                .company(user.getCompany())
                .active(true)
                .build();

        UserPermissionOverride saved = overrideRepository.save(override);
        permissionCacheService.invalidate(user.getId());

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "PERMISSION_OVERRIDE_CREATED",
                "USER",
                user.getId(),
                dto.type() + " override created for permission '" + permission.getCode() + "'"
        );

        log.info("User permission override created successfully with id: {}", saved.getId());
        return overrideMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserPermissionOverrideResponseDto update(Long companyId, Long id, UserPermissionOverrideRequestDto dto) {
        log.info("Updating user permission override with id: {} in company: {}", id, companyId);

        enforceFullAdminPrivilege(companyId);

        UserPermissionOverride override = findOverrideOrThrow(companyId, id);

        if (!override.getUser().getId().equals(dto.userId()) || !override.getPermission().getId().equals(dto.permissionId())) {
            log.warn("Attempt to change non-updatable fields (user or permission) for override id: {}", id);
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        override.updateType(dto.type());
        permissionCacheService.invalidate(override.getUser().getId());

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "PERMISSION_OVERRIDE_UPDATED",
                "USER",
                override.getUser().getId(),
                "Override type changed to " + dto.type() + " for permission '" + override.getPermission().getCode() + "'"
        );

        log.info("User permission override updated successfully with id: {}", id);
        return overrideMapper.toResponseDto(override);
    }

    @Override
    public UserPermissionOverrideResponseDto getById(Long companyId, Long id) {
        log.debug("Fetching user permission override with id: {} in company: {}", id, companyId);
        UserPermissionOverride override = findOverrideOrThrow(companyId, id);
        return overrideMapper.toResponseDto(override);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAll(Long companyId, Pageable pageable) {
        log.debug("Fetching all user permission overrides for company: {}, page: {}", companyId, pageable);
        return overrideRepository.findAllByCompanyId(companyId, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable) {
        log.debug("Fetching user permission overrides by active={} for company: {}, page: {}", active, companyId, pageable);
        return overrideRepository.findAllByCompanyIdAndActive(companyId, active, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAllByUserId(Long companyId, Long userId, Pageable pageable) {
        log.debug("Fetching user permission overrides for userId={} in company: {}, page: {}", userId, companyId, pageable);
        return overrideRepository.findAllByCompanyIdAndUserId(companyId, userId, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAllByUserIdAndActive(Long companyId, Long userId, boolean active, Pageable pageable) {
        log.debug("Fetching user permission overrides for userId={}, active={} in company: {}, page: {}",
                userId, active, companyId, pageable);
        return overrideRepository.findAllByCompanyIdAndUserIdAndActive(companyId, userId, active, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable) {
        log.debug("Searching user permission overrides with keyword='{}', active={} in company: {}", keyword, active, companyId);
        return overrideRepository.searchByKeyword(companyId, active, keyword, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void deactivate(Long companyId, Long id) {
        log.info("Deactivating user permission override with id: {} in company: {}", id, companyId);
        enforceFullAdminPrivilege(companyId);
        UserPermissionOverride override = findOverrideOrThrow(companyId, id);
        override.deactivate();
        permissionCacheService.invalidate(override.getUser().getId());

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "PERMISSION_OVERRIDE_DEACTIVATED",
                "USER",
                override.getUser().getId(),
                "Override deactivated for permission '" + override.getPermission().getCode() + "'"
        );
    }

    @Override
    @Transactional
    public void activate(Long companyId, Long id) {
        log.info("Activating user permission override with id: {} in company: {}", id, companyId);
        enforceFullAdminPrivilege(companyId);
        UserPermissionOverride override = findOverrideOrThrow(companyId, id);
        override.activate();
        permissionCacheService.invalidate(override.getUser().getId());

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "PERMISSION_OVERRIDE_ACTIVATED",
                "USER",
                override.getUser().getId(),
                "Override activated for permission '" + override.getPermission().getCode() + "'"
        );
    }

    // ----- Private helpers -----

    private UserPermissionOverride findOverrideOrThrow(Long companyId, Long id) {
        return overrideRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> {
                    log.warn("User permission override not found with id: {} in company: {}", id, companyId);
                    return new BusinessException(ErrorCode.PERMISSION_OVERRIDE_NOT_FOUND);
                });
    }

    private User findUserAndValidateTenant(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.getCompany().getId().equals(companyId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Permission findPermissionOrThrow(Long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> {
                    log.warn("Permission not found with id: {}", permissionId);
                    return new BusinessException(ErrorCode.PERMISSION_NOT_FOUND);
                });
    }

    private void validateOverrideNotExists(Long companyId, Long userId, Long permissionId) {
        overrideRepository.findByCompanyIdAndUserIdAndPermissionId(companyId, userId, permissionId)
                .ifPresent(existing -> {
                    log.warn("User permission override already exists for userId: {}, permissionId: {} in company: {} (existing type: {})",
                            userId, permissionId, companyId, existing.getType());
                    throw new BusinessException(ErrorCode.DUPLICATE_PERMISSION_OVERRIDE);
                });
    }

    /**
     * Rol modülündeki enforceFullAdminPrivilege ile birebir aynı mantık:
     * Yetki override'ları (GRANT/REVOKE), rol sisteminden bağımsız, doğrudan
     * bir kullanıcıya herhangi bir izni verebilen/alabilen bir mekanizma
     * olduğu için, sadece SuperAdmin, owner veya tüm izinlere sahip bir
     * admin bu işlemi yapabilir — aksi halde privilege escalation riski oluşur.
     */
    private void enforceFullAdminPrivilege(Long companyId) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        if (currentUser.superAdmin()) {
            return;
        }

        User actor = userRepository.findById(currentUser.userId())
                .filter(user -> user.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (actor.isOwner()) {
            return;
        }

        if (permissionResolutionService.hasAllPermissions(actor.getId())) {
            return;
        }

        log.warn("User {} attempted to manage permission overrides without full admin privilege in company {}",
                currentUser.userId(), companyId);

        throw new BusinessException(ErrorCode.ROLE_PERMISSION_MANAGEMENT_FORBIDDEN);
    }
}
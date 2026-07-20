package com.yasarbilgi.visitormeetingmanagment.userpermission.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.repository.PermissionRepository;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request.UserPermissionOverrideRequestDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response.UserPermissionOverrideResponseDto;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import com.yasarbilgi.visitormeetingmanagment.userpermission.mapper.UserPermissionOverrideMapper;
import com.yasarbilgi.visitormeetingmanagment.userpermission.repository.UserPermissionOverrideRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.repository.UserPermissionUserRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.service.UserPermissionOverrideService;
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
    private final UserPermissionUserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionOverrideMapper overrideMapper;

    @Override
    @Transactional
    public UserPermissionOverrideResponseDto create(UserPermissionOverrideRequestDto dto) {
        log.info("Creating user permission override for user: {}, permission: {}", dto.userId(), dto.permissionId());

        validateOverrideNotExists(dto.userId(), dto.permissionId());

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", dto.userId());
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        Permission permission = permissionRepository.findById(dto.permissionId())
                .orElseThrow(() -> {
                    log.warn("Permission not found with id: {}", dto.permissionId());
                    return new BusinessException(ErrorCode.PERMISSION_NOT_FOUND);
                });

        UserPermissionOverride override = UserPermissionOverride.builder()
                .user(user)
                .permission(permission)
                .type(dto.type())
                .company(user.getCompany())
                .active(true)
                .build();

        UserPermissionOverride saved = overrideRepository.save(override);
        log.info("User permission override created successfully with id: {}", saved.getId());
        return overrideMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserPermissionOverrideResponseDto update(Long id, UserPermissionOverrideRequestDto dto) {
        log.info("Updating user permission override with id: {}", id);

        UserPermissionOverride override = findOverrideOrThrow(id);

        if (!override.getUser().getId().equals(dto.userId()) || !override.getPermission().getId().equals(dto.permissionId())) {
            log.warn("Attempt to change non-updatable fields (user or permission) for override id: {}", id);
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION);
        }

        override.updateType(dto.type());

        log.info("User permission override updated successfully with id: {}", id);
        return overrideMapper.toResponseDto(override);
    }

    @Override
    public UserPermissionOverrideResponseDto getById(Long id) {
        log.debug("Fetching user permission override with id: {}", id);
        UserPermissionOverride override = findOverrideOrThrow(id);
        return overrideMapper.toResponseDto(override);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAll(Pageable pageable) {
        log.debug("Fetching all user permission overrides, page: {}", pageable);
        return overrideRepository.findAll(pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAllByActive(boolean active, Pageable pageable) {
        log.debug("Fetching user permission overrides by active={}, page: {}", active, pageable);
        return overrideRepository.findAllByActive(active, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAllByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching user permission overrides for userId={}, page: {}", userId, pageable);
        return overrideRepository.findAllByUserId(userId, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> getAllByUserIdAndActive(Long userId, boolean active, Pageable pageable) {
        log.debug("Fetching user permission overrides for userId={}, active={}, page: {}", userId, active, pageable);
        return overrideRepository.findAllByUserIdAndActive(userId, active, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    public Page<UserPermissionOverrideResponseDto> search(boolean active, String keyword, Pageable pageable) {
        log.debug("Searching user permission overrides with keyword='{}', active={}", keyword, active);
        return overrideRepository.searchByKeyword(active, keyword, pageable)
                .map(overrideMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        log.info("Deactivating user permission override with id: {}", id);
        UserPermissionOverride override = findOverrideOrThrow(id);
        override.deactivate();
    }

    @Override
    @Transactional
    public void activate(Long id) {
        log.info("Activating user permission override with id: {}", id);
        UserPermissionOverride override = findOverrideOrThrow(id);
        override.activate();
    }

    private UserPermissionOverride findOverrideOrThrow(Long id) {
        return overrideRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User permission override not found with id: {}", id);
                    return new BusinessException(ErrorCode.PERMISSION_OVERRIDE_NOT_FOUND);
                });
    }

    private void validateOverrideNotExists(Long userId, Long permissionId) {
        if (overrideRepository.existsByUserIdAndPermissionId(userId, permissionId)) {
            log.warn("User permission override already exists for userId: {}, permissionId: {}", userId, permissionId);
            throw new BusinessException(ErrorCode.DUPLICATE_PERMISSION_OVERRIDE);
        }
    }
}

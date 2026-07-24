package com.yasarbilgi.visitormeetingmanagment.security.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.OverrideType;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import com.yasarbilgi.visitormeetingmanagment.userpermission.repository.UserPermissionOverrideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Efektif izin hesaplama mantığının merkezi implementasyonu.
 *
 * Formül:
 *   Efektif İzinler = (Tüm Rollerin İzinlerinin Birleşimi)
 *                     + (Kişiye Özel GRANT edilen izinler)
 *                     - (Kişiye Özel REVOKE edilen izinler)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionResolutionServiceImpl implements PermissionResolutionService {

    private final UserRepository userRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;

    @Override
    public Set<String> resolveEffectivePermissions(Long userId) {
        log.debug("Resolving effective permissions for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot resolve permissions: user not found with id: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
        if (user.isOwner()) {
            log.debug("User {} is company owner, granting all permissions", userId);
            return Arrays.stream(PermissionCode.values())
                    .map(Enum::name)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode().name())
                .collect(Collectors.toCollection(HashSet::new));

        List<UserPermissionOverride> overrides =
                userPermissionOverrideRepository.findAllByUserIdAndActive(userId, true);

        for (UserPermissionOverride override : overrides) {
            String permissionCode = override.getPermission().getCode().name();

            if (override.getType() == OverrideType.GRANT) {
                permissions.add(permissionCode);
            } else if (override.getType() == OverrideType.REVOKE) {
                permissions.remove(permissionCode);
            }
        }

        log.debug("Resolved {} effective permissions for user: {}", permissions.size(), userId);
        return permissions;
    }
}
package com.yasarbilgi.visitormeetingmanagment.security.service;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.security.service.impl.PermissionResolutionServiceImpl;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.OverrideType;
import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import com.yasarbilgi.visitormeetingmanagment.userpermission.repository.UserPermissionOverrideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionResolutionServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionOverrideRepository userPermissionOverrideRepository;

    @InjectMocks
    private PermissionResolutionServiceImpl permissionResolutionService;

    private Permission roomViewPermission;
    private Permission userCreatePermission;
    private Permission companyUpdatePermission;

    @BeforeEach
    void setUp() {
        roomViewPermission = createPermission(PermissionCode.ROOM_VIEW);
        userCreatePermission = createPermission(PermissionCode.USER_CREATE);
        companyUpdatePermission = createPermission(PermissionCode.COMPANY_UPDATE);
    }

    // ----- resolveEffectivePermissions() -----

    @Test
    void resolveEffectivePermissions_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> permissionResolutionService.resolveEffectivePermissions(USER_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.USER_NOT_FOUND
                );

        verify(userPermissionOverrideRepository, never())
                .findAllByUserIdAndActive(USER_ID, true);
    }

    @Test
    void resolveEffectivePermissions_shouldReturnAllPermissions_whenUserIsOwner() {
        User owner = createUser(true, Set.of());

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(owner));

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        Set<String> allPermissionCodes =
                Arrays.stream(PermissionCode.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet());

        assertThat(result)
                .containsExactlyInAnyOrderElementsOf(allPermissionCodes);

        assertThat(result)
                .hasSize(PermissionCode.values().length);

        verify(userPermissionOverrideRepository, never())
                .findAllByUserIdAndActive(USER_ID, true);
    }

    @Test
    void resolveEffectivePermissions_shouldReturnBaselinePermissions_whenUserHasNoRoles() {
        User user = createUser(false, Set.of());

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of());

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactlyInAnyOrder(
                PermissionCode.ROOM_VIEW.name(),
                PermissionCode.ROOM_VIEW_AVAILABILITY.name(),
                PermissionCode.RESERVATION_VIEW_OWN.name(),
                PermissionCode.DASHBOARD_VIEW.name()
        );
    }

    @Test
    void resolveEffectivePermissions_shouldReturnPermissionsFromActiveRoles() {
        Role activeRole = createRole(
                true,
                Set.of(roomViewPermission, userCreatePermission)
        );

        User user = createUser(
                false,
                Set.of(activeRole)
        );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of());

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactlyInAnyOrder(
                PermissionCode.ROOM_VIEW.name(),
                PermissionCode.USER_CREATE.name()
        );
    }

    @Test
    void resolveEffectivePermissions_shouldIgnoreInactiveRoles() {
        Role inactiveRole = createRole(
                false,
                Set.of(roomViewPermission, userCreatePermission)
        );

        User user = createUser(
                false,
                Set.of(inactiveRole)
        );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of());

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void resolveEffectivePermissions_shouldMergePermissionsFromMultipleRoles() {
        Role firstRole = createRole(
                true,
                Set.of(roomViewPermission, userCreatePermission)
        );

        Role secondRole = createRole(
                true,
                Set.of(userCreatePermission, companyUpdatePermission)
        );

        User user = createUser(
                false,
                Set.of(firstRole, secondRole)
        );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of());

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactlyInAnyOrder(
                PermissionCode.ROOM_VIEW.name(),
                PermissionCode.USER_CREATE.name(),
                PermissionCode.COMPANY_UPDATE.name()
        );
    }

    @Test
    void resolveEffectivePermissions_shouldAddPermission_whenOverrideTypeIsGrant() {
        Role role = createRole(
                true,
                Set.of(roomViewPermission)
        );

        User user = createUser(
                false,
                Set.of(role)
        );

        UserPermissionOverride grantOverride =
                createOverride(
                        user,
                        companyUpdatePermission,
                        OverrideType.GRANT
                );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of(grantOverride));

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactlyInAnyOrder(
                PermissionCode.ROOM_VIEW.name(),
                PermissionCode.COMPANY_UPDATE.name()
        );
    }

    @Test
    void resolveEffectivePermissions_shouldRemovePermission_whenOverrideTypeIsRevoke() {
        Role role = createRole(
                true,
                Set.of(roomViewPermission, userCreatePermission)
        );

        User user = createUser(
                false,
                Set.of(role)
        );

        UserPermissionOverride revokeOverride =
                createOverride(
                        user,
                        userCreatePermission,
                        OverrideType.REVOKE
                );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of(revokeOverride));

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactly(
                PermissionCode.ROOM_VIEW.name()
        );
    }

    @Test
    void resolveEffectivePermissions_shouldApplyGrantAndRevokeOverridesTogether() {
        Role role = createRole(
                true,
                Set.of(roomViewPermission, userCreatePermission)
        );

        User user = createUser(
                false,
                Set.of(role)
        );

        UserPermissionOverride grantOverride =
                createOverride(
                        user,
                        companyUpdatePermission,
                        OverrideType.GRANT
                );

        UserPermissionOverride revokeOverride =
                createOverride(
                        user,
                        userCreatePermission,
                        OverrideType.REVOKE
                );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of(grantOverride, revokeOverride));

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactlyInAnyOrder(
                PermissionCode.ROOM_VIEW.name(),
                PermissionCode.COMPANY_UPDATE.name()
        );
    }

    @Test
    void resolveEffectivePermissions_shouldNotDuplicateGrantedPermission() {
        Role role = createRole(
                true,
                Set.of(roomViewPermission)
        );

        User user = createUser(
                false,
                Set.of(role)
        );

        UserPermissionOverride grantOverride =
                createOverride(
                        user,
                        roomViewPermission,
                        OverrideType.GRANT
                );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of(grantOverride));

        Set<String> result =
                permissionResolutionService.resolveEffectivePermissions(USER_ID);

        assertThat(result).containsExactly(
                PermissionCode.ROOM_VIEW.name()
        );
    }

    // ----- hasAllPermissions() -----

    @Test
    void hasAllPermissions_shouldReturnTrue_whenUserIsOwner() {
        User owner = createUser(true, Set.of());

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(owner));

        boolean result =
                permissionResolutionService.hasAllPermissions(USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void hasAllPermissions_shouldReturnFalse_whenUserHasLimitedPermissions() {
        Role role = createRole(
                true,
                Set.of(roomViewPermission)
        );

        User user = createUser(
                false,
                Set.of(role)
        );

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(userPermissionOverrideRepository
                .findAllByUserIdAndActive(USER_ID, true))
                .thenReturn(List.of());

        boolean result =
                permissionResolutionService.hasAllPermissions(USER_ID);

        assertThat(result).isFalse();
    }

    // ----- Helpers -----

    private Permission createPermission(PermissionCode code) {
        return Permission.builder()
                .code(code)
                .name(code.name())
                .description(code.name() + " permission")
                .active(true)
                .build();
    }

    private Role createRole(
            boolean active,
            Set<Permission> permissions
    ) {
        return Role.builder()
                .name("Test Role")
                .description("Test role description")
                .permissions(new HashSet<>(permissions))
                .active(active)
                .build();
    }

    private User createUser(
            boolean owner,
            Set<Role> roles
    ) {
        return User.builder()
                .id(USER_ID)
                .firstName("Emir")
                .lastName("Doğruer")
                .email("emir@test.com")
                .username("emird")
                .passwordHash("encodedPassword")
                .owner(owner)
                .roles(new HashSet<>(roles))
                .active(true)
                .build();
    }

    private UserPermissionOverride createOverride(
            User user,
            Permission permission,
            OverrideType type
    ) {
        return UserPermissionOverride.builder()
                .user(user)
                .permission(permission)
                .type(type)
                .active(true)
                .build();
    }
}
package com.yasarbilgi.visitormeetingmanagment.permission.service;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.request.PermissionUpdateRequestDto;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.response.PermissionResponseDto;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCategory;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import com.yasarbilgi.visitormeetingmanagment.permission.mapper.PermissionMapper;
import com.yasarbilgi.visitormeetingmanagment.permission.repository.PermissionRepository;
import com.yasarbilgi.visitormeetingmanagment.permission.service.impl.PermissionServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private static final Long PERMISSION_ID = 1L;

    private Permission permission;
    private PermissionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        permission = Permission.builder()
                .code(PermissionCode.ROOM_VIEW)
                .name("Oda Görüntüleme")
                .description("Toplantı odalarını görüntüleme yetkisi")
                .category(PermissionCategory.ROOM_MANAGEMENT)
                .systemPermission(false)
                .displayOrder(1)
                .build();

        responseDto = PermissionResponseDto.builder()
                .id(PERMISSION_ID)
                .code(PermissionCode.ROOM_VIEW.name())
                .name("Oda Görüntüleme")
                .description("Toplantı odalarını görüntüleme yetkisi")
                .category(PermissionCategory.ROOM_MANAGEMENT)
                .systemPermission(false)
                .displayOrder(1)
                .active(true)
                .build();
    }

    // ----- update() -----

    @Test
    void update_shouldSucceed_whenPermissionExists() {
        PermissionUpdateRequestDto dto =
                PermissionUpdateRequestDto.builder()
                        .name("Oda Görme")
                        .description("Güncellenmiş açıklama")
                        .build();

        PermissionResponseDto updatedResponse =
                PermissionResponseDto.builder()
                        .id(PERMISSION_ID)
                        .code(PermissionCode.ROOM_VIEW.name())
                        .name("Oda Görme")
                        .description("Güncellenmiş açıklama")
                        .category(PermissionCategory.ROOM_MANAGEMENT)
                        .systemPermission(false)
                        .displayOrder(1)
                        .active(true)
                        .build();

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(updatedResponse);

        PermissionResponseDto result =
                permissionService.update(PERMISSION_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Oda Görme");
        assertThat(permission.getName()).isEqualTo("Oda Görme");
        assertThat(permission.getDescription())
                .isEqualTo("Güncellenmiş açıklama");
    }

    @Test
    void update_shouldThrowException_whenPermissionNotFound() {
        PermissionUpdateRequestDto dto =
                PermissionUpdateRequestDto.builder()
                        .name("Yeni İsim")
                        .description("Yeni açıklama")
                        .build();

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                permissionService.update(PERMISSION_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );

        verify(permissionMapper, never())
                .toResponseDto(permission);
    }

    @Test
    void update_shouldThrowException_whenNameIsBlank() {
        PermissionUpdateRequestDto dto =
                PermissionUpdateRequestDto.builder()
                        .name(" ")
                        .description("Açıklama")
                        .build();

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        assertThatThrownBy(() ->
                permissionService.update(PERMISSION_ID, dto)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NAME_REQUIRED
                );

        verify(permissionMapper, never())
                .toResponseDto(permission);
    }

    // ----- getById() -----

    @Test
    void getById_shouldReturnPermission_whenPermissionExists() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        PermissionResponseDto result =
                permissionService.getById(PERMISSION_ID);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Oda Görüntüleme");
        assertThat(result.code()).isEqualTo("ROOM_VIEW");
    }

    @Test
    void getById_shouldThrowException_whenPermissionNotFound() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                permissionService.getById(PERMISSION_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );
    }

    // ----- getByCode() -----

    @Test
    void getByCode_shouldReturnPermission_whenCodeExists() {
        when(permissionRepository.findByCode(
                PermissionCode.ROOM_VIEW
        )).thenReturn(Optional.of(permission));

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        PermissionResponseDto result =
                permissionService.getByCode(
                        PermissionCode.ROOM_VIEW
                );

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("ROOM_VIEW");
    }

    @Test
    void getByCode_shouldThrowException_whenCodeNotFound() {
        when(permissionRepository.findByCode(
                PermissionCode.ROOM_VIEW
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                permissionService.getByCode(
                        PermissionCode.ROOM_VIEW
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );
    }

    // ----- getAll() -----

    @Test
    void getAll_shouldReturnPagedPermissions() {
        Pageable pageable = Pageable.unpaged();
        Page<Permission> permissionPage =
                new PageImpl<>(List.of(permission));

        when(permissionRepository.findAll(pageable))
                .thenReturn(permissionPage);

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        Page<PermissionResponseDto> result =
                permissionService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name())
                .isEqualTo("Oda Görüntüleme");
    }

    // ----- getAllByActive() -----

    @Test
    void getAllByActive_shouldReturnFilteredPermissions() {
        Pageable pageable = Pageable.unpaged();
        Page<Permission> permissionPage =
                new PageImpl<>(List.of(permission));

        when(permissionRepository.findAllByActive(
                true,
                pageable
        )).thenReturn(permissionPage);

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        Page<PermissionResponseDto> result =
                permissionService.getAllByActive(
                        true,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).active())
                .isTrue();
    }

    // ----- getAllByCategory() -----

    @Test
    void getAllByCategory_shouldReturnFilteredPermissions() {
        Pageable pageable = Pageable.unpaged();
        Page<Permission> permissionPage =
                new PageImpl<>(List.of(permission));

        when(permissionRepository.findAllByCategory(
                PermissionCategory.ROOM_MANAGEMENT,
                pageable
        )).thenReturn(permissionPage);

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        Page<PermissionResponseDto> result =
                permissionService.getAllByCategory(
                        PermissionCategory.ROOM_MANAGEMENT,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).category())
                .isEqualTo(PermissionCategory.ROOM_MANAGEMENT);
    }

    // ----- getAllByActiveAndCategory() -----

    @Test
    void getAllByActiveAndCategory_shouldReturnFilteredPermissions() {
        Pageable pageable = Pageable.unpaged();
        Page<Permission> permissionPage =
                new PageImpl<>(List.of(permission));

        when(permissionRepository.findAllByActiveAndCategory(
                true,
                PermissionCategory.ROOM_MANAGEMENT,
                pageable
        )).thenReturn(permissionPage);

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        Page<PermissionResponseDto> result =
                permissionService.getAllByActiveAndCategory(
                        true,
                        PermissionCategory.ROOM_MANAGEMENT,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    // ----- search() -----

    @Test
    void search_shouldReturnMatchingPermissions() {
        Pageable pageable = Pageable.unpaged();
        Page<Permission> permissionPage =
                new PageImpl<>(List.of(permission));

        when(permissionRepository.searchByKeyword(
                true,
                "Oda",
                pageable
        )).thenReturn(permissionPage);

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        Page<PermissionResponseDto> result =
                permissionService.search(
                        true,
                        "Oda",
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name())
                .contains("Oda");
    }

    // ----- getAllByCategoryOrdered() -----

    @Test
    void getAllByCategoryOrdered_shouldReturnOrderedPermissions() {
        Pageable pageable = Pageable.unpaged();
        Page<Permission> permissionPage =
                new PageImpl<>(List.of(permission));

        when(permissionRepository
                .findAllByCategoryOrderByDisplayOrderAsc(
                        PermissionCategory.ROOM_MANAGEMENT,
                        pageable
                )).thenReturn(permissionPage);

        when(permissionMapper.toResponseDto(permission))
                .thenReturn(responseDto);

        Page<PermissionResponseDto> result =
                permissionService.getAllByCategoryOrdered(
                        PermissionCategory.ROOM_MANAGEMENT,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).displayOrder())
                .isEqualTo(1);
    }

    // ----- countByCategory() -----

    @Test
    void countByCategory_shouldReturnPermissionCount() {
        when(permissionRepository.countByCategory(
                PermissionCategory.ROOM_MANAGEMENT
        )).thenReturn(5L);

        long result =
                permissionService.countByCategory(
                        PermissionCategory.ROOM_MANAGEMENT
                );

        assertThat(result).isEqualTo(5L);
    }

    // ----- countBySystemPermission() -----

    @Test
    void countBySystemPermission_shouldReturnPermissionCount() {
        when(permissionRepository.countBySystemPermission(true))
                .thenReturn(12L);

        long result =
                permissionService.countBySystemPermission(true);

        assertThat(result).isEqualTo(12L);
    }

    // ----- deactivate() -----

    @Test
    void deactivate_shouldSucceed_whenCustomPermissionExists() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        permissionService.deactivate(PERMISSION_ID);

        assertThat(permission.isActive()).isFalse();
    }

    @Test
    void deactivate_shouldThrowException_whenPermissionNotFound() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                permissionService.deactivate(PERMISSION_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );
    }

    @Test
    void deactivate_shouldThrowException_whenPermissionIsSystemPermission() {
        Permission systemPermission =
                Permission.builder()
                        .code(PermissionCode.PERMISSION_VIEW)
                        .name("Yetki Görüntüleme")
                        .description("Sistem yetkisi")
                        .category(
                                PermissionCategory.PERMISSION_MANAGEMENT
                        )
                        .systemPermission(true)
                        .displayOrder(1)
                        .build();

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(systemPermission));

        assertThatThrownBy(() ->
                permissionService.deactivate(PERMISSION_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.SYSTEM_PERMISSION_CANNOT_BE_DEACTIVATED
                );
    }

    // ----- activate() -----

    @Test
    void activate_shouldSucceed_whenPermissionExists() {
        permission.deactivate();

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        permissionService.activate(PERMISSION_ID);

        assertThat(permission.isActive()).isTrue();
    }

    @Test
    void activate_shouldThrowException_whenPermissionNotFound() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                permissionService.activate(PERMISSION_ID)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.PERMISSION_NOT_FOUND
                );
    }
}
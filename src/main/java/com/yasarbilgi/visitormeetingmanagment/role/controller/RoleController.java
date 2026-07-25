package com.yasarbilgi.visitormeetingmanagment.role.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.response.RoleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Role kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/roles

 * companyId query parametresi, TenantPathGuardInterceptor tarafından
 * otomatik olarak isteği yapan kullanıcının kendi companyId'siyle
 * karşılaştırılır.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Belirtilen şirket için yeni bir rol oluşturur.
     * Başarılı olursa 201 Created döner.
     */
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDto>> create(
            @RequestParam Long companyId,
            @Valid @RequestBody CreateRoleRequestDto dto
    ) {
        RoleResponseDto created = roleService.create(companyId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", created));
    }

    /**
     * Var olan bir rolü günceller.
     */
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> update(
            @PathVariable Long id,
            @RequestParam Long companyId,
            @Valid @RequestBody UpdateRoleRequestDto dto
    ) {
        RoleResponseDto updated = roleService.update(companyId, id, dto);

        return ResponseEntity.ok(
                ApiResponse.success("Role updated successfully", updated)
        );
    }

    /**
     * ID ve şirket bilgisine göre tek bir rol getirir.
     */
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getById(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        RoleResponseDto role = roleService.getById(companyId, id);

        return ResponseEntity.ok(ApiResponse.success(role));
    }

    /**
     * Belirtilen şirkete ait tüm rolleri sayfalanmış şekilde listeler.
     */
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoleResponseDto>>> getAll(
            @RequestParam Long companyId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<RoleResponseDto> roles =
                PageResponse.of(roleService.getAll(companyId, pageable));

        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Aktif veya pasif rolleri sayfalanmış şekilde listeler.
     */
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponseDto>>> getAllByActive(
            @RequestParam Long companyId,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<RoleResponseDto> roles =
                PageResponse.of(
                        roleService.getAllByActive(companyId, active, pageable)
                );

        return ResponseEntity.ok(ApiResponse.success(roles));
    }


    /**
     * Rol adı veya açıklaması üzerinde anahtar kelime araması yapar.
     */
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponseDto>>> search(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<RoleResponseDto> roles =
                PageResponse.of(
                        roleService.search(
                                companyId,
                                active,
                                keyword,
                                pageable
                        )
                );

        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Bir role yeni bir permission ekler.
     */
    @PreAuthorize("hasAuthority('ROLE_ASSIGN_PERMISSION')")
    @PatchMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId,
            @RequestParam Long companyId
    ) {
        RoleResponseDto updated =
                roleService.assignPermission(
                        companyId,
                        roleId,
                        permissionId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission assigned to role successfully",
                        updated
                )
        );
    }

    /**
     * Bir rolden permission kaldırır.
     */
    @PreAuthorize("hasAuthority('ROLE_REVOKE_PERMISSION')")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> revokePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId,
            @RequestParam Long companyId
    ) {
        RoleResponseDto updated =
                roleService.revokePermission(
                        companyId,
                        roleId,
                        permissionId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission revoked from role successfully",
                        updated
                )
        );
    }

    /**
     * Bir rolü pasif duruma getirir.
     */
    @PreAuthorize("hasAuthority('ROLE_DEACTIVATE')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        roleService.deactivate(companyId, id);

        return ResponseEntity.ok(
                ApiResponse.success("Role deactivated successfully")
        );
    }

    /**
     * Pasif durumdaki bir rolü tekrar aktif eder.
     */
    @PreAuthorize("hasAuthority('ROLE_ACTIVATE')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        roleService.activate(companyId, id);

        return ResponseEntity.ok(
                ApiResponse.success("Role activated successfully")
        );
    }
}
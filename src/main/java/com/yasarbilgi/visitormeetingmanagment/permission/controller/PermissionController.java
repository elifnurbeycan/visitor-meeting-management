package com.yasarbilgi.visitormeetingmanagment.permission.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.request.PermissionUpdateRequestDto;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.response.PermissionResponseDto;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCategory;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import com.yasarbilgi.visitormeetingmanagment.permission.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Permission kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/permissions
 * Not: create endpoint'i bilinçli olarak yok — her permission'ın code'u
 * PermissionCode enum'undan gelir ve migration ile seed edilir.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Var olan bir permission'ın görünen adını/açıklamasını günceller.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequestDto dto
    ) {
        PermissionResponseDto updated = permissionService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", updated));
    }

    /**
     * ID'ye göre tekil bir permission getirir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponseDto>> getById(@PathVariable Long id) {
        PermissionResponseDto permission = permissionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    /**
     * Sabit PermissionCode değerine göre tekil bir permission getirir.
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PermissionResponseDto>> getByCode(@PathVariable PermissionCode code) {
        PermissionResponseDto permission = permissionService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    /**
     * Tüm permission'ları sayfalanmış şekilde listeler.
     * Varsayılan: sayfa boyutu 20, isme göre artan sıralama.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponseDto>>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<PermissionResponseDto> permissions = PageResponse.of(permissionService.getAll(pageable));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Aktif/pasif durumuna göre filtrelenmiş permission'ları sayfalanmış şekilde listeler.
     */
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponseDto>>> getAllByActive(
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<PermissionResponseDto> permissions =
                PageResponse.of(permissionService.getAllByActive(active, pageable));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Kategoriye göre filtrelenmiş permission'ları sayfalanmış şekilde listeler.
     */
    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponseDto>>> getAllByCategory(
            @RequestParam PermissionCategory category,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<PermissionResponseDto> permissions =
                PageResponse.of(permissionService.getAllByCategory(category, pageable));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Hem aktiflik durumuna hem de kategoriye göre filtrelenmiş permission'ları listeler.
     */
    @GetMapping("/by-active-and-category")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponseDto>>> getAllByActiveAndCategory(
            @RequestParam boolean active,
            @RequestParam PermissionCategory category,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<PermissionResponseDto> permissions =
                PageResponse.of(permissionService.getAllByActiveAndCategory(active, category, pageable));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * İsim veya açıklama üzerinde anahtar kelime araması yapar.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponseDto>>> search(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<PermissionResponseDto> permissions =
                PageResponse.of(permissionService.search(active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Bir kategorideki permission'ları displayOrder'a göre sıralı listeler
     * (yetki yönetimi ekranında gruplu/sıralı göstermek için).
     */
    @GetMapping("/by-category/ordered")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponseDto>>> getAllByCategoryOrdered(
            @RequestParam PermissionCategory category,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        PageResponse<PermissionResponseDto> permissions =
                PageResponse.of(permissionService.getAllByCategoryOrdered(category, pageable));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Bir kategorideki toplam permission sayısını döner.
     */
    @GetMapping("/by-category/count")
    public ResponseEntity<ApiResponse<Long>> countByCategory(@RequestParam PermissionCategory category) {
        long count = permissionService.countByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Sistem tanımlı ya da custom permission sayısını döner.
     */
    @GetMapping("/by-system/count")
    public ResponseEntity<ApiResponse<Long>> countBySystemPermission(@RequestParam boolean systemPermission) {
        long count = permissionService.countBySystemPermission(systemPermission);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Bir permission'ı pasif hale getirir. Sistem tanımlı permission'lar pasife alınamaz.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        permissionService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deactivated successfully"));
    }

    /**
     * Pasif bir permission'ı tekrar aktif eder.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        permissionService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Permission activated successfully"));
    }
}

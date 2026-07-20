package com.yasarbilgi.visitormeetingmanagment.platform.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.dto.response.SuperAdminResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.service.SuperAdminService;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SuperAdmin (platform seviyesi) için REST endpoint'leri.
 * URL şeması: /api/v1/platform/...
 *
 * Bu controller, normal tenant kullanıcılarından tamamen ayrı bir yetki
 * seviyesini temsil eder. Security fazında, bu endpoint'lerin tamamı
 * @PreAuthorize("hasRole('SUPER_ADMIN')") ile korunacak ve ayrı bir
 * authentication filter zincirine bağlanacak.
 */
@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    // ----- SuperAdmin'in kendi yönetimi -----

    @GetMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<SuperAdminResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(superAdminService.getById(id)));
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<PageResponse<SuperAdminResponseDto>>> getAll(
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable
    ) {
        PageResponse<SuperAdminResponseDto> admins = PageResponse.of(superAdminService.getAll(pageable));
        return ResponseEntity.ok(ApiResponse.success(admins));
    }

    @GetMapping("/admins/by-active")
    public ResponseEntity<ApiResponse<PageResponse<SuperAdminResponseDto>>> getAllByActive(
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable
    ) {
        PageResponse<SuperAdminResponseDto> admins =
                PageResponse.of(superAdminService.getAllByActive(active, pageable));
        return ResponseEntity.ok(ApiResponse.success(admins));
    }

    @GetMapping("/admins/search")
    public ResponseEntity<ApiResponse<PageResponse<SuperAdminResponseDto>>> search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable
    ) {
        PageResponse<SuperAdminResponseDto> admins =
                PageResponse.of(superAdminService.search(keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(admins));
    }

    @GetMapping("/admins/count/active")
    public ResponseEntity<ApiResponse<Long>> countActive() {
        return ResponseEntity.ok(ApiResponse.success(superAdminService.countActive()));
    }

    @PatchMapping("/admins/{id}/approve")
    public ResponseEntity<ApiResponse<SuperAdminResponseDto>> approve(@PathVariable Long id) {
        SuperAdminResponseDto approved = superAdminService.approve(id);
        return ResponseEntity.ok(ApiResponse.success("Super admin approved successfully", approved));
    }

    @PatchMapping("/admins/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        superAdminService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Super admin deactivated successfully"));
    }

    @PatchMapping("/admins/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        superAdminService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Super admin activated successfully"));
    }

    // ----- Şirket yönetimi -----

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> getAllCompanies(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies =
                PageResponse.of(superAdminService.getAllCompanies(pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @GetMapping("/companies/pending")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> getPendingCompanies(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies =
                PageResponse.of(superAdminService.getPendingCompanies(pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @PatchMapping("/companies/{companyId}/approve")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> approveCompany(
            @PathVariable Long companyId
    ) {
        CompanyResponseDto approved = superAdminService.approveCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success("Company approved successfully", approved));
    }

    @PatchMapping("/companies/{companyId}/reject")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> rejectCompany(
            @PathVariable Long companyId,
            @RequestParam String reason
    ) {
        CompanyResponseDto rejected = superAdminService.rejectCompany(companyId, reason);
        return ResponseEntity.ok(ApiResponse.success("Company rejected successfully", rejected));
    }

    @PatchMapping("/companies/{companyId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCompany(@PathVariable Long companyId) {
        superAdminService.deactivateCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success("Company deactivated successfully"));
    }

    @PatchMapping("/companies/{companyId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateCompany(@PathVariable Long companyId) {
        superAdminService.activateCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success("Company activated successfully"));
    }

    @DeleteMapping("/companies/{companyId}")
    public ResponseEntity<Void> hardDeleteCompany(@PathVariable Long companyId) {
        superAdminService.hardDeleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    // ----- Kullanıcı/Owner yönetimi -----

    @PatchMapping("/companies/{companyId}/force-transfer-ownership")
    public ResponseEntity<ApiResponse<UserResponseDto>> forceTransferOwnership(
            @PathVariable Long companyId,
            @RequestParam Long newOwnerId
    ) {
        UserResponseDto updated = superAdminService.forceTransferCompanyOwnership(companyId, newOwnerId);
        return ResponseEntity.ok(ApiResponse.success("Ownership forcibly transferred", updated));
    }
}
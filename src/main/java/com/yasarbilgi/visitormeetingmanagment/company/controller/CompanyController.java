package com.yasarbilgi.visitormeetingmanagment.company.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.CompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.UpdateCompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.company.service.CompanyService;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Company kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/companies
 */
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Yeni bir şirket kaydı oluşturur. Başarılı olursa 201 Created döner.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponseDto>> create(
            @Valid @RequestBody CompanyRequestDto dto
    ) {
        CompanyResponseDto created = companyService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created successfully", created));
    }

    /**
     * Var olan bir şirketi günceller.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or (#id == authentication.principal.companyId and hasAuthority('COMPANY_UPDATE'))")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyRequestDto dto
    ) {
        CompanyResponseDto updated = companyService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", updated));
    }

    /**
     * ID'ye göre tekil bir şirket getirir.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or (#id == authentication.principal.companyId and hasAuthority('COMPANY_VIEW'))")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> getById(@PathVariable Long id) {
        CompanyResponseDto company = companyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    /**
     * Slug'a göre tekil bir şirket getirir (okunabilir URL senaryoları için).
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> getBySlug(@PathVariable String slug) {
        CompanyResponseDto company = companyService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    /**
     * Tüm şirketleri sayfalanmış şekilde listeler.
     * Varsayılan: sayfa boyutu 20, isme göre artan sıralama.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies = PageResponse.of(companyService.getAll(pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Aktif/pasif durumuna göre filtrelenmiş şirketleri sayfalanmış şekilde listeler.
     */
    @GetMapping("/by-active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> getAllByActive(
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies =
                PageResponse.of(companyService.getAllByActive(active, pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Onay durumuna göre filtrelenmiş şirketleri sayfalanmış şekilde listeler.
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> getAllByStatus(
            @RequestParam CompanyStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies =
                PageResponse.of(companyService.getAllByStatus(status, pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * İsim veya slug üzerinde anahtar kelime araması yapar.
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> search(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies =
                PageResponse.of(companyService.search(active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * SuperAdmin'in onayını bekleyen şirketleri, en eski başvuru önce olacak
     * şekilde sayfalanmış olarak listeler.
     */
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDto>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<CompanyResponseDto> companies =
                PageResponse.of(companyService.getPendingApprovals(pageable));
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Onay bekleyen toplam şirket sayısını döner.
     */
    @GetMapping("/pending-approvals/count")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countPendingApprovals() {
        long count = companyService.countPendingApprovals();
        return ResponseEntity.ok(ApiResponse.success(count));
    }


    /**
     * Bir şirketi pasif hale getirir (soft-delete).
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (#id == authentication.principal.companyId and hasAuthority('COMPANY_MANAGE_SETTINGS'))")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        companyService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Company deactivated successfully"));
    }

    /**
     * Pasif bir şirketi tekrar aktif eder.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (#id == authentication.principal.companyId and hasAuthority('COMPANY_MANAGE_SETTINGS'))")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        companyService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Company activated successfully"));
    }
}
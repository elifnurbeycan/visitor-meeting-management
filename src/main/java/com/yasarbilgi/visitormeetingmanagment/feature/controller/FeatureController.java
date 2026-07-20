package com.yasarbilgi.visitormeetingmanagment.feature.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.request.FeatureRequestDto;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.response.FeatureResponseDto;
import com.yasarbilgi.visitormeetingmanagment.feature.service.FeatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feature (oda özelliği) kaynağı için REST endpoint'leri.
 * URL şeması: /api/v1/companies/{companyId}/features
 * Feature, TenantBaseEntity olduğu için her zaman bir şirkete bağlı — bu yüzden
 * Company kaynağının altında nested olarak tasarlandı.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    /**
     * Bir şirket için yeni bir oda özelliği oluşturur. Başarılı olursa 201 Created döner.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FeatureResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody FeatureRequestDto dto
    ) {
        FeatureResponseDto created = featureService.create(companyId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feature created successfully", created));
    }

    /**
     * Var olan bir feature'ı günceller.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long id,
            @Valid @RequestBody FeatureRequestDto dto
    ) {
        FeatureResponseDto updated = featureService.update(companyId, id, dto);
        return ResponseEntity.ok(ApiResponse.success("Feature updated successfully", updated));
    }

    /**
     * ID'ye göre tekil bir feature getirir.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long id
    ) {
        FeatureResponseDto feature = featureService.getById(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(feature));
    }

    /**
     * Bir şirkete ait tüm feature'ları sayfalanmış şekilde listeler.
     * Varsayılan: sayfa boyutu 20, isme göre artan sıralama.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeatureResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<FeatureResponseDto> features = PageResponse.of(featureService.getAll(companyId, pageable));
        return ResponseEntity.ok(ApiResponse.success(features));
    }

    /**
     * Bir şirkete ait, aktif/pasif durumuna göre filtrelenmiş feature'ları listeler.
     */
    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<FeatureResponseDto>>> getAllByActive(
            @PathVariable Long companyId,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<FeatureResponseDto> features =
                PageResponse.of(featureService.getAllByActive(companyId, active, pageable));
        return ResponseEntity.ok(ApiResponse.success(features));
    }

    /**
     * Bir şirket içinde isim veya açıklama üzerinde anahtar kelime araması yapar.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<FeatureResponseDto>>> search(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<FeatureResponseDto> features =
                PageResponse.of(featureService.search(companyId, active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(features));
    }

    /**
     * Bir şirketin toplam feature sayısını döner.
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countAll(@PathVariable Long companyId) {
        long count = featureService.countAll(companyId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Bir feature'ı pasif hale getirir (soft-delete).
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long id
    ) {
        featureService.deactivate(companyId, id);
        return ResponseEntity.ok(ApiResponse.success("Feature deactivated successfully"));
    }

    /**
     * Pasif bir feature'ı tekrar aktif eder.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long companyId,
            @PathVariable Long id
    ) {
        featureService.activate(companyId, id);
        return ResponseEntity.ok(ApiResponse.success("Feature activated successfully"));
    }
}

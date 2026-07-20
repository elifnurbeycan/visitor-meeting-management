package com.yasarbilgi.visitormeetingmanagment.feature.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.request.FeatureRequestDto;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.response.FeatureResponseDto;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import com.yasarbilgi.visitormeetingmanagment.feature.mapper.FeatureMapper;
import com.yasarbilgi.visitormeetingmanagment.feature.repository.FeatureRepository;
import com.yasarbilgi.visitormeetingmanagment.feature.service.FeatureService;
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
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    private final CompanyRepository companyRepository;
    private final FeatureMapper featureMapper;

    /**
     * Bir şirket için yeni bir oda özelliği (TV, Projeksiyon vb.) oluşturur.
     * Aynı şirkette isim çakışmasını DB constraint'ine düşmeden önce kontrol eder.
     * Feature, TenantBaseEntity olduğu için company zorunlu (nullable=false) —
     * bu yüzden mapper üzerinden değil, builder ile doğrudan burada kuruluyor.
     */
    @Override
    @Transactional
    public FeatureResponseDto create(Long companyId, FeatureRequestDto dto) {
        log.info("Creating feature '{}' for companyId: {}", dto.name(), companyId);

        validateNameNotTaken(companyId, dto.name());
        Company company = findCompanyOrThrow(companyId);

        Feature feature = Feature.builder()
                .name(dto.name())
                .description(dto.description())
                .company(company)
                .build();
        Feature saved = featureRepository.save(feature);

        log.info("Feature created successfully with id: {}", saved.getId());
        return featureMapper.toResponseDto(saved);
    }

    /**
     * Var olan bir feature'ın adını/açıklamasını günceller.
     * İsim gerçekten değiştiyse aynı şirket içinde tekrar benzersizlik kontrolü yapar;
     * değişmediyse gereksiz "zaten kullanılıyor" hatası vermez.
     */
    @Override
    @Transactional
    public FeatureResponseDto update(Long companyId, Long id, FeatureRequestDto dto) {
        log.info("Updating feature with id: {} for companyId: {}", id, companyId);

        Feature feature = findFeatureOrThrow(companyId, id);

        if (!feature.getName().equalsIgnoreCase(dto.name())) {
            validateNameNotTaken(companyId, dto.name());
        }

        feature.rename(dto.name());
        feature.updateDescription(dto.description());

        log.info("Feature updated successfully with id: {}", id);
        return featureMapper.toResponseDto(feature);
    }

    /**
     * ID'ye göre, ilgili şirkete ait tekil bir feature getirir.
     * Feature başka bir şirkete aitse de FEATURE_NOT_FOUND döner (tenant izolasyonu).
     */
    @Override
    public FeatureResponseDto getById(Long companyId, Long id) {
        log.debug("Fetching feature with id: {} for companyId: {}", id, companyId);
        Feature feature = findFeatureOrThrow(companyId, id);
        return featureMapper.toResponseDto(feature);
    }

    /**
     * Bir şirkete ait tüm feature'ları sayfalanmış şekilde getirir.
     */
    @Override
    public Page<FeatureResponseDto> getAll(Long companyId, Pageable pageable) {
        log.debug("Fetching all features for companyId: {}, page: {}", companyId, pageable);
        return featureRepository.findAllByCompanyId(companyId, pageable)
                .map(featureMapper::toResponseDto);
    }

    /**
     * Bir şirkete ait, aktif/pasif durumuna göre filtrelenmiş feature'ları getirir.
     */
    @Override
    public Page<FeatureResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable) {
        log.debug("Fetching features for companyId: {}, active={}, page: {}", companyId, active, pageable);
        return featureRepository.findAllByCompanyIdAndActive(companyId, active, pageable)
                .map(featureMapper::toResponseDto);
    }

    /**
     * Bir şirket içinde, isim veya açıklama üzerinde anahtar kelime araması yapar.
     */
    @Override
    public Page<FeatureResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable) {
        log.debug("Searching features for companyId: {}, keyword='{}', active={}", companyId, keyword, active);
        return featureRepository.searchByCompanyIdAndKeyword(companyId, active, keyword, pageable)
                .map(featureMapper::toResponseDto);
    }

    /**
     * Bir şirketin toplam feature sayısını döner.
     */
    @Override
    public long countAll(Long companyId) {
        return featureRepository.countByCompanyId(companyId);
    }

    /**
     * Bir feature'ı pasif hale getirir (soft-delete).
     */
    @Override
    @Transactional
    public void deactivate(Long companyId, Long id) {
        log.info("Deactivating feature with id: {} for companyId: {}", id, companyId);
        Feature feature = findFeatureOrThrow(companyId, id);
        feature.deactivate();
    }

    /**
     * Daha önce pasif hale getirilmiş bir feature'ı tekrar aktif eder.
     */
    @Override
    @Transactional
    public void activate(Long companyId, Long id) {
        log.info("Activating feature with id: {} for companyId: {}", id, companyId);
        Feature feature = findFeatureOrThrow(companyId, id);
        feature.activate();
    }

    // ----- Private helpers -----

    /**
     * ID ve companyId'ye göre feature'ı bulur, bulunamazsa (ya da başka bir
     * şirkete aitse) FEATURE_NOT_FOUND fırlatır.
     */
    private Feature findFeatureOrThrow(Long companyId, Long id) {
        return featureRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> {
                    log.warn("Feature not found with id: {} for companyId: {}", id, companyId);
                    return new BusinessException(ErrorCode.FEATURE_NOT_FOUND);
                });
    }

    /**
     * ID'ye göre şirketi bulur, bulunamazsa COMPANY_NOT_FOUND fırlatır.
     */
    private Company findCompanyOrThrow(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> {
                    log.warn("Company not found with id: {}", companyId);
                    return new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
                });
    }

    /**
     * Verilen ismin, aynı şirket içinde başka bir feature tarafından
     * kullanılıp kullanılmadığını kontrol eder. Kullanılıyorsa FEATURE_ALREADY_EXISTS fırlatır.
     */
    private void validateNameNotTaken(Long companyId, String name) {
        if (featureRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
            throw new BusinessException(ErrorCode.FEATURE_ALREADY_EXISTS);
        }
    }
}

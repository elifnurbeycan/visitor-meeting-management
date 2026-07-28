package com.yasarbilgi.visitormeetingmanagment.feature.service;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.request.FeatureRequestDto;
import com.yasarbilgi.visitormeetingmanagment.feature.dto.response.FeatureResponseDto;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import com.yasarbilgi.visitormeetingmanagment.feature.mapper.FeatureMapper;
import com.yasarbilgi.visitormeetingmanagment.feature.repository.FeatureRepository;
import com.yasarbilgi.visitormeetingmanagment.feature.service.impl.FeatureServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FeatureServiceImpl için kapsamlı birim (unit) testler.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 * Her metod için hem "mutlu yol" (başarılı senaryo) hem de olası hata
 * senaryoları ayrı test metodlarıyla kapsanıyor.
 */
@ExtendWith(MockitoExtension.class)
class FeatureServiceImplTest {

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private FeatureMapper featureMapper;

    @InjectMocks
    private FeatureServiceImpl featureService;

    private static final Long COMPANY_ID = 7L;
    private static final Long FEATURE_ID = 1L;

    private Company company;
    private Feature feature;
    private FeatureResponseDto responseDto;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(COMPANY_ID)
                .name("Test Şirketi")
                .slug("test-sirketi")
                .build();

        feature = Feature.builder()
                .id(FEATURE_ID)
                .company(company)
                .name("Projeksiyon")
                .description("4K Projeksiyon Cihazı")
                .build();

        responseDto = FeatureResponseDto.builder()
                .id(FEATURE_ID)
                .name("Projeksiyon")
                .description("4K Projeksiyon Cihazı")
                .active(true)
                .build();
    }

    // ----- 1. create() -----

    @Test
    void create_shouldSucceed_whenValidRequest() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Projeksiyon")
                .description("4K Projeksiyon Cihazı")
                .build();

        when(featureRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(false);
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(featureRepository.save(any(Feature.class))).thenReturn(feature);
        when(featureMapper.toResponseDto(any(Feature.class))).thenReturn(responseDto);

        // Act
        FeatureResponseDto result = featureService.create(COMPANY_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Projeksiyon");
        verify(featureRepository, times(1)).save(any(Feature.class));
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyTaken() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Projeksiyon")
                .build();

        when(featureRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> featureService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_ALREADY_EXISTS);

        verify(featureRepository, never()).save(any(Feature.class));
    }

    @Test
    void create_shouldThrowException_whenCompanyNotFound() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Projeksiyon")
                .build();

        when(featureRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, dto.name())).thenReturn(false);
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> featureService.create(COMPANY_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMPANY_NOT_FOUND);

        verify(featureRepository, never()).save(any(Feature.class));
    }

    // ----- 2. update() -----

    @Test
    void update_shouldSucceed_whenNameUnchanged() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Projeksiyon") // isim aynı
                .description("Güncellenmiş açıklama")
                .build();

        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.of(feature));
        when(featureMapper.toResponseDto(feature)).thenReturn(responseDto);

        // Act
        FeatureResponseDto result = featureService.update(COMPANY_ID, FEATURE_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        // İsim değişmediği için validateNameNotTaken (existsByCompanyIdAndNameIgnoreCase) HİÇ çağrılmamalı
        verify(featureRepository, never()).existsByCompanyIdAndNameIgnoreCase(any(), anyString());
    }

    @Test
    void update_shouldSucceed_whenNameChangedAndNotTaken() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Akıllı Tahta")
                .description("Yeni özellik")
                .build();

        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.of(feature));
        when(featureRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, "Akıllı Tahta")).thenReturn(false);
        when(featureMapper.toResponseDto(feature)).thenReturn(responseDto);

        // Act
        FeatureResponseDto result = featureService.update(COMPANY_ID, FEATURE_ID, dto);

        // Assert
        assertThat(result).isNotNull();
        verify(featureRepository, times(1)).existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, "Akıllı Tahta");
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyTaken() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Mevcut Özellik")
                .build();

        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.of(feature));
        when(featureRepository.existsByCompanyIdAndNameIgnoreCase(COMPANY_ID, "Mevcut Özellik")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> featureService.update(COMPANY_ID, FEATURE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_ALREADY_EXISTS);
    }

    @Test
    void update_shouldThrowException_whenFeatureNotFound() {
        // Arrange
        FeatureRequestDto dto = FeatureRequestDto.builder()
                .name("Herhangi Bir İsim")
                .build();

        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> featureService.update(COMPANY_ID, FEATURE_ID, dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }

    // ----- 3. getById() -----

    @Test
    void getById_shouldReturnFeatureResponseDto_whenFound() {
        // Arrange
        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.of(feature));
        when(featureMapper.toResponseDto(feature)).thenReturn(responseDto);

        // Act
        FeatureResponseDto result = featureService.getById(COMPANY_ID, FEATURE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Projeksiyon");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        // Arrange
        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> featureService.getById(COMPANY_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }

    // ----- 4. getAll() -----

    @Test
    void getAll_shouldReturnPagedFeatures() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Feature> page = new PageImpl<>(List.of(feature));

        when(featureRepository.findAllByCompanyId(COMPANY_ID, pageable)).thenReturn(page);
        when(featureMapper.toResponseDto(feature)).thenReturn(responseDto);

        // Act
        Page<FeatureResponseDto> result = featureService.getAll(COMPANY_ID, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Projeksiyon");
    }

    // ----- 5. getAllByActive() -----

    @Test
    void getAllByActive_shouldReturnFilteredPagedFeatures() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Feature> page = new PageImpl<>(List.of(feature));

        when(featureRepository.findAllByCompanyIdAndActive(COMPANY_ID, true, pageable)).thenReturn(page);
        when(featureMapper.toResponseDto(feature)).thenReturn(responseDto);

        // Act
        Page<FeatureResponseDto> result = featureService.getAllByActive(COMPANY_ID, true, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    // ----- 6. search() -----

    @Test
    void search_shouldReturnMatchingFeatures() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Feature> page = new PageImpl<>(List.of(feature));

        when(featureRepository.searchByCompanyIdAndKeyword(COMPANY_ID, true, "Projeksiyon", pageable)).thenReturn(page);
        when(featureMapper.toResponseDto(feature)).thenReturn(responseDto);

        // Act
        Page<FeatureResponseDto> result = featureService.search(COMPANY_ID, true, "Projeksiyon", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    // ----- 7. countAll() -----

    @Test
    void countAll_shouldReturnCorrectCount() {
        // Arrange
        when(featureRepository.countByCompanyId(COMPANY_ID)).thenReturn(5L);

        // Act
        long count = featureService.countAll(COMPANY_ID);

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(featureRepository, times(1)).countByCompanyId(COMPANY_ID);
    }

    // ----- 8. deactivate() -----

    @Test
    void deactivate_shouldDeactivateFeature_whenFound() {
        // Arrange
        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.of(feature));

        // Act
        featureService.deactivate(COMPANY_ID, FEATURE_ID);

        // Assert
        assertThat(feature.isActive()).isFalse();
    }

    @Test
    void deactivate_shouldThrowException_whenNotFound() {
        // Arrange
        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> featureService.deactivate(COMPANY_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }

    // ----- 9. activate() -----

    @Test
    void activate_shouldActivateFeature_whenFound() {
        // Arrange
        feature.deactivate(); // pasif duruma getirelim
        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.of(feature));

        // Act
        featureService.activate(COMPANY_ID, FEATURE_ID);

        // Assert
        assertThat(feature.isActive()).isTrue();
    }

    @Test
    void activate_shouldThrowException_whenNotFound() {
        // Arrange
        when(featureRepository.findByIdAndCompanyId(FEATURE_ID, COMPANY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> featureService.activate(COMPANY_ID, FEATURE_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEATURE_NOT_FOUND);
    }
}

package com.yasarbilgi.visitormeetingmanagment.job.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.job.dto.request.JobTitleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.job.dto.response.JobTitleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.job.mapper.JobTitleMapper;
import com.yasarbilgi.visitormeetingmanagment.job.repository.JobTitleRepository;
import com.yasarbilgi.visitormeetingmanagment.job.service.JobTitleService;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

/**
 * JobTitleService arayüzünün iş mantığı implementasyon sınıfı.
 * İşlemler kiracı (Company) bazında doğrulanır ve izole edilir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobTitleServiceImpl implements JobTitleService {

    private final JobTitleRepository jobTitleRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final JobTitleMapper jobTitleMapper;

    /**
     * Şirkete ait yeni bir iş unvanı tanımlar.
     * İsim benzersizliği ve şirketin varlığı kontrol edilir.
     */
    @Override
    @Transactional
    public JobTitleResponseDto create(Long companyId, JobTitleRequestDto dto) {
        log.info("Creating job title: {} for company: {}", dto.name(), companyId);

        Company company = findCompanyOrThrow(companyId);
        validateNameNotTaken(dto.name(), companyId);

        Set<Role> roles = loadRolesIfProvided(dto.defaultRoleIds(), companyId);

        JobTitle jobTitle = JobTitle.builder()
                .name(dto.name())
                .description(dto.description())
                .company(company)
                .defaultRoles(roles)
                .active(true)
                .build();

        JobTitle saved = jobTitleRepository.save(jobTitle);
        log.info("Job title created successfully with id: {}", saved.getId());
        return jobTitleMapper.toResponseDto(saved);
    }

    /**
     * Var olan iş unvanının bilgilerini ve varsayılan rollerini günceller.
     * Eğer isim değiştiyse tekrar benzersizlik kontrolü uygulanır.
     */
    @Override
    @Transactional
    public JobTitleResponseDto update(Long companyId, Long id, JobTitleRequestDto dto) {
        log.info("Updating job title id: {} for company: {}", id, companyId);

        JobTitle jobTitle = findJobTitleOrThrow(id, companyId);

        if (!jobTitle.getName().equalsIgnoreCase(dto.name())) {
            validateNameNotTakenForUpdate(dto.name(), companyId, id);
        }

        jobTitle.rename(dto.name());
        jobTitle.updateDescription(dto.description());

        Set<Role> updatedRoles = loadRolesIfProvided(dto.defaultRoleIds(), companyId);
        jobTitle.getDefaultRoles().clear();
        jobTitle.getDefaultRoles().addAll(updatedRoles);

        log.info("Job title updated successfully with id: {}", id);
        return jobTitleMapper.toResponseDto(jobTitle);
    }

    /**
     * Belirtilen şirkete ait olan iş unvanını ID'sine göre getirir.
     */
    @Override
    public JobTitleResponseDto getById(Long companyId, Long id) {
        log.debug("Fetching job title id: {} for company: {}", id, companyId);
        JobTitle jobTitle = findJobTitleOrThrow(id, companyId);
        return jobTitleMapper.toResponseDto(jobTitle);
    }

    /**
     * Şirkete ait tüm iş unvanlarını sayfalanmış olarak getirir.
     */
    @Override
    public Page<JobTitleResponseDto> getAll(Long companyId, Pageable pageable) {
        log.debug("Fetching all job titles for company: {}, page: {}", companyId, pageable);
        return jobTitleRepository.findAllByCompanyId(companyId, pageable)
                .map(jobTitleMapper::toResponseDto);
    }

    /**
     * Şirkete ait aktiflik durumuna göre filtrelenmiş iş unvanlarını sayfalanmış getirir.
     */
    @Override
    public Page<JobTitleResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable) {
        log.debug("Fetching job titles by active={}, company: {}", active, companyId);
        return jobTitleRepository.findAllByCompanyIdAndActive(companyId, active, pageable)
                .map(jobTitleMapper::toResponseDto);
    }

    /**
     * Şirket içinde arama kelimesine göre iş unvanı arar.
     */
    @Override
    public Page<JobTitleResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable) {
        log.debug("Searching job titles keyword='{}', active={}, company: {}", keyword, active, companyId);
        return jobTitleRepository.searchByKeyword(companyId, active, keyword, pageable)
                .map(jobTitleMapper::toResponseDto);
    }

    /**
     * İş unvanını pasif duruma getirir (soft delete).
     */
    @Override
    @Transactional
    public void deactivate(Long companyId, Long id) {
        log.info("Deactivating job title id: {} for company: {}", id, companyId);
        JobTitle jobTitle = findJobTitleOrThrow(id, companyId);
        jobTitle.deactivate();
    }

    /**
     * Pasif durumdaki iş unvanını tekrar aktif hale getirir.
     */
    @Override
    @Transactional
    public void activate(Long companyId, Long id) {
        log.info("Activating job title id: {} for company: {}", id, companyId);
        JobTitle jobTitle = findJobTitleOrThrow(id, companyId);
        jobTitle.activate();
    }

    // ----- Yardımcı Metotlar -----

    /** Şirketi bulur veya bulamazsa hata fırlatır. */
    private Company findCompanyOrThrow(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
    }

    /** Şirket ID'si ile eşleşen iş unvanını bulur veya bulamazsa hata fırlatır. */
    private JobTitle findJobTitleOrThrow(Long id, Long companyId) {
        return jobTitleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_TITLE_NOT_FOUND));
    }

    /** Şirket içinde ismin kullanımda olup olmadığını kontrol eder. */
    private void validateNameNotTaken(String name, Long companyId) {
        if (jobTitleRepository.existsByNameAndCompanyId(name, companyId)) {
            throw new BusinessException(ErrorCode.JOB_TITLE_ALREADY_EXISTS);
        }
    }

    /** Güncelleme işlemi için isim çakışması kontrolü yapar. */
    private void validateNameNotTakenForUpdate(String name, Long companyId, Long id) {
        if (jobTitleRepository.existsByNameAndCompanyIdAndIdNot(name, companyId, id)) {
            throw new BusinessException(ErrorCode.JOB_TITLE_ALREADY_EXISTS);
        }
    }

    /** İstekten gelen varsayılan rol listesini veritabanından güvenli bir şekilde yükler. */
    private Set<Role> loadRolesIfProvided(Set<Long> roleIds, Long companyId) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // JpaRepository'nin varsayılan "findAllById" metodunu kullanarak rolleri çekiyoruz
        java.util.List<Role> rolesList = roleRepository.findAllById(roleIds);

        // İstenen tüm rollerin veritabanında bulunduğundan emin oluyoruz
        if (rolesList.size() != roleIds.size()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        // Çekilen her rolün bizim companyId değerimize ait olup olmadığını (kiracı izolasyonunu) kontrol ediyoruz
        for (Role role : rolesList) {
            if (!role.getCompany().getId().equals(companyId)) {
                throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
            }
        }

        return new java.util.HashSet<>(rolesList);
    }

}

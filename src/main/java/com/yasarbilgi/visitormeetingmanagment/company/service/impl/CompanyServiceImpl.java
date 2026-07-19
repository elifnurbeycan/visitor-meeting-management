package com.yasarbilgi.visitormeetingmanagment.company.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.CompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.request.UpdateCompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.mapper.CompanyMapper;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.company.service.CompanyService;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
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
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    /**
     * Yeni bir şirket kaydı oluşturur.
     * Slug ve vergi numarasının benzersizliğini DB constraint'ine düşmeden
     * önce kontrol eder, anlamlı bir hata mesajı döner.
     * Oluşan şirket, entity varsayılanı gereği PENDING_APPROVAL durumunda başlar.
     */
    @Override
    @Transactional
    public CompanyResponseDto create(CompanyRequestDto dto) {
        log.info("Creating company with slug: {}", dto.slug());

        validateSlugNotTaken(dto.slug());
        validateTaxNumberNotTaken(dto.taxNumber());

        Company company = companyMapper.toEntity(dto);
        Company saved = companyRepository.save(company);

        log.info("Company created successfully with id: {}", saved.getId());
        return companyMapper.toResponseDto(saved);
    }

    /**
     * Var olan bir şirketin bilgilerini günceller.
     * Slug veya vergi numarası gerçekten değiştiyse tekrar benzersizlik kontrolü yapar;
     * değişmediyse gereksiz "zaten kullanılıyor" hatası vermez.
     * Her alan, entity'nin kendi business metodu üzerinden güncellenir (setter kullanılmaz).
     */
    @Override
    @Transactional
    public CompanyResponseDto update(Long id, UpdateCompanyRequestDto dto) {
        log.info("Updating company with id: {}", id);

        Company company = findCompanyOrThrow(id);

        if (!company.getSlug().equals(dto.slug())) {
            validateSlugNotTaken(dto.slug());
        }
        if (dto.taxNumber() != null && !dto.taxNumber().equals(company.getTaxNumber())) {
            validateTaxNumberNotTaken(dto.taxNumber());
        }

        company.rename(dto.name());
        company.changeSlug(dto.slug());
        company.updateContactInfo(dto.contactEmail(), dto.contactPhone(), dto.address());
        company.updateDescription(dto.description());
        company.updateTaxNumber(dto.taxNumber());
        company.updateIndustry(dto.industry());

        log.info("Company updated successfully with id: {}", id);
        return companyMapper.toResponseDto(company);
    }

    /**
     * ID'ye göre tekil bir şirket getirir.
     * Bulunamazsa COMPANY_NOT_FOUND fırlatır.
     */
    @Override
    public CompanyResponseDto getById(Long id) {
        log.debug("Fetching company with id: {}", id);
        Company company = findCompanyOrThrow(id);
        return companyMapper.toResponseDto(company);
    }

    /**
     * Slug'a göre tekil bir şirket getirir (örn. /companies/acme-corp gibi
     * okunabilir URL senaryoları için). Bulunamazsa COMPANY_NOT_FOUND fırlatır.
     */
    @Override
    public CompanyResponseDto getBySlug(String slug) {
        log.debug("Fetching company with slug: {}", slug);
        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    log.warn("Company not found with slug: {}", slug);
                    return new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
                });
        return companyMapper.toResponseDto(company);
    }

    /**
     * Tüm şirketleri, herhangi bir filtre uygulamadan, sayfalanmış şekilde getirir.
     */
    @Override
    public Page<CompanyResponseDto> getAll(Pageable pageable) {
        log.debug("Fetching all companies, page: {}", pageable);
        return companyRepository.findAll(pageable)
                .map(companyMapper::toResponseDto);
    }

    /**
     * Aktif/pasif durumuna göre filtrelenmiş şirketleri, sayfalanmış şekilde getirir.
     */
    @Override
    public Page<CompanyResponseDto> getAllByActive(boolean active, Pageable pageable) {
        log.debug("Fetching companies by active={}, page: {}", active, pageable);
        return companyRepository.findAllByActive(active, pageable)
                .map(companyMapper::toResponseDto);
    }

    /**
     * Onay durumuna (PENDING_APPROVAL / ACTIVE / REJECTED) göre filtrelenmiş
     * şirketleri, sayfalanmış şekilde getirir.
     */
    @Override
    public Page<CompanyResponseDto> getAllByStatus(CompanyStatus status, Pageable pageable) {
        log.debug("Fetching companies by status={}, page: {}", status, pageable);
        return companyRepository.findAllByStatus(status, pageable)
                .map(companyMapper::toResponseDto);
    }

    /**
     * İsim veya slug üzerinde, case-insensitive anahtar kelime araması yapar.
     * Sonuçlar ayrıca aktiflik durumuna göre de filtrelenir.
     */
    @Override
    public Page<CompanyResponseDto> search(boolean active, String keyword, Pageable pageable) {
        log.debug("Searching companies with keyword='{}', active={}", keyword, active);
        return companyRepository.searchByKeyword(active, keyword, pageable)
                .map(companyMapper::toResponseDto);
    }

    /**
     * SuperAdmin'in onayını bekleyen şirketleri, en eski başvuru önce gelecek
     * şekilde (FIFO), sayfalanmış olarak getirir.
     */
    @Override
    public Page<CompanyResponseDto> getPendingApprovals(Pageable pageable) {
        log.debug("Fetching pending approval companies, page: {}", pageable);
        return companyRepository.findAllByStatusOrderByCreatedAtAsc(CompanyStatus.PENDING_APPROVAL, pageable)
                .map(companyMapper::toResponseDto);
    }

    /**
     * Onay bekleyen toplam şirket sayısını döner
     * (SuperAdmin dashboard'unda sayaç göstermek için).
     */
    @Override
    public long countPendingApprovals() {
        return companyRepository.countByStatus(CompanyStatus.PENDING_APPROVAL);
    }

    /**
     * Onay bekleyen bir şirketi onaylar, durumunu ACTIVE'e çevirir.
     * Sadece PENDING_APPROVAL durumundaki şirketler onaylanabilir;
     * aksi halde entity kendi içinde COMPANY_NOT_PENDING_APPROVAL fırlatır.
     */
    @Override
    @Transactional
    public CompanyResponseDto approve(Long id) {
        log.info("Approving company with id: {}", id);

        Company company = findCompanyOrThrow(id);
        company.approve();

        log.info("Company approved successfully with id: {}", id);
        return companyMapper.toResponseDto(company);
    }

    /**
     * Onay bekleyen bir şirketi, belirtilen sebeple reddeder.
     * Sadece PENDING_APPROVAL durumundaki şirketler reddedilebilir.
     */
    @Override
    @Transactional
    public CompanyResponseDto reject(Long id, String reason) {
        log.info("Rejecting company with id: {}, reason: {}", id, reason);

        Company company = findCompanyOrThrow(id);
        company.reject(reason);

        log.info("Company rejected successfully with id: {}", id);
        return companyMapper.toResponseDto(company);
    }

    /**
     * Bir şirketi pasif hale getirir (soft-delete).
     * Kayıt veritabanından silinmez, sadece active=false yapılır.
     */
    @Override
    @Transactional
    public void deactivate(Long id) {
        log.info("Deactivating company with id: {}", id);
        Company company = findCompanyOrThrow(id);
        company.deactivate();
    }

    /**
     * Daha önce pasif hale getirilmiş bir şirketi tekrar aktif eder.
     */
    @Override
    @Transactional
    public void activate(Long id) {
        log.info("Activating company with id: {}", id);
        Company company = findCompanyOrThrow(id);
        company.activate();
    }

    // ----- Private helpers -----

    /**
     * ID'ye göre şirketi bulur, bulunamazsa COMPANY_NOT_FOUND fırlatır.
     * Tüm metodlarda tekrar eden "bul, yoksa hata fırlat" mantığını merkezileştirir.
     */
    private Company findCompanyOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Company not found with id: {}", id);
                    return new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
                });
    }

    /**
     * Verilen slug'ın başka bir şirket tarafından kullanılıp kullanılmadığını kontrol eder.
     * Kullanılıyorsa COMPANY_ALREADY_EXISTS fırlatır.
     */
    private void validateSlugNotTaken(String slug) {
        if (companyRepository.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_EXISTS);
        }
    }

    /**
     * Verilen vergi numarasının başka bir şirket tarafından kullanılıp
     * kullanılmadığını kontrol eder (null ise kontrol atlanır, çünkü tax
     * number opsiyoneldir). Kullanılıyorsa COMPANY_ALREADY_EXISTS fırlatır.
     */
    private void validateTaxNumberNotTaken(String taxNumber) {
        if (taxNumber != null && companyRepository.existsByTaxNumber(taxNumber)) {
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_EXISTS);
        }
    }
}
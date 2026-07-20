package com.yasarbilgi.visitormeetingmanagment.platform.service;

import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.dto.response.SuperAdminResponseDto;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SuperAdminService {

    // ----- SuperAdmin'in kendi yönetimi -----

    SuperAdminResponseDto getById(Long id);

    Page<SuperAdminResponseDto> getAll(Pageable pageable);

    Page<SuperAdminResponseDto> getAllByActive(boolean active, Pageable pageable);

    Page<SuperAdminResponseDto> search(String keyword, Pageable pageable);

    long countActive();

    /**
     * Bekleyen bir SuperAdmin kaydını onaylar (active=true yapar).
     * Sistemdeki tek onaylı-olmayan (active=false) admin'i kalıcı
     * durumdan çıkarır.
     */
    SuperAdminResponseDto approve(Long id);

    /**
     * Bir SuperAdmin'i pasif hale getirir. Sistemde en az 1 aktif
     * SuperAdmin kalması zorunlu olduğu için, son aktif admin
     * pasife alınamaz.
     */
    void deactivate(Long id);

    void activate(Long id);

    // ----- Şirket yönetimi (SuperAdmin'e özel geniş yetkiler) -----

    Page<CompanyResponseDto> getAllCompanies(Pageable pageable);

    Page<CompanyResponseDto> getPendingCompanies(Pageable pageable);

    CompanyResponseDto approveCompany(Long companyId);

    CompanyResponseDto rejectCompany(Long companyId, String reason);

    void deactivateCompany(Long companyId);

    void activateCompany(Long companyId);

    void hardDeleteCompany(Long companyId);

    // ----- Kullanıcı/Owner yönetimi (şirketler arası) -----

    UserResponseDto forceTransferCompanyOwnership(Long companyId, Long newOwnerId);

}
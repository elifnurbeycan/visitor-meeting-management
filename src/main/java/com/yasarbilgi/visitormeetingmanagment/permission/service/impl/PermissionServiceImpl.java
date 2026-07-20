package com.yasarbilgi.visitormeetingmanagment.permission.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.request.PermissionUpdateRequestDto;
import com.yasarbilgi.visitormeetingmanagment.permission.dto.response.PermissionResponseDto;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCategory;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import com.yasarbilgi.visitormeetingmanagment.permission.mapper.PermissionMapper;
import com.yasarbilgi.visitormeetingmanagment.permission.repository.PermissionRepository;
import com.yasarbilgi.visitormeetingmanagment.permission.service.PermissionService;
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
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    /**
     * Var olan bir permission'ın görünen adını ve açıklamasını günceller.
     * Permission oluşturma (create) bilinçli olarak yok: her permission'ın
     * `code`'u PermissionCode enum'undan gelir ve migration ile seed edilir,
     * API üzerinden keyfi yeni bir kod türetilmesine izin verilmez.
     * `code` ve `category` burada değiştirilemez, sadece metadata güncellenir.
     */
    @Override
    @Transactional
    public PermissionResponseDto update(Long id, PermissionUpdateRequestDto dto) {
        log.info("Updating permission with id: {}", id);

        Permission permission = findPermissionOrThrow(id);

        permission.rename(dto.name());
        permission.updateDescription(dto.description());

        log.info("Permission updated successfully with id: {}", id);
        return permissionMapper.toResponseDto(permission);
    }

    /**
     * ID'ye göre tekil bir permission getirir.
     * Bulunamazsa PERMISSION_NOT_FOUND fırlatır.
     */
    @Override
    public PermissionResponseDto getById(Long id) {
        log.debug("Fetching permission with id: {}", id);
        Permission permission = findPermissionOrThrow(id);
        return permissionMapper.toResponseDto(permission);
    }

    /**
     * Sabit PermissionCode değerine göre tekil bir permission getirir
     * (örn. yetkilendirme kontrolünde kod üzerinden arama senaryoları için).
     * Bulunamazsa PERMISSION_NOT_FOUND fırlatır.
     */
    @Override
    public PermissionResponseDto getByCode(PermissionCode code) {
        log.debug("Fetching permission with code: {}", code);
        Permission permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Permission not found with code: {}", code);
                    return new BusinessException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        return permissionMapper.toResponseDto(permission);
    }

    /**
     * Tüm permission'ları, herhangi bir filtre uygulamadan, sayfalanmış şekilde getirir.
     */
    @Override
    public Page<PermissionResponseDto> getAll(Pageable pageable) {
        log.debug("Fetching all permissions, page: {}", pageable);
        return permissionRepository.findAll(pageable)
                .map(permissionMapper::toResponseDto);
    }

    /**
     * Aktif/pasif durumuna göre filtrelenmiş permission'ları, sayfalanmış şekilde getirir.
     */
    @Override
    public Page<PermissionResponseDto> getAllByActive(boolean active, Pageable pageable) {
        log.debug("Fetching permissions by active={}, page: {}", active, pageable);
        return permissionRepository.findAllByActive(active, pageable)
                .map(permissionMapper::toResponseDto);
    }

    /**
     * Kategoriye (ROOM_MANAGEMENT, RESERVATION_MANAGEMENT vb.) göre filtrelenmiş
     * permission'ları, sayfalanmış şekilde getirir.
     */
    @Override
    public Page<PermissionResponseDto> getAllByCategory(PermissionCategory category, Pageable pageable) {
        log.debug("Fetching permissions by category={}, page: {}", category, pageable);
        return permissionRepository.findAllByCategory(category, pageable)
                .map(permissionMapper::toResponseDto);
    }

    /**
     * Hem aktiflik durumuna hem de kategoriye göre filtrelenmiş permission'ları getirir.
     */
    @Override
    public Page<PermissionResponseDto> getAllByActiveAndCategory(boolean active, PermissionCategory category, Pageable pageable) {
        log.debug("Fetching permissions by active={}, category={}, page: {}", active, category, pageable);
        return permissionRepository.findAllByActiveAndCategory(active, category, pageable)
                .map(permissionMapper::toResponseDto);
    }

    /**
     * İsim veya açıklama üzerinde, case-insensitive anahtar kelime araması yapar.
     * Sonuçlar ayrıca aktiflik durumuna göre de filtrelenir.
     */
    @Override
    public Page<PermissionResponseDto> search(boolean active, String keyword, Pageable pageable) {
        log.debug("Searching permissions with keyword='{}', active={}", keyword, active);
        return permissionRepository.searchByKeyword(active, keyword, pageable)
                .map(permissionMapper::toResponseDto);
    }

    /**
     * Bir kategorideki permission'ları, entity üzerindeki displayOrder alanına göre
     * sıralı şekilde getirir (yetki yönetimi ekranında gruplu/sıralı listelemek için).
     */
    @Override
    public Page<PermissionResponseDto> getAllByCategoryOrdered(PermissionCategory category, Pageable pageable) {
        log.debug("Fetching ordered permissions by category={}, page: {}", category, pageable);
        return permissionRepository.findAllByCategoryOrderByDisplayOrderAsc(category, pageable)
                .map(permissionMapper::toResponseDto);
    }

    /**
     * Bir kategorideki toplam permission sayısını döner.
     */
    @Override
    public long countByCategory(PermissionCategory category) {
        return permissionRepository.countByCategory(category);
    }

    /**
     * Sistem tanımlı ya da custom permission sayısını döner
     * (dashboard'da "X sistem, Y custom permission" gibi göstermek için).
     */
    @Override
    public long countBySystemPermission(boolean systemPermission) {
        return permissionRepository.countBySystemPermission(systemPermission);
    }

    /**
     * Bir permission'ı pasif hale getirir (soft-delete).
     * Sistem tanımlı (systemPermission=true) permission'lar pasife alınamaz,
     * entity kendi içinde SYSTEM_PERMISSION_CANNOT_BE_DEACTIVATED fırlatır.
     */
    @Override
    @Transactional
    public void deactivate(Long id) {
        log.info("Deactivating permission with id: {}", id);
        Permission permission = findPermissionOrThrow(id);
        permission.deactivateIfAllowed();
    }

    /**
     * Daha önce pasif hale getirilmiş bir permission'ı tekrar aktif eder.
     */
    @Override
    @Transactional
    public void activate(Long id) {
        log.info("Activating permission with id: {}", id);
        Permission permission = findPermissionOrThrow(id);
        permission.activate();
    }

    // ----- Private helpers -----

    /**
     * ID'ye göre permission'ı bulur, bulunamazsa PERMISSION_NOT_FOUND fırlatır.
     * Tüm metodlarda tekrar eden "bul, yoksa hata fırlat" mantığını merkezileştirir.
     */
    private Permission findPermissionOrThrow(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Permission not found with id: {}", id);
                    return new BusinessException(ErrorCode.PERMISSION_NOT_FOUND);
                });
    }
}

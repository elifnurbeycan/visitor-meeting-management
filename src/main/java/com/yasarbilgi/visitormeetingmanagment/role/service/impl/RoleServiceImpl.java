package com.yasarbilgi.visitormeetingmanagment.role.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.repository.PermissionRepository;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.CreateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.request.UpdateRoleRequestDto;
import com.yasarbilgi.visitormeetingmanagment.role.dto.response.RoleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.mapper.RoleMapper;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final CompanyRepository companyRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Belirtilen şirkete ait yeni bir rol oluşturur.
     *
     * Aynı şirket içerisinde aynı isimde başka bir rol bulunamaz.
     * DTO içerisinde permission ID'leri gönderilmişse ilgili izinler bulunarak
     * yeni role atanır.
     *
     * Kullanıcı tarafından oluşturulan roller sistem rolü değildir.
     */
    @Override
    @Transactional
    public RoleResponseDto create(Long companyId, CreateRoleRequestDto dto) {
        log.info(
                "Creating role with name: {} for company id: {}",
                dto.name(),
                companyId
        );

        Company company = findCompanyOrThrow(companyId);

        validateRoleNameNotTaken(
                companyId,
                dto.name()
        );

        Set<Permission> permissions =
                findPermissionsOrThrow(dto.permissionIds());

        Role role = Role.builder()
                .company(company)
                .name(dto.name())
                .description(dto.description())
                .systemRole(false)
                .permissions(new HashSet<>(permissions))
                .build();

        Role saved = roleRepository.save(role);

        log.info(
                "Role created successfully with id: {} for company id: {}",
                saved.getId(),
                companyId
        );

        return roleMapper.toResponseDto(saved);
    }

    /**
     * Belirtilen şirkete ait mevcut rolü günceller.
     *
     * Rol adı değişmişse aynı şirket içinde benzersizlik kontrolü yapılır.
     * Permission listesi DTO'dan gelen güncel değerlerle değiştirilir.
     */
    @Override
    @Transactional
    public RoleResponseDto update(
            Long companyId,
            Long id,
            UpdateRoleRequestDto dto
    ) {
        log.info(
                "Updating role with id: {} for company id: {}",
                id,
                companyId
        );

        Role role = findRoleOrThrow(
                companyId,
                id
        );

        if (!role.getName().equalsIgnoreCase(dto.name())) {
            validateRoleNameNotTakenForUpdate(
                    companyId,
                    dto.name(),
                    id
            );
        }

        Set<Permission> requestedPermissions =
                findPermissionsOrThrow(dto.permissionIds());

        role.rename(dto.name());
        role.updateDescription(dto.description());

        updateRolePermissions(
                role,
                requestedPermissions
        );

        log.info(
                "Role updated successfully with id: {} for company id: {}",
                id,
                companyId
        );

        return roleMapper.toResponseDto(role);
    }

    /**
     * ID ve şirket bilgisine göre tek bir rol getirir.
     *
     * Rol başka bir şirkete aitse veya bulunamazsa ROLE_NOT_FOUND fırlatılır.
     */
    @Override
    public RoleResponseDto getById(
            Long companyId,
            Long id
    ) {
        log.debug(
                "Fetching role with id: {} for company id: {}",
                id,
                companyId
        );

        Role role = findRoleOrThrow(
                companyId,
                id
        );

        return roleMapper.toResponseDto(role);
    }

    /**
     * Belirtilen şirkete ait bütün rolleri sayfalanmış şekilde getirir.
     */
    @Override
    public Page<RoleResponseDto> getAll(
            Long companyId,
            Pageable pageable
    ) {
        log.debug(
                "Fetching all roles for company id: {}, page: {}",
                companyId,
                pageable
        );

        return roleRepository
                .findAllByCompanyId(companyId, pageable)
                .map(roleMapper::toResponseDto);
    }

    /**
     * Belirtilen şirkete ait rolleri aktiflik durumuna göre filtreler.
     */
    @Override
    public Page<RoleResponseDto> getAllByActive(
            Long companyId,
            boolean active,
            Pageable pageable
    ) {
        log.debug(
                "Fetching roles for company id: {}, active: {}, page: {}",
                companyId,
                active,
                pageable
        );

        return roleRepository
                .findAllByCompanyIdAndActive(
                        companyId,
                        active,
                        pageable
                )
                .map(roleMapper::toResponseDto);
    }

    /**
     * Belirtilen şirkete ait sistem rollerini veya özel rolleri getirir.
     */
    @Override
    public Page<RoleResponseDto> getAllBySystemRole(
            Long companyId,
            boolean systemRole,
            Pageable pageable
    ) {
        log.debug(
                "Fetching roles for company id: {}, systemRole: {}, page: {}",
                companyId,
                systemRole,
                pageable
        );

        return roleRepository
                .findAllByCompanyIdAndSystemRole(
                        companyId,
                        systemRole,
                        pageable
                )
                .map(roleMapper::toResponseDto);
    }

    /**
     * Rol adı veya açıklaması üzerinde anahtar kelime araması yapar.
     * Sonuçlar şirket ve aktiflik durumuna göre filtrelenir.
     */
    @Override
    public Page<RoleResponseDto> search(
            Long companyId,
            boolean active,
            String keyword,
            Pageable pageable
    ) {
        log.debug(
                "Searching roles for company id: {}, keyword: '{}', active: {}",
                companyId,
                keyword,
                active
        );

        String normalizedKeyword =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return roleRepository
                .searchByKeyword(
                        companyId,
                        active,
                        normalizedKeyword,
                        pageable
                )
                .map(roleMapper::toResponseDto);
    }

    /**
     * Belirtilen permission'ı role ekler.
     *
     * Permission daha önceden role atanmışsa Set yapısı nedeniyle
     * aynı permission tekrar eklenmez.
     */
    @Override
    @Transactional
    public RoleResponseDto assignPermission(
            Long companyId,
            Long roleId,
            Long permissionId
    ) {
        log.info(
                "Assigning permission id: {} to role id: {} for company id: {}",
                permissionId,
                roleId,
                companyId
        );

        Role role = findRoleOrThrow(
                companyId,
                roleId
        );

        Permission permission =
                findPermissionOrThrow(permissionId);

        role.assignPermission(permission);

        log.info(
                "Permission id: {} assigned successfully to role id: {}",
                permissionId,
                roleId
        );

        return roleMapper.toResponseDto(role);
    }

    /**
     * Belirtilen permission'ı rolden kaldırır.
     */
    @Override
    @Transactional
    public RoleResponseDto revokePermission(
            Long companyId,
            Long roleId,
            Long permissionId
    ) {
        log.info(
                "Revoking permission id: {} from role id: {} for company id: {}",
                permissionId,
                roleId,
                companyId
        );

        Role role = findRoleOrThrow(
                companyId,
                roleId
        );

        Permission permission =
                findPermissionOrThrow(permissionId);

        role.revokePermission(permission);

        log.info(
                "Permission id: {} revoked successfully from role id: {}",
                permissionId,
                roleId
        );

        return roleMapper.toResponseDto(role);
    }

    /**
     * Bir rolü pasif hale getirir.
     *
     * Rol sistem rolüyse Role entity içerisindeki iş kuralı
     * ROLE_SYSTEM_ROLE_CANNOT_BE_DEACTIVATED hatasını fırlatır.
     */
    @Override
    @Transactional
    public void deactivate(
            Long companyId,
            Long id
    ) {
        log.info(
                "Deactivating role with id: {} for company id: {}",
                id,
                companyId
        );

        Role role = findRoleOrThrow(
                companyId,
                id
        );

        role.deactivateIfAllowed();

        log.info(
                "Role deactivated successfully with id: {}",
                id
        );
    }

    /**
     * Pasif durumdaki rolü tekrar aktif hale getirir.
     */
    @Override
    @Transactional
    public void activate(
            Long companyId,
            Long id
    ) {
        log.info(
                "Activating role with id: {} for company id: {}",
                id,
                companyId
        );

        Role role = findRoleOrThrow(
                companyId,
                id
        );

        role.activate();

        log.info(
                "Role activated successfully with id: {}",
                id
        );
    }

    // ----- Private helpers -----

    /**
     * ID ve company ID bilgisine göre rolü bulur.
     * Rol bulunamazsa ROLE_NOT_FOUND fırlatır.
     */
    private Role findRoleOrThrow(
            Long companyId,
            Long roleId
    ) {
        return roleRepository
                .findByIdAndCompanyId(
                        roleId,
                        companyId
                )
                .orElseThrow(() -> {
                    log.warn(
                            "Role not found with id: {} for company id: {}",
                            roleId,
                            companyId
                    );

                    return new BusinessException(
                            ErrorCode.ROLE_NOT_FOUND
                    );
                });
    }

    /**
     * Company ID'ye göre şirketi bulur.
     * Bulunamazsa COMPANY_NOT_FOUND fırlatır.
     */
    private Company findCompanyOrThrow(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(() -> {
                    log.warn(
                            "Company not found with id: {}",
                            companyId
                    );

                    return new BusinessException(
                            ErrorCode.COMPANY_NOT_FOUND
                    );
                });
    }

    /**
     * Permission ID'ye göre permission'ı bulur.
     * Bulunamazsa PERMISSION_NOT_FOUND fırlatır.
     */
    private Permission findPermissionOrThrow(Long permissionId) {
        return permissionRepository
                .findById(permissionId)
                .orElseThrow(() -> {
                    log.warn(
                            "Permission not found with id: {}",
                            permissionId
                    );

                    return new BusinessException(
                            ErrorCode.PERMISSION_NOT_FOUND
                    );
                });
    }

    /**
     * Permission ID kümesindeki bütün permission kayıtlarını getirir.
     *
     * ID listesi boşsa boş Set döndürür.
     * Gönderilen ID sayısı ile veritabanından bulunan permission sayısı
     * eşleşmiyorsa en az bir permission bulunamamış demektir.
     */
    private Set<Permission> findPermissionsOrThrow(
            Set<Long> permissionIds
    ) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Permission> permissions =
                new HashSet<>(
                        permissionRepository.findAllById(permissionIds)
                );

        if (permissions.size() != permissionIds.size()) {
            log.warn(
                    "One or more permissions could not be found. Requested ids: {}",
                    permissionIds
            );

            throw new BusinessException(
                    ErrorCode.PERMISSION_NOT_FOUND
            );
        }

        return permissions;
    }

    /**
     * Yeni rol oluşturulurken aynı şirkette aynı isimde rol bulunup
     * bulunmadığını kontrol eder.
     */
    private void validateRoleNameNotTaken(
            Long companyId,
            String name
    ) {
        if (roleRepository.existsByCompanyIdAndNameIgnoreCase(
                companyId,
                name
        )) {
            throw new BusinessException(
                    ErrorCode.ROLE_ALREADY_EXISTS
            );
        }
    }

    /**
     * Rol güncellenirken mevcut rol hariç aynı isimde başka bir rol
     * bulunup bulunmadığını kontrol eder.
     */
    private void validateRoleNameNotTakenForUpdate(
            Long companyId,
            String name,
            Long roleId
    ) {
        if (roleRepository
                .existsByCompanyIdAndNameIgnoreCaseAndIdNot(
                        companyId,
                        name,
                        roleId
                )) {
            throw new BusinessException(
                    ErrorCode.ROLE_ALREADY_EXISTS
            );
        }
    }

    /**
     * Rolün mevcut permission listesini DTO'dan gelen yeni permission
     * listesiyle senkronize eder.
     *
     * Artık gönderilmeyen permission'lar kaldırılır,
     * yeni gönderilen permission'lar eklenir.
     */
    private void updateRolePermissions(
            Role role,
            Set<Permission> requestedPermissions
    ) {
        Set<Permission> currentPermissions =
                new HashSet<>(role.getPermissions());

        currentPermissions.stream()
                .filter(permission ->
                        !requestedPermissions.contains(permission)
                )
                .forEach(role::revokePermission);

        requestedPermissions.stream()
                .filter(permission ->
                        !role.hasPermission(permission)
                )
                .forEach(role::assignPermission);
    }
}
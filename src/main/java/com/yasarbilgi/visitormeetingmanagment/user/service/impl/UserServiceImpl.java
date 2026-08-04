package com.yasarbilgi.visitormeetingmanagment.user.service.impl;

import com.yasarbilgi.visitormeetingmanagment.audit.service.AuditLogService;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.department.entity.Department;
import com.yasarbilgi.visitormeetingmanagment.department.repository.DepartmentRepository;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.job.repository.JobTitleRepository;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionResolutionService;
import com.yasarbilgi.visitormeetingmanagment.security.util.CurrentUserProvider;
import com.yasarbilgi.visitormeetingmanagment.user.dto.request.UserRequestDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserDirectoryResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import com.yasarbilgi.visitormeetingmanagment.user.mapper.UserMapper;
import com.yasarbilgi.visitormeetingmanagment.user.repository.UserRepository;
import com.yasarbilgi.visitormeetingmanagment.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JobTitleRepository jobTitleRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionResolutionService permissionResolutionService;
    private final CurrentUserProvider currentUserProvider;
    private final PermissionCacheService permissionCacheService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserResponseDto create(Long companyId, UserRequestDto dto) {
        log.info("Creating user with email: {} for company: {}", dto.email(), companyId);

        validateEmailNotTaken(companyId, dto.email());
        validateUsernameNotTaken(dto.username());

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> {
                    log.warn("Cannot create user: company not found with id: {}", companyId);
                    return new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
                });

        JobTitle jobTitle = resolveJobTitle(companyId, dto.jobTitleId());
        Department department = resolveDepartment(companyId, dto.departmentId());
        Set<Role> roles = resolveRoles(companyId, dto.roleIds());

        User user = User.builder()
                .company(company)
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .username(dto.username())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .jobTitle(jobTitle)
                .department(department)
                .build();
        roles.forEach(user::assignRole);

        if (jobTitle != null) {
            jobTitle.getDefaultRoles().forEach(user::assignRole);
        }

        User saved = userRepository.save(user);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "USER_CREATED",
                "USER",
                saved.getId(),
                "User '" + saved.getFullName() + "' created"
        );

        log.info("User created successfully with id: {}", saved.getId());
        return userMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserResponseDto update(Long companyId, Long userId, UserRequestDto dto) {
        log.info("Updating user with id: {} for company: {}", userId, companyId);

        User user = findUserOrThrow(companyId, userId);

        if (!user.getEmail().equals(dto.email())) {
            validateEmailNotTaken(companyId, dto.email());
        }
        if (!user.getUsername().equals(dto.username())) {
            validateUsernameNotTaken(dto.username());
        }

        user.updateName(dto.firstName(), dto.lastName());
        user.changeEmail(dto.email());
        user.changeUsername(dto.username());
        user.changePasswordHash(passwordEncoder.encode(dto.password()));

        if (dto.jobTitleId() != null) {
            JobTitle jobTitle = resolveJobTitle(companyId, dto.jobTitleId());
            user.changeJobTitle(jobTitle);
        }

        if (dto.departmentId() != null) {
            Department department = resolveDepartment(companyId, dto.departmentId());
            user.changeDepartment(department);
        }

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "USER_UPDATED",
                "USER",
                userId,
                "User '" + user.getFullName() + "' updated"
        );

        log.info("User updated successfully with id: {}", userId);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getById(Long companyId, Long userId) {
        log.debug("Fetching user with id: {} for company: {}", userId, companyId);
        User user = findUserOrThrow(companyId, userId);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getByEmail(Long companyId, String email) {
        log.debug("Fetching user with email: {} for company: {}", email, companyId);
        User user = userRepository.findByCompanyIdAndEmail(companyId, email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {} for company: {}", email, companyId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getOwner(Long companyId) {
        log.debug("Fetching owner for company: {}", companyId);
        User owner = userRepository.findByCompanyIdAndOwnerTrue(companyId)
                .orElseThrow(() -> {
                    log.warn("No owner found for company: {}", companyId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
        return userMapper.toResponseDto(owner);
    }

    @Override
    public Page<UserResponseDto> getAll(Long companyId, Pageable pageable) {
        log.debug("Fetching all users for company: {}, page: {}", companyId, pageable);
        return userRepository.findAllByCompanyId(companyId, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable) {
        log.debug("Fetching users by active={} for company: {}", active, companyId);
        return userRepository.findAllByCompanyIdAndActive(companyId, active, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserResponseDto> getAllByJobTitle(Long companyId, Long jobTitleId, Pageable pageable) {
        log.debug("Fetching users by jobTitleId={} for company: {}", jobTitleId, companyId);
        return userRepository.findAllByCompanyIdAndJobTitleId(companyId, jobTitleId, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserResponseDto> getAllByDepartment(Long companyId, Long departmentId, Pageable pageable) {
        log.debug("Fetching users by departmentId={} for company: {}", departmentId, companyId);
        return userRepository.findAllByCompanyIdAndDepartmentId(companyId, departmentId, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserResponseDto> getAllByRole(Long companyId, Long roleId, Pageable pageable) {
        log.debug("Fetching users by roleId={} for company: {}", roleId, companyId);
        return userRepository.findAllByCompanyIdAndRoleId(companyId, roleId, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable) {
        log.debug("Searching users with keyword='{}' for company: {}", keyword, companyId);
        return userRepository.searchByKeyword(companyId, active, keyword, pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    public Page<UserDirectoryResponseDto> searchDirectory(Long companyId, String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(companyId, true, keyword, pageable)
                .map(user -> UserDirectoryResponseDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build());
    }

    @Override
    @Transactional
    public void deactivate(Long companyId, Long userId) {
        log.info("Deactivating user with id: {} for company: {}", userId, companyId);
        enforceAdminHierarchy(companyId, userId);
        User user = findUserOrThrow(companyId, userId);
        user.deactivateIfAllowed();
        permissionCacheService.invalidate(userId);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "USER_DEACTIVATED",
                "USER",
                userId,
                "User '" + user.getFullName() + "' deactivated"
        );
    }

    @Override
    @Transactional
    public void activate(Long companyId, Long userId) {
        log.info("Activating user with id: {} for company: {}", userId, companyId);
        enforceAdminHierarchy(companyId, userId);
        User user = findUserOrThrow(companyId, userId);
        user.activate();
        permissionCacheService.invalidate(userId);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "USER_ACTIVATED",
                "USER",
                userId,
                "User '" + user.getFullName() + "' activated"
        );

    }

    @Override
    @Transactional
    public UserResponseDto assignRole(Long companyId, Long userId, Long roleId) {
        log.info("Assigning role: {} to user: {} in company: {}", roleId, userId, companyId);
        enforceAdminHierarchy(companyId, userId);
        User user = findUserOrThrow(companyId, userId);
        Role role = findRoleOrThrow(companyId, roleId);

        if (user.hasRole(role)) {
            log.warn("User {} already has role {}", userId, roleId);
            throw new BusinessException(ErrorCode.ROLE_ALREADY_ASSIGNED);
        }

        user.assignRole(role);
        log.info("Role assigned successfully");
        permissionCacheService.invalidate(userId);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "ROLE_ASSIGNED",
                "USER",
                userId,
                "Role '" + role.getName() + "' assigned to user '" + user.getFullName() + "'"
        );

        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto revokeRole(Long companyId, Long userId, Long roleId) {
        log.info("Revoking role: {} from user: {} in company: {}", roleId, userId, companyId);
        enforceAdminHierarchy(companyId, userId);
        User user = findUserOrThrow(companyId, userId);
        Role role = findRoleOrThrow(companyId, roleId);
        user.revokeRole(role);
        log.info("Role revoked successfully");
        permissionCacheService.invalidate(userId);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "ROLE_REVOKED",
                "USER",
                userId,
                "Role '" + role.getName() + "' revoked from user '" + user.getFullName() + "'"
        );

        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto changeJobTitle(Long companyId, Long userId, Long jobTitleId) {
        log.info("Changing job title to: {} for user: {} in company: {}", jobTitleId, userId, companyId);
        enforceAdminHierarchy(companyId, userId);
        User user = findUserOrThrow(companyId, userId);
        JobTitle jobTitle = resolveJobTitle(companyId, jobTitleId);
        user.changeJobTitle(jobTitle);

        jobTitle.getDefaultRoles().forEach(user::assignRole);
        permissionCacheService.invalidate(userId);

        log.info("Job title changed successfully");
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto promoteToOwner(Long companyId, Long userId) {
        log.info("Promoting user: {} to owner in company: {}", userId, companyId);

        if (userRepository.existsByCompanyIdAndOwnerTrue(companyId)) {
            log.warn("Company {} already has an owner, promotion rejected", companyId);
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_HAS_OWNER);
        }

        User user = findUserOrThrow(companyId, userId);
        if (!user.isActive()) {
            log.warn("User {} is not active, cannot be promoted to owner", userId);
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }
        user.promoteToOwner();

        permissionCacheService.invalidate(userId);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "USER_PROMOTED_TO_OWNER",
                "USER",
                userId,
                "User '" + user.getFullName() + "' promoted to owner"
        );

        log.info("User promoted to owner successfully");
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto transferOwnership(Long companyId, Long currentOwnerId, Long newOwnerId) {
        log.info("Transferring ownership from: {} to: {} in company: {}",
                currentOwnerId, newOwnerId, companyId);

        User currentOwner = findUserOrThrow(companyId, currentOwnerId);
        if (!currentOwner.isOwner()) {
            log.warn("User {} is not the current owner, transfer rejected", currentOwnerId);
            throw new BusinessException(ErrorCode.USER_NOT_OWNER);
        }

        User newOwner = findUserOrThrow(companyId, newOwnerId);
        if (!newOwner.isActive()) {
            log.warn("User {} is not active, cannot be promoted to owner", newOwnerId);
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        currentOwner.demoteFromOwner();
        newOwner.promoteToOwner();

        permissionCacheService.invalidate(currentOwnerId);
        permissionCacheService.invalidate(newOwnerId);

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "OWNERSHIP_TRANSFERRED",
                "USER",
                newOwnerId,
                "Ownership transferred from user '" + currentOwner.getFullName() + "' to user '" + newOwner.getFullName() + "'"
        );

        log.info("Ownership transferred successfully");
        return userMapper.toResponseDto(newOwner);
    }

    @Override
    @Transactional
    public UserResponseDto forceTransferOwnership(Long companyId, Long newOwnerId) {
        log.warn("FORCE ownership transfer to user: {} in company: {} (triggered by SuperAdmin)",
                newOwnerId, companyId);

        userRepository.findByCompanyIdAndOwnerTrue(companyId)
                .ifPresent(currentOwner -> {
                    currentOwner.demoteFromOwner();
                    log.warn("Previous owner {} demoted", currentOwner.getId());
                    permissionCacheService.invalidate(currentOwner.getId());
                });

        User newOwner = findUserOrThrow(companyId, newOwnerId);
        newOwner.promoteToOwner();
        permissionCacheService.invalidate(newOwnerId);

        auditLogService.log(
                companyId,
                null,
                "OWNERSHIP_FORCE_TRANSFERRED",
                "USER",
                newOwnerId,
                "SuperAdmin force-transferred ownership to user '" + newOwner.getFullName() + "'"
        );

        log.warn("User {} force-promoted to owner in company {}", newOwnerId, companyId);
        return userMapper.toResponseDto(newOwner);
    }

    @Override
    public long countUsers(Long companyId) {
        return userRepository.countByCompanyId(companyId);
    }

    @Override
    public long countActiveUsers(Long companyId) {
        return userRepository.countByCompanyIdAndActive(companyId, true);
    }

    @Override
    @Transactional
    public void forcePasswordReset(Long companyId, Long userId) {
        log.info("Forcing password reset for user: {} in company: {}", userId, companyId);
        enforceAdminHierarchy(companyId, userId);
        User user = findUserOrThrow(companyId, userId);
        user.forcePasswordChangeOnNextLogin();

        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "PASSWORD_RESET_FORCED",
                "USER",
                userId,
                "User forced to change password on next login"
        );

        log.info("Password reset forced successfully for user: {}", userId);
    }

    // ----- Private helpers -----

    private User findUserOrThrow(Long companyId, Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getCompany().getId().equals(companyId))
                .orElseThrow(() -> {
                    log.warn("User not found with id: {} in company: {}", userId, companyId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });
    }

    private Role findRoleOrThrow(Long companyId, Long roleId) {
        return roleRepository.findById(roleId)
                .filter(role -> role.getCompany().getId().equals(companyId))
                .orElseThrow(() -> {
                    log.warn("Role not found with id: {} in company: {}", roleId, companyId);
                    return new BusinessException(ErrorCode.ROLE_NOT_FOUND);
                });
    }

    private JobTitle resolveJobTitle(Long companyId, Long jobTitleId) {
        if (jobTitleId == null) {
            return null;
        }
        return jobTitleRepository.findById(jobTitleId)
                .filter(jobTitle -> jobTitle.getCompany().getId().equals(companyId))
                .orElseThrow(() -> {
                    log.warn("Job title not found with id: {} in company: {}", jobTitleId, companyId);
                    return new BusinessException(ErrorCode.JOB_TITLE_NOT_FOUND);
                });
    }

    private Department resolveDepartment(Long companyId, Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .filter(department -> department.getCompany().getId().equals(companyId))
                .orElseThrow(() -> {
                    log.warn("Department not found with id: {} in company: {}", departmentId, companyId);
                    return new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
                });
    }

    private Set<Role> resolveRoles(Long companyId, Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .filter(role -> role.getCompany().getId().equals(companyId))
                        .orElseThrow(() -> {
                            log.warn("Role not found with id: {} in company: {}", id, companyId);
                            return new BusinessException(ErrorCode.ROLE_NOT_FOUND);
                        }))
                .collect(Collectors.toSet());
    }

    private void validateEmailNotTaken(Long companyId, String email) {
        if (userRepository.existsByCompanyIdAndEmail(companyId, email)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }

    private void validateUsernameNotTaken(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
    }

    /**
     * "Adminler birbirine müdahale edemez" kuralını uygular.
     * Owner her zaman herkese müdahale edebilir. Admin (tüm izinlere sahip
     * ama owner olmayan) sıradan kullanıcılara müdahale edebilir, ama başka
     * bir admine (kendisi hariç) müdahale edemez.
     */
    private void enforceAdminHierarchy(Long companyId, Long targetUserId) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        Long actorUserId = currentUser.userId();

        if (actorUserId.equals(targetUserId)) {
            return;
        }

        User actor = findUserOrThrow(companyId, actorUserId);
        if (actor.isOwner()) {
            return;
        }

        boolean actorIsAdmin = permissionResolutionService.hasAllPermissions(actorUserId);
        if (!actorIsAdmin) {
            return;
        }

        boolean targetIsAdmin = permissionResolutionService.hasAllPermissions(targetUserId);
        if (targetIsAdmin) {
            log.warn("Admin {} attempted to modify another admin {} in company {}",
                    actorUserId, targetUserId, companyId);
            throw new BusinessException(ErrorCode.ADMIN_CANNOT_MODIFY_ANOTHER_ADMIN);
        }
    }

    @Override
    @Transactional
    public List<UserResponseDto> importUsers(Long companyId, MultipartFile file) {
        log.info("Excel dosyasından toplu kullanıcı içe aktarılıyor. Şirket ID: {}", companyId);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Yüklenen Excel dosyası boş olamaz.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Geçersiz dosya formatı. Sadece Excel (.xlsx, .xls) dosyaları yüklenebilir.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // Şirketin default "Çalışan" rolünü bul
        Role defaultRole = roleRepository.findByCompanyIdAndNameIgnoreCase(companyId, "Çalışan")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        List<User> usersToSave = new ArrayList<>();

        try (java.io.InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount <= 1) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Excel dosyasında veri bulunamadı.");
            }

            // Başlık satırını atlayıp 1. satırdan (ikinci satır) okumaya başlıyoruz
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Cell usernameCell = row.getCell(0);
                Cell emailCell = row.getCell(1);

                // Satır tamamen boşsa atla
                if (isCellEmpty(usernameCell) && isCellEmpty(emailCell)) {
                    continue;
                }

                String username = getCellValueAsString(usernameCell);
                String email = getCellValueAsString(emailCell);

                int rowNum = i + 1; // Hata mesajları için 1 tabanlı satır numarası

                if (username == null || username.isBlank()) {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, rowNum + ". satırda kullanıcı adı alanı zorunludur.");
                }
                if (email == null || email.isBlank()) {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, rowNum + ". satırda e-posta alanı zorunludur.");
                }

                username = username.trim();
                email = email.trim().toLowerCase();

                // E-posta formatı kontrolü
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, rowNum + ". satırdaki e-posta formatı geçersiz: " + email);
                }

                // Benzersizlik (Unique) doğrulamaları
                validateEmailNotTaken(companyId, email);
                validateUsernameNotTaken(username);

                // Varsayılan şifre olarak kullanıcının kendi kullanıcı adı şifreleniyor
                String defaultPasswordHash = passwordEncoder.encode(username);

                User user = User.builder()
                        .company(company)
                        .firstName("-") // Geçici placeholder
                        .lastName("-")  // Geçici placeholder
                        .email(email)
                        .username(username)
                        .passwordHash(defaultPasswordHash)
                        .mustChangePassword(true) // İlk girişte şifre değiştirme zorunlu
                        .build();

                user.assignRole(defaultRole);
                usersToSave.add(user);
            }

        } catch (java.io.IOException e) {
            log.error("Excel okuma sırasında I/O hatası oluştu: ", e);
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Excel dosyası okunamadı: " + e.getMessage());
        }

        if (usersToSave.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Excel dosyasında eklenecek geçerli veri bulunamadı.");
        }

        List<User> savedUsers = userRepository.saveAll(usersToSave);

        // Denetim Günlüğü (Audit Log) Kaydı
        auditLogService.log(
                companyId,
                currentUserProvider.getCurrentUser().map(AuthenticatedUser::userId).orElse(null),
                "USER_BULK_IMPORTED",
                "USER",
                null,
                savedUsers.size() + " adet kullanıcı Excel üzerinden toplu olarak içe aktarıldı."
        );

        return savedUsers.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK ||
               (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    return String.format("%d", (long) val);
                } else {
                    return String.format("%s", val);
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}
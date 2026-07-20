package com.yasarbilgi.visitormeetingmanagment.user.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.job.repository.JobTitleRepository;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.role.repository.RoleRepository;
import com.yasarbilgi.visitormeetingmanagment.user.dto.request.UserRequestDto;
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

/**
 * UserService'in gerçek implementasyonu.
 * Sınıf seviyesinde @Transactional(readOnly = true) tanımlı; yazma yapan
 * metodlar kendi üzerlerinde @Transactional ile bunu override eder.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JobTitleRepository jobTitleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Yeni bir kullanıcı oluşturur. Email'in şirket içinde benzersiz olduğunu
     * kontrol eder, şifreyi BCrypt ile hashler, verilen rol ID'lerini gerçek
     * Role nesnelerine çevirip atar. jobTitleId opsiyoneldir.
     */
    @Override
    @Transactional
    public UserResponseDto create(Long companyId, UserRequestDto dto) {
        log.info("Creating user with email: {} for company: {}", dto.email(), companyId);

        validateEmailNotTaken(companyId, dto.email());

        JobTitle jobTitle = resolveJobTitle(dto.jobTitleId());
        Set<Role> roles = resolveRoles(dto.roleIds());

        User user = User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .jobTitle(jobTitle)
                .build();

        roles.forEach(user::assignRole);

        User saved = userRepository.save(user);

        log.info("User created successfully with id: {}", saved.getId());
        return userMapper.toResponseDto(saved);
    }

    /**
     * Var olan bir kullanıcının bilgilerini günceller. Email değiştiyse
     * benzersizlik kontrolü tekrar yapılır. Şifre her zaman yeniden hashlenir
     * (dto'da yeni şifre gönderildiği varsayılır).
     */
    @Override
    @Transactional
    public UserResponseDto update(Long companyId, Long userId, UserRequestDto dto) {
        log.info("Updating user with id: {} for company: {}", userId, companyId);

        User user = findUserOrThrow(companyId, userId);

        if (!user.getEmail().equals(dto.email())) {
            validateEmailNotTaken(companyId, dto.email());
        }

        user.updateName(dto.firstName(), dto.lastName());
        user.changeEmail(dto.email());
        user.changePasswordHash(passwordEncoder.encode(dto.password()));

        if (dto.jobTitleId() != null) {
            JobTitle jobTitle = resolveJobTitle(dto.jobTitleId());
            user.changeJobTitle(jobTitle);
        }

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

    /**
     * Şirketin mevcut owner'ını getirir. Her şirkette en fazla 1 owner
     * olabileceği için (partial unique index ile DB'de garanti altında),
     * bu sorgu her zaman ya tek bir sonuç ya da hiç sonuç döner.
     */
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

    /**
     * SuperAdmin tarafından tetiklenen, zorla owner değişikliği. Mevcut
     * owner'ın rızası aranmaz. Şirkette bir owner varsa önce demote edilir,
     * yeni kullanıcı owner yapılır. Şirkette hiç owner yoksa direkt atanır.
     */
    @Override
    @Transactional
    public UserResponseDto forceTransferOwnership(Long companyId, Long newOwnerId) {
        log.warn("FORCE ownership transfer to user: {} in company: {} (triggered by SuperAdmin)",
                newOwnerId, companyId);

        userRepository.findByCompanyIdAndOwnerTrue(companyId)
                .ifPresent(currentOwner -> {
                    currentOwner.demoteFromOwner();
                    log.warn("Previous owner {} demoted", currentOwner.getId());
                });

        User newOwner = findUserOrThrow(companyId, newOwnerId);
        newOwner.promoteToOwner();

        log.warn("User {} force-promoted to owner in company {}", newOwnerId, companyId);
        return userMapper.toResponseDto(newOwner);
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

    /**
     * Bir kullanıcıyı pasif hale getirir. Owner ise entity kendi içinde
     * USER_OWNER_CANNOT_BE_DEACTIVATED fırlatır (deactivateIfAllowed).
     */
    @Override
    @Transactional
    public void deactivate(Long companyId, Long userId) {
        log.info("Deactivating user with id: {} for company: {}", userId, companyId);
        User user = findUserOrThrow(companyId, userId);
        user.deactivateIfAllowed();
    }

    @Override
    @Transactional
    public void activate(Long companyId, Long userId) {
        log.info("Activating user with id: {} for company: {}", userId, companyId);
        User user = findUserOrThrow(companyId, userId);
        user.activate();
    }

    /**
     * Bir kullanıcıya rol atar. Rolün, aynı şirkete ait olduğu doğrulanır
     * (başka şirketin rolü yanlışlıkla atanamaz).
     */
    @Override
    @Transactional
    public UserResponseDto assignRole(Long companyId, Long userId, Long roleId) {
        log.info("Assigning role: {} to user: {} in company: {}", roleId, userId, companyId);

        User user = findUserOrThrow(companyId, userId);
        Role role = findRoleOrThrow(companyId, roleId);

        user.assignRole(role);

        log.info("Role assigned successfully");
        return userMapper.toResponseDto(user);
    }

    /**
     * Bir kullanıcıdan rol geri alır. Owner ise entity kendi içinde
     * USER_OWNER_ROLE_MODIFICATION_FORBIDDEN fırlatır.
     */
    @Override
    @Transactional
    public UserResponseDto revokeRole(Long companyId, Long userId, Long roleId) {
        log.info("Revoking role: {} from user: {} in company: {}", roleId, userId, companyId);

        User user = findUserOrThrow(companyId, userId);
        Role role = findRoleOrThrow(companyId, roleId);

        user.revokeRole(role);

        log.info("Role revoked successfully");
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto changeJobTitle(Long companyId, Long userId, Long jobTitleId) {
        log.info("Changing job title to: {} for user: {} in company: {}", jobTitleId, userId, companyId);

        User user = findUserOrThrow(companyId, userId);
        JobTitle jobTitle = resolveJobTitle(jobTitleId);

        user.changeJobTitle(jobTitle);

        log.info("Job title changed successfully");
        return userMapper.toResponseDto(user);
    }

    /**
     * Bir kullanıcıyı owner yapar. Şirkette zaten bir owner varsa reddedilir
     * — "tek owner" kuralının servis katmanındaki güvenlik ağı (DB'de de
     * partial unique index ile garanti altında).
     */
    @Override
    @Transactional
    public UserResponseDto promoteToOwner(Long companyId, Long userId) {
        log.info("Promoting user: {} to owner in company: {}", userId, companyId);

        if (userRepository.existsByCompanyIdAndOwnerTrue(companyId)) {
            log.warn("Company {} already has an owner, promotion rejected", companyId);
            throw new BusinessException(ErrorCode.COMPANY_ALREADY_HAS_OWNER);
        }

        User user = findUserOrThrow(companyId, userId);
        user.promoteToOwner();

        log.info("User promoted to owner successfully");
        return userMapper.toResponseDto(user);
    }

    /**
     * Owner'lığı bir kullanıcıdan diğerine devreder. Sadece mevcut owner'ın
     * kendisi bu işlemi tetikleyebilir (currentOwnerId ile doğrulanır).
     * Eski owner'ın owner bayrağı kaldırılır, yeni kullanıcı owner yapılır.
     */
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

        currentOwner.demoteFromOwner();
        newOwner.promoteToOwner();

        log.info("Ownership transferred successfully");
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

    private JobTitle resolveJobTitle(Long jobTitleId) {
        if (jobTitleId == null) {
            return null;
        }
        return jobTitleRepository.findById(jobTitleId)
                .orElseThrow(() -> {
                    log.warn("Job title not found with id: {}", jobTitleId);
                    return new BusinessException(ErrorCode.JOB_TITLE_NOT_FOUND);
                });
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> {
                            log.warn("Role not found with id: {}", id);
                            return new BusinessException(ErrorCode.ROLE_NOT_FOUND);
                        }))
                .collect(Collectors.toSet());
    }

    private void validateEmailNotTaken(Long companyId, String email) {
        if (userRepository.existsByCompanyIdAndEmail(companyId, email)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }
}
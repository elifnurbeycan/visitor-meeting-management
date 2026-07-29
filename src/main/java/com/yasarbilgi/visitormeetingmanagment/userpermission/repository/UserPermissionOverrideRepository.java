package com.yasarbilgi.visitormeetingmanagment.userpermission.repository;

import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.UserPermissionOverride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {

    boolean existsByUserIdAndPermissionId(Long userId, Long permissionId);


    Page<UserPermissionOverride> findAllByActive(boolean active, Pageable pageable);

    Page<UserPermissionOverride> findAllByUserId(Long userId, Pageable pageable);

    Page<UserPermissionOverride> findAllByUserIdAndActive(Long userId, boolean active, Pageable pageable);

    List<UserPermissionOverride> findAllByUserIdAndActive(Long userId, boolean active);

    Optional<UserPermissionOverride> findByIdAndCompanyId(Long id, Long companyId);

    Page<UserPermissionOverride> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<UserPermissionOverride> findAllByCompanyIdAndActive(Long companyId, boolean active, Pageable pageable);

    Page<UserPermissionOverride> findAllByCompanyIdAndUserId(Long companyId, Long userId, Pageable pageable);

    Page<UserPermissionOverride> findAllByCompanyIdAndUserIdAndActive(Long companyId, Long userId, boolean active, Pageable pageable);

    Optional<UserPermissionOverride> findByCompanyIdAndUserIdAndPermissionId(Long companyId, Long userId, Long permissionId);


    @Query("""
        SELECT upo FROM UserPermissionOverride upo
        WHERE upo.company.id = :companyId
        AND upo.active = :active
        AND (:keyword IS NULL 
             OR LOWER(upo.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(upo.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(upo.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(upo.permission.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<UserPermissionOverride> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

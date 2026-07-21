package com.yasarbilgi.visitormeetingmanagment.user.repository;

import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCompanyIdAndEmail(Long companyId, String email);

    boolean existsByCompanyIdAndEmail(Long companyId, String email);

    Optional<User> findByCompanyIdAndOwnerTrue(Long companyId);

    boolean existsByCompanyIdAndOwnerTrue(Long companyId);

    Page<User> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<User> findAllByCompanyIdAndActive(Long companyId, boolean active, Pageable pageable);

    Page<User> findAllByCompanyIdAndJobTitleId(Long companyId, Long jobTitleId, Pageable pageable);

    Page<User> findAllByCompanyIdAndDepartmentId(Long companyId, Long departmentId, Pageable pageable);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Query("""
            SELECT u FROM User u
            WHERE u.company.id = :companyId
            AND u.active = :active
            AND (:keyword IS NULL
                 OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<User> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.company.id = :companyId
            AND r.id = :roleId
            """)
    Page<User> findAllByCompanyIdAndRoleId(
            @Param("companyId") Long companyId,
            @Param("roleId") Long roleId,
            Pageable pageable
    );


    @Query("""
            SELECT COUNT(DISTINCT u) FROM User u
            JOIN u.roles r
            JOIN r.permissions p
            WHERE u.company.id = :companyId
            AND u.active = true
            AND p.code = com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode.USER_MANAGE
            """)
    long countActiveUsersWithUserManagePermission(@Param("companyId") Long companyId);

    // ----- İstatistik -----

    long countByCompanyId(Long companyId);

    long countByCompanyIdAndActive(Long companyId, boolean active);

}

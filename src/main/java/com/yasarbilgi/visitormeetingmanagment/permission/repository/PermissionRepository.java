package com.yasarbilgi.visitormeetingmanagment.permission.repository;

import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCategory;
import com.yasarbilgi.visitormeetingmanagment.permission.enums.PermissionCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(PermissionCode code);

    boolean existsByCode(PermissionCode code);

    Page<Permission> findAllByActive(boolean active, Pageable pageable);

    Page<Permission> findAllByCategory(PermissionCategory category, Pageable pageable);

    Page<Permission> findAllByActiveAndCategory(boolean active, PermissionCategory category, Pageable pageable);

    @Query("""
            SELECT p FROM Permission p
            WHERE p.active = :active
            AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Permission> searchByKeyword(
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<Permission> findAllByCategoryOrderByDisplayOrderAsc(PermissionCategory category, Pageable pageable);

    long countByCategory(PermissionCategory category);

    long countBySystemPermission(boolean systemPermission);

}

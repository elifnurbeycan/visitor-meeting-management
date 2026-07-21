package com.yasarbilgi.visitormeetingmanagment.department.repository;

import com.yasarbilgi.visitormeetingmanagment.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCompanyIdAndId(Long companyId, Long id);

    boolean existsByCompanyIdAndName(Long companyId, String name);

    Page<Department> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<Department> findAllByCompanyIdAndActive(Long companyId, boolean active, Pageable pageable);

    @Query("""
            SELECT d FROM Department d
            WHERE d.company.id = :companyId
            AND d.active = :active
            AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Department> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
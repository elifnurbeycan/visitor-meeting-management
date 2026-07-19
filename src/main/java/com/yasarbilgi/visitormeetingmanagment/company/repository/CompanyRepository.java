package com.yasarbilgi.visitormeetingmanagment.company.repository;

import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByTaxNumber(String taxNumber);

    Page<Company> findAllByActive(boolean active, Pageable pageable);

    Page<Company> findAllByStatus(CompanyStatus status, Pageable pageable);

    Page<Company> findAllByActiveAndStatus(boolean active, CompanyStatus status, Pageable pageable);


    @Query("""
            SELECT c FROM Company c
            WHERE c.active = :active
            AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Company> searchByKeyword(
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<Company> findAllByStatusOrderByCreatedAtAsc(CompanyStatus status, Pageable pageable);

    long countByStatus(CompanyStatus status);

}

package com.yasarbilgi.visitormeetingmanagment.feature.repository;

import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, Long> {

    boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

    Optional<Feature> findByIdAndCompanyId(Long id, Long companyId);

    Page<Feature> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<Feature> findAllByCompanyIdAndActive(Long companyId, boolean active, Pageable pageable);

    @Query("""
            SELECT f FROM Feature f
            WHERE f.company.id = :companyId
            AND f.active = :active
            AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Feature> searchByCompanyIdAndKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByCompanyId(Long companyId);

}

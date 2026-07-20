package com.yasarbilgi.visitormeetingmanagment.platform.repository;

import com.yasarbilgi.visitormeetingmanagment.platform.entity.SuperAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {

    Optional<SuperAdmin> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<SuperAdmin> findAllByActive(boolean active, Pageable pageable);

    long countByActive(boolean active);

    @Query("""
            SELECT s FROM SuperAdmin s
            WHERE (:keyword IS NULL
                   OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<SuperAdmin> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
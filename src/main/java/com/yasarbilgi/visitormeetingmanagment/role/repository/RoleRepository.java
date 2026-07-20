package com.yasarbilgi.visitormeetingmanagment.role.repository;

import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Belirli bir şirkete ait rolü ID üzerinden getirir.
     * Tenant ayrımını korumak için companyId ile birlikte sorgulanır.
     */
    Optional<Role> findByIdAndCompanyId(Long id, Long companyId);

    /**
     * Belirli bir şirkette aynı isimde rol bulunup bulunmadığını kontrol eder.
     */
    boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

    /**
     * Güncelleme sırasında mevcut rol hariç aynı isimde başka rol
     * olup olmadığını kontrol eder.
     */
    boolean existsByCompanyIdAndNameIgnoreCaseAndIdNot(
            Long companyId,
            String name,
            Long id
    );

    /**
     * Belirli bir şirkete ait tüm rolleri sayfalanmış şekilde getirir.
     */
    Page<Role> findAllByCompanyId(Long companyId, Pageable pageable);

    /**
     * Belirli bir şirkete ait aktif veya pasif rolleri getirir.
     */
    Page<Role> findAllByCompanyIdAndActive(
            Long companyId,
            boolean active,
            Pageable pageable
    );

    /**
     * Belirli bir şirkete ait sistem rollerini veya özel rolleri getirir.
     */
    Page<Role> findAllByCompanyIdAndSystemRole(
            Long companyId,
            boolean systemRole,
            Pageable pageable
    );

    /**
     * Rol adı veya açıklaması üzerinde anahtar kelime araması yapar.
     */
    @Query("""
            SELECT r
            FROM Role r
            WHERE r.company.id = :companyId
              AND r.active = :active
              AND (
                    :keyword IS NULL
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(r.description, ''))
                       LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Role> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
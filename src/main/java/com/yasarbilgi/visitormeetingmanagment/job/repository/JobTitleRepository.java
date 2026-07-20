package com.yasarbilgi.visitormeetingmanagment.job.repository;

import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JobTitle (İş Unvanı) entity'si için veritabanı işlemlerini yürüten repository arayüzü.
 */
@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {

    /**
     * ID ve Şirket ID'sine göre tekil bir iş unvanı sorgular.
     * Bu metot çoklu kiracılık (tenant) izolasyonunun temelidir.
     */
    Optional<JobTitle> findByIdAndCompanyId(Long id, Long companyId);

    /**
     * Şirket içinde aynı isimde iş unvanı olup olmadığını kontrol eder.
     * Yeni kayıt oluştururken çakışmaları önlemek için kullanılır.
     */
    boolean existsByNameAndCompanyId(String name, Long companyId);

    /**
     * Güncelleme sırasında, kendi ID'si hariç aynı isimde başka bir kayıt olup olmadığını kontrol eder.
     */
    boolean existsByNameAndCompanyIdAndIdNot(String name, Long companyId, Long id);

    /**
     * Şirkete ait tüm iş unvanlarını sayfalanmış olarak listeler.
     */
    Page<JobTitle> findAllByCompanyId(Long companyId, Pageable pageable);

    /**
     * Şirkete ait aktif veya pasif iş unvanlarını sayfalanmış olarak listeler.
     */
    Page<JobTitle> findAllByCompanyIdAndActive(Long companyId, boolean active, Pageable pageable);

    /**
     * Şirket içinde iş unvanı ismi veya açıklamasında arama yapar (Case-insensitive).
     */
    @Query("""
            SELECT j FROM JobTitle j
            WHERE j.company.id = :companyId
            AND j.active = :active
            AND (:keyword IS NULL OR LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<JobTitle> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

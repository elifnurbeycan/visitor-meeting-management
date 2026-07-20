package com.yasarbilgi.visitormeetingmanagment.room.repository;

import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Belirli bir şirkete ait odayı ID üzerinden getirir.
     * Tenant ayrımını korumak için companyId ile birlikte sorgulanır.
     */
    Optional<Room> findByIdAndCompanyId(Long id, Long companyId);

    /**
     * Aynı şirket içerisinde aynı isimde başka bir oda olup olmadığını kontrol eder.
     */
    boolean existsByCompanyIdAndNameIgnoreCase(
            Long companyId,
            String name
    );

    /**
     * Güncelleme sırasında mevcut oda hariç aynı isimde başka oda
     * olup olmadığını kontrol eder.
     */
    boolean existsByCompanyIdAndNameIgnoreCaseAndIdNot(
            Long companyId,
            String name,
            Long id
    );

    /**
     * Belirli bir şirkete ait bütün odaları sayfalanmış şekilde getirir.
     */
    Page<Room> findAllByCompanyId(
            Long companyId,
            Pageable pageable
    );

    /**
     * Belirli bir şirkete ait aktif veya pasif odaları getirir.
     */
    Page<Room> findAllByCompanyIdAndActive(
            Long companyId,
            boolean active,
            Pageable pageable
    );

    /**
     * Belirli bir şirkette kapasitesi verilen değerden büyük veya eşit olan
     * aktif odaları listeler.
     */
    Page<Room> findAllByCompanyIdAndActiveAndCapacityGreaterThanEqual(
            Long companyId,
            boolean active,
            int capacity,
            Pageable pageable
    );

    /**
     * Oda adı, konumu veya açıklaması üzerinde anahtar kelime araması yapar.
     */
    @Query("""
            SELECT r
            FROM Room r
            WHERE r.company.id = :companyId
              AND r.active = :active
              AND (
                    :keyword IS NULL
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(r.location, ''))
                       LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(r.description, ''))
                       LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Room> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
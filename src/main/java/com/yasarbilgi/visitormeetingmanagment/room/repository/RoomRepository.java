package com.yasarbilgi.visitormeetingmanagment.room.repository;

import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Belirli bir şirkete ait odayı ID üzerinden getirir.
     * Tenant ayrımını korumak için companyId ile birlikte sorgulanır.
     */
    @Query("SELECT r FROM Room r WHERE r.id = :id AND r.company.id = :companyId AND r.archived = false")
    Optional<Room> findByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

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
    @Query("SELECT r FROM Room r WHERE r.company.id = :companyId AND r.archived = false")
    Page<Room> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * Belirli bir şirkete ait aktif veya pasif odaları getirir.
     */
    @Query("SELECT r FROM Room r WHERE r.company.id = :companyId AND r.active = :active AND r.archived = false")
    Page<Room> findAllByCompanyIdAndActive(@Param("companyId") Long companyId, @Param("active") boolean active, Pageable pageable);

    /**
     * Belirli bir şirkette kapasitesi verilen değerden büyük veya eşit olan
     * aktif odaları listeler.
     */
    @Query("SELECT r FROM Room r WHERE r.company.id = :companyId AND r.active = :active AND r.archived = false AND r.capacity >= :capacity")
    Page<Room> findAllByCompanyIdAndActiveAndCapacityGreaterThanEqual(@Param("companyId") Long companyId, @Param("active") boolean active, @Param("capacity") int capacity, Pageable pageable);

    /**
     * Oda adı, konumu veya açıklaması üzerinde anahtar kelime araması yapar.
     */
    @Query("""
            SELECT r
            FROM Room r
            WHERE r.company.id = :companyId
              AND r.active = :active
              AND r.archived = false
              AND (
                    :keyword IS NULL
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                    OR LOWER(COALESCE(r.location, ''))
                       LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                    OR LOWER(COALESCE(r.description, ''))
                       LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
              )
            """)
    Page<Room> searchByKeyword(
            @Param("companyId") Long companyId,
            @Param("active") boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT r FROM Room r JOIN r.features f WHERE f.id = :featureId AND r.company.id = :companyId AND r.archived = false")
    List<Room> findAllByFeatures_IdAndCompanyId(@Param("featureId") Long featureId, @Param("companyId") Long companyId);
}

package com.yasarbilgi.visitormeetingmanagment.reservation.repository;

import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection.CancellationByUserProjection;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection.RoomUsageProjection;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection.UserReservationStatsProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndCompanyId(
            Long id,
            Long companyId
    );

    Page<Reservation> findAllByCompanyId(
            Long companyId,
            Pageable pageable
    );

    Page<Reservation> findAllByRoomIdAndCompanyId(
            Long roomId,
            Long companyId,
            Pageable pageable
    );

    Page<Reservation> findAllByOrganizerIdAndCompanyId(
            Long organizerId,
            Long companyId,
            Pageable pageable
    );

    boolean existsByRoomIdAndCompanyIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Long companyId,
            List<ReservationStatus> statuses,
            LocalDateTime requestedEndTime,
            LocalDateTime requestedStartTime
    );

    boolean existsByRoomIdAndCompanyIdAndIdNotAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Long companyId,
            Long excludedReservationId,
            List<ReservationStatus> statuses,
            LocalDateTime requestedEndTime,
            LocalDateTime requestedStartTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.room.id = :roomId
        AND r.company.id = :companyId
        AND r.status = 'PENDING_APPROVAL'
        AND r.startTime < :endTime
        AND r.endTime > :startTime
        ORDER BY r.id
        """)
    List<Reservation> findPendingConflictsForUpdate(
            @Param("roomId") Long roomId,
            @Param("companyId") Long companyId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'PENDING_APPROVAL'
        AND r.approvalDeadline < :now
        ORDER BY r.id
        """)
    List<Reservation> findExpiredPendingForUpdate(@Param("now") Instant now);

    Page<Reservation> findAllByCompanyIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long companyId,
            LocalDateTime rangeEnd,
            LocalDateTime rangeStart,
            Pageable pageable
    );

    @Query("""
        SELECT r.room.id AS roomId,
               r.room.name AS roomName,
               COUNT(r) AS reservationCount,
               SUM(timestampdiff(MINUTE, r.startTime, r.endTime)) AS totalMinutesBooked
        FROM Reservation r
        WHERE (:companyId IS NULL OR r.company.id = :companyId)
        AND r.status IN :statuses
        AND r.startTime >= :from
        AND r.startTime < :to
        GROUP BY r.room.id, r.room.name
        ORDER BY COUNT(r) DESC
        """)
    List<RoomUsageProjection> findRoomUsage(
            @Param("companyId") Long companyId,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT r.organizer.id AS userId,
               r.organizer.firstName AS firstName,
               r.organizer.lastName AS lastName,
               COUNT(r) AS cancelledCount
        FROM Reservation r
        WHERE (:companyId IS NULL OR r.company.id = :companyId)
        AND r.status = 'CANCELLED'
        AND r.startTime >= :from
        AND r.startTime < :to
        GROUP BY r.organizer.id, r.organizer.firstName, r.organizer.lastName
        ORDER BY COUNT(r) DESC
        """)
    List<CancellationByUserProjection> findCancellationsByUser(
            @Param("companyId") Long companyId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );


    /**
     * Kullanıcı bazında TÜM rezervasyonların (durum fark etmeksizin) sayısını
     * hesaplar, ayrıca bunların kaçının "başarılı" (ACTIVE/COMPLETED) ve
     * kaçının "başarısız" (REJECTED/CANCELLED/EXPIRED) olduğunu tek sorguda
     * kırılım olarak döner. totalCount, successfulCount+unsuccessfulCount'tan
     * BÜYÜK olabilir — aradaki fark, hâlâ onay bekleyen (PENDING_APPROVAL)
     * rezervasyonlardır. En çok rezervasyon yapan kullanıcı en üstte olacak
     * şekilde sıralanır. companyId null ise tüm şirketler dahil edilir
     * (SuperAdmin için).
     */
    @Query("""
        SELECT r.organizer.id AS userId,
               r.organizer.firstName AS firstName,
               r.organizer.lastName AS lastName,
               COUNT(r) AS totalCount,
               SUM(CASE WHEN r.status IN ('ACTIVE', 'COMPLETED') THEN 1 ELSE 0 END) AS successfulCount,
               SUM(CASE WHEN r.status IN ('REJECTED', 'CANCELLED', 'EXPIRED') THEN 1 ELSE 0 END) AS unsuccessfulCount
        FROM Reservation r
        WHERE (:companyId IS NULL OR r.company.id = :companyId)
        AND r.startTime >= :from
        AND r.startTime < :to
        GROUP BY r.organizer.id, r.organizer.firstName, r.organizer.lastName
        ORDER BY COUNT(r) DESC
        """)
    List<UserReservationStatsProjection> findReservationStatsByUser(
            @Param("companyId") Long companyId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
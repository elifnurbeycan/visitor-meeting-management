package com.yasarbilgi.visitormeetingmanagment.reservation.repository;

import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
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

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

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

}
package com.yasarbilgi.visitormeetingmanagment.reservation.repository;

import com.yasarbilgi.visitormeetingmanagment.reservation.entity.Reservation;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndCompanyId(
            Long id,
            Long companyId
    );

    List<Reservation> findAllByCompanyId(
            Long companyId
    );

    List<Reservation> findAllByRoomIdAndCompanyId(
            Long roomId,
            Long companyId
    );

    List<Reservation> findAllByOrganizerIdAndCompanyId(
            Long organizerId,
            Long companyId
    );

    boolean existsByRoomIdAndCompanyIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Long companyId,
            ReservationStatus excludedStatus,
            LocalDateTime requestedEndTime,
            LocalDateTime requestedStartTime
    );
}
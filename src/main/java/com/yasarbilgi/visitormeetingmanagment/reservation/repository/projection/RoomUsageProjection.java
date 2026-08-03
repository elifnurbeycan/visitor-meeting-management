package com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection;

/**
 * ReservationRepository.findRoomUsage() sorgusunun GROUP BY sonucunu
 * eşlemek için kullanılan Spring Data projection'ı. Alan adları,
 * @Query içindeki "AS" takma adlarıyla birebir eşleşmeli.
 */
public interface RoomUsageProjection {

    Long getRoomId();

    String getRoomName();

    Long getReservationCount();

    Long getTotalMinutesBooked();

}
package com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection;

/**
 * ReservationRepository.findReservationStatsByUser() sorgusunun GROUP BY
 * sonucunu eşlemek için kullanılan Spring Data projection'ı. Alan adları,
 * @Query içindeki "AS" takma adlarıyla birebir eşleşmeli.
 */
public interface UserReservationStatsProjection {

    Long getUserId();

    String getFirstName();

    String getLastName();

    Long getTotalCount();

    Long getSuccessfulCount();

    Long getUnsuccessfulCount();

}
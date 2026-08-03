package com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection;

/**
 * ReservationRepository.findCancellationsByUser() sorgusunun GROUP BY
 * sonucunu eşlemek için kullanılan Spring Data projection'ı. Alan adları,
 * @Query içindeki "AS" takma adlarıyla birebir eşleşmeli.
 */
public interface CancellationByUserProjection {

    Long getUserId();

    String getFirstName();

    String getLastName();

    Long getCancelledCount();

}
package com.yasarbilgi.visitormeetingmanagment.report.dto.response;

import lombok.Builder;

/**
 * Bir kullanıcının belirli bir tarih aralığındaki (ya da tüm zamanların)
 * rezervasyon talep özeti. totalCount tüm durumları kapsar; successfulCount
 * (ACTIVE/COMPLETED) ve unsuccessfulCount (REJECTED/CANCELLED/EXPIRED) bunun
 * bir kırılımıdır. totalCount, successfulCount+unsuccessfulCount'tan büyük
 * olabilir — aradaki fark hâlâ onay bekleyen (PENDING_APPROVAL) rezervasyonlardır.
 */
@Builder
public record UserReservationStatsDto(
        Long userId,
        String userName,
        long totalCount,
        long successfulCount,
        long unsuccessfulCount
) {
}
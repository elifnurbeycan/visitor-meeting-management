package com.yasarbilgi.visitormeetingmanagment.report.dto.response;

import lombok.Builder;

/**
 * Bir odanın belirli bir tarih aralığındaki (ya da tüm zamanların) kullanım
 * özeti. Sadece ACTIVE ve COMPLETED durumundaki rezervasyonlar sayılır —
 * yani gerçekten gerçekleşmiş/geçerli kullanım. Reddedilen, iptal edilen
 * veya süresi dolan rezervasyonlar bu sayıma dahil edilmez.
 */
@Builder
public record RoomUsageReportDto(
        Long roomId,
        String roomName,
        long reservationCount,
        double totalHoursBooked
) {
}
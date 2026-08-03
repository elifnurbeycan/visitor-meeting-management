package com.yasarbilgi.visitormeetingmanagment.report.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * İptal edilen (status = CANCELLED) rezervasyonların özeti: toplam sayı
 * ve kullanıcı bazlı kırılım — hangi kullanıcı kaç rezervasyon iptal etmiş.
 */
@Builder
public record CancellationReportDto(
        long totalCancelled,
        List<UserCancellationDto> byUser
) {

    @Builder
    public record UserCancellationDto(
            Long userId,
            String userName,
            long cancelledCount
    ) {
    }
}
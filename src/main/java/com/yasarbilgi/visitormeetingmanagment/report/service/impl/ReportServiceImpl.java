package com.yasarbilgi.visitormeetingmanagment.report.service.impl;

import com.yasarbilgi.visitormeetingmanagment.report.dto.response.CancellationReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.RoomUsageReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.service.ReportService;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.ReservationRepository;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection.CancellationByUserProjection;
import com.yasarbilgi.visitormeetingmanagment.reservation.repository.projection.RoomUsageProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rezervasyon verilerinden özetlenmiş (agregasyon içeren) raporlar üretir.
 * AuditLog export'undan farkı: burada ham kayıt dökümü değil, sayı/toplam/
 * sıralama gibi hesaplanmış istatistikler dönülür.

 * "ForCompany" ve "ForSuperAdmin" metodları kasıtlı olarak ayrı tutuldu
 * (AuditLogServiceImpl ile aynı desen) — ikisi de aynı null-safe repository
 * sorgusuna dayanıyor, ama ayrı isimlendirme hem loglamada hem çağıran
 * controller'da niyeti netleştiriyor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final List<ReservationStatus> USAGE_STATUSES =
            List.of(ReservationStatus.ACTIVE, ReservationStatus.COMPLETED);

    private final ReservationRepository reservationRepository;

    private static final LocalDateTime DISTANT_PAST = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime DISTANT_FUTURE = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    @Override
    public List<RoomUsageReportDto> getRoomUsageForCompany(Long companyId, LocalDate from, LocalDate to) {
        log.info("Generating room usage report for company: {}", companyId);
        return buildRoomUsageReport(companyId, from, to);
    }

    @Override
    public List<RoomUsageReportDto> getRoomUsageForSuperAdmin(Long companyId, LocalDate from, LocalDate to) {
        log.info("SuperAdmin generating room usage report. companyId: {}", companyId);
        return buildRoomUsageReport(companyId, from, to);
    }

    @Override
    public CancellationReportDto getCancellationReportForCompany(Long companyId, LocalDate from, LocalDate to) {
        log.info("Generating cancellation report for company: {}", companyId);
        return buildCancellationReport(companyId, from, to);
    }

    @Override
    public CancellationReportDto getCancellationReportForSuperAdmin(Long companyId, LocalDate from, LocalDate to) {
        log.info("SuperAdmin generating cancellation report. companyId: {}", companyId);
        return buildCancellationReport(companyId, from, to);
    }

    private List<RoomUsageReportDto> buildRoomUsageReport(Long companyId, LocalDate from, LocalDate to) {
        LocalDateTime rangeStart = toStartOfDay(from);
        LocalDateTime rangeEnd = toExclusiveEndOfDay(to);

        List<RoomUsageProjection> rows =
                reservationRepository.findRoomUsage(companyId, USAGE_STATUSES, rangeStart, rangeEnd);

        return rows.stream()
                .map(row -> RoomUsageReportDto.builder()
                        .roomId(row.getRoomId())
                        .roomName(row.getRoomName())
                        .reservationCount(row.getReservationCount())
                        .totalHoursBooked(minutesToHours(row.getTotalMinutesBooked()))
                        .build())
                .toList();
    }

    private CancellationReportDto buildCancellationReport(Long companyId, LocalDate from, LocalDate to) {
        LocalDateTime rangeStart = toStartOfDay(from);
        LocalDateTime rangeEnd = toExclusiveEndOfDay(to);

        List<CancellationByUserProjection> rows =
                reservationRepository.findCancellationsByUser(companyId, rangeStart, rangeEnd);

        List<CancellationReportDto.UserCancellationDto> byUser = rows.stream()
                .map(row -> CancellationReportDto.UserCancellationDto.builder()
                        .userId(row.getUserId())
                        .userName(row.getFirstName() + " " + row.getLastName())
                        .cancelledCount(row.getCancelledCount())
                        .build())
                .toList();

        long total = byUser.stream()
                .mapToLong(CancellationReportDto.UserCancellationDto::cancelledCount)
                .sum();

        return CancellationReportDto.builder()
                .totalCancelled(total)
                .byUser(byUser)
                .build();
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : DISTANT_PAST;
    }

    private LocalDateTime toExclusiveEndOfDay(LocalDate date) {
        return date != null ? date.plusDays(1).atStartOfDay() : DISTANT_FUTURE;
    }

    private double minutesToHours(Long totalMinutes) {
        if (totalMinutes == null) {
            return 0.0;
        }
        return Math.round((totalMinutes / 60.0) * 100.0) / 100.0;
    }
}
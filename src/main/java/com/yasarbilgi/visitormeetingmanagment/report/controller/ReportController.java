package com.yasarbilgi.visitormeetingmanagment.report.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.CancellationReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.RoomUsageReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.UserReservationStatsDto;
import com.yasarbilgi.visitormeetingmanagment.report.service.ReportExportService;
import com.yasarbilgi.visitormeetingmanagment.report.service.ReportService;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Şirket içi raporlama endpoint'leri.
 * URL şeması: /api/v1/reports
 * companyId her zaman JWT'den gelir (AuditLogController ile aynı desen) —
 * bir şirketin kullanıcısı sadece kendi şirketinin raporlarını görebilir.
 * Şirketler arası raporlar için bkz. SuperAdminController.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    @PreAuthorize("hasAuthority('REPORT_VIEW_ROOM_USAGE')")
    @GetMapping("/room-usage")
    public ResponseEntity<ApiResponse<List<RoomUsageReportDto>>> getRoomUsage(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<RoomUsageReportDto> report =
                reportService.getRoomUsageForCompany(currentUser.companyId(), from, to);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PreAuthorize("hasAuthority('REPORT_EXPORT_EXCEL')")
    @GetMapping("/room-usage/export")
    public ResponseEntity<byte[]> exportRoomUsage(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] excelBytes =
                reportExportService.exportRoomUsageForCompany(currentUser.companyId(), from, to);

        String filename = "room-usage-" + currentUser.companyId() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    @PreAuthorize("hasAuthority('REPORT_VIEW_CANCELLATION_STATS')")
    @GetMapping("/cancellations")
    public ResponseEntity<ApiResponse<CancellationReportDto>> getCancellations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        CancellationReportDto report =
                reportService.getCancellationReportForCompany(currentUser.companyId(), from, to);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PreAuthorize("hasAuthority('REPORT_EXPORT_EXCEL')")
    @GetMapping("/cancellations/export")
    public ResponseEntity<byte[]> exportCancellations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] excelBytes =
                reportExportService.exportCancellationsForCompany(currentUser.companyId(), from, to);

        String filename = "cancellations-" + currentUser.companyId() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    @PreAuthorize("hasAuthority('REPORT_VIEW_RESERVATION_STATS')")
    @GetMapping("/user-reservation-stats")
    public ResponseEntity<ApiResponse<List<UserReservationStatsDto>>> getUserReservationStats(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<UserReservationStatsDto> stats =
                reportService.getUserReservationStatsForCompany(currentUser.companyId(), from, to);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PreAuthorize("hasAuthority('REPORT_EXPORT_EXCEL')")
    @GetMapping("/user-reservation-stats/export")
    public ResponseEntity<byte[]> exportUserReservationStats(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] excelBytes =
                reportExportService.exportUserReservationStatsForCompany(currentUser.companyId(), from, to);

        String filename = "user-reservation-stats-" + currentUser.companyId() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}
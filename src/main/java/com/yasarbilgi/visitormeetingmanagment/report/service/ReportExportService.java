package com.yasarbilgi.visitormeetingmanagment.report.service;

import java.time.LocalDate;

public interface ReportExportService {

    byte[] exportRoomUsageForCompany(Long companyId, LocalDate from, LocalDate to);

    byte[] exportRoomUsageForSuperAdmin(Long companyId, LocalDate from, LocalDate to);

    byte[] exportCancellationsForCompany(Long companyId, LocalDate from, LocalDate to);

    byte[] exportCancellationsForSuperAdmin(Long companyId, LocalDate from, LocalDate to);

    byte[] exportUserReservationStatsForCompany(Long companyId, LocalDate from, LocalDate to);

    byte[] exportUserReservationStatsForSuperAdmin(Long companyId, LocalDate from, LocalDate to);
}
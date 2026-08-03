package com.yasarbilgi.visitormeetingmanagment.report.service;

import com.yasarbilgi.visitormeetingmanagment.report.dto.response.CancellationReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.RoomUsageReportDto;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<RoomUsageReportDto> getRoomUsageForCompany(Long companyId, LocalDate from, LocalDate to);

    List<RoomUsageReportDto> getRoomUsageForSuperAdmin(Long companyId, LocalDate from, LocalDate to);

    CancellationReportDto getCancellationReportForCompany(Long companyId, LocalDate from, LocalDate to);

    CancellationReportDto getCancellationReportForSuperAdmin(Long companyId, LocalDate from, LocalDate to);
}
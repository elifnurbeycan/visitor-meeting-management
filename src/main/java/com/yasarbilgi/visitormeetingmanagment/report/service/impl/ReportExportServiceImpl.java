package com.yasarbilgi.visitormeetingmanagment.report.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.CancellationReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.dto.response.RoomUsageReportDto;
import com.yasarbilgi.visitormeetingmanagment.report.service.ReportExportService;
import com.yasarbilgi.visitormeetingmanagment.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Rapor DTO'larını Excel'e dönüştürür. Veri çekme mantığı burada tekrar
 * yazılmıyor — ReportService'in zaten üretmiş olduğu DTO'lar kullanılıyor,
 * bu sınıf sadece "DTO -> Excel" dönüşümüyle ilgileniyor. AuditLogExportServiceImpl
 * ile aynı POI (Apache Excel kütüphanesi) kullanım stiline sadık kalındı.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {

    private static final String[] ROOM_USAGE_HEADERS = {
            "Oda ID", "Oda Adı", "Rezervasyon Sayısı", "Toplam Saat"
    };

    private static final String[] CANCELLATION_HEADERS = {
            "Kullanıcı ID", "Kullanıcı Adı", "İptal Sayısı"
    };

    private final ReportService reportService;

    @Override
    public byte[] exportRoomUsageForCompany(Long companyId, LocalDate from, LocalDate to) {
        log.info("Exporting room usage report for company: {}", companyId);
        List<RoomUsageReportDto> report = reportService.getRoomUsageForCompany(companyId, from, to);
        return buildRoomUsageExcel(report);
    }

    @Override
    public byte[] exportRoomUsageForSuperAdmin(Long companyId, LocalDate from, LocalDate to) {
        log.info("SuperAdmin exporting room usage report. companyId: {}", companyId);
        List<RoomUsageReportDto> report = reportService.getRoomUsageForSuperAdmin(companyId, from, to);
        return buildRoomUsageExcel(report);
    }

    @Override
    public byte[] exportCancellationsForCompany(Long companyId, LocalDate from, LocalDate to) {
        log.info("Exporting cancellation report for company: {}", companyId);
        CancellationReportDto report = reportService.getCancellationReportForCompany(companyId, from, to);
        return buildCancellationExcel(report);
    }

    @Override
    public byte[] exportCancellationsForSuperAdmin(Long companyId, LocalDate from, LocalDate to) {
        log.info("SuperAdmin exporting cancellation report. companyId: {}", companyId);
        CancellationReportDto report = reportService.getCancellationReportForSuperAdmin(companyId, from, to);
        return buildCancellationExcel(report);
    }

    private byte[] buildRoomUsageExcel(List<RoomUsageReportDto> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Oda Kullanimi");

            CellStyle headerStyle = createHeaderStyle(workbook);
            writeHeader(sheet, headerStyle, ROOM_USAGE_HEADERS, 0);

            int rowIndex = 1;
            for (RoomUsageReportDto row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.roomId());
                excelRow.createCell(1).setCellValue(row.roomName());
                excelRow.createCell(2).setCellValue(row.reservationCount());
                excelRow.createCell(3).setCellValue(row.totalHoursBooked());
            }

            autoSizeColumns(sheet, ROOM_USAGE_HEADERS.length);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate room usage Excel export", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private byte[] buildCancellationExcel(CancellationReportDto report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Iptal Istatistikleri");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row totalRow = sheet.createRow(0);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Toplam Iptal:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalValueCell = totalRow.createCell(1);
            totalValueCell.setCellValue(report.totalCancelled());
            totalValueCell.setCellStyle(headerStyle);

            writeHeader(sheet, headerStyle, CANCELLATION_HEADERS, 2);

            int rowIndex = 3;
            for (CancellationReportDto.UserCancellationDto row : report.byUser()) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.userId());
                excelRow.createCell(1).setCellValue(row.userName());
                excelRow.createCell(2).setCellValue(row.cancelledCount());
            }

            autoSizeColumns(sheet, CANCELLATION_HEADERS.length);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate cancellation Excel export", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        return headerStyle;
    }

    private void writeHeader(Sheet sheet, CellStyle headerStyle, String[] headers, int rowIndex) {
        Row headerRow = sheet.createRow(rowIndex);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
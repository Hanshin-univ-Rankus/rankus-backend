package org.univ.rankus.adapter.out.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.ExcelExportPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Apache POI를 사용한 Excel 내보내기 어댑터
 * <p>
 * ExcelExportPort의 구현체로서, Apache POI 라이브러리를 사용하여
 * 출석 데이터를 다양한 형식의 Excel 파일로 내보냅니다.
 *
 * @since 1.0
 */
@Slf4j
@Component
public class ApachePOIExcelExporter implements ExcelExportPort {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 출석 세션 데이터를 Excel 파일로 내보냅니다.
     */
    @Override
    public byte[] exportAttendanceToExcel(List<AttendanceSession> sessions, List<AttendanceRecord> records, String fileName) {
        validateExportAttendanceInputs(sessions, records, fileName);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            log.debug("출석 데이터 Excel 내보내기 시작 - 세션 수: {}, 기록 수: {}", sessions.size(), records.size());

            // 세션별 출석 기록 그룹화
            Map<Long, List<AttendanceRecord>> recordsBySession = records.stream()
                    .collect(Collectors.groupingBy(record -> record.getAttendanceSession().getSessionId()));

            // 각 세션별로 시트 생성
            for (AttendanceSession session : sessions) {
                createSessionSheet(workbook, session, recordsBySession.get(session.getSessionId()));
            }

            // 전체 요약 시트 생성
            createSummarySheet(workbook, sessions, records);

            workbook.write(outputStream);

            byte[] result = outputStream.toByteArray();
            log.debug("출석 데이터 Excel 내보내기 완료 - 파일 크기: {} bytes", result.length);
            return result;

        } catch (IOException e) {
            log.error("출석 데이터 Excel 내보내기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Excel 파일 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 랩의 출석 통계를 Excel 파일로 내보냅니다.
     */
    @Override
    public byte[] exportLabAttendanceStatistics(Long labId, LocalDate fromDate, LocalDate toDate, List<AttendanceRecord> records) {
        validateLabStatisticsInputs(labId, fromDate, toDate, records);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            log.debug("랩 출석 통계 Excel 내보내기 시작 - 랩 ID: {}, 기간: {} ~ {}", labId, fromDate, toDate);

            // 통계 시트 생성
            createStatisticsSheet(workbook, labId, fromDate, toDate, records);

            workbook.write(outputStream);

            byte[] result = outputStream.toByteArray();
            log.debug("랩 출석 통계 Excel 내보내기 완료 - 파일 크기: {} bytes", result.length);
            return result;

        } catch (IOException e) {
            log.error("랩 출석 통계 Excel 내보내기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Excel 파일 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 출석 기록을 Excel 파일로 내보냅니다.
     */
    @Override
    public byte[] exportUserAttendanceHistory(Long userId, String userName, List<AttendanceRecord> records) {
        validateUserHistoryInputs(userId, userName, records);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            log.debug("사용자 출석 기록 Excel 내보내기 시작 - 사용자: {} ({})", userName, userId);

            // 출석 기록 시트 생성
            createUserHistorySheet(workbook, userId, userName, records);

            workbook.write(outputStream);

            byte[] result = outputStream.toByteArray();
            log.debug("사용자 출석 기록 Excel 내보내기 완료 - 파일 크기: {} bytes", result.length);
            return result;

        } catch (IOException e) {
            log.error("사용자 출석 기록 Excel 내보내기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Excel 파일 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 출석 증명서를 Excel 형식으로 생성합니다.
     */
    @Override
    public byte[] generateAttendanceCertificate(
            Long userId, String userName, String labName,
            LocalDate fromDate, LocalDate toDate,
            double attendanceRate, int totalSessions, int attendedSessions) {

        validateCertificateInputs(userId, userName, labName, fromDate, toDate, attendanceRate, totalSessions, attendedSessions);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            log.debug("출석 증명서 생성 시작 - 사용자: {} ({}), 랩: {}", userName, userId, labName);

            // 증명서 시트 생성
            createCertificateSheet(workbook, userId, userName, labName, fromDate, toDate,
                    attendanceRate, totalSessions, attendedSessions);

            workbook.write(outputStream);

            byte[] result = outputStream.toByteArray();
            log.debug("출석 증명서 생성 완료 - 파일 크기: {} bytes", result.length);
            return result;

        } catch (IOException e) {
            log.error("출석 증명서 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("출석 증명서 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 세션별 출석 기록 시트를 생성합니다.
     */
    private void createSessionSheet(Workbook workbook, AttendanceSession session, List<AttendanceRecord> records) {
        String sheetName = sanitizeSheetName(session.getTitle());
        Sheet sheet = workbook.createSheet(sheetName);

        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // 제목 행
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("출석 세션: " + session.getTitle());
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 세션 정보
        rowNum++; // 빈 행
        createInfoRow(sheet, rowNum++, "세션 날짜:", session.getStartTime().format(DATETIME_FORMATTER), dataStyle);
        createInfoRow(sheet, rowNum++, "유효 시간:", session.getQrValidityMinutes() + "분", dataStyle);
        createInfoRow(sheet, rowNum++, "총 참석자:", String.valueOf(records != null ? records.size() : 0), dataStyle);

        rowNum++; // 빈 행

        // 출석 기록 헤더
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"사용자 ID", "출석 상태", "체크 시간", "수동 조정", "조정 사유"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 출석 기록 데이터
        if (records != null) {
            for (AttendanceRecord record : records) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(record.getUserId());
                dataRow.createCell(1).setCellValue(getStatusDisplayName(record.getStatus()));
                dataRow.createCell(2).setCellValue(record.getCheckedAt() != null ?
                        record.getCheckedAt().format(DATETIME_FORMATTER) : "");
                dataRow.createCell(3).setCellValue(record.getIsManuallyAdjusted() ? "예" : "아니오");
                dataRow.createCell(4).setCellValue(record.getAdjustmentReason() != null ?
                        record.getAdjustmentReason() : "");
            }
        }

        // 열 크기 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 전체 요약 시트를 생성합니다.
     */
    private void createSummarySheet(Workbook workbook, List<AttendanceSession> sessions, List<AttendanceRecord> records) {
        Sheet sheet = workbook.createSheet("전체 요약");

        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;

        // 제목
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("출석 현황 전체 요약");
        titleCell.setCellStyle(headerStyle);

        rowNum++; // 빈 행

        // 통계 정보
        sheet.createRow(rowNum++).createCell(0).setCellValue("총 세션 수: " + sessions.size());
        sheet.createRow(rowNum++).createCell(0).setCellValue("총 출석 기록 수: " + records.size());

        // 출석 상태별 통계
        Map<AttendanceStatus, Long> statusCounts = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

        rowNum++; // 빈 행
        sheet.createRow(rowNum++).createCell(0).setCellValue("출석 상태별 통계:");

        for (Map.Entry<AttendanceStatus, Long> entry : statusCounts.entrySet()) {
            sheet.createRow(rowNum++).createCell(0)
                    .setCellValue("  " + getStatusDisplayName(entry.getKey()) + ": " + entry.getValue() + "건");
        }

        // 열 크기 자동 조정
        sheet.autoSizeColumn(0);
    }

    /**
     * 통계 시트를 생성합니다.
     */
    private void createStatisticsSheet(Workbook workbook, Long labId, LocalDate fromDate, LocalDate toDate, List<AttendanceRecord> records) {
        Sheet sheet = workbook.createSheet("출석 통계");

        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;

        // 제목 및 기간 정보
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("랩 출석 통계 (Lab ID: " + labId + ")");
        titleRow.getCell(0).setCellStyle(headerStyle);

        rowNum++; // 빈 행
        sheet.createRow(rowNum++).createCell(0).setCellValue("기간: " + fromDate.format(DATE_FORMATTER) + " ~ " + toDate.format(DATE_FORMATTER));

        // 통계 계산 및 표시
        Map<Long, Long> userAttendanceCounts = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getUserId, Collectors.counting()));

        rowNum++; // 빈 행
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("사용자 ID");
        headerRow.createCell(1).setCellValue("출석 횟수");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);

        for (Map.Entry<Long, Long> entry : userAttendanceCounts.entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * 사용자 출석 기록 시트를 생성합니다.
     */
    private void createUserHistorySheet(Workbook workbook, Long userId, String userName, List<AttendanceRecord> records) {
        Sheet sheet = workbook.createSheet("출석 기록");

        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;

        // 사용자 정보
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("출석 기록 - " + userName + " (" + userId + ")");
        titleRow.getCell(0).setCellStyle(headerStyle);

        rowNum++; // 빈 행

        // 헤더
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"날짜", "세션명", "출석 상태", "체크 시간"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터
        for (AttendanceRecord record : records) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(record.getAttendanceSession().getStartTime().format(DATE_FORMATTER));
            dataRow.createCell(1).setCellValue(record.getAttendanceSession().getTitle());
            dataRow.createCell(2).setCellValue(getStatusDisplayName(record.getStatus()));
            dataRow.createCell(3).setCellValue(record.getCheckedAt() != null ?
                    record.getCheckedAt().format(DATETIME_FORMATTER) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 출석 증명서 시트를 생성합니다.
     */
    private void createCertificateSheet(Workbook workbook, Long userId, String userName, String labName,
                                        LocalDate fromDate, LocalDate toDate,
                                        double attendanceRate, int totalSessions, int attendedSessions) {
        Sheet sheet = workbook.createSheet("출석 증명서");

        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;

        // 증명서 제목
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("출 석 증 명 서");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum += 2; // 빈 행

        // 증명 내용
        createInfoRow(sheet, rowNum++, "성명:", userName, headerStyle);
        createInfoRow(sheet, rowNum++, "사용자 ID:", userId.toString(), headerStyle);
        createInfoRow(sheet, rowNum++, "소속 랩실:", labName, headerStyle);
        createInfoRow(sheet, rowNum++, "증명 기간:", fromDate.format(DATE_FORMATTER) + " ~ " + toDate.format(DATE_FORMATTER), headerStyle);

        rowNum++; // 빈 행

        createInfoRow(sheet, rowNum++, "총 세션 수:", totalSessions + "회", headerStyle);
        createInfoRow(sheet, rowNum++, "출석 세션 수:", attendedSessions + "회", headerStyle);
        createInfoRow(sheet, rowNum++, "출석률:", String.format("%.1f%%", attendanceRate), headerStyle);

        rowNum += 2; // 빈 행

        // 발급 정보
        Row issueRow = sheet.createRow(rowNum++);
        issueRow.createCell(0).setCellValue("위 사실을 증명합니다.");

        rowNum++; // 빈 행

        Row dateRow = sheet.createRow(rowNum);
        dateRow.createCell(0).setCellValue("발급일: " + LocalDate.now().format(DATE_FORMATTER));

        // 열 크기 조정
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // 스타일 생성 메서드들
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // 유틸리티 메서드들
    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        if (style != null) {
            labelCell.setCellStyle(style);
        }

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }

    private String sanitizeSheetName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Sheet";
        }

        // Excel 시트명에서 허용되지 않는 문자 제거
        String sanitized = name.replaceAll("[\\[\\]\\*\\?/\\\\:]+", "_");

        // 시트명 길이 제한 (31자)
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 28) + "...";
        }

        return sanitized;
    }

    private String getStatusDisplayName(AttendanceStatus status) {
        switch (status) {
            case PRESENT:
                return "출석";
            case ABSENT:
                return "결석";
            case LATE:
                return "지각";
            default:
                return status.name();
        }
    }

    // 유효성 검사 메서드들
    private void validateExportAttendanceInputs(List<AttendanceSession> sessions, List<AttendanceRecord> records, String fileName) {
        if (sessions == null) {
            throw new IllegalArgumentException("세션 목록이 null입니다.");
        }
        if (records == null) {
            throw new IllegalArgumentException("출석 기록 목록이 null입니다.");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 null이거나 비어있습니다.");
        }
    }

    private void validateLabStatisticsInputs(Long labId, LocalDate fromDate, LocalDate toDate, List<AttendanceRecord> records) {
        if (labId == null || labId <= 0) {
            throw new IllegalArgumentException("랩 ID가 유효하지 않습니다.");
        }
        if (fromDate == null) {
            throw new IllegalArgumentException("시작 날짜가 null입니다.");
        }
        if (toDate == null) {
            throw new IllegalArgumentException("종료 날짜가 null입니다.");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("시작 날짜가 종료 날짜보다 늦습니다.");
        }
        if (records == null) {
            throw new IllegalArgumentException("출석 기록 목록이 null입니다.");
        }
    }

    private void validateUserHistoryInputs(Long userId, String userName, List<AttendanceRecord> records) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID가 유효하지 않습니다.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 이름이 null이거나 비어있습니다.");
        }
        if (records == null) {
            throw new IllegalArgumentException("출석 기록 목록이 null입니다.");
        }
    }

    private void validateCertificateInputs(Long userId, String userName, String labName,
                                           LocalDate fromDate, LocalDate toDate,
                                           double attendanceRate, int totalSessions, int attendedSessions) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID가 유효하지 않습니다.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 이름이 null이거나 비어있습니다.");
        }
        if (labName == null || labName.trim().isEmpty()) {
            throw new IllegalArgumentException("랩실 이름이 null이거나 비어있습니다.");
        }
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("날짜가 null입니다.");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("시작 날짜가 종료 날짜보다 늦습니다.");
        }
        if (attendanceRate < 0 || attendanceRate > 100) {
            throw new IllegalArgumentException("출석률은 0-100% 범위여야 합니다.");
        }
        if (totalSessions < 0 || attendedSessions < 0) {
            throw new IllegalArgumentException("세션 수는 0 이상이어야 합니다.");
        }
        if (attendedSessions > totalSessions) {
            throw new IllegalArgumentException("출석 세션 수가 총 세션 수보다 클 수 없습니다.");
        }
    }
}
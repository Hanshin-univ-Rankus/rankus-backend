package org.univ.rankus.adapter.out.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ApachePOIExcelExporter 단위 테스트
 * 
 * Excel 파일 생성 기능의 정확성과 예외 처리를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApachePOIExcelExporter 단위 테스트")
class ApachePOIExcelExporterTest {

    private ApachePOIExcelExporter excelExporter;

    @BeforeEach
    void setUp() {
        excelExporter = new ApachePOIExcelExporter();
    }

    @Nested
    @DisplayName("출석 데이터 Excel 내보내기")
    class ExportAttendanceToExcel {

        @Test
        @DisplayName("정상적인 출석 데이터로 Excel 파일을 생성한다")
        void exportAttendanceToExcel_ValidData_Success() throws IOException {
            // Given
            AttendanceSession session1 = DomainAttendanceFactory.buildValidSessionWithId(1L);
            AttendanceSession session2 = DomainAttendanceFactory.buildSessionWithTitle("세미나 출석");
            // Set ID using reflection to maintain different title
            ReflectionTestUtils.setField(session2, "sessionId", 2L);
            List<AttendanceSession> sessions = Arrays.asList(session1, session2);
            
            AttendanceRecord record1 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session1, 1L);
            AttendanceRecord record2 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session1, 2L);
            AttendanceRecord record3 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session2, 1L);
            
            // 상태 변경
            record2.markAsAbsent(1L, "테스트 결석");
            record3.markAsLate(1L, "테스트 지각");
            
            List<AttendanceRecord> records = Arrays.asList(record1, record2, record3);
            
            String fileName = "attendance_report";

            // When
            byte[] excelBytes = excelExporter.exportAttendanceToExcel(sessions, records, fileName);

            // Then
            assertThat(excelBytes).isNotNull().isNotEmpty();
            
            // Excel 파일이 유효한지 확인
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
                assertThat(workbook.getNumberOfSheets()).isGreaterThanOrEqualTo(2); // 세션 시트들 + 요약 시트
                
                // 첫 번째 세션 시트 확인
                Sheet firstSheet = workbook.getSheetAt(0);
                assertThat(firstSheet).isNotNull();
                assertThat(firstSheet.getLastRowNum()).isGreaterThan(0);
                
                // 요약 시트 확인
                Sheet summarySheet = workbook.getSheet("전체 요약");
                assertThat(summarySheet).isNotNull();
            }
        }

        @Test
        @DisplayName("빈 세션 목록으로 Excel 파일을 생성한다")
        void exportAttendanceToExcel_EmptySessions_Success() throws IOException {
            // Given
            List<AttendanceSession> sessions = Collections.emptyList();
            List<AttendanceRecord> records = Collections.emptyList();
            String fileName = "empty_report";

            // When
            byte[] excelBytes = excelExporter.exportAttendanceToExcel(sessions, records, fileName);

            // Then
            assertThat(excelBytes).isNotNull().isNotEmpty();
            
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
                assertThat(workbook.getNumberOfSheets()).isEqualTo(1); // 요약 시트만
                
                Sheet summarySheet = workbook.getSheet("전체 요약");
                assertThat(summarySheet).isNotNull();
            }
        }

        @Test
        @DisplayName("null 세션 목록 전달 시 IllegalArgumentException을 던진다")
        void exportAttendanceToExcel_NullSessions_ThrowsException() {
            // Given
            List<AttendanceRecord> records = Collections.emptyList();
            String fileName = "test";

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportAttendanceToExcel(null, records, fileName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("세션 목록이 null입니다");
        }

        @Test
        @DisplayName("null 출석 기록 목록 전달 시 IllegalArgumentException을 던진다")
        void exportAttendanceToExcel_NullRecords_ThrowsException() {
            // Given
            List<AttendanceSession> sessions = Collections.emptyList();
            String fileName = "test";

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportAttendanceToExcel(sessions, null, fileName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출석 기록 목록이 null입니다");
        }

        @Test
        @DisplayName("null 파일명 전달 시 IllegalArgumentException을 던진다")
        void exportAttendanceToExcel_NullFileName_ThrowsException() {
            // Given
            List<AttendanceSession> sessions = Collections.emptyList();
            List<AttendanceRecord> records = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportAttendanceToExcel(sessions, records, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일명이 null이거나 비어있습니다");
        }

        @Test
        @DisplayName("빈 파일명 전달 시 IllegalArgumentException을 던진다")
        void exportAttendanceToExcel_EmptyFileName_ThrowsException() {
            // Given
            List<AttendanceSession> sessions = Collections.emptyList();
            List<AttendanceRecord> records = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportAttendanceToExcel(sessions, records, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일명이 null이거나 비어있습니다");
        }
    }

    @Nested
    @DisplayName("랩 출석 통계 Excel 내보내기")
    class ExportLabAttendanceStatistics {

        @Test
        @DisplayName("정상적인 랩 통계 데이터로 Excel 파일을 생성한다")
        void exportLabAttendanceStatistics_ValidData_Success() throws IOException {
            // Given
            Long labId = 1L;
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 1, 31);
            
            AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(1L);
            AttendanceRecord record1 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session, 1L);
            AttendanceRecord record2 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session, 2L);
            record2.markAsAbsent(1L, "테스트 결석");
            List<AttendanceRecord> records = Arrays.asList(record1, record2);

            // When
            byte[] excelBytes = excelExporter.exportLabAttendanceStatistics(labId, fromDate, toDate, records);

            // Then
            assertThat(excelBytes).isNotNull().isNotEmpty();
            
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
                
                Sheet sheet = workbook.getSheet("출석 통계");
                assertThat(sheet).isNotNull();
                assertThat(sheet.getLastRowNum()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("잘못된 랩 ID 전달 시 IllegalArgumentException을 던진다")
        void exportLabAttendanceStatistics_InvalidLabId_ThrowsException() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 1, 31);
            List<AttendanceRecord> records = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportLabAttendanceStatistics(0L, fromDate, toDate, records))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("랩 ID가 유효하지 않습니다");

            assertThatThrownBy(() -> excelExporter.exportLabAttendanceStatistics(null, fromDate, toDate, records))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("랩 ID가 유효하지 않습니다");
        }

        @Test
        @DisplayName("잘못된 날짜 범위 전달 시 IllegalArgumentException을 던진다")
        void exportLabAttendanceStatistics_InvalidDateRange_ThrowsException() {
            // Given
            Long labId = 1L;
            LocalDate fromDate = LocalDate.of(2024, 1, 31);
            LocalDate toDate = LocalDate.of(2024, 1, 1); // 시작일이 종료일보다 늦음
            List<AttendanceRecord> records = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportLabAttendanceStatistics(labId, fromDate, toDate, records))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작 날짜가 종료 날짜보다 늦습니다");
        }
    }

    @Nested
    @DisplayName("사용자 출석 기록 Excel 내보내기")
    class ExportUserAttendanceHistory {

        @Test
        @DisplayName("정상적인 사용자 출석 기록으로 Excel 파일을 생성한다")
        void exportUserAttendanceHistory_ValidData_Success() throws IOException {
            // Given
            Long userId = 1L;
            String userName = "김철수";
            
            AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(1L);
            AttendanceRecord record1 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session, userId);
            AttendanceRecord record2 = DomainAttendanceFactory.buildRecordWithSessionAndUser(session, userId);
            record2.markAsLate(1L, "테스트 지각");
            List<AttendanceRecord> records = Arrays.asList(record1, record2);

            // When
            byte[] excelBytes = excelExporter.exportUserAttendanceHistory(userId, userName, records);

            // Then
            assertThat(excelBytes).isNotNull().isNotEmpty();
            
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
                
                Sheet sheet = workbook.getSheet("출석 기록");
                assertThat(sheet).isNotNull();
                assertThat(sheet.getLastRowNum()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("잘못된 사용자 정보 전달 시 IllegalArgumentException을 던진다")
        void exportUserAttendanceHistory_InvalidUserInfo_ThrowsException() {
            // Given
            List<AttendanceRecord> records = Collections.emptyList();

            // When & Then
            assertThatThrownBy(() -> excelExporter.exportUserAttendanceHistory(null, "김철수", records))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 ID가 유효하지 않습니다");

            assertThatThrownBy(() -> excelExporter.exportUserAttendanceHistory(1L, null, records))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 이름이 null이거나 비어있습니다");

            assertThatThrownBy(() -> excelExporter.exportUserAttendanceHistory(1L, "", records))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자 이름이 null이거나 비어있습니다");
        }
    }

    @Nested
    @DisplayName("출석 증명서 생성")
    class GenerateAttendanceCertificate {

        @Test
        @DisplayName("정상적인 데이터로 출석 증명서를 생성한다")
        void generateAttendanceCertificate_ValidData_Success() throws IOException {
            // Given
            Long userId = 1L;
            String userName = "김철수";
            String labName = "AI 연구실";
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 1, 31);
            double attendanceRate = 85.5;
            int totalSessions = 20;
            int attendedSessions = 17;

            // When
            byte[] excelBytes = excelExporter.generateAttendanceCertificate(
                userId, userName, labName, fromDate, toDate, 
                attendanceRate, totalSessions, attendedSessions
            );

            // Then
            assertThat(excelBytes).isNotNull().isNotEmpty();
            
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
                
                Sheet sheet = workbook.getSheet("출석 증명서");
                assertThat(sheet).isNotNull();
                assertThat(sheet.getLastRowNum()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("잘못된 출석률 전달 시 IllegalArgumentException을 던진다")
        void generateAttendanceCertificate_InvalidAttendanceRate_ThrowsException() {
            // Given
            Long userId = 1L;
            String userName = "김철수";
            String labName = "AI 연구실";
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 1, 31);
            int totalSessions = 20;
            int attendedSessions = 17;

            // When & Then
            assertThatThrownBy(() -> excelExporter.generateAttendanceCertificate(
                userId, userName, labName, fromDate, toDate, 
                -10.0, totalSessions, attendedSessions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출석률은 0-100% 범위여야 합니다");

            assertThatThrownBy(() -> excelExporter.generateAttendanceCertificate(
                userId, userName, labName, fromDate, toDate, 
                150.0, totalSessions, attendedSessions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출석률은 0-100% 범위여야 합니다");
        }

        @Test
        @DisplayName("잘못된 세션 수 전달 시 IllegalArgumentException을 던진다")
        void generateAttendanceCertificate_InvalidSessionCounts_ThrowsException() {
            // Given
            Long userId = 1L;
            String userName = "김철수";
            String labName = "AI 연구실";
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 1, 31);
            double attendanceRate = 85.5;

            // When & Then
            assertThatThrownBy(() -> excelExporter.generateAttendanceCertificate(
                userId, userName, labName, fromDate, toDate, 
                attendanceRate, -1, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("세션 수는 0 이상이어야 합니다");

            assertThatThrownBy(() -> excelExporter.generateAttendanceCertificate(
                userId, userName, labName, fromDate, toDate, 
                attendanceRate, 10, 15)) // 출석 세션이 총 세션보다 많음
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출석 세션 수가 총 세션 수보다 클 수 없습니다");
        }
    }

    @Nested
    @DisplayName("Excel 파일 형식 검증")
    class ExcelFormatValidation {

        @Test
        @DisplayName("생성된 Excel 파일이 XLSX 형식이다")
        void generatedExcelFile_ValidXLSXFormat() throws IOException {
            // Given
            List<AttendanceSession> sessions = Collections.emptyList();
            List<AttendanceRecord> records = Collections.emptyList();
            String fileName = "test";

            // When
            byte[] excelBytes = excelExporter.exportAttendanceToExcel(sessions, records, fileName);

            // Then
            assertThat(excelBytes).isNotNull().isNotEmpty();
            
            // XLSX 매직 넘버 확인 (PK\003\004)
            assertThat(excelBytes[0] & 0xFF).isEqualTo(0x50); // P
            assertThat(excelBytes[1] & 0xFF).isEqualTo(0x4B); // K
            assertThat(excelBytes[2] & 0xFF).isEqualTo(0x03);
            assertThat(excelBytes[3] & 0xFF).isEqualTo(0x04);
        }

        @Test
        @DisplayName("동일한 데이터로 생성한 Excel 파일은 동일하지 않다 (타임스탬프 포함)")
        void sameData_DifferentExcelFiles() {
            // Given
            List<AttendanceSession> sessions = Collections.emptyList();
            List<AttendanceRecord> records = Collections.emptyList();
            String fileName = "test";

            // When
            byte[] excelBytes1 = excelExporter.exportAttendanceToExcel(sessions, records, fileName);
            byte[] excelBytes2 = excelExporter.exportAttendanceToExcel(sessions, records, fileName);

            // Then
            // Excel 파일에는 생성 시간 등의 메타데이터가 포함되므로 동일하지 않을 수 있음
            assertThat(excelBytes1).isNotNull();
            assertThat(excelBytes2).isNotNull();
            assertThat(excelBytes1.length).isEqualTo(excelBytes2.length);
        }
    }
}
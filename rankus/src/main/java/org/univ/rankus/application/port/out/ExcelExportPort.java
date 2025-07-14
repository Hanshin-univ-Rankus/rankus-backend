package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;

import java.time.LocalDate;
import java.util.List;

/**
 * Excel 파일 내보내기를 위한 아웃바운드 포트
 * <p>
 * 출석 데이터를 Excel 형식으로 내보내는 기능을 제공합니다.
 *
 * @since 1.0
 */
public interface ExcelExportPort {

    /**
     * 출석 세션 데이터를 Excel 파일로 내보냅니다.
     *
     * @param sessions 출석 세션 목록
     * @param records  출석 기록 목록
     * @param fileName 파일명 (확장자 제외)
     * @return Excel 파일 바이트 배열
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         Excel 파일 생성 실패 시
     */
    byte[] exportAttendanceToExcel(List<AttendanceSession> sessions, List<AttendanceRecord> records, String fileName);

    /**
     * 특정 랩의 출석 통계를 Excel 파일로 내보냅니다.
     *
     * @param labId    랩실 ID
     * @param fromDate 시작 날짜
     * @param toDate   종료 날짜
     * @param records  출석 기록 목록
     * @return Excel 파일 바이트 배열
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         Excel 파일 생성 실패 시
     */
    byte[] exportLabAttendanceStatistics(Long labId, LocalDate fromDate, LocalDate toDate, List<AttendanceRecord> records);

    /**
     * 사용자별 출석 기록을 Excel 파일로 내보냅니다.
     *
     * @param userId   사용자 ID
     * @param userName 사용자 이름
     * @param records  출석 기록 목록
     * @return Excel 파일 바이트 배열
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         Excel 파일 생성 실패 시
     */
    byte[] exportUserAttendanceHistory(Long userId, String userName, List<AttendanceRecord> records);

    /**
     * 출석 증명서를 Excel 형식으로 생성합니다.
     *
     * @param userId           사용자 ID
     * @param userName         사용자 이름
     * @param labName          랩실 이름
     * @param fromDate         증명 시작 날짜
     * @param toDate           증명 종료 날짜
     * @param attendanceRate   출석률
     * @param totalSessions    총 세션 수
     * @param attendedSessions 출석한 세션 수
     * @return 출석 증명서 Excel 파일 바이트 배열
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         Excel 파일 생성 실패 시
     */
    byte[] generateAttendanceCertificate(
            Long userId,
            String userName,
            String labName,
            LocalDate fromDate,
            LocalDate toDate,
            double attendanceRate,
            int totalSessions,
            int attendedSessions
    );
}
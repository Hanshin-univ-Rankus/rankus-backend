package org.univ.rankus.application.port.in.lab.export;

import java.time.LocalDate;

/**
 * 랩실 출석 Excel 내보내기 쿼리 인터페이스
 */
public interface ExportLabAttendanceQuery {

    /**
     * 랩실 출석 데이터를 Excel 파일로 내보냅니다.
     *
     * @param command 출석 내보내기 명령
     * @return Excel 파일 바이트 배열
     */
    byte[] exportLabAttendance(ExportLabAttendanceCommand command);

    /**
     * 출석 내보내기 명령 클래스
     */
    record ExportLabAttendanceCommand(Long labId, Long requesterId) {
    }

    /**
     * 랩실 출석 데이터를 Excel 파일로 내보냅니다.
     *
     * @param labId       랩실 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return Excel 파일 바이트 배열
     */
    byte[] exportLabAttendanceToExcel(Long labId, Long requesterId);

    /**
     * 랩실 출석 통계를 Excel 파일로 내보냅니다.
     *
     * @param labId       랩실 ID
     * @param fromDate    시작 날짜
     * @param toDate      종료 날짜
     * @param requesterId 요청자 ID (권한 검증용)
     * @return Excel 파일 바이트 배열
     */
    byte[] exportLabAttendanceStatistics(Long labId, LocalDate fromDate, LocalDate toDate, Long requesterId);

    /**
     * 특정 멤버의 출석 기록을 Excel 파일로 내보냅니다.
     *
     * @param labId       랩실 ID
     * @param memberId    멤버 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return Excel 파일 바이트 배열
     */
    byte[] exportMemberAttendanceHistory(Long labId, Long memberId, Long requesterId);

    /**
     * 출석 증명서를 Excel 형식으로 생성합니다.
     *
     * @param labId       랩실 ID
     * @param memberId    멤버 ID
     * @param fromDate    증명 시작 날짜
     * @param toDate      증명 종료 날짜
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 출석 증명서 Excel 파일 바이트 배열
     */
    byte[] generateAttendanceCertificate(Long labId, Long memberId, LocalDate fromDate, LocalDate toDate, Long requesterId);
}
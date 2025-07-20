package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;

/**
 * 출석 세션 명령 유스케이스
 * - 출석 세션 생성, 수정, 종료
 * - QR 코드 생성
 * - 출석 체크 처리
 */
public interface AttendanceSessionCommandUseCase {

    /**
     * 출석 세션 생성
     *
     * @param labId             랩실 ID
     * @param title             세션 제목
     * @param qrValidityMinutes QR 유효시간 (분)
     * @param createdBy         생성자 ID
     * @return 생성된 출석 세션
     */
    AttendanceSession createSession(Long labId, String title, Integer qrValidityMinutes, Long createdBy);

    /**
     * 출석 세션 종료
     *
     * @param labId     랩실 ID (경로 일관성 검증용)
     * @param sessionId 세션 ID
     * @param userId    요청자 ID
     * @return 종료된 출석 세션
     */
    AttendanceSession endSession(Long labId, Long sessionId, Long userId);

    /**
     * 출석 세션 취소
     *
     * @param labId     랩실 ID (경로 일관성 검증용)
     * @param sessionId 세션 ID
     * @param userId    요청자 ID
     * @return 취소된 출석 세션
     */
    AttendanceSession cancelSession(Long labId, Long sessionId, Long userId);

    /**
     * 출석 세션 제목 수정
     *
     * @param labId     랩실 ID (경로 일관성 검증용)
     * @param sessionId 세션 ID
     * @param newTitle  새 제목
     * @param userId    요청자 ID
     * @return 수정된 출석 세션
     */
    AttendanceSession updateSessionTitle(Long labId, Long sessionId, String newTitle, Long userId);

    /**
     * QR 유효시간 수정
     *
     * @param labId              랩실 ID (경로 일관성 검증용)
     * @param sessionId          세션 ID
     * @param newValidityMinutes 새 유효시간
     * @param userId             요청자 ID
     * @return 수정된 출석 세션
     */
    AttendanceSession updateQRValidityMinutes(Long labId, Long sessionId, Integer newValidityMinutes, Long userId);

    /**
     * QR 코드 생성
     *
     * @param labId     랩실 ID (경로 일관성 검증용)
     * @param sessionId 세션 ID
     * @param userId    요청자 ID
     * @return 생성된 QR 토큰
     */
    QRToken generateQRCode(Long labId, Long sessionId, Long userId);

    /**
     * QR 코드를 통한 출석 체크
     *
     * @param labId   랩실 ID (경로 일관성 검증용)
     * @param qrToken QR 토큰 문자열
     * @param userId  출석 체크하는 사용자 ID
     * @return 생성된 출석 기록
     */
    AttendanceRecord checkAttendance(Long labId, String qrToken, Long userId);
}
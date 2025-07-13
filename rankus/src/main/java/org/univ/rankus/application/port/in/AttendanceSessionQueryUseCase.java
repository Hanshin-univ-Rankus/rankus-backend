package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.attendance.AttendanceSession;

import java.util.List;

/**
 * 출석 세션 조회 유스케이스
 * - 출석 세션 조회
 * - 출석 통계 조회
 */
public interface AttendanceSessionQueryUseCase {

    /**
     * 세션 ID로 출석 세션 조회
     *
     * @param sessionId 세션 ID
     * @param userId    요청자 ID
     * @return 출석 세션
     */
    AttendanceSession findSessionById(Long sessionId, Long userId);

    /**
     * 랩실의 활성 출석 세션 목록 조회
     *
     * @param labId  랩실 ID
     * @param userId 요청자 ID
     * @return 활성 세션 목록
     */
    List<AttendanceSession> findActiveSessionsByLabId(Long labId, Long userId);

    /**
     * 랩실의 모든 출석 세션 목록 조회 (페이징)
     *
     * @param labId  랩실 ID
     * @param userId 요청자 ID
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지 크기
     * @return 세션 목록
     */
    List<AttendanceSession> findSessionsByLabId(Long labId, Long userId, int page, int size);

    /**
     * 세션 통계 조회
     *
     * @param sessionId 세션 ID
     * @param userId    요청자 ID
     * @return 출석 통계
     */
    AttendanceSession.AttendanceStatistics getSessionStatistics(Long sessionId, Long userId);

    /**
     * 사용자별 세션 목록 조회 (내가 참여한 세션들)
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 참여한 세션 목록
     */
    List<AttendanceSession> findSessionsByUserId(Long userId, int page, int size);
}
package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.attendance.AttendanceRecord;

import java.util.List;

/**
 * 출석 기록 조회 유스케이스
 * - 출석 기록 조회
 * - 출석 내역 조회
 */
public interface AttendanceRecordQueryUseCase {

    /**
     * 출석 기록 ID로 조회
     *
     * @param recordId 출석 기록 ID
     * @param userId   요청자 ID
     * @return 출석 기록
     */
    AttendanceRecord findRecordById(Long recordId, Long userId);

    /**
     * 세션의 모든 출석 기록 조회
     *
     * @param sessionId 세션 ID
     * @param userId    요청자 ID
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findRecordsBySessionId(Long sessionId, Long userId);

    /**
     * 사용자의 출석 기록 조회 (특정 세션)
     *
     * @param sessionId     세션 ID
     * @param targetUserId  조회할 사용자 ID
     * @param requestUserId 요청자 ID
     * @return 출석 기록 (없으면 null)
     */
    AttendanceRecord findRecordBySessionIdAndUserId(Long sessionId, Long targetUserId, Long requestUserId);

    /**
     * 사용자의 전체 출석 기록 조회 (페이징)
     *
     * @param userId        사용자 ID
     * @param requestUserId 요청자 ID
     * @param page          페이지 번호
     * @param size          페이지 크기
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findRecordsByUserId(Long userId, Long requestUserId, int page, int size);

    /**
     * 랩실의 전체 출석 기록 조회 (페이징)
     *
     * @param labId  랩실 ID
     * @param userId 요청자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findRecordsByLabId(Long labId, Long userId, int page, int size);
}
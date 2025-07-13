package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.util.List;
import java.util.Optional;

/**
 * 출석 세션 리포지토리 포트
 */
public interface AttendanceSessionRepositoryPort {

    /**
     * 출석 세션 저장
     *
     * @param session 출석 세션
     * @return 저장된 출석 세션
     */
    AttendanceSession save(AttendanceSession session);

    /**
     * ID로 출석 세션 조회
     *
     * @param sessionId 세션 ID
     * @return 출석 세션
     */
    Optional<AttendanceSession> findById(Long sessionId);

    /**
     * 모든 출석 세션 조회
     *
     * @return 출석 세션 목록
     */
    List<AttendanceSession> findAll();

    /**
     * 랩실 ID로 출석 세션 조회
     *
     * @param labId 랩실 ID
     * @return 출석 세션 목록
     */
    List<AttendanceSession> findByLabId(Long labId);

    /**
     * 랩실 ID와 상태로 출석 세션 조회
     *
     * @param labId  랩실 ID
     * @param status 세션 상태
     * @return 출석 세션 목록
     */
    List<AttendanceSession> findByLabIdAndStatus(Long labId, SessionStatus status);

    /**
     * 생성자 ID로 출석 세션 조회
     *
     * @param createdBy 생성자 ID
     * @return 출석 세션 목록
     */
    List<AttendanceSession> findByCreatedBy(Long createdBy);

    /**
     * 랩실 ID로 출석 세션 조회 (페이징)
     *
     * @param labId 랩실 ID
     * @param page  페이지 번호 (0부터 시작)
     * @param size  페이지 크기
     * @return 출석 세션 목록
     */
    List<AttendanceSession> findByLabIdWithPaging(Long labId, int page, int size);

    /**
     * 사용자가 참여한 출석 세션 조회 (페이징)
     * - 해당 사용자의 출석 기록이 있는 세션들
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 출석 세션 목록
     */
    List<AttendanceSession> findSessionsByUserIdWithPaging(Long userId, int page, int size);

    /**
     * 랩실의 활성 세션 수 조회
     *
     * @param labId 랩실 ID
     * @return 활성 세션 수
     */
    long countActiveSessionsByLabId(Long labId);

    /**
     * 출석 세션 삭제
     *
     * @param session 출석 세션
     */
    void delete(AttendanceSession session);

    /**
     * ID로 출석 세션 삭제
     *
     * @param sessionId 세션 ID
     */
    void deleteById(Long sessionId);

    /**
     * ID 존재 여부 확인
     *
     * @param sessionId 세션 ID
     * @return 존재 여부
     */
    boolean existsById(Long sessionId);

    /**
     * 랩실의 활성 세션들 조회
     *
     * @param labId 랩실 ID
     * @return 활성 세션 목록
     */
    List<AttendanceSession> findActiveSessionsByLabId(Long labId);

    /**
     * 랩실의 세션들 조회 (페이징)
     *
     * @param labId 랩실 ID
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return 세션 목록
     */
    List<AttendanceSession> findSessionsByLabId(Long labId, int page, int size);

    /**
     * 사용자 ID로 세션 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 세션 목록
     */
    List<AttendanceSession> findSessionsByUserId(Long userId, int page, int size);
}
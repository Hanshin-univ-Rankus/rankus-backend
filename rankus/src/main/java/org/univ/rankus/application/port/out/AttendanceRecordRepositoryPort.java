package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.util.List;
import java.util.Optional;

/**
 * 출석 기록 리포지토리 포트
 */
public interface AttendanceRecordRepositoryPort {

    /**
     * 출석 기록 저장
     *
     * @param record 출석 기록
     * @return 저장된 출석 기록
     */
    AttendanceRecord save(AttendanceRecord record);

    /**
     * ID로 출석 기록 조회
     *
     * @param recordId 출석 기록 ID
     * @return 출석 기록
     */
    Optional<AttendanceRecord> findById(Long recordId);

    /**
     * 모든 출석 기록 조회
     *
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findAll();

    /**
     * 세션 ID로 출석 기록 조회
     *
     * @param sessionId 세션 ID
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findBySessionId(Long sessionId);

    /**
     * 사용자 ID로 출석 기록 조회
     *
     * @param userId 사용자 ID
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByUserId(Long userId);

    /**
     * 사용자 ID로 출석 기록 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByUserId(Long userId, int page, int size);

    /**
     * 세션 ID와 사용자 ID로 출석 기록 조회
     *
     * @param sessionId 세션 ID
     * @param userId    사용자 ID
     * @return 출석 기록
     */
    Optional<AttendanceRecord> findBySessionIdAndUserId(Long sessionId, Long userId);

    /**
     * 세션 ID와 출석 상태로 출석 기록 조회
     *
     * @param sessionId 세션 ID
     * @param status    출석 상태
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findBySessionIdAndStatus(Long sessionId, AttendanceStatus status);

    /**
     * 사용자 ID로 출석 기록 조회 (페이징) - 별칭
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지 크기
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByUserIdWithPaging(Long userId, int page, int size);

    /**
     * 랩실 ID로 출석 기록 조회 (페이징)
     * - 해당 랩실의 모든 세션에 대한 출석 기록
     *
     * @param labId 랩실 ID
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByLabId(Long labId, int page, int size);

    /**
     * 랩실 ID로 출석 기록 조회 (페이징) - 별칭
     *
     * @param labId 랩실 ID
     * @param page  페이지 번호
     * @param size  페이지 크기
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByLabIdWithPaging(Long labId, int page, int size);

    /**
     * 세션의 출석률 계산을 위한 통계 조회
     *
     * @param sessionId 세션 ID
     * @return 상태별 출석 기록 수
     */
    AttendanceStatistics findStatisticsBySessionId(Long sessionId);

    /**
     * 중복 출석 체크
     *
     * @param sessionId 세션 ID
     * @param userId    사용자 ID
     * @return 이미 출석했는지 여부
     */
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    /**
     * 출석 기록 삭제
     *
     * @param record 출석 기록
     */
    void delete(AttendanceRecord record);

    /**
     * ID로 출석 기록 삭제
     *
     * @param recordId 출석 기록 ID
     */
    void deleteById(Long recordId);

    /**
     * 세션 ID로 모든 출석 기록 삭제
     *
     * @param sessionId 세션 ID
     */
    void deleteBySessionId(Long sessionId);

    /**
     * 출석 통계 DTO
     */
    class AttendanceStatistics {
        private final long totalCount;
        private final long presentCount;
        private final long absentCount;
        private final long lateCount;

        public AttendanceStatistics(long totalCount, long presentCount, long absentCount, long lateCount) {
            this.totalCount = totalCount;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getPresentCount() {
            return presentCount;
        }

        public long getAbsentCount() {
            return absentCount;
        }

        public long getLateCount() {
            return lateCount;
        }
    }
}
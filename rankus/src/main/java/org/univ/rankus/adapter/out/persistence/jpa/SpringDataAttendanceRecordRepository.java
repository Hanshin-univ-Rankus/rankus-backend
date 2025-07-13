package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA 출석 기록 리포지토리
 */
@Repository
public interface SpringDataAttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 세션 ID로 출석 기록 조회
     */
    @Query("SELECT r FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId")
    List<AttendanceRecord> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 사용자 ID로 출석 기록 조회
     */
    List<AttendanceRecord> findByUserId(Long userId);

    /**
     * 세션 ID와 사용자 ID로 출석 기록 조회
     */
    @Query("SELECT r FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId AND r.userId = :userId")
    Optional<AttendanceRecord> findBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 세션 ID와 출석 상태로 출석 기록 조회
     */
    @Query("SELECT r FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId AND r.status = :status")
    List<AttendanceRecord> findBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") AttendanceStatus status);

    /**
     * 사용자 ID로 출석 기록 조회 (페이징)
     */
    Page<AttendanceRecord> findByUserId(Long userId, Pageable pageable);

    /**
     * 랩실 ID로 출석 기록 조회 (페이징)
     * - 해당 랩실의 모든 세션에 대한 출석 기록
     */
    @Query("SELECT r FROM AttendanceRecord r WHERE r.attendanceSession.labId = :labId")
    Page<AttendanceRecord> findByLabId(@Param("labId") Long labId, Pageable pageable);

    /**
     * 세션의 출석률 계산을 위한 통계 조회
     */
    @Query("SELECT " +
            "COUNT(r) as totalCount, " +
            "SUM(CASE WHEN r.status = 'PRESENT' THEN 1 ELSE 0 END) as presentCount, " +
            "SUM(CASE WHEN r.status = 'ABSENT' THEN 1 ELSE 0 END) as absentCount, " +
            "SUM(CASE WHEN r.status = 'LATE' THEN 1 ELSE 0 END) as lateCount " +
            "FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId")
    Object[] findStatisticsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 중복 출석 체크
     */
    @Query("SELECT COUNT(r) > 0 FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId AND r.userId = :userId")
    boolean existsBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 세션 ID로 모든 출석 기록 삭제
     */
    @Query("DELETE FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 특정 사용자의 출석 상태별 기록 조회
     */
    List<AttendanceRecord> findByUserIdAndStatus(Long userId, AttendanceStatus status);

    /**
     * 수동 수정된 출석 기록 조회
     */
    List<AttendanceRecord> findByIsManuallyAdjusted(boolean isManuallyAdjusted);

    /**
     * 특정 세션의 수동 수정된 출석 기록 조회
     */
    @Query("SELECT r FROM AttendanceRecord r WHERE r.attendanceSession.sessionId = :sessionId AND r.isManuallyAdjusted = true")
    List<AttendanceRecord> findManuallyAdjustedBySessionId(@Param("sessionId") Long sessionId);
}
package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.util.List;

/**
 * Spring Data JPA 출석 세션 리포지토리
 */
@Repository
public interface SpringDataAttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    /**
     * 랩실 ID로 출석 세션 조회
     */
    List<AttendanceSession> findByLabId(Long labId);

    /**
     * 랩실 ID와 상태로 출석 세션 조회
     */
    List<AttendanceSession> findByLabIdAndStatus(Long labId, SessionStatus status);

    /**
     * 생성자 ID로 출석 세션 조회
     */
    List<AttendanceSession> findByCreatedBy(Long createdBy);

    /**
     * 랩실 ID로 출석 세션 조회 (페이징)
     */
    Page<AttendanceSession> findByLabId(Long labId, Pageable pageable);

    /**
     * 사용자가 참여한 출석 세션 조회 (페이징)
     * - 해당 사용자의 출석 기록이 있는 세션들
     */
    @Query("SELECT DISTINCT s FROM AttendanceSession s " +
            "JOIN AttendanceRecord r ON s.sessionId = r.attendanceSession.sessionId " +
            "WHERE r.userId = :userId")
    Page<AttendanceSession> findSessionsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 랩실의 활성 세션 수 조회
     */
    long countByLabIdAndStatus(Long labId, SessionStatus status);

    /**
     * 생성자 ID와 상태로 세션 조회
     */
    List<AttendanceSession> findByCreatedByAndStatus(Long createdBy, SessionStatus status);

    /**
     * 세션 제목으로 검색 (LIKE 검색)
     */
    @Query("SELECT s FROM AttendanceSession s WHERE s.labId = :labId AND s.title LIKE %:title%")
    List<AttendanceSession> findByLabIdAndTitleContaining(@Param("labId") Long labId, @Param("title") String title);
}
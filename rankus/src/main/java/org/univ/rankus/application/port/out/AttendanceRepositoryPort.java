package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.util.List;
import java.util.Optional;

/**
 * 출석 관련 통합 리포지토리 포트
 * AttendanceRecordRepositoryPort와 AttendanceSessionRepositoryPort를 통합하고 충돌 해결
 */
public interface AttendanceRepositoryPort {

    // AttendanceRecord 관련 메서드
    AttendanceRecord save(AttendanceRecord record);

    List<AttendanceRecord> saveAll(List<AttendanceRecord> records);

    Optional<AttendanceRecord> findById(Long recordId);

    List<AttendanceRecord> findByIdIn(List<Long> recordIds);

    List<AttendanceRecord> findAll();

    List<AttendanceRecord> findBySessionId(Long sessionId);

    List<AttendanceRecord> findByUserId(Long userId);

    List<AttendanceRecord> findByUserId(Long userId, int page, int size);

    Optional<AttendanceRecord> findBySessionIdAndUserId(Long sessionId, Long userId);

    List<AttendanceRecord> findBySessionIdAndStatus(Long sessionId, AttendanceStatus status);

    List<AttendanceRecord> findByUserIdWithPaging(Long userId, int page, int size);

    List<AttendanceRecord> findByLabId(Long labId, int page, int size);

    List<AttendanceRecord> findRecordsByLabIdWithPaging(Long labId, int page, int size);

    AttendanceRecordRepositoryPort.AttendanceStatistics findStatisticsBySessionId(Long sessionId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    void delete(AttendanceRecord record);

    void deleteById(Long recordId);

    void deleteBySessionId(Long sessionId);

    // AttendanceSession 관련 메서드
    AttendanceSession saveSession(AttendanceSession session);

    Optional<AttendanceSession> findSessionById(Long sessionId);

    List<AttendanceSession> findAllSessions();

    List<AttendanceSession> findSessionsByLabId(Long labId);

    List<AttendanceSession> findByLabIdAndStatus(Long labId, SessionStatus status);

    List<AttendanceSession> findByCreatedBy(Long createdBy);

    List<AttendanceSession> findSessionsByLabIdWithPaging(Long labId, int page, int size);

    List<AttendanceSession> findSessionsByUserIdWithPaging(Long userId, int page, int size);

    long countActiveSessionsByLabId(Long labId);

    void deleteSession(AttendanceSession session);

    void deleteSessionById(Long sessionId);

    boolean existsById(Long sessionId);

    List<AttendanceSession> findActiveSessionsByLabId(Long labId);
}
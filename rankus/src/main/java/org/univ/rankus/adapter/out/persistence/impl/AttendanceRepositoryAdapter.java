package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.util.List;
import java.util.Optional;

/**
 * 출석 관련 통합 리포지토리 어댑터
 * AttendanceRecordRepositoryPort와 AttendanceSessionRepositoryPort를 통합하고 충돌 해결
 */
@Repository
@RequiredArgsConstructor
public class AttendanceRepositoryAdapter implements AttendanceRepositoryPort {

    private final AttendanceRecordRepositoryPort attendanceRecordRepository;
    private final AttendanceSessionRepositoryPort attendanceSessionRepository;

    // AttendanceRecord 관련 메서드 - 위임
    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return attendanceRecordRepository.save(record);
    }

    @Override
    public List<AttendanceRecord> saveAll(List<AttendanceRecord> records) {
        // saveAll 메서드가 없으므로 개별 save 호출
        return records.stream()
                .map(attendanceRecordRepository::save)
                .toList();
    }

    @Override
    public Optional<AttendanceRecord> findById(Long recordId) {
        return attendanceRecordRepository.findById(recordId);
    }

    @Override
    public List<AttendanceRecord> findByIdIn(List<Long> recordIds) {
        // findByIdIn 메서드가 없으므로 개별 findById 호출
        return recordIds.stream()
                .map(attendanceRecordRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return attendanceRecordRepository.findAll();
    }

    @Override
    public List<AttendanceRecord> findBySessionId(Long sessionId) {
        return attendanceRecordRepository.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> findByUserId(Long userId) {
        return attendanceRecordRepository.findByUserId(userId);
    }

    @Override
    public List<AttendanceRecord> findByUserId(Long userId, int page, int size) {
        return attendanceRecordRepository.findByUserId(userId, page, size);
    }

    @Override
    public Optional<AttendanceRecord> findBySessionIdAndUserId(Long sessionId, Long userId) {
        return attendanceRecordRepository.findBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public List<AttendanceRecord> findBySessionIdAndStatus(Long sessionId, AttendanceStatus status) {
        return attendanceRecordRepository.findBySessionIdAndStatus(sessionId, status);
    }

    @Override
    public List<AttendanceRecord> findByUserIdWithPaging(Long userId, int page, int size) {
        return attendanceRecordRepository.findByUserIdWithPaging(userId, page, size);
    }

    @Override
    public List<AttendanceRecord> findByLabId(Long labId, int page, int size) {
        return attendanceRecordRepository.findByLabId(labId, page, size);
    }

    @Override
    public List<AttendanceRecord> findRecordsByLabIdWithPaging(Long labId, int page, int size) {
        return attendanceRecordRepository.findByLabIdWithPaging(labId, page, size);
    }

    @Override
    public AttendanceRecordRepositoryPort.AttendanceStatistics findStatisticsBySessionId(Long sessionId) {
        return attendanceRecordRepository.findStatisticsBySessionId(sessionId);
    }

    @Override
    public boolean existsBySessionIdAndUserId(Long sessionId, Long userId) {
        return attendanceRecordRepository.existsBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public void delete(AttendanceRecord record) {
        attendanceRecordRepository.delete(record);
    }

    @Override
    public void deleteById(Long recordId) {
        attendanceRecordRepository.deleteById(recordId);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        attendanceRecordRepository.deleteBySessionId(sessionId);
    }

    // AttendanceSession 관련 메서드 - 위임
    @Override
    public AttendanceSession saveSession(AttendanceSession session) {
        return attendanceSessionRepository.save(session);
    }

    @Override
    public Optional<AttendanceSession> findSessionById(Long sessionId) {
        return attendanceSessionRepository.findById(sessionId);
    }

    @Override
    public List<AttendanceSession> findAllSessions() {
        return attendanceSessionRepository.findAll();
    }

    @Override
    public List<AttendanceSession> findSessionsByLabId(Long labId) {
        return attendanceSessionRepository.findByLabId(labId);
    }

    @Override
    public List<AttendanceSession> findByLabIdAndStatus(Long labId, SessionStatus status) {
        return attendanceSessionRepository.findByLabIdAndStatus(labId, status);
    }

    @Override
    public List<AttendanceSession> findByCreatedBy(Long createdBy) {
        return attendanceSessionRepository.findByCreatedBy(createdBy);
    }

    @Override
    public List<AttendanceSession> findSessionsByLabIdWithPaging(Long labId, int page, int size) {
        return attendanceSessionRepository.findSessionsByLabId(labId, page, size);
    }

    @Override
    public List<AttendanceSession> findSessionsByUserIdWithPaging(Long userId, int page, int size) {
        return attendanceSessionRepository.findSessionsByUserId(userId, page, size);
    }

    @Override
    public long countActiveSessionsByLabId(Long labId) {
        return attendanceSessionRepository.countActiveSessionsByLabId(labId);
    }

    @Override
    public void deleteSession(AttendanceSession session) {
        attendanceSessionRepository.delete(session);
    }

    @Override
    public void deleteSessionById(Long sessionId) {
        attendanceSessionRepository.deleteById(sessionId);
    }

    @Override
    public boolean existsById(Long sessionId) {
        return attendanceSessionRepository.existsById(sessionId);
    }

    @Override
    public List<AttendanceSession> findActiveSessionsByLabId(Long labId) {
        return attendanceSessionRepository.findActiveSessionsByLabId(labId);
    }
}
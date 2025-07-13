package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataAttendanceRecordRepository;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.util.List;
import java.util.Optional;

/**
 * 출석 기록 리포지토리 어댑터
 */
@Repository
@RequiredArgsConstructor
public class AttendanceRecordRepositoryAdapter implements AttendanceRecordRepositoryPort {

    private final SpringDataAttendanceRecordRepository jpaRepository;

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return jpaRepository.save(record);
    }

    @Override
    public Optional<AttendanceRecord> findById(Long recordId) {
        return jpaRepository.findById(recordId);
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<AttendanceRecord> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<AttendanceRecord> findBySessionIdAndUserId(Long sessionId, Long userId) {
        return jpaRepository.findBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public List<AttendanceRecord> findBySessionIdAndStatus(Long sessionId, AttendanceStatus status) {
        return jpaRepository.findBySessionIdAndStatus(sessionId, status);
    }

    @Override
    public List<AttendanceRecord> findByUserIdWithPaging(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkedAt"));
        return jpaRepository.findByUserId(userId, pageable).getContent();
    }

    @Override
    public List<AttendanceRecord> findByLabIdWithPaging(Long labId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkedAt"));
        return jpaRepository.findByLabId(labId, pageable).getContent();
    }

    @Override
    public AttendanceStatistics findStatisticsBySessionId(Long sessionId) {
        Object[] result = jpaRepository.findStatisticsBySessionId(sessionId);

        if (result == null || result.length == 0) {
            return new AttendanceStatistics(0L, 0L, 0L, 0L);
        }

        Long totalCount = (Long) result[0];
        Long presentCount = (Long) result[1];
        Long absentCount = (Long) result[2];
        Long lateCount = (Long) result[3];

        return new AttendanceStatistics(
                totalCount != null ? totalCount : 0L,
                presentCount != null ? presentCount : 0L,
                absentCount != null ? absentCount : 0L,
                lateCount != null ? lateCount : 0L
        );
    }

    @Override
    public boolean existsBySessionIdAndUserId(Long sessionId, Long userId) {
        return jpaRepository.existsBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public void delete(AttendanceRecord record) {
        jpaRepository.delete(record);
    }

    @Override
    public void deleteById(Long recordId) {
        jpaRepository.deleteById(recordId);
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        jpaRepository.deleteBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> findByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkedAt"));
        return jpaRepository.findByUserId(userId, pageable).getContent();
    }

    @Override
    public List<AttendanceRecord> findByLabId(Long labId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkedAt"));
        return jpaRepository.findByLabId(labId, pageable).getContent();
    }
}
package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataAttendanceSessionRepository;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.util.List;
import java.util.Optional;

/**
 * 출석 세션 리포지토리 어댑터
 */
@Repository
@RequiredArgsConstructor
public class AttendanceSessionRepositoryAdapter implements AttendanceSessionRepositoryPort {

    private final SpringDataAttendanceSessionRepository jpaRepository;

    @Override
    public AttendanceSession save(AttendanceSession session) {
        return jpaRepository.save(session);
    }

    @Override
    public Optional<AttendanceSession> findById(Long sessionId) {
        return jpaRepository.findById(sessionId);
    }

    @Override
    public List<AttendanceSession> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<AttendanceSession> findByLabId(Long labId) {
        return jpaRepository.findByLabId(labId);
    }

    @Override
    public List<AttendanceSession> findByLabIdAndStatus(Long labId, SessionStatus status) {
        return jpaRepository.findByLabIdAndStatus(labId, status);
    }

    @Override
    public List<AttendanceSession> findByCreatedBy(Long createdBy) {
        return jpaRepository.findByCreatedBy(createdBy);
    }

    @Override
    public List<AttendanceSession> findByLabIdWithPaging(Long labId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findByLabId(labId, pageable).getContent();
    }

    @Override
    public List<AttendanceSession> findSessionsByUserIdWithPaging(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findSessionsByUserId(userId, pageable).getContent();
    }

    @Override
    public long countActiveSessionsByLabId(Long labId) {
        return jpaRepository.countByLabIdAndStatus(labId, SessionStatus.ACTIVE);
    }

    @Override
    public void delete(AttendanceSession session) {
        jpaRepository.delete(session);
    }

    @Override
    public void deleteById(Long sessionId) {
        jpaRepository.deleteById(sessionId);
    }

    @Override
    public boolean existsById(Long sessionId) {
        return jpaRepository.existsById(sessionId);
    }

    @Override
    public List<AttendanceSession> findActiveSessionsByLabId(Long labId) {
        return jpaRepository.findByLabIdAndStatus(labId, SessionStatus.ACTIVE);
    }

    @Override
    public List<AttendanceSession> findSessionsByLabId(Long labId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findByLabId(labId, pageable).getContent();
    }

    @Override
    public List<AttendanceSession> findSessionsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findSessionsByUserId(userId, pageable).getContent();
    }
}
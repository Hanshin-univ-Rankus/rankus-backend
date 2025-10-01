package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendancePermissionException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.util.List;

/**
 * 출석 세션 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceSessionQueryService implements AttendanceSessionQueryUseCase {

    private final AttendanceSessionRepositoryPort attendanceSessionRepository;
    private final AttendanceRecordRepositoryPort attendanceRecordRepository;
    private final UserRepositoryPort userRepository;
    private final LabRepositoryPort labRepository;

    @Override
    public AttendanceSession findSessionById(Long sessionId, Long userId) {
        // 1. 세션 조회
        AttendanceSession session = findSessionByIdOrThrow(sessionId);

        // 2. 권한 검증
        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanViewAttendance(user, lab);

        return session;
    }

    @Override
    public AttendanceSession findSessionByIdPublic(Long sessionId) {
        // 권한 검증 없이 존재/상태만 확인 (QR resolve 용)
        return attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AttendanceValidationException(AttendanceErrorCode.SESSION_NOT_FOUND));
    }

    @Override
    public List<AttendanceSession> findActiveSessionsByLabId(Long labId, Long userId) {
        // 1. 권한 검증
        User user = findUserById(userId);
        Lab lab = findLabById(labId);

        validateCanViewAttendance(user, lab);

        // 2. 활성 세션 조회
        return attendanceSessionRepository.findByLabIdAndStatus(labId, SessionStatus.ACTIVE);
    }

    @Override
    public List<AttendanceSession> findSessionsByLabId(Long labId, Long userId, int page, int size) {
        // 1. 권한 검증
        User user = findUserById(userId);
        Lab lab = findLabById(labId);

        validateCanViewAttendance(user, lab);

        // 2. 세션 목록 조회 (페이징)
        return attendanceSessionRepository.findByLabIdWithPaging(labId, page, size);
    }

    @Override
    public AttendanceSession.AttendanceStatistics getSessionStatistics(Long sessionId, Long userId) {
        // 1. 세션 조회 및 권한 검증
        AttendanceSession session = findSessionById(sessionId, userId);

        // 2. 출석 통계 조회
        var statistics = attendanceRecordRepository.findStatisticsBySessionId(sessionId);

        // 3. 도메인 통계 객체로 변환
        return AttendanceSession.AttendanceStatistics.from(
                statistics.getTotalCount(),
                statistics.getPresentCount(),
                statistics.getAbsentCount(),
                statistics.getLateCount()
        );
    }

    @Override
    public List<AttendanceSession> findSessionsByUserId(Long userId, int page, int size) {
        // 1. 사용자 조회
        User user = findUserById(userId);

        // 2. 사용자가 참여한 세션 조회
        return attendanceSessionRepository.findSessionsByUserIdWithPaging(userId, page, size);
    }

    private AttendanceSession findSessionByIdOrThrow(Long sessionId) {
        return attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AttendanceValidationException(AttendanceErrorCode.SESSION_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    private Lab findLabById(Long labId) {
        return labRepository.findById(labId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND, "랩실을 찾을 수 없습니다"));
    }

    private void validateCanViewAttendance(User user, Lab lab) {
        // 출석 조회 권한: 랩실 멤버 이상
        if (!user.canViewLabAttendance(lab)) {
            throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
        }
    }
}
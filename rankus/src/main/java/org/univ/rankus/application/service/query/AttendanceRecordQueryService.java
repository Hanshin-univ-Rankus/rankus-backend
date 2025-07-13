package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.AttendanceRecordQueryUseCase;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendancePermissionException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.util.List;

/**
 * 출석 기록 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceRecordQueryService implements AttendanceRecordQueryUseCase {

    private final AttendanceRecordRepositoryPort attendanceRecordRepository;
    private final AttendanceSessionRepositoryPort attendanceSessionRepository;
    private final UserRepositoryPort userRepository;
    private final LabRepositoryPort labRepository;

    @Override
    public AttendanceRecord findRecordById(Long recordId, Long userId) {
        // 1. 출석 기록 조회
        AttendanceRecord record = findRecordByIdOrThrow(recordId);

        // 2. 세션 조회
        AttendanceSession session = findSessionById(record.getSessionId());

        // 3. 권한 검증
        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanViewAttendanceRecord(user, lab, record);

        return record;
    }

    @Override
    public List<AttendanceRecord> findRecordsBySessionId(Long sessionId, Long userId) {
        // 1. 세션 조회
        AttendanceSession session = findSessionById(sessionId);

        // 2. 권한 검증
        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanViewAttendance(user, lab);

        // 3. 출석 기록 조회
        return attendanceRecordRepository.findBySessionId(sessionId);
    }

    @Override
    public AttendanceRecord findRecordBySessionIdAndUserId(Long sessionId, Long targetUserId, Long requestUserId) {
        // 1. 세션 조회
        AttendanceSession session = findSessionById(sessionId);

        // 2. 권한 검증
        User requestUser = findUserById(requestUserId);
        User targetUser = findUserById(targetUserId);
        Lab lab = findLabById(session.getLabId());

        validateCanViewUserAttendance(requestUser, targetUser, lab);

        // 3. 출석 기록 조회
        return attendanceRecordRepository.findBySessionIdAndUserId(sessionId, targetUserId)
                .orElse(null);
    }

    @Override
    public List<AttendanceRecord> findRecordsByUserId(Long userId, Long requestUserId, int page, int size) {
        // 1. 권한 검증
        User requestUser = findUserById(requestUserId);
        User targetUser = findUserById(userId);

        validateCanViewUserAttendance(requestUser, targetUser);

        // 2. 출석 기록 조회 (페이징)
        return attendanceRecordRepository.findByUserIdWithPaging(userId, page, size);
    }

    @Override
    public List<AttendanceRecord> findRecordsByLabId(Long labId, Long userId, int page, int size) {
        // 1. 권한 검증
        User user = findUserById(userId);
        Lab lab = findLabById(labId);

        validateCanViewAttendance(user, lab);

        // 2. 랩실 출석 기록 조회 (페이징)
        return attendanceRecordRepository.findByLabIdWithPaging(labId, page, size);
    }

    private AttendanceRecord findRecordByIdOrThrow(Long recordId) {
        return attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.RECORD_NOT_FOUND));
    }

    private AttendanceSession findSessionById(Long sessionId) {
        return attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    private Lab findLabById(Long labId) {
        return labRepository.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
    }

    private void validateCanViewAttendance(User user, Lab lab) {
        // 출석 조회 권한: 랩실 멤버 이상
        if (!user.canViewLabAttendance(lab)) {
            throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
        }
    }

    private void validateCanViewAttendanceRecord(User user, Lab lab, AttendanceRecord record) {
        // 본인의 출석 기록이거나 랩실 관리 권한이 있는 경우
        if (record.getUserId().equals(user.getUserId()) || user.canManageLabAttendance(lab)) {
            return;
        }

        // 랩실 멤버라면 다른 멤버의 출석 기록도 조회 가능
        if (user.canViewLabAttendance(lab)) {
            return;
        }

        throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
    }

    private void validateCanViewUserAttendance(User requestUser, User targetUser, Lab lab) {
        // 본인의 출석 기록이거나 랩실 관리 권한이 있는 경우
        if (requestUser.getUserId().equals(targetUser.getUserId()) || requestUser.canManageLabAttendance(lab)) {
            return;
        }

        // 랩실 멤버라면 다른 멤버의 출석 기록도 조회 가능
        if (requestUser.canViewLabAttendance(lab)) {
            return;
        }

        throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
    }

    private void validateCanViewUserAttendance(User requestUser, User targetUser) {
        // 본인의 출석 기록이거나 관리자인 경우
        if (requestUser.getUserId().equals(targetUser.getUserId()) || requestUser.isAdmin()) {
            return;
        }

        throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
    }
}
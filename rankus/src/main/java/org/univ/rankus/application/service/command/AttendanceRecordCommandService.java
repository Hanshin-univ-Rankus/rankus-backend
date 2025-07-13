package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.AttendanceRecordCommandUseCase;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendancePermissionException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

/**
 * 출석 기록 명령 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceRecordCommandService implements AttendanceRecordCommandUseCase {

    private final AttendanceRecordRepositoryPort attendanceRecordRepository;
    private final AttendanceSessionRepositoryPort attendanceSessionRepository;
    private final UserRepositoryPort userRepository;
    private final LabRepositoryPort labRepository;

    @Override
    public AttendanceRecord markAsAbsent(Long recordId, Long adjustedBy, String reason) {
        return updateAttendanceStatus(recordId, AttendanceStatus.ABSENT, adjustedBy, reason);
    }

    @Override
    public AttendanceRecord markAsLate(Long recordId, Long adjustedBy, String reason) {
        return updateAttendanceStatus(recordId, AttendanceStatus.LATE, adjustedBy, reason);
    }

    @Override
    public AttendanceRecord markAsPresent(Long recordId, Long adjustedBy, String reason) {
        return updateAttendanceStatus(recordId, AttendanceStatus.PRESENT, adjustedBy, reason);
    }

    @Override
    public AttendanceRecord updateAttendanceStatus(Long recordId, AttendanceStatus newStatus, Long adjustedBy, String reason) {
        // 1. 출석 기록 조회
        AttendanceRecord record = findRecordById(recordId);

        // 2. 세션 조회
        AttendanceSession session = findSessionById(record.getSessionId());

        // 3. 권한 검증
        User adjuster = findUserById(adjustedBy);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(adjuster, lab);

        // 4. 출석 상태 변경 (도메인 로직)
        record.adjustStatus(newStatus, adjustedBy, reason);

        // 5. 저장 및 반환
        return attendanceRecordRepository.save(record);
    }

    private AttendanceRecord findRecordById(Long recordId) {
        return attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.RECORD_NOT_FOUND));
    }

    private AttendanceSession findSessionById(Long sessionId) {
        return attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND, "사용자를 찾을 수 없습니다"));
    }

    private Lab findLabById(Long labId) {
        return labRepository.findById(labId)
                .orElseThrow(() -> new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND, "랩실을 찾을 수 없습니다"));
    }

    private void validateCanManageAttendance(User user, Lab lab) {
        // 출석 관리 권한: 랩실 관리자, 랩장, 교수, 관리자
        if (!user.canManageLabAttendance(lab)) {
            throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
        }
    }
}
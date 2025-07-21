package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.AttendanceSessionCommandUseCase;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;
import org.univ.rankus.domain.model.attendance.SecureQRToken;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendancePermissionException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDateTime;

/**
 * 출석 세션 명령 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceSessionCommandService implements AttendanceSessionCommandUseCase {

    private final AttendanceSessionRepositoryPort attendanceSessionRepository;
    private final AttendanceRecordRepositoryPort attendanceRecordRepository;
    private final UserRepositoryPort userRepository;
    private final LabRepositoryPort labRepository;

    @Override
    public AttendanceSession createSession(Long labId, String title, Integer qrValidityMinutes, Long createdBy) {
        // 1. 권한 검증
        User creator = findUserById(createdBy);
        Lab lab = findLabById(labId);

        validateCanManageAttendance(creator, lab);

        // 2. 도메인 객체 생성
        AttendanceSession session = AttendanceSession.create(labId, createdBy, title, qrValidityMinutes);

        // 3. 저장 및 반환
        return attendanceSessionRepository.save(session);
    }

    @Override
    public AttendanceSession endSession(Long labId, Long sessionId, Long userId) {
        // 1. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(user, lab);

        // 2. 세션 종료
        session.endSession();

        // 3. 저장 및 반환
        return attendanceSessionRepository.save(session);
    }

    @Override
    public AttendanceSession cancelSession(Long labId, Long sessionId, Long userId) {
        // 1. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(user, lab);

        // 2. 세션 취소
        session.cancelSession();

        // 3. 저장 및 반환
        return attendanceSessionRepository.save(session);
    }

    @Override
    public AttendanceSession updateSessionTitle(Long labId, Long sessionId, String newTitle, Long userId) {
        // 1. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(user, lab);

        // 2. 제목 수정
        session.updateTitle(newTitle);

        // 3. 저장 및 반환
        return attendanceSessionRepository.save(session);
    }

    @Override
    public AttendanceSession updateQRValidityMinutes(Long labId, Long sessionId, Integer newValidityMinutes, Long userId) {
        // 1. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(user, lab);

        // 2. QR 유효시간 수정
        session.updateQRValidityMinutes(newValidityMinutes);

        // 3. 저장 및 반환
        return attendanceSessionRepository.save(session);
    }

    @Override
    public QRToken generateQRCode(Long labId, Long sessionId, Long userId) {
        // 1. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(user, lab);

        // 2. QR 코드 생성
        return session.generateQRToken();
    }

    @Override
    public SecureQRToken generateSecureQRCode(Long labId, Long sessionId, Long userId) {
        // 1. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        validateCanManageAttendance(user, lab);

        // 2. 보안 QR 코드 생성
        // TODO: 실제 환경에서는 설정에서 secretKey를 가져와야 함
        String secretKey = "MySecretKey1234567890123456789012"; // 32바이트
        return session.generateSecureQRToken(secretKey);
    }

    @Override
    public AttendanceRecord checkAttendance(Long labId, String qrToken, Long userId) {
        // 1. QR 토큰 검증 (보안 강화 + 레거시 지원)
        Long sessionId;
        LocalDateTime currentTime = LocalDateTime.now();

        // 우선 보안 토큰으로 시도, 실패시 레거시 토큰으로 폴백
        try {
            // 보안 강화된 토큰 처리
            String secretKey = "MySecretKey1234567890123456789012"; // 32바이트
            SecureQRToken.QRTokenPayload payload = SecureQRToken.decrypt(qrToken, secretKey);

            // 토큰 만료 검증
            if (currentTime.isAfter(payload.getParsedExpiresAt())) {
                throw new AttendancePermissionException(AttendanceErrorCode.QR_TOKEN_EXPIRED);
            }

            // 경로 일관성 검증
            validatePathConsistency(labId, payload.getLabId());
            sessionId = payload.getSessionId();

        } catch (Exception e) {
            // 보안 토큰 복호화 실패, 레거시 토큰으로 시도
            try {
                QRToken legacyToken = QRToken.fromString(qrToken);

                // 레거시 토큰 만료 검증
                if (legacyToken.isExpired()) {
                    throw new AttendancePermissionException(AttendanceErrorCode.QR_TOKEN_EXPIRED);
                }

                sessionId = legacyToken.getSessionId();

            } catch (AttendanceValidationException validationException) {
                // 레거시 토큰 검증 실패 - 원본 예외 유지
                throw validationException;
            } catch (Exception legacyException) {
                // 기타 예외는 일반적인 토큰 무효 처리
                throw new AttendancePermissionException(AttendanceErrorCode.QR_TOKEN_INVALID);
            }
        }

        // 2. 세션 조회 및 경로 일관성 검증
        AttendanceSession session = findSessionById(sessionId);
        validatePathConsistency(labId, session.getLabId());

        User user = findUserById(userId);
        Lab lab = findLabById(session.getLabId());

        // 3. 출석 권한 검증 (랩실 멤버인지 확인)
        validateCanCheckAttendance(user, lab);

        // 4. 중복 출석 체크
        boolean alreadyChecked = attendanceRecordRepository.existsBySessionIdAndUserId(session.getSessionId(), userId);
        if (alreadyChecked) {
            throw new AttendancePermissionException(AttendanceErrorCode.ALREADY_CHECKED_IN);
        }

        // 5. 출석 체크 (도메인 로직)
        AttendanceRecord record = session.checkAttendance(userId, currentTime);

        // 6. 저장 및 반환
        return attendanceRecordRepository.save(record);
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

    private void validateCanManageAttendance(User user, Lab lab) {
        // 출석 관리 권한: 랩실 관리자, 랩장, 교수, 관리자
        if (!user.canManageLabAttendance(lab)) {
            throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
        }
    }

    private void validateCanCheckAttendance(User user, Lab lab) {
        // 출석 체크 권한: 랩실 멤버 이상
        if (!user.canViewLabAttendance(lab)) {
            throw new AttendancePermissionException(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
        }
    }

    private void validatePathConsistency(Long pathLabId, Long sessionLabId) {
        // 경로의 labId와 세션이 속한 labId가 일치하는지 검증
        if (!pathLabId.equals(sessionLabId)) {
            throw new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND);
        }
    }
}
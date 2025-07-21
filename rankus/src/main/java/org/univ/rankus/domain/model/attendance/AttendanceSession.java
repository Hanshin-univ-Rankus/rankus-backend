package org.univ.rankus.domain.model.attendance;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 출석 세션 엔티티 (Root Aggregate)
 * - 출석 관리의 핵심 도메인 객체
 * - QR 코드 생성, 출석 체크, 세션 관리 등의 비즈니스 로직 포함
 */
@Getter
@Entity
@Table(name = "attendance_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(nullable = false)
    private Long labId;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer qrValidityMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @OneToMany(mappedBy = "attendanceSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    private AttendanceSession(Long labId, Long createdBy, String title, Integer qrValidityMinutes) {
        validateInputs(labId, createdBy, title, qrValidityMinutes);

        this.labId = labId;
        this.createdBy = createdBy;
        this.title = title;
        this.qrValidityMinutes = qrValidityMinutes;
        this.startTime = LocalDateTime.now();
        this.status = SessionStatus.ACTIVE;
    }

    /**
     * 출석 세션 생성 팩토리 메서드
     */
    public static AttendanceSession create(Long labId, Long createdBy, String title, Integer qrValidityMinutes) {
        return new AttendanceSession(labId, createdBy, title, qrValidityMinutes);
    }

    /**
     * QR 토큰 생성 (legacy 지원)
     *
     * @return 생성된 QR 토큰
     */
    public QRToken generateQRToken() {
        validateSessionActive();
        return QRToken.create(this.labId, this.sessionId, this.qrValidityMinutes);
    }

    /**
     * 보안 강화된 QR 토큰 생성
     *
     * @param secretKey 암호화 키 (32바이트)
     * @return 생성된 보안 QR 토큰
     */
    public SecureQRToken generateSecureQRToken(String secretKey) {
        validateSessionActive();
        return SecureQRToken.create(this.labId, this.sessionId, this.qrValidityMinutes, secretKey);
    }

    /**
     * 출석 체크 처리
     *
     * @param userId    사용자 ID
     * @param checkedAt 체크 시간
     * @return 생성된 출석 기록
     */
    public AttendanceRecord checkAttendance(Long userId, LocalDateTime checkedAt) {
        validateSessionActive();
        validateUserNotAlreadyChecked(userId);

        AttendanceRecord record = AttendanceRecord.create(this, userId, checkedAt);
        this.attendanceRecords.add(record);

        return record;
    }

    /**
     * 출석 세션 종료
     */
    public void endSession() {
        validateSessionActive();
        this.status = SessionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    /**
     * 출석 세션 취소
     */
    public void cancelSession() {
        validateSessionActive();
        this.status = SessionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    /**
     * 세션 제목 수정
     *
     * @param newTitle 새 제목
     */
    public void updateTitle(String newTitle) {
        validateSessionActive();
        validateTitle(newTitle);
        this.title = newTitle;
    }

    /**
     * QR 유효시간 수정
     *
     * @param newValidityMinutes 새 유효시간
     */
    public void updateQRValidityMinutes(Integer newValidityMinutes) {
        validateSessionActive();
        validateQRValidityMinutes(newValidityMinutes);
        this.qrValidityMinutes = newValidityMinutes;
    }

    /**
     * 세션 소유자 확인
     *
     * @param userId 사용자 ID
     * @return 소유자 여부
     */
    public boolean isOwnedBy(Long userId) {
        return this.createdBy.equals(userId);
    }

    /**
     * 출석 통계 계산
     *
     * @return 출석 통계
     */
    public AttendanceStatistics calculateStatistics() {
        long presentCount = attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absentCount = attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long lateCount = attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.LATE)
                .count();

        return new AttendanceStatistics(
                attendanceRecords.size(),
                (int) presentCount,
                (int) absentCount,
                (int) lateCount
        );
    }

    /**
     * 특정 사용자의 출석 기록 조회
     *
     * @param userId 사용자 ID
     * @return 출석 기록
     */
    public AttendanceRecord getAttendanceRecord(Long userId) {
        return attendanceRecords.stream()
                .filter(record -> record.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private void validateSessionActive() {
        if (this.status != SessionStatus.ACTIVE) {
            throw new AttendanceValidationException(AttendanceErrorCode.SESSION_NOT_ACTIVE);
        }
    }

    private void validateUserNotAlreadyChecked(Long userId) {
        boolean alreadyChecked = attendanceRecords.stream()
                .anyMatch(record -> record.getUserId().equals(userId));

        if (alreadyChecked) {
            throw new AttendanceValidationException(AttendanceErrorCode.ALREADY_CHECKED_IN);
        }
    }

    private static void validateInputs(Long labId, Long createdBy, String title, Integer qrValidityMinutes) {
        if (labId == null || labId <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.LAB_ID_REQUIRED);
        }

        if (createdBy == null || createdBy <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.CREATOR_ID_REQUIRED);
        }

        validateTitle(title);
        validateQRValidityMinutes(qrValidityMinutes);
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new AttendanceValidationException(AttendanceErrorCode.TITLE_REQUIRED);
        }

        if (title.length() > 100) {
            throw new AttendanceValidationException(AttendanceErrorCode.TITLE_TOO_LONG);
        }
    }

    private static void validateQRValidityMinutes(Integer qrValidityMinutes) {
        if (qrValidityMinutes == null) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_VALIDITY_REQUIRED);
        }

        if (qrValidityMinutes < 1 || qrValidityMinutes > 10) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_VALIDITY_INVALID);
        }
    }

    /**
     * 출석 통계 값 객체
     */
    @Getter
    public static class AttendanceStatistics {
        private final int totalMembers;
        private final int presentCount;
        private final int absentCount;
        private final int lateCount;

        public AttendanceStatistics(int totalMembers, int presentCount, int absentCount, int lateCount) {
            this.totalMembers = totalMembers;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
        }

        public double getAttendanceRate() {
            if (totalMembers == 0) return 0.0;
            return (double) presentCount / totalMembers * 100;
        }

        /**
         * 통계 데이터로부터 AttendanceStatistics 객체 생성
         */
        public static AttendanceStatistics from(long totalCount, long presentCount, long absentCount, long lateCount) {
            return new AttendanceStatistics(
                    (int) totalCount,
                    (int) presentCount,
                    (int) absentCount,
                    (int) lateCount
            );
        }
    }
}
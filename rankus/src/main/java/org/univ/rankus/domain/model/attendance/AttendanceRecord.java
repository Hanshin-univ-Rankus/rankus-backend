package org.univ.rankus.domain.model.attendance;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;

import java.time.LocalDateTime;

/**
 * 출석 기록 엔티티
 * - 개별 사용자의 출석 기록을 관리하는 도메인 객체
 * - 출석 상태 변경, 수동 수정 이력 등의 비즈니스 로직 포함
 */
@Getter
@Entity
@Table(name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private Boolean isManuallyAdjusted;

    @Column
    private String adjustmentReason;

    @Column
    private Long adjustedBy;

    @Column
    private LocalDateTime adjustedAt;

    private AttendanceRecord(AttendanceSession attendanceSession, Long userId, LocalDateTime checkedAt) {
        validateInputs(attendanceSession, userId, checkedAt);

        this.attendanceSession = attendanceSession;
        this.userId = userId;
        this.checkedAt = checkedAt;
        this.status = AttendanceStatus.PRESENT;
        this.isManuallyAdjusted = false;
    }

    /**
     * 출석 기록 생성 팩토리 메서드
     */
    public static AttendanceRecord create(AttendanceSession attendanceSession, Long userId, LocalDateTime checkedAt) {
        return new AttendanceRecord(attendanceSession, userId, checkedAt);
    }

    /**
     * 출석 상태를 결석으로 변경
     *
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     */
    public void markAsAbsent(Long adjustedBy, String reason) {
        validateManualAdjustment(adjustedBy, reason);
        updateStatus(AttendanceStatus.ABSENT, adjustedBy, reason);
    }

    /**
     * 출석 상태를 지각으로 변경
     *
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     */
    public void markAsLate(Long adjustedBy, String reason) {
        validateManualAdjustment(adjustedBy, reason);
        updateStatus(AttendanceStatus.LATE, adjustedBy, reason);
    }

    /**
     * 출석 상태를 출석으로 변경
     *
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     */
    public void markAsPresent(Long adjustedBy, String reason) {
        validateManualAdjustment(adjustedBy, reason);
        updateStatus(AttendanceStatus.PRESENT, adjustedBy, reason);
    }

    /**
     * 출석 상태 직접 변경
     *
     * @param newStatus  새 상태
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     */
    public void updateStatus(AttendanceStatus newStatus, Long adjustedBy, String reason) {
        validateManualAdjustment(adjustedBy, reason);
        validateStatusChange(newStatus);

        this.status = newStatus;
        this.isManuallyAdjusted = true;
        this.adjustedBy = adjustedBy;
        this.adjustmentReason = reason;
        this.adjustedAt = LocalDateTime.now();
    }

    /**
     * 출석 상태 수정 (alias method)
     */
    public void adjustStatus(AttendanceStatus newStatus, Long adjustedBy, String reason) {
        updateStatus(newStatus, adjustedBy, reason);
    }

    /**
     * 출석 기록 소유자 확인
     *
     * @param userId 사용자 ID
     * @return 소유자 여부
     */
    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    /**
     * 세션 소속 확인
     *
     * @param sessionId 세션 ID
     * @return 소속 여부
     */
    public boolean belongsToSession(Long sessionId) {
        return this.attendanceSession.getSessionId().equals(sessionId);
    }

    /**
     * 세션 ID 반환
     */
    public Long getSessionId() {
        return this.attendanceSession.getSessionId();
    }

    /**
     * 수동 수정 여부 확인
     *
     * @return 수동 수정 여부
     */
    public boolean isManuallyAdjusted() {
        return this.isManuallyAdjusted;
    }

    /**
     * 출석 상태 확인 메서드들
     */
    public boolean isPresent() {
        return this.status == AttendanceStatus.PRESENT;
    }

    public boolean isAbsent() {
        return this.status == AttendanceStatus.ABSENT;
    }

    public boolean isLate() {
        return this.status == AttendanceStatus.LATE;
    }

    /**
     * 출석 체크 시간과 세션 시작 시간 비교
     *
     * @return 지각 여부
     */
    public boolean isCheckedInLate() {
        LocalDateTime sessionStartTime = this.attendanceSession.getStartTime();
        // 세션 시작 후 5분 이후 체크인 시 지각으로 간주
        return this.checkedAt.isAfter(sessionStartTime.plusMinutes(5));
    }

    /**
     * 출석 기록 요약 정보 반환
     *
     * @return 출석 기록 요약
     */
    public AttendanceRecordSummary getSummary() {
        return new AttendanceRecordSummary(
                this.recordId,
                this.userId,
                this.status,
                this.checkedAt,
                this.isManuallyAdjusted,
                this.adjustmentReason,
                this.adjustedBy,
                this.adjustedAt
        );
    }

    private void validateManualAdjustment(Long adjustedBy, String reason) {
        if (adjustedBy == null || adjustedBy <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.ADJUSTED_BY_REQUIRED);
        }

        if (reason == null || reason.isBlank()) {
            throw new AttendanceValidationException(AttendanceErrorCode.ADJUSTMENT_REASON_REQUIRED);
        }

        if (reason.length() > 200) {
            throw new AttendanceValidationException(AttendanceErrorCode.ADJUSTMENT_REASON_TOO_LONG);
        }
    }

    private void validateStatusChange(AttendanceStatus newStatus) {
        if (newStatus == null) {
            throw new AttendanceValidationException(AttendanceErrorCode.STATUS_REQUIRED);
        }

        if (this.status == newStatus) {
            throw new AttendanceValidationException(AttendanceErrorCode.INVALID_STATUS_TRANSITION, "동일한 상태로는 변경할 수 없습니다");
        }
    }

    private static void validateInputs(AttendanceSession attendanceSession, Long userId, LocalDateTime checkedAt) {
        if (attendanceSession == null) {
            throw new AttendanceValidationException(AttendanceErrorCode.SESSION_NOT_FOUND);
        }

        if (userId == null || userId <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.USER_ID_REQUIRED);
        }

        if (checkedAt == null) {
            throw new AttendanceValidationException(AttendanceErrorCode.CHECKED_AT_REQUIRED);
        }
    }

    /**
     * 출석 기록 요약 값 객체
     */
    @Getter
    public static class AttendanceRecordSummary {
        private final Long recordId;
        private final Long userId;
        private final AttendanceStatus status;
        private final LocalDateTime checkedAt;
        private final Boolean isManuallyAdjusted;
        private final String adjustmentReason;
        private final Long adjustedBy;
        private final LocalDateTime adjustedAt;

        public AttendanceRecordSummary(Long recordId, Long userId, AttendanceStatus status,
                                       LocalDateTime checkedAt, Boolean isManuallyAdjusted,
                                       String adjustmentReason, Long adjustedBy, LocalDateTime adjustedAt) {
            this.recordId = recordId;
            this.userId = userId;
            this.status = status;
            this.checkedAt = checkedAt;
            this.isManuallyAdjusted = isManuallyAdjusted;
            this.adjustmentReason = adjustmentReason;
            this.adjustedBy = adjustedBy;
            this.adjustedAt = adjustedAt;
        }
    }
}
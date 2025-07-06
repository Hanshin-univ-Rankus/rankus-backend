package org.univ.rankus.domain.model.interview;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;

import java.time.LocalDateTime;

/**
 * 면접 슬롯 엔티티
 * - 구체적인 면접 시간 슬롯 관리
 * - 슬롯별 지원자 수 제한
 * - 슬롯 예약 상태 관리
 */
@Getter
@Entity
@Table(name = "interview_slots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSlot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "max_applicants", nullable = false)
    private Integer maxApplicants;

    @Column(name = "current_applicants", nullable = false)
    private Integer currentApplicants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SlotStatus status;

    /**
     * 면접 슬롯 생성자
     *
     * @param interview     소속 면접
     * @param startTime     슬롯 시작 시간
     * @param endTime       슬롯 종료 시간
     * @param maxApplicants 최대 지원자 수
     */
    public InterviewSlot(Interview interview, LocalDateTime startTime,
                         LocalDateTime endTime, Integer maxApplicants) {

        validateInterview(interview);
        validateTimes(startTime, endTime);
        validateMaxApplicants(maxApplicants);

        this.interview = interview;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxApplicants = maxApplicants;
        this.currentApplicants = 0;
        this.status = SlotStatus.AVAILABLE;
    }

    /**
     * 지원자 예약 (슬롯에 지원자 추가)
     */
    public void reserve() {
        if (status == SlotStatus.CANCELLED) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_CANCELLED);
        }

        if (status == SlotStatus.FULL) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_FULL);
        }

        if (currentApplicants >= maxApplicants) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_FULL);
        }

        this.currentApplicants++;

        if (this.currentApplicants >= this.maxApplicants) {
            this.status = SlotStatus.FULL;
        }
    }

    /**
     * 지원자 예약 취소 (슬롯에서 지원자 제거)
     */
    public void cancelReservation() {
        if (currentApplicants <= 0) {
            throw new InterviewValidationException(InterviewErrorCode.NO_RESERVATION_TO_CANCEL);
        }

        this.currentApplicants--;

        if (this.status == SlotStatus.FULL && this.currentApplicants < this.maxApplicants) {
            this.status = SlotStatus.AVAILABLE;
        }
    }

    /**
     * 슬롯 취소
     */
    public void cancel() {
        if (status == SlotStatus.CANCELLED) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_ALREADY_CANCELLED);
        }

        if (currentApplicants > 0) {
            throw new InterviewValidationException(InterviewErrorCode.CANNOT_CANCEL_SLOT_WITH_APPLICANTS);
        }

        this.status = SlotStatus.CANCELLED;
    }

    /**
     * 슬롯 재활성화
     */
    public void reactivate() {
        if (status != SlotStatus.CANCELLED) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_NOT_CANCELLED);
        }

        this.status = currentApplicants >= maxApplicants ? SlotStatus.FULL : SlotStatus.AVAILABLE;
    }

    /**
     * 슬롯 예약 가능 여부 확인
     */
    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE && currentApplicants < maxApplicants;
    }

    /**
     * 슬롯이 가득 찬지 확인
     */
    public boolean isFull() {
        return status == SlotStatus.FULL || currentApplicants >= maxApplicants;
    }

    /**
     * 슬롯이 취소되었는지 확인
     */
    public boolean isCancelled() {
        return status == SlotStatus.CANCELLED;
    }

    /**
     * 슬롯 시간이 지났는지 확인
     */
    public boolean isPast() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * 슬롯의 남은 자리 수 반환
     */
    public int getAvailableSpots() {
        return Math.max(0, maxApplicants - currentApplicants);
    }

    /**
     * 면접 검증
     */
    private void validateInterview(Interview interview) {
        if (interview == null) {
            throw new InterviewValidationException(InterviewErrorCode.INTERVIEW_REQUIRED);
        }
    }

    /**
     * 시간 검증
     */
    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InterviewValidationException(InterviewErrorCode.TIME_REQUIRED);
        }

        if (startTime.isAfter(endTime)) {
            throw new InterviewValidationException(InterviewErrorCode.INVALID_TIME_RANGE);
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new InterviewValidationException(InterviewErrorCode.PAST_TIME_NOT_ALLOWED);
        }
    }

    /**
     * 최대 지원자 수 검증
     */
    private void validateMaxApplicants(Integer maxApplicants) {
        if (maxApplicants == null || maxApplicants <= 0) {
            throw new InterviewValidationException(InterviewErrorCode.INVALID_MAX_APPLICANTS);
        }

        if (maxApplicants > 10) { // 슬롯당 최대 10명 제한
            throw new InterviewValidationException(InterviewErrorCode.TOO_MANY_APPLICANTS);
        }
    }
}
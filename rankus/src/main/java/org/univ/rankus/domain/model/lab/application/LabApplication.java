package org.univ.rankus.domain.model.lab.application;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabApplicationErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabApplicationValidationException;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "lab_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존과 동일하게 Lab을 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    // ★ User 엔티티를 직접 참조하도록 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_slot_id", nullable = false)
    private InterviewSlot interviewSlot;  // 면접 슬롯 참조 (필수)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;     // 신청 상태 (PENDING, APPROVED, REJECTED)

    /**
     * 변경된 생성자:
     * - 기존: (Lab lab, User user, LocalDateTime interviewTime)
     * - 변경: interviewTime → InterviewSlot 참조로 변경
     */
    public LabApplication(Lab lab, User user, InterviewSlot interviewSlot) {
        if (lab == null) {
            throw new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND);
        }
        this.lab = lab;

        if (user == null) {
            throw new UserNotFoundException(UserErrorCode.USER_NOT_FOUND);
        }
        this.user = user;

        if (interviewSlot == null) {
            throw new InterviewNotFoundException(InterviewErrorCode.SLOT_NOT_FOUND);
        }

        // 면접 슬롯의 면접이 해당 랩실과 일치하는지 확인
        if (!interviewSlot.getInterview().getLab().getId().equals(lab.getId())) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.INVALID_INTERVIEW_TIME);
        }

        // 면접 슬롯이 예약 가능한지 확인
        if (!interviewSlot.isAvailable()) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.INVALID_INTERVIEW_TIME);
        }

        this.interviewSlot = interviewSlot;
        this.status = ApplicationStatus.PENDING;  // 기본 상태

        // 슬롯 예약 처리
        interviewSlot.reserve();
    }

    public void approve() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED);
        }
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED);
        }
        this.status = ApplicationStatus.REJECTED;
    }

    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }

    /**
     * 지원서 취소 시 슬롯 예약도 함께 취소
     */
    public void cancel() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED);
        }

        // 슬롯 예약 취소 처리
        if (this.interviewSlot != null) {
            this.interviewSlot.cancelReservation();
        }
    }

    /**
     * 면접 시간 조회 (호환성을 위한 메서드)
     */
    public LocalDateTime getInterviewTime() {
        return interviewSlot != null ? interviewSlot.getStartTime() : null;
    }
}
package org.univ.rankus.domain.model.interview;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 면접 설정 엔티티
 * - 랩실별 면접 기간 및 설정 관리
 * - 면접 슬롯 생성 및 관리
 * - 면접 활성화/비활성화 제어
 */
@Getter
@Entity
@Table(name = "interviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "max_applicants_per_slot", nullable = false)
    private Integer maxApplicantsPerSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterviewStatus status;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewSlot> slots = new ArrayList<>();

    /**
     * 면접 설정 생성자
     *
     * @param lab                  면접을 진행할 랩실
     * @param startDate            면접 시작일
     * @param endDate              면접 종료일
     * @param durationMinutes      면접 소요 시간 (분)
     * @param maxApplicantsPerSlot 슬롯당 최대 지원자 수
     */
    public Interview(Lab lab, LocalDate startDate, LocalDate endDate,
                     Integer durationMinutes, Integer maxApplicantsPerSlot) {

        validateLab(lab);
        validateDates(startDate, endDate);
        validateDuration(durationMinutes);
        validateMaxApplicants(maxApplicantsPerSlot);

        this.lab = lab;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinutes = durationMinutes;
        this.maxApplicantsPerSlot = maxApplicantsPerSlot;
        this.status = InterviewStatus.INACTIVE;
    }

    /**
     * 면접 활성화
     */
    public void activate() {
        if (this.status == InterviewStatus.ACTIVE) {
            throw new InterviewValidationException(InterviewErrorCode.ALREADY_ACTIVATED);
        }

        if (this.status == InterviewStatus.CLOSED) {
            throw new InterviewValidationException(InterviewErrorCode.CANNOT_ACTIVATE_CLOSED);
        }

        validateInterviewPeriod();
        this.status = InterviewStatus.ACTIVE;
    }

    /**
     * 면접 비활성화
     */
    public void deactivate() {
        if (this.status == InterviewStatus.INACTIVE) {
            throw new InterviewValidationException(InterviewErrorCode.ALREADY_DEACTIVATED);
        }

        if (this.status == InterviewStatus.CLOSED) {
            throw new InterviewValidationException(InterviewErrorCode.CANNOT_DEACTIVATE_CLOSED);
        }

        this.status = InterviewStatus.INACTIVE;
    }

    /**
     * 면접 종료
     */
    public void close() {
        if (this.status == InterviewStatus.CLOSED) {
            throw new InterviewValidationException(InterviewErrorCode.ALREADY_CLOSED);
        }

        this.status = InterviewStatus.CLOSED;
    }

    /**
     * 면접 슬롯 추가
     *
     * @param slot 추가할 면접 슬롯
     */
    public void addSlot(InterviewSlot slot) {
        if (slot == null) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_REQUIRED);
        }

        validateSlotTime(slot);
        this.slots.add(slot);
    }

    /**
     * 면접이 활성화되어 있는지 확인
     */
    public boolean isActive() {
        return this.status == InterviewStatus.ACTIVE;
    }

    /**
     * 면접이 종료되었는지 확인
     */
    public boolean isClosed() {
        return this.status == InterviewStatus.CLOSED;
    }

    /**
     * 지원 가능한 상태인지 확인
     */
    public boolean isApplicationAvailable() {
        return this.status == InterviewStatus.ACTIVE && isWithinApplicationPeriod();
    }

    /**
     * 현재 지원 가능한 기간인지 확인
     */
    private boolean isWithinApplicationPeriod() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * 랩실 검증
     */
    private void validateLab(Lab lab) {
        if (lab == null) {
            throw new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND);
        }
    }

    /**
     * 면접 기간 검증
     */
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InterviewValidationException(InterviewErrorCode.DATE_REQUIRED);
        }

        if (startDate.isAfter(endDate)) {
            throw new InterviewValidationException(InterviewErrorCode.INVALID_DATE_RANGE);
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new InterviewValidationException(InterviewErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    /**
     * 면접 소요 시간 검증
     */
    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new InterviewValidationException(InterviewErrorCode.INVALID_DURATION);
        }

        if (durationMinutes > 180) { // 3시간 초과 방지
            throw new InterviewValidationException(InterviewErrorCode.DURATION_TOO_LONG);
        }
    }

    /**
     * 최대 지원자 수 검증
     */
    private void validateMaxApplicants(Integer maxApplicantsPerSlot) {
        if (maxApplicantsPerSlot == null || maxApplicantsPerSlot <= 0) {
            throw new InterviewValidationException(InterviewErrorCode.INVALID_MAX_APPLICANTS);
        }

        if (maxApplicantsPerSlot > 10) { // 슬롯당 최대 10명 제한
            throw new InterviewValidationException(InterviewErrorCode.TOO_MANY_APPLICANTS);
        }
    }

    /**
     * 면접 기간 유효성 검증
     */
    private void validateInterviewPeriod() {
        LocalDate today = LocalDate.now();
        if (endDate.isBefore(today)) {
            throw new InterviewValidationException(InterviewErrorCode.EXPIRED_INTERVIEW_PERIOD);
        }
    }

    /**
     * 슬롯 시간 검증
     */
    private void validateSlotTime(InterviewSlot slot) {
        LocalDateTime slotStart = slot.getStartTime();
        LocalDate slotDate = slotStart.toLocalDate();

        if (slotDate.isBefore(startDate) || slotDate.isAfter(endDate)) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_OUTSIDE_INTERVIEW_PERIOD);
        }
    }
}
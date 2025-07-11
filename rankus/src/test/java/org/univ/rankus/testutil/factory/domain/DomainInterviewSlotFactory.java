package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.SlotStatus;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.time.LocalDateTime;

/**
 * DomainInterviewSlotFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 InterviewSlot 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainInterviewSlotFactory {
    private DomainInterviewSlotFactory() {
    }

    public static InterviewSlot buildValidSlot() {
        Lab lab = DomainLabFactory.buildValidLab();
        Interview interview = DomainInterviewFactory.buildInterviewWithLab(lab);
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    public static InterviewSlot buildSlotWithTime(LocalDateTime startTime) {
        Lab lab = DomainLabFactory.buildValidLab();
        Interview interview = DomainInterviewFactory.buildInterviewWithLab(lab);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    public static InterviewSlot buildSlotWithTimeAndLab(LocalDateTime startTime, Lab lab) {
        Interview interview = DomainInterviewFactory.buildInterviewWithLab(lab);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    public static InterviewSlot buildSlotWithInterview(Interview interview) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    public static InterviewSlot buildSlotWithCapacity(Integer maxApplicants) {
        Interview interview = DomainInterviewFactory.buildValidInterview();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, maxApplicants);
    }

    public static InterviewSlot buildSlotWithId(Long id) {
        InterviewSlot slot = buildValidSlot();
        ReflectionTestUtils.setField(slot, "id", id);
        return slot;
    }

    public static InterviewSlot buildSlotWithStatus(SlotStatus status) {
        InterviewSlot slot = buildValidSlot();
        ReflectionTestUtils.setField(slot, "status", status);
        return slot;
    }

    public static InterviewSlot buildAvailableSlot() {
        return buildSlotWithStatus(SlotStatus.AVAILABLE);
    }

    public static InterviewSlot buildCancelledSlot() {
        return buildSlotWithStatus(SlotStatus.CANCELLED);
    }

    public static InterviewSlot buildFullSlot() {
        InterviewSlot slot = buildSlotWithCapacity(1);
        ReflectionTestUtils.setField(slot, "currentApplicants", 1);
        return slot;
    }

    public static InterviewSlot buildSlotWithApplicants(Integer currentApplicants, Integer maxApplicants) {
        InterviewSlot slot = buildSlotWithCapacity(maxApplicants);
        ReflectionTestUtils.setField(slot, "currentApplicants", currentApplicants);
        return slot;
    }

    public static InterviewSlot buildTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Interview interview = DomainInterviewFactory.buildValidInterview();
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    public static InterviewSlot buildValidSlotForInterview(Interview interview) {
        // 면접 기간 내에서 미래 시간으로 슬롯 생성
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = interview.getStartDate().atTime(10, 0);
        LocalDateTime endTime = startTime.plusHours(1);

        // 만약 면접 시작 시간이 과거라면 미래 시간으로 조정
        if (startTime.isBefore(now)) {
            startTime = now.plusHours(1);
            endTime = startTime.plusHours(1);
        }

        return new InterviewSlot(interview, startTime, endTime, 3);
    }

    public static InterviewSlot buildSlotOutsideInterview(Interview interview) {
        LocalDateTime startTime = interview.getEndDate().plusDays(1).atTime(10, 0);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 3);
    }

    public static InterviewSlot buildSlotWithOneSpotLeft() {
        InterviewSlot slot = buildSlotWithCapacity(3);
        ReflectionTestUtils.setField(slot, "currentApplicants", 2);
        return slot;
    }

    public static InterviewSlot buildSlotWithReservations() {
        InterviewSlot slot = buildSlotWithCapacity(5);
        ReflectionTestUtils.setField(slot, "currentApplicants", 2);
        return slot;
    }

    public static InterviewSlot buildPastSlot() {
        Interview interview = DomainInterviewFactory.buildValidInterview();
        // Create valid slot first, then modify times using reflection
        LocalDateTime futureStartTime = LocalDateTime.now().plusHours(1);
        LocalDateTime futureEndTime = futureStartTime.plusHours(1);
        InterviewSlot slot = new InterviewSlot(interview, futureStartTime, futureEndTime, 3);

        // Set past times using reflection to simulate past slot
        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(2);
        LocalDateTime pastEndTime = pastStartTime.plusHours(1);
        ReflectionTestUtils.setField(slot, "startTime", pastStartTime);
        ReflectionTestUtils.setField(slot, "endTime", pastEndTime);

        return slot;
    }
}
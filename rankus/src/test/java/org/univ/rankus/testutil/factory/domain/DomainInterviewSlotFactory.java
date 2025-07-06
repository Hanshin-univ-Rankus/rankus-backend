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
}
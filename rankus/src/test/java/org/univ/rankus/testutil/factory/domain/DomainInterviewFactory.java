package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewStatus;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.time.LocalDate;

/**
 * DomainInterviewFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 Interview 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainInterviewFactory {
    private DomainInterviewFactory() {
    }

    public static Interview buildValidInterview() {
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);
        Interview interview = new Interview(lab, startDate, endDate, 60, 5);
        return interview;
    }

    public static Interview buildInterviewWithLab(Lab lab) {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);
        Interview interview = new Interview(lab, startDate, endDate, 60, 5);
        return interview;
    }

    public static Interview buildInterviewWithDates(LocalDate startDate, LocalDate endDate) {
        Lab lab = DomainLabFactory.buildValidLab();
        return new Interview(lab, startDate, endDate, 60, 5);
    }

    public static Interview buildInterviewWithDuration(Integer durationMinutes) {
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);
        return new Interview(lab, startDate, endDate, durationMinutes, 5);
    }

    public static Interview buildInterviewWithCapacity(Integer maxApplicantsPerSlot) {
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);
        return new Interview(lab, startDate, endDate, 60, maxApplicantsPerSlot);
    }

    public static Interview buildInterviewWithId(Long id) {
        Interview interview = buildValidInterview();
        ReflectionTestUtils.setField(interview, "id", id);
        return interview;
    }

    public static Interview buildInterviewWithStatus(InterviewStatus status) {
        Interview interview = buildValidInterview();
        ReflectionTestUtils.setField(interview, "status", status);
        return interview;
    }

    public static Interview buildActiveInterview() {
        return buildInterviewWithStatus(InterviewStatus.ACTIVE);
    }

    public static Interview buildInactiveInterview() {
        return buildInterviewWithStatus(InterviewStatus.INACTIVE);
    }

    public static Interview buildClosedInterview() {
        return buildInterviewWithStatus(InterviewStatus.CLOSED);
    }

    public static Interview buildDefaultInterview() {
        return buildValidInterview();
    }

    public static Interview buildInterviewForPeriod(LocalDate startDate, LocalDate endDate, Integer durationMinutes, Integer maxApplicantsPerSlot) {
        Lab lab = DomainLabFactory.buildValidLab();
        return new Interview(lab, startDate, endDate, durationMinutes, maxApplicantsPerSlot);
    }
}
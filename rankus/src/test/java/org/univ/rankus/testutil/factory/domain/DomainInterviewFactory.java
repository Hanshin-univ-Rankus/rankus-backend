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
        // Lab ID가 없으면 설정 (Interview 생성 시 필요)
        if (lab.getId() == null) {
            ReflectionTestUtils.setField(lab, "id", 1L);
        }

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
        // Create interview that spans current date so it can be activated
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now();  // Start today
        LocalDate endDate = LocalDate.now().plusDays(7);  // End in 7 days
        Interview interview = new Interview(lab, startDate, endDate, 60, 5);
        ReflectionTestUtils.setField(interview, "status", InterviewStatus.ACTIVE);
        return interview;
    }

    public static Interview buildInactiveInterview() {
        // Create interview that spans current date so it can be activated later
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now();  // Start today
        LocalDate endDate = LocalDate.now().plusDays(7);  // End in 7 days
        Interview interview = new Interview(lab, startDate, endDate, 60, 5);
        // Status is already INACTIVE by default
        return interview;
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

    public static Interview buildExpiredInterview() {
        Lab lab = DomainLabFactory.buildValidLab();
        // Create valid interview first, then modify dates using reflection
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        Interview interview = new Interview(lab, startDate, endDate, 60, 5);

        // Set past dates using reflection to simulate expired interview
        LocalDate pastStartDate = LocalDate.now().minusDays(14);
        LocalDate pastEndDate = LocalDate.now().minusDays(7);
        ReflectionTestUtils.setField(interview, "startDate", pastStartDate);
        ReflectionTestUtils.setField(interview, "endDate", pastEndDate);

        return interview;
    }
}
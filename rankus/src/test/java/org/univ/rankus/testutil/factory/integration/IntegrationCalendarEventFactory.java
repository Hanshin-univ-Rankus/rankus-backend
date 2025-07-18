package org.univ.rankus.testutil.factory.integration;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * IntegrationCalendarEventFactory - 통합 테스트 전용 팩토리
 * 데이터베이스 연동 테스트에서 CalendarEvent 엔티티 생성 메서드를 제공합니다.
 */
public final class IntegrationCalendarEventFactory {
    private IntegrationCalendarEventFactory() {
    }

    private static long eventCounter = 1L;

    public static CalendarEvent createValidSchedule() {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLab();
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                LocalDate.now().plusDays(1)
        );
    }

    public static CalendarEvent createValidScheduleWithId(Long id) {
        CalendarEvent event = createValidSchedule();
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    public static CalendarEvent createValidInterview() {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLab();
        return new CalendarEvent(
                lab,
                "Test Interview " + suffix,
                "Interview description " + suffix,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                100L + eventCounter
        );
    }

    public static CalendarEvent createValidInterviewWithId(Long id) {
        CalendarEvent event = createValidInterview();
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    public static CalendarEvent createScheduleWithLab(Lab lab) {
        String suffix = String.valueOf(eventCounter++);
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                LocalDate.now().plusDays(1)
        );
    }

    public static CalendarEvent createInterviewWithLab(Lab lab) {
        String suffix = String.valueOf(eventCounter++);
        return new CalendarEvent(
                lab,
                "Test Interview " + suffix,
                "Interview description " + suffix,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                100L + eventCounter
        );
    }

    public static CalendarEvent createScheduleWithDate(LocalDate date) {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLab();
        // ID는 설정하지 않음 - 테스트에서 영속화할 때 자동 생성
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                date
        );
    }

    public static CalendarEvent createInterviewWithDate(LocalDate date) {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLab();
        return new CalendarEvent(
                lab,
                "Test Interview " + suffix,
                "Interview description " + suffix,
                date,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                100L + eventCounter
        );
    }

    public static CalendarEvent createCustomSchedule(String title, String description, LocalDate date, Lab lab) {
        return new CalendarEvent(lab, title, description, date);
    }

    public static CalendarEvent createCustomInterview(String title, String description, LocalDate date,
                                                      LocalTime startTime, LocalTime endTime, Lab lab, Long interviewId) {
        return new CalendarEvent(lab, title, description, date, startTime, endTime, interviewId);
    }

    public static CalendarEvent createEventWithInterviewId(Long interviewId) {
        Lab lab = DomainLabFactory.buildValidLab();
        return new CalendarEvent(
                lab,
                "Interview Event",
                "Interview description",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                interviewId
        );
    }

    public static CalendarEvent createEventWithInterviewIdAndId(Long eventId, Long interviewId) {
        CalendarEvent event = createEventWithInterviewId(interviewId);
        ReflectionTestUtils.setField(event, "id", eventId);
        return event;
    }

    public static CalendarEvent createEventWithDateRange(LocalDate startDate, LocalDate endDate) {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate eventDate = startDate.plusDays((endDate.toEpochDay() - startDate.toEpochDay()) / 2);
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                eventDate
        );
    }

    public static CalendarEvent createInterviewWithDateRange(LocalDate startDate, LocalDate endDate) {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate eventDate = startDate.plusDays((endDate.toEpochDay() - startDate.toEpochDay()) / 2);
        return new CalendarEvent(
                lab,
                "Test Interview " + suffix,
                "Interview description " + suffix,
                eventDate,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                100L + eventCounter
        );
    }
}
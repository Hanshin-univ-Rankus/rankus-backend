package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DomainCalendarEventFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 CalendarEvent 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainCalendarEventFactory {
    private DomainCalendarEventFactory() {
    }

    private static long eventCounter = 1L;

    public static CalendarEvent buildValidSchedule() {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                LocalDate.now().plusDays(1)
        );
    }

    public static CalendarEvent buildValidScheduleWithId(Long id) {
        CalendarEvent event = buildValidSchedule();
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    public static CalendarEvent buildValidInterview() {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
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

    public static CalendarEvent buildValidInterviewWithId(Long id) {
        CalendarEvent event = buildValidInterview();
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    public static CalendarEvent buildScheduleWithLab(Lab lab) {
        String suffix = String.valueOf(eventCounter++);
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                LocalDate.now().plusDays(1)
        );
    }

    public static CalendarEvent buildInterviewWithLab(Lab lab) {
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

    public static CalendarEvent buildScheduleWithDate(LocalDate date) {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        return new CalendarEvent(
                lab,
                "Test Schedule " + suffix,
                "Schedule description " + suffix,
                date
        );
    }

    public static CalendarEvent buildInterviewWithDate(LocalDate date) {
        String suffix = String.valueOf(eventCounter++);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
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

    public static CalendarEvent buildCustomSchedule(String title, String description, LocalDate date, Lab lab) {
        return new CalendarEvent(lab, title, description, date);
    }

    public static CalendarEvent buildCustomInterview(String title, String description, LocalDate date,
                                                     LocalTime startTime, LocalTime endTime, Lab lab, Long interviewId) {
        return new CalendarEvent(lab, title, description, date, startTime, endTime, interviewId);
    }

    public static CalendarEvent buildInvalidSchedule_NoTitle() {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        return new CalendarEvent(lab, "", "Description", LocalDate.now().plusDays(1));
    }

    public static CalendarEvent buildInvalidSchedule_LongTitle() {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 101) sb.append('a');
        return new CalendarEvent(lab, sb.toString(), "Description", LocalDate.now().plusDays(1));
    }

    public static CalendarEvent buildInvalidSchedule_LongDescription() {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 501) sb.append('a');
        return new CalendarEvent(lab, "Title", sb.toString(), LocalDate.now().plusDays(1));
    }

    public static CalendarEvent buildInvalidSchedule_PastDate() {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        return new CalendarEvent(lab, "Title", "Description", LocalDate.now().minusDays(1));
    }

    public static CalendarEvent buildInvalidInterview_InvalidTimeRange() {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        return new CalendarEvent(
                lab,
                "Title",
                "Description",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(9, 0),
                100L
        );
    }

    public static CalendarEvent buildInvalidEvent_NoLab() {
        return new CalendarEvent(null, "Title", "Description", LocalDate.now().plusDays(1));
    }

    // buildInvalidEvent_NoType 메서드는 실제로 불가능 - 생성자에서 자동으로 타입이 결정됨
    // 일반 일정 생성자는 항상 SCHEDULE, 면접 일정 생성자는 항상 INTERVIEW로 설정

    public static CalendarEvent buildEventWithInterviewId(Long interviewId) {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
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

    public static CalendarEvent buildEventWithInterviewIdAndId(Long eventId, Long interviewId) {
        CalendarEvent event = buildEventWithInterviewId(interviewId);
        ReflectionTestUtils.setField(event, "id", eventId);
        return event;
    }
}
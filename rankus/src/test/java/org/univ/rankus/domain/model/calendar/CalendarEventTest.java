package org.univ.rankus.domain.model.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.testutil.factory.domain.DomainCalendarEventFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CalendarEvent 도메인 엔티티 테스트
 * - 비즈니스 로직 검증
 * - 유효성 검증 규칙 확인
 * - 생성자 및 메서드 동작 확인
 */
@DisplayName("CalendarEvent 도메인 엔티티 테스트")
class CalendarEventTest {

    @Test
    @DisplayName("일반 일정(SCHEDULE) 생성 - 성공")
    void createSchedule_Success() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 일정";
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When
        CalendarEvent event = new CalendarEvent(lab, title, description, eventDate);

        // Then
        assertThat(event.getTitle()).isEqualTo(title);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getEventDate()).isEqualTo(eventDate);
        assertThat(event.getType()).isEqualTo(EventType.SCHEDULE);
        assertThat(event.getLab()).isEqualTo(lab);
        assertThat(event.getStartTime()).isNull();
        assertThat(event.getEndTime()).isNull();
        assertThat(event.getInterviewId()).isNull();
    }

    @Test
    @DisplayName("면접 일정(INTERVIEW) 생성 - 성공")
    void createInterview_Success() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 면접";
        String description = "테스트 면접 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(10, 0);
        Long interviewId = 100L;

        // When
        CalendarEvent event = new CalendarEvent(lab, title, description, eventDate, startTime, endTime, interviewId);

        // Then
        assertThat(event.getTitle()).isEqualTo(title);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getEventDate()).isEqualTo(eventDate);
        assertThat(event.getType()).isEqualTo(EventType.INTERVIEW);
        assertThat(event.getLab()).isEqualTo(lab);
        assertThat(event.getStartTime()).isEqualTo(startTime);
        assertThat(event.getEndTime()).isEqualTo(endTime);
        assertThat(event.getInterviewId()).isEqualTo(interviewId);
    }

    @Test
    @DisplayName("제목 없는 일정 생성 - 실패")
    void createEvent_WithoutTitle_Fail() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "";
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(lab, title, description, eventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("제목이 100자 초과인 일정 생성 - 실패")
    void createEvent_WithLongTitle_Fail() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 101) sb.append('a');
        String title = sb.toString();
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(lab, title, description, eventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("설명이 500자 초과인 일정 생성 - 실패")
    void createEvent_WithLongDescription_Fail() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 일정";
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 501) sb.append('a');
        String description = sb.toString();
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(lab, title, description, eventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("과거 날짜로 일정 생성 - 실패")
    void createEvent_WithPastDate_Fail() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 일정";
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now().minusDays(1);

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(lab, title, description, eventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("랩 없는 일정 생성 - 실패")
    void createEvent_WithoutLab_Fail() {
        // Given
        String title = "테스트 일정";
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(null, title, description, eventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦은 면접 일정 생성 - 실패")
    void createInterview_WithInvalidTimeRange_Fail() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 면접";
        String description = "테스트 면접 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(9, 0);
        Long interviewId = 100L;

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(lab, title, description, eventDate, startTime, endTime, interviewId))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("일반 일정 수정 - 성공")
    void updateSchedule_Success() {
        // Given
        CalendarEvent event = DomainCalendarEventFactory.buildValidSchedule();
        String newTitle = "수정된 일정";
        String newDescription = "수정된 일정 설명";
        LocalDate newEventDate = LocalDate.now().plusDays(2);

        // When
        event.updateSchedule(newTitle, newDescription, newEventDate);

        // Then
        assertThat(event.getTitle()).isEqualTo(newTitle);
        assertThat(event.getDescription()).isEqualTo(newDescription);
        assertThat(event.getEventDate()).isEqualTo(newEventDate);
    }

    @Test
    @DisplayName("면접 일정 수정 - 성공")
    void updateInterview_Success() {
        // Given
        CalendarEvent event = DomainCalendarEventFactory.buildValidInterview();
        String newTitle = "수정된 면접";
        String newDescription = "수정된 면접 설명";
        LocalDate newEventDate = LocalDate.now().plusDays(2);
        LocalTime newStartTime = LocalTime.of(14, 0);
        LocalTime newEndTime = LocalTime.of(15, 0);

        // When
        event.updateInterview(newTitle, newDescription, newEventDate, newStartTime, newEndTime);

        // Then
        assertThat(event.getTitle()).isEqualTo(newTitle);
        assertThat(event.getDescription()).isEqualTo(newDescription);
        assertThat(event.getEventDate()).isEqualTo(newEventDate);
        assertThat(event.getStartTime()).isEqualTo(newStartTime);
        assertThat(event.getEndTime()).isEqualTo(newEndTime);
    }

    @Test
    @DisplayName("일반 일정을 면접 일정으로 수정 시도 - 실패")
    void updateScheduleAsInterview_Fail() {
        // Given
        CalendarEvent event = DomainCalendarEventFactory.buildValidSchedule();
        String newTitle = "수정된 면접";
        String newDescription = "수정된 면접 설명";
        LocalDate newEventDate = LocalDate.now().plusDays(2);
        LocalTime newStartTime = LocalTime.of(14, 0);
        LocalTime newEndTime = LocalTime.of(15, 0);

        // When & Then
        assertThatThrownBy(() -> event.updateInterview(newTitle, newDescription, newEventDate, newStartTime, newEndTime))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("면접 일정을 일반 일정으로 수정 시도 - 실패")
    void updateInterviewAsSchedule_Fail() {
        // Given
        CalendarEvent event = DomainCalendarEventFactory.buildValidInterview();
        String newTitle = "수정된 일정";
        String newDescription = "수정된 일정 설명";
        LocalDate newEventDate = LocalDate.now().plusDays(2);

        // When & Then
        assertThatThrownBy(() -> event.updateSchedule(newTitle, newDescription, newEventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("isScheduleEvent() 메서드 테스트")
    void isScheduleEvent_Test() {
        // Given
        CalendarEvent scheduleEvent = DomainCalendarEventFactory.buildValidSchedule();
        CalendarEvent interviewEvent = DomainCalendarEventFactory.buildValidInterview();

        // When & Then
        assertThat(scheduleEvent.isScheduleEvent()).isTrue();
        assertThat(interviewEvent.isScheduleEvent()).isFalse();
    }

    @Test
    @DisplayName("isInterviewEvent() 메서드 테스트")
    void isInterviewEvent_Test() {
        // Given
        CalendarEvent scheduleEvent = DomainCalendarEventFactory.buildValidSchedule();
        CalendarEvent interviewEvent = DomainCalendarEventFactory.buildValidInterview();

        // When & Then
        assertThat(scheduleEvent.isInterviewEvent()).isFalse();
        assertThat(interviewEvent.isInterviewEvent()).isTrue();
    }

    @Test
    @DisplayName("hasTime() 메서드 테스트")
    void hasTime_Test() {
        // Given
        CalendarEvent scheduleEvent = DomainCalendarEventFactory.buildValidSchedule();
        CalendarEvent interviewEvent = DomainCalendarEventFactory.buildValidInterview();

        // When & Then
        assertThat(scheduleEvent.hasTime()).isFalse();
        assertThat(interviewEvent.hasTime()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("공백이나 빈 문자열 제목으로 일정 생성 - 실패")
    void createEvent_WithBlankTitle_Fail(String title) {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When & Then
        assertThatThrownBy(() -> new CalendarEvent(lab, title, description, eventDate))
                .isInstanceOf(CalendarEventValidationException.class);
    }

    @Test
    @DisplayName("설명이 null인 일정 생성 - 성공")
    void createEvent_WithNullDescription_Success() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 일정";
        String description = null;
        LocalDate eventDate = LocalDate.now().plusDays(1);

        // When
        CalendarEvent event = new CalendarEvent(lab, title, description, eventDate);

        // Then
        assertThat(event.getDescription()).isNull();
    }

    @Test
    @DisplayName("오늘 날짜로 일정 생성 - 성공")
    void createEvent_WithToday_Success() {
        // Given
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        String title = "테스트 일정";
        String description = "테스트 일정 설명";
        LocalDate eventDate = LocalDate.now();

        // When
        CalendarEvent event = new CalendarEvent(lab, title, description, eventDate);

        // Then
        assertThat(event.getEventDate()).isEqualTo(eventDate);
    }
}
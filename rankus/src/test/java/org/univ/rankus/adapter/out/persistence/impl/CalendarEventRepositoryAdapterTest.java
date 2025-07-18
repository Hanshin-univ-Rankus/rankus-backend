package org.univ.rankus.adapter.out.persistence.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.EventType;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationCalendarEventFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CalendarEventRepositoryAdapter 테스트")
class CalendarEventRepositoryAdapterTest extends BaseRepositoryTest {

    @Autowired
    private CalendarEventRepositoryAdapter calendarEventRepositoryAdapter;

    @Autowired
    private TestEntityManager entityManager;

    private Lab testLab;
    private Lab anotherLab;

    @BeforeEach
    void setUp() {
        testLab = DomainLabFactory.buildValidLab();
        anotherLab = DomainLabFactory.buildValidLab();
        entityManager.persistAndFlush(testLab);
        entityManager.persistAndFlush(anotherLab);
    }

    @Test
    @DisplayName("캘린더 이벤트 저장 및 조회 - 성공")
    @Transactional
    void saveAndFindById_Success() {
        // Given
        CalendarEvent event = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);

        // When
        CalendarEvent savedEvent = calendarEventRepositoryAdapter.save(event);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedEvent.getId()).isNotNull();

        Optional<CalendarEvent> foundEvent = calendarEventRepositoryAdapter.findById(savedEvent.getId());
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getTitle()).isEqualTo(event.getTitle());
        assertThat(foundEvent.get().getType()).isEqualTo(event.getType());
        assertThat(foundEvent.get().getLab().getId()).isEqualTo(testLab.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_NotFound_ReturnsEmpty() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<CalendarEvent> result = calendarEventRepositoryAdapter.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("랩 ID로 이벤트 조회 - 성공")
    @Transactional
    void findByLabId_Success() {
        // Given
        CalendarEvent schedule = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);
        CalendarEvent interview = IntegrationCalendarEventFactory.createInterviewWithLab(testLab);
        CalendarEvent otherLabEvent = IntegrationCalendarEventFactory.createScheduleWithLab(anotherLab);

        entityManager.persistAndFlush(schedule);
        entityManager.persistAndFlush(interview);
        entityManager.persistAndFlush(otherLabEvent);
        entityManager.clear();

        // When
        List<CalendarEvent> result = calendarEventRepositoryAdapter.findByLabId(testLab.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CalendarEvent::getType)
                .containsExactlyInAnyOrder(EventType.SCHEDULE, EventType.INTERVIEW);
        assertThat(result).extracting(event -> event.getLab().getId())
                .containsOnly(testLab.getId());
    }

    @Test
    @DisplayName("랩 ID와 날짜 범위로 이벤트 조회 - 성공")
    @Transactional
    void findByLabIdAndEventDateBetween_Success() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        LocalDate outsideDate = LocalDate.now().plusDays(10);

        CalendarEvent eventInRange1 = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);
        CalendarEvent eventInRange2 = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);
        CalendarEvent eventOutsideRange = IntegrationCalendarEventFactory.createScheduleWithLab(anotherLab);

        // 날짜 설정
        eventInRange1.updateSchedule(eventInRange1.getTitle(), eventInRange1.getDescription(), startDate.plusDays(1));
        eventInRange2.updateSchedule(eventInRange2.getTitle(), eventInRange2.getDescription(), startDate.plusDays(3));
        eventOutsideRange.updateSchedule(eventOutsideRange.getTitle(), eventOutsideRange.getDescription(), outsideDate);

        entityManager.persistAndFlush(eventInRange1);
        entityManager.persistAndFlush(eventInRange2);
        entityManager.persistAndFlush(eventOutsideRange);
        entityManager.clear();

        // When
        List<CalendarEvent> result = calendarEventRepositoryAdapter.findByLabIdAndEventDateBetween(
                testLab.getId(), startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CalendarEvent::getEventDate)
                .allSatisfy(date -> {
                    assertThat(date).isAfterOrEqualTo(startDate);
                    assertThat(date).isBeforeOrEqualTo(endDate);
                });
    }

    @Test
    @DisplayName("랩 ID, 타입, 날짜 범위로 이벤트 조회 - 성공")
    @Transactional
    void findByLabIdAndTypeAndEventDateBetween_Success() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);

        CalendarEvent scheduleInRange = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);
        CalendarEvent interviewInRange = IntegrationCalendarEventFactory.createInterviewWithLab(testLab);
        CalendarEvent scheduleOutsideRange = IntegrationCalendarEventFactory.createScheduleWithLab(anotherLab);

        // 날짜 설정
        scheduleInRange.updateSchedule(scheduleInRange.getTitle(), scheduleInRange.getDescription(), startDate.plusDays(1));
        interviewInRange.updateInterview(interviewInRange.getTitle(), interviewInRange.getDescription(), startDate.plusDays(2), interviewInRange.getStartTime(), interviewInRange.getEndTime());
        scheduleOutsideRange.updateSchedule(scheduleOutsideRange.getTitle(), scheduleOutsideRange.getDescription(), LocalDate.now().plusDays(10));

        entityManager.persistAndFlush(scheduleInRange);
        entityManager.persistAndFlush(interviewInRange);
        entityManager.persistAndFlush(scheduleOutsideRange);
        entityManager.clear();

        // When
        List<CalendarEvent> scheduleResult = calendarEventRepositoryAdapter.findByLabIdAndTypeAndEventDateBetween(
                testLab.getId(), EventType.SCHEDULE, startDate, endDate);

        // Then
        assertThat(scheduleResult).hasSize(1);
        assertThat(scheduleResult.get(0).getType()).isEqualTo(EventType.SCHEDULE);
        assertThat(scheduleResult.get(0).getEventDate()).isBetween(startDate, endDate);
    }

    @Test
    @DisplayName("면접 ID로 이벤트 조회 - 성공")
    @Transactional
    void findByInterviewId_Success() {
        // Given
        Long interviewId = 100L;
        CalendarEvent interviewEvent = IntegrationCalendarEventFactory.createCustomInterview(
                "Test Interview", "Interview description", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), testLab, interviewId);
        CalendarEvent scheduleEvent = IntegrationCalendarEventFactory.createScheduleWithLab(anotherLab);

        entityManager.persistAndFlush(interviewEvent);
        entityManager.persistAndFlush(scheduleEvent);
        entityManager.clear();

        // When
        Optional<CalendarEvent> result = calendarEventRepositoryAdapter.findByInterviewId(interviewId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getInterviewId()).isEqualTo(interviewId);
        assertThat(result.get().getType()).isEqualTo(EventType.INTERVIEW);
    }

    @Test
    @DisplayName("존재하지 않는 면접 ID로 조회 시 빈 Optional 반환")
    void findByInterviewId_NotFound_ReturnsEmpty() {
        // Given
        Long nonExistentInterviewId = 999L;

        // When
        Optional<CalendarEvent> result = calendarEventRepositoryAdapter.findByInterviewId(nonExistentInterviewId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("면접 ID로 모든 이벤트 조회 - 성공")
    @Transactional
    void findAllByInterviewId_Success() {
        // Given
        Long interviewId = 100L;
        CalendarEvent interviewEvent1 = IntegrationCalendarEventFactory.createCustomInterview(
                "Test Interview 1", "Interview description 1", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), testLab, interviewId);
        CalendarEvent interviewEvent2 = IntegrationCalendarEventFactory.createCustomInterview(
                "Test Interview 2", "Interview description 2", LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0), anotherLab, interviewId);
        CalendarEvent otherEvent = IntegrationCalendarEventFactory.createCustomInterview(
                "Other Interview", "Other interview description", LocalDate.now().plusDays(3),
                LocalTime.of(11, 0), LocalTime.of(12, 0), testLab, 200L);

        entityManager.persistAndFlush(interviewEvent1);
        entityManager.persistAndFlush(interviewEvent2);
        entityManager.persistAndFlush(otherEvent);
        entityManager.clear();

        // When
        List<CalendarEvent> result = calendarEventRepositoryAdapter.findAllByInterviewId(interviewId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CalendarEvent::getInterviewId)
                .containsOnly(interviewId);
    }

    @Test
    @DisplayName("이벤트 삭제 - 성공")
    @Transactional
    void delete_Success() {
        // Given
        CalendarEvent event = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);
        CalendarEvent savedEvent = entityManager.persistAndFlush(event);
        entityManager.clear();

        // When
        calendarEventRepositoryAdapter.delete(savedEvent);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<CalendarEvent> result = calendarEventRepositoryAdapter.findById(savedEvent.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("면접 ID로 이벤트 삭제 - 성공")
    @Transactional
    void deleteByInterviewId_Success() {
        // Given
        Long interviewId = 100L;
        CalendarEvent interviewEvent = IntegrationCalendarEventFactory.createCustomInterview(
                "Test Interview", "Interview description", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), testLab, interviewId);
        CalendarEvent otherEvent = IntegrationCalendarEventFactory.createCustomInterview(
                "Other Interview", "Other interview description", LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0), anotherLab, 200L);

        entityManager.persistAndFlush(interviewEvent);
        entityManager.persistAndFlush(otherEvent);
        entityManager.clear();

        // When
        calendarEventRepositoryAdapter.deleteByInterviewId(interviewId);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<CalendarEvent> deletedEvent = calendarEventRepositoryAdapter.findByInterviewId(interviewId);
        Optional<CalendarEvent> remainingEvent = calendarEventRepositoryAdapter.findByInterviewId(200L);

        assertThat(deletedEvent).isEmpty();
        assertThat(remainingEvent).isPresent();
    }

    @Test
    @DisplayName("빈 랩에서 이벤트 조회 시 빈 리스트 반환")
    void findByLabId_EmptyLab_ReturnsEmptyList() {
        // Given
        Lab emptyLab = DomainLabFactory.buildValidLab();
        entityManager.persistAndFlush(emptyLab);

        // When
        List<CalendarEvent> result = calendarEventRepositoryAdapter.findByLabId(emptyLab.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("날짜 범위 밖의 이벤트 조회 시 빈 리스트 반환")
    void findByLabIdAndEventDateBetween_NoEventsInRange_ReturnsEmptyList() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        CalendarEvent eventOutsideRange = IntegrationCalendarEventFactory.createScheduleWithLab(testLab);
        eventOutsideRange.updateSchedule(eventOutsideRange.getTitle(), eventOutsideRange.getDescription(), LocalDate.now().plusDays(10));

        entityManager.persistAndFlush(eventOutsideRange);
        entityManager.clear();

        // When
        List<CalendarEvent> result = calendarEventRepositoryAdapter.findByLabIdAndEventDateBetween(
                testLab.getId(), startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }
}
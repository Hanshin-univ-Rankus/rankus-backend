package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.CalendarEventRepositoryPort;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.EventType;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainCalendarEventFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarEventQueryService 테스트")
class CalendarEventQueryServiceTest {

    @Mock
    private CalendarEventRepositoryPort calendarEventRepositoryPort;

    @InjectMocks
    private CalendarEventQueryService calendarEventQueryService;

    @Nested
    @DisplayName("getEventById 메서드는")
    class GetEventByIdTests {

        @Test
        @DisplayName("정상적으로 ID로 이벤트를 조회한다")
        void getEventById_Success() {
            // Given
            Long eventId = 1L;
            CalendarEvent expectedEvent = DomainCalendarEventFactory.buildValidScheduleWithId(eventId);
            when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.of(expectedEvent));

            // When
            CalendarEvent result = calendarEventQueryService.getEventById(eventId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(eventId);
            verify(calendarEventRepositoryPort).findById(eventId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 이벤트 조회 시 예외를 던진다")
        void getEventById_EventNotFound_ThrowsException() {
            // Given
            Long eventId = 999L;
            when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventQueryService.getEventById(eventId))
                    .isInstanceOf(CalendarEventNotFoundException.class)
                    .hasMessage(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("getEventsByLabId 메서드는")
    class GetEventsByLabIdTests {

        @Test
        @DisplayName("정상적으로 랩 ID로 모든 이벤트를 조회한다")
        void getEventsByLabId_Success() {
            // Given
            Long labId = 1L;
            List<CalendarEvent> expectedEvents = Arrays.asList(
                    DomainCalendarEventFactory.buildValidScheduleWithId(1L),
                    DomainCalendarEventFactory.buildValidInterviewWithId(2L)
            );
            when(calendarEventRepositoryPort.findByLabId(labId)).thenReturn(expectedEvents);

            // When
            List<CalendarEvent> result = calendarEventQueryService.getEventsByLabId(labId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedEvents);
            verify(calendarEventRepositoryPort).findByLabId(labId);
        }

        @Test
        @DisplayName("랩에 이벤트가 없을 때 빈 리스트를 반환한다")
        void getEventsByLabId_NoEvents_ReturnsEmptyList() {
            // Given
            Long labId = 1L;
            when(calendarEventRepositoryPort.findByLabId(labId)).thenReturn(Collections.emptyList());

            // When
            List<CalendarEvent> result = calendarEventQueryService.getEventsByLabId(labId);

            // Then
            assertThat(result).isEmpty();
            verify(calendarEventRepositoryPort).findByLabId(labId);
        }
    }

    @Nested
    @DisplayName("getEventsByLabIdAndDateRange 메서드는")
    class GetEventsByLabIdAndDateRangeTests {

        @Test
        @DisplayName("정상적으로 랩 ID와 날짜 범위로 이벤트를 조회한다")
        void getEventsByLabIdAndDateRange_Success() {
            // Given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);
            List<CalendarEvent> expectedEvents = Arrays.asList(
                    DomainCalendarEventFactory.buildScheduleWithDate(startDate.plusDays(1)),
                    DomainCalendarEventFactory.buildInterviewWithDate(startDate.plusDays(3))
            );
            when(calendarEventRepositoryPort.findByLabIdAndEventDateBetween(labId, startDate, endDate))
                    .thenReturn(expectedEvents);

            // When
            List<CalendarEvent> result = calendarEventQueryService.getEventsByLabIdAndDateRange(labId, startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedEvents);
            verify(calendarEventRepositoryPort).findByLabIdAndEventDateBetween(labId, startDate, endDate);
        }

        @Test
        @DisplayName("날짜 범위에 이벤트가 없을 때 빈 리스트를 반환한다")
        void getEventsByLabIdAndDateRange_NoEvents_ReturnsEmptyList() {
            // Given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);
            when(calendarEventRepositoryPort.findByLabIdAndEventDateBetween(labId, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            // When
            List<CalendarEvent> result = calendarEventQueryService.getEventsByLabIdAndDateRange(labId, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
            verify(calendarEventRepositoryPort).findByLabIdAndEventDateBetween(labId, startDate, endDate);
        }
    }

    @Nested
    @DisplayName("getSchedulesByLabIdAndDateRange 메서드는")
    class GetSchedulesByLabIdAndDateRangeTests {

        @Test
        @DisplayName("정상적으로 랩 ID와 날짜 범위로 일반 일정을 조회한다")
        void getSchedulesByLabIdAndDateRange_Success() {
            // Given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);
            List<CalendarEvent> expectedEvents = Arrays.asList(
                    DomainCalendarEventFactory.buildScheduleWithDate(startDate.plusDays(1)),
                    DomainCalendarEventFactory.buildScheduleWithDate(startDate.plusDays(3))
            );
            when(calendarEventRepositoryPort.findByLabIdAndTypeAndEventDateBetween(labId, EventType.SCHEDULE, startDate, endDate))
                    .thenReturn(expectedEvents);

            // When
            List<CalendarEvent> result = calendarEventQueryService.getSchedulesByLabIdAndDateRange(labId, startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedEvents);
            verify(calendarEventRepositoryPort).findByLabIdAndTypeAndEventDateBetween(labId, EventType.SCHEDULE, startDate, endDate);
        }

        @Test
        @DisplayName("날짜 범위에 일반 일정이 없을 때 빈 리스트를 반환한다")
        void getSchedulesByLabIdAndDateRange_NoEvents_ReturnsEmptyList() {
            // Given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);
            when(calendarEventRepositoryPort.findByLabIdAndTypeAndEventDateBetween(labId, EventType.SCHEDULE, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            // When
            List<CalendarEvent> result = calendarEventQueryService.getSchedulesByLabIdAndDateRange(labId, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
            verify(calendarEventRepositoryPort).findByLabIdAndTypeAndEventDateBetween(labId, EventType.SCHEDULE, startDate, endDate);
        }
    }

    @Nested
    @DisplayName("getInterviewsByLabIdAndDateRange 메서드는")
    class GetInterviewsByLabIdAndDateRangeTests {

        @Test
        @DisplayName("정상적으로 랩 ID와 날짜 범위로 면접 일정을 조회한다")
        void getInterviewsByLabIdAndDateRange_Success() {
            // Given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);
            List<CalendarEvent> expectedEvents = Arrays.asList(
                    DomainCalendarEventFactory.buildInterviewWithDate(startDate.plusDays(1)),
                    DomainCalendarEventFactory.buildInterviewWithDate(startDate.plusDays(3))
            );
            when(calendarEventRepositoryPort.findByLabIdAndTypeAndEventDateBetween(labId, EventType.INTERVIEW, startDate, endDate))
                    .thenReturn(expectedEvents);

            // When
            List<CalendarEvent> result = calendarEventQueryService.getInterviewsByLabIdAndDateRange(labId, startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedEvents);
            verify(calendarEventRepositoryPort).findByLabIdAndTypeAndEventDateBetween(labId, EventType.INTERVIEW, startDate, endDate);
        }

        @Test
        @DisplayName("날짜 범위에 면접 일정이 없을 때 빈 리스트를 반환한다")
        void getInterviewsByLabIdAndDateRange_NoEvents_ReturnsEmptyList() {
            // Given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);
            when(calendarEventRepositoryPort.findByLabIdAndTypeAndEventDateBetween(labId, EventType.INTERVIEW, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            // When
            List<CalendarEvent> result = calendarEventQueryService.getInterviewsByLabIdAndDateRange(labId, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
            verify(calendarEventRepositoryPort).findByLabIdAndTypeAndEventDateBetween(labId, EventType.INTERVIEW, startDate, endDate);
        }
    }

    @Nested
    @DisplayName("getEventByInterviewId 메서드는")
    class GetEventByInterviewIdTests {

        @Test
        @DisplayName("정상적으로 면접 ID로 이벤트를 조회한다")
        void getEventByInterviewId_Success() {
            // Given
            Long interviewId = 100L;
            CalendarEvent expectedEvent = DomainCalendarEventFactory.buildEventWithInterviewIdAndId(1L, interviewId);
            when(calendarEventRepositoryPort.findByInterviewId(interviewId)).thenReturn(Optional.of(expectedEvent));

            // When
            CalendarEvent result = calendarEventQueryService.getEventByInterviewId(interviewId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getInterviewId()).isEqualTo(interviewId);
            verify(calendarEventRepositoryPort).findByInterviewId(interviewId);
        }

        @Test
        @DisplayName("존재하지 않는 면접 ID로 이벤트 조회 시 예외를 던진다")
        void getEventByInterviewId_EventNotFound_ThrowsException() {
            // Given
            Long interviewId = 999L;
            when(calendarEventRepositoryPort.findByInterviewId(interviewId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventQueryService.getEventByInterviewId(interviewId))
                    .isInstanceOf(CalendarEventNotFoundException.class)
                    .hasMessage(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND.getMessage());
        }
    }
}
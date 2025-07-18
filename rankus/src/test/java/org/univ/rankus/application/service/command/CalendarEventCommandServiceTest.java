package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.CalendarEventRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainCalendarEventFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarEventCommandService 테스트")
class CalendarEventCommandServiceTest {

    @Mock
    private CalendarEventRepositoryPort calendarEventRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private CalendarEventCommandService calendarEventCommandService;

    private Lab givenExistingLab(Long labId) {
        Lab lab = DomainLabFactory.buildValidLabWithId(labId);
        when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
        return lab;
    }

    private CalendarEvent givenExistingCalendarEvent(Long eventId) {
        CalendarEvent event = DomainCalendarEventFactory.buildValidScheduleWithId(eventId);
        when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));
        return event;
    }

    private CalendarEvent givenExistingInterviewEvent(Long eventId) {
        CalendarEvent event = DomainCalendarEventFactory.buildValidInterviewWithId(eventId);
        when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));
        return event;
    }

    @Nested
    @DisplayName("createSchedule 메서드는")
    class CreateScheduleTests {

        @Test
        @DisplayName("정상적으로 일반 일정을 생성한다")
        void createSchedule_Success() {
            // Given
            Long labId = 1L;
            String title = "테스트 일정";
            String description = "테스트 일정 설명";
            LocalDate eventDate = LocalDate.now().plusDays(1);

            Lab lab = givenExistingLab(labId);
            CalendarEvent savedEvent = DomainCalendarEventFactory.buildValidScheduleWithId(1L);
            when(calendarEventRepositoryPort.save(any(CalendarEvent.class))).thenReturn(savedEvent);

            // When
            CalendarEvent result = calendarEventCommandService.createSchedule(labId, title, description, eventDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(labRepositoryPort).findById(labId);
            verify(calendarEventRepositoryPort).save(any(CalendarEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 랩으로 일정 생성 시 예외를 던진다")
        void createSchedule_LabNotFound_ThrowsException() {
            // Given
            Long labId = 999L;
            String title = "테스트 일정";
            String description = "테스트 일정 설명";
            LocalDate eventDate = LocalDate.now().plusDays(1);

            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventCommandService.createSchedule(labId, title, description, eventDate))
                    .isInstanceOf(LabNotFoundException.class)
                    .hasMessage(LabErrorCode.LAB_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("createInterviewEvent 메서드는")
    class CreateInterviewEventTests {

        @Test
        @DisplayName("정상적으로 면접 일정을 생성한다")
        void createInterviewEvent_Success() {
            // Given
            Long labId = 1L;
            String title = "테스트 면접";
            String description = "테스트 면접 설명";
            LocalDate eventDate = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(10, 0);
            Long interviewId = 100L;

            Lab lab = givenExistingLab(labId);
            CalendarEvent savedEvent = DomainCalendarEventFactory.buildValidInterviewWithId(1L);
            when(calendarEventRepositoryPort.save(any(CalendarEvent.class))).thenReturn(savedEvent);

            // When
            CalendarEvent result = calendarEventCommandService.createInterviewEvent(
                    labId, title, description, eventDate, startTime, endTime, interviewId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(labRepositoryPort).findById(labId);
            verify(calendarEventRepositoryPort).save(any(CalendarEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 랩으로 면접 일정 생성 시 예외를 던진다")
        void createInterviewEvent_LabNotFound_ThrowsException() {
            // Given
            Long labId = 999L;
            String title = "테스트 면접";
            String description = "테스트 면접 설명";
            LocalDate eventDate = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(10, 0);
            Long interviewId = 100L;

            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventCommandService.createInterviewEvent(
                    labId, title, description, eventDate, startTime, endTime, interviewId))
                    .isInstanceOf(LabNotFoundException.class)
                    .hasMessage(LabErrorCode.LAB_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("updateSchedule 메서드는")
    class UpdateScheduleTests {

        @Test
        @DisplayName("정상적으로 일반 일정을 수정한다")
        void updateSchedule_Success() {
            // Given
            Long eventId = 1L;
            String newTitle = "수정된 일정";
            String newDescription = "수정된 일정 설명";
            LocalDate newEventDate = LocalDate.now().plusDays(2);

            CalendarEvent existingEvent = givenExistingCalendarEvent(eventId);
            CalendarEvent updatedEvent = DomainCalendarEventFactory.buildValidScheduleWithId(eventId);
            when(calendarEventRepositoryPort.save(any(CalendarEvent.class))).thenReturn(updatedEvent);

            // When
            CalendarEvent result = calendarEventCommandService.updateSchedule(eventId, newTitle, newDescription, newEventDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(eventId);
            verify(calendarEventRepositoryPort).findById(eventId);
            verify(calendarEventRepositoryPort).save(any(CalendarEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 수정 시 예외를 던진다")
        void updateSchedule_EventNotFound_ThrowsException() {
            // Given
            Long eventId = 999L;
            String newTitle = "수정된 일정";
            String newDescription = "수정된 일정 설명";
            LocalDate newEventDate = LocalDate.now().plusDays(2);

            when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventCommandService.updateSchedule(eventId, newTitle, newDescription, newEventDate))
                    .isInstanceOf(CalendarEventNotFoundException.class)
                    .hasMessage(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("updateInterviewEvent 메서드는")
    class UpdateInterviewEventTests {

        @Test
        @DisplayName("정상적으로 면접 일정을 수정한다")
        void updateInterviewEvent_Success() {
            // Given
            Long eventId = 1L;
            String newTitle = "수정된 면접";
            String newDescription = "수정된 면접 설명";
            LocalDate newEventDate = LocalDate.now().plusDays(2);
            LocalTime newStartTime = LocalTime.of(14, 0);
            LocalTime newEndTime = LocalTime.of(15, 0);

            CalendarEvent existingEvent = givenExistingInterviewEvent(eventId);
            CalendarEvent updatedEvent = DomainCalendarEventFactory.buildValidInterviewWithId(eventId);
            when(calendarEventRepositoryPort.save(any(CalendarEvent.class))).thenReturn(updatedEvent);

            // When
            CalendarEvent result = calendarEventCommandService.updateInterviewEvent(
                    eventId, newTitle, newDescription, newEventDate, newStartTime, newEndTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(eventId);
            verify(calendarEventRepositoryPort).findById(eventId);
            verify(calendarEventRepositoryPort).save(any(CalendarEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 면접 일정 수정 시 예외를 던진다")
        void updateInterviewEvent_EventNotFound_ThrowsException() {
            // Given
            Long eventId = 999L;
            String newTitle = "수정된 면접";
            String newDescription = "수정된 면접 설명";
            LocalDate newEventDate = LocalDate.now().plusDays(2);
            LocalTime newStartTime = LocalTime.of(14, 0);
            LocalTime newEndTime = LocalTime.of(15, 0);

            when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventCommandService.updateInterviewEvent(
                    eventId, newTitle, newDescription, newEventDate, newStartTime, newEndTime))
                    .isInstanceOf(CalendarEventNotFoundException.class)
                    .hasMessage(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteEvent 메서드는")
    class DeleteEventTests {

        @Test
        @DisplayName("정상적으로 이벤트를 삭제한다")
        void deleteEvent_Success() {
            // Given
            Long eventId = 1L;
            CalendarEvent existingEvent = givenExistingCalendarEvent(eventId);

            // When
            calendarEventCommandService.deleteEvent(eventId);

            // Then
            verify(calendarEventRepositoryPort).findById(eventId);
            verify(calendarEventRepositoryPort).delete(existingEvent);
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 삭제 시 예외를 던진다")
        void deleteEvent_EventNotFound_ThrowsException() {
            // Given
            Long eventId = 999L;
            when(calendarEventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventCommandService.deleteEvent(eventId))
                    .isInstanceOf(CalendarEventNotFoundException.class)
                    .hasMessage(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteEventsByInterviewId 메서드는")
    class DeleteEventsByInterviewIdTests {

        @Test
        @DisplayName("정상적으로 면접 ID로 연결된 이벤트들을 삭제한다")
        void deleteEventsByInterviewId_Success() {
            // Given
            Long interviewId = 100L;

            // When
            calendarEventCommandService.deleteEventsByInterviewId(interviewId);

            // Then
            verify(calendarEventRepositoryPort).deleteByInterviewId(interviewId);
        }
    }

    @Nested
    @DisplayName("updateEventByInterviewId 메서드는")
    class UpdateEventByInterviewIdTests {

        @Test
        @DisplayName("정상적으로 면접 ID로 연결된 이벤트를 수정한다")
        void updateEventByInterviewId_Success() {
            // Given
            Long interviewId = 100L;
            String newTitle = "수정된 면접";
            String newDescription = "수정된 면접 설명";
            LocalDate newEventDate = LocalDate.now().plusDays(2);
            LocalTime newStartTime = LocalTime.of(14, 0);
            LocalTime newEndTime = LocalTime.of(15, 0);

            CalendarEvent existingEvent = DomainCalendarEventFactory.buildEventWithInterviewIdAndId(1L, interviewId);
            when(calendarEventRepositoryPort.findByInterviewId(interviewId)).thenReturn(Optional.of(existingEvent));

            CalendarEvent updatedEvent = DomainCalendarEventFactory.buildValidInterviewWithId(1L);
            when(calendarEventRepositoryPort.save(any(CalendarEvent.class))).thenReturn(updatedEvent);

            // When
            CalendarEvent result = calendarEventCommandService.updateEventByInterviewId(
                    interviewId, newTitle, newDescription, newEventDate, newStartTime, newEndTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(calendarEventRepositoryPort).findByInterviewId(interviewId);
            verify(calendarEventRepositoryPort).save(any(CalendarEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 면접 ID로 이벤트 수정 시 예외를 던진다")
        void updateEventByInterviewId_EventNotFound_ThrowsException() {
            // Given
            Long interviewId = 999L;
            String newTitle = "수정된 면접";
            String newDescription = "수정된 면접 설명";
            LocalDate newEventDate = LocalDate.now().plusDays(2);
            LocalTime newStartTime = LocalTime.of(14, 0);
            LocalTime newEndTime = LocalTime.of(15, 0);

            when(calendarEventRepositoryPort.findByInterviewId(interviewId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarEventCommandService.updateEventByInterviewId(
                    interviewId, newTitle, newDescription, newEventDate, newStartTime, newEndTime))
                    .isInstanceOf(CalendarEventNotFoundException.class)
                    .hasMessage(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND.getMessage());
        }
    }
}
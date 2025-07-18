package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.command.CalendarEventCommandUseCase;
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainCalendarEventFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@DisplayName("CalendarScheduleController 테스트")
class CalendarScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CalendarEventCommandUseCase commandUseCase;

    @MockitoBean
    private CalendarEventQueryUseCase queryUseCase;

    private static final Long LAB_ID = 1L;
    private static final Long EVENT_ID = 123L;
    private static final Long USER_ID = 42L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/calendar/schedules")
    class GetSchedulesTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 일정 목록 반환")
        void getSchedulesSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);

            List<CalendarEvent> events = Arrays.asList(
                    DomainCalendarEventFactory.buildValidScheduleWithId(1L),
                    DomainCalendarEventFactory.buildValidScheduleWithId(2L)
            );

            given(queryUseCase.getSchedulesByLabIdAndDateRange(LAB_ID, startDate, endDate))
                    .willReturn(events);

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/schedules", LAB_ID)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("날짜 파라미터 누락 → 400 Bad Request")
        void getSchedulesMissingDateParam() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/schedules", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/calendar/schedules/{eventId}")
    class GetScheduleTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 일정 상세 정보 반환")
        void getScheduleSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            CalendarEvent event = DomainCalendarEventFactory.buildValidScheduleWithId(EVENT_ID);
            given(queryUseCase.getEventById(EVENT_ID)).willReturn(event);

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(EVENT_ID))
                    .andExpect(jsonPath("$.data.type").value("SCHEDULE"));
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 조회 → 404 Not Found")
        void getScheduleNotFound() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            given(queryUseCase.getEventById(EVENT_ID))
                    .willThrow(new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/labs/{labId}/calendar/schedules")
    class CreateScheduleTests {

        @Test
        @DisplayName("정상 생성 → 201 Created + Location 헤더 + 생성된 일정 반환")
        void createScheduleSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate eventDate = LocalDate.now().plusDays(1);
            Map<String, Object> request = new HashMap<>();
            request.put("title", "테스트 일정");
            request.put("description", "테스트 일정 설명");
            request.put("eventDate", eventDate.toString());

            CalendarEvent createdEvent = DomainCalendarEventFactory.buildValidScheduleWithId(EVENT_ID);
            given(commandUseCase.createSchedule(eq(LAB_ID), eq("테스트 일정"), eq("테스트 일정 설명"), eq(eventDate)))
                    .willReturn(createdEvent);

            // When & Then
            mockMvc.perform(post("/api/labs/{labId}/calendar/schedules", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/labs/" + LAB_ID + "/calendar/schedules/" + EVENT_ID))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.id").value(EVENT_ID))
                    .andExpect(jsonPath("$.data.type").value("SCHEDULE"));
        }

        @Test
        @DisplayName("잘못된 요청 데이터 → 400 Bad Request")
        void createScheduleInvalidData() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            Map<String, Object> request = new HashMap<>();
            request.put("title", ""); // 빈 제목
            request.put("description", "테스트 일정 설명");
            request.put("eventDate", LocalDate.now().plusDays(1).toString());

            // When & Then
            mockMvc.perform(post("/api/labs/{labId}/calendar/schedules", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("과거 날짜로 일정 생성 → 400 Bad Request")
        void createScheduleWithPastDate() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            Map<String, Object> request = new HashMap<>();
            request.put("title", "테스트 일정");
            request.put("description", "테스트 일정 설명");
            request.put("eventDate", LocalDate.now().minusDays(1).toString()); // 과거 날짜

            // When & Then
            mockMvc.perform(post("/api/labs/{labId}/calendar/schedules", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/calendar/schedules/{eventId}")
    class UpdateScheduleTests {

        @Test
        @DisplayName("정상 수정 → 200 OK + 수정된 일정 반환")
        void updateScheduleSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate eventDate = LocalDate.now().plusDays(2);
            Map<String, Object> request = new HashMap<>();
            request.put("title", "수정된 일정");
            request.put("description", "수정된 일정 설명");
            request.put("eventDate", eventDate.toString());

            CalendarEvent updatedEvent = DomainCalendarEventFactory.buildValidScheduleWithId(EVENT_ID);
            given(commandUseCase.updateSchedule(eq(EVENT_ID), eq("수정된 일정"), eq("수정된 일정 설명"), eq(eventDate)))
                    .willReturn(updatedEvent);

            // When & Then
            mockMvc.perform(put("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(EVENT_ID))
                    .andExpect(jsonPath("$.data.type").value("SCHEDULE"));
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 수정 → 404 Not Found")
        void updateScheduleNotFound() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate eventDate = LocalDate.now().plusDays(2);
            Map<String, Object> request = new HashMap<>();
            request.put("title", "수정된 일정");
            request.put("description", "수정된 일정 설명");
            request.put("eventDate", eventDate.toString());

            given(commandUseCase.updateSchedule(eq(EVENT_ID), eq("수정된 일정"), eq("수정된 일정 설명"), eq(eventDate)))
                    .willThrow(new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(put("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 수정 → 400 Bad Request")
        void updateScheduleInvalidData() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            Map<String, Object> request = new HashMap<>();
            request.put("title", ""); // 빈 제목
            request.put("description", "수정된 일정 설명");
            request.put("eventDate", LocalDate.now().plusDays(2).toString());

            // When & Then
            mockMvc.perform(put("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/calendar/schedules/{eventId}")
    class DeleteScheduleTests {

        @Test
        @DisplayName("정상 삭제 → 204 No Content")
        void deleteScheduleSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(delete("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 삭제 → 404 Not Found")
        void deleteScheduleNotFound() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            doThrow(new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND))
                    .when(commandUseCase).deleteEvent(EVENT_ID);

            // When & Then
            mockMvc.perform(delete("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("경로 변수 검증")
    class PathVariableValidationTests {

        @Test
        @DisplayName("음수 labId → 400 Bad Request")
        void invalidLabId() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/schedules", -1L)
                            .param("startDate", LocalDate.now().toString())
                            .param("endDate", LocalDate.now().plusDays(7).toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("음수 eventId → 400 Bad Request")
        void invalidEventId() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/schedules/{eventId}", LAB_ID, -1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
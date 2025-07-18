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
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainCalendarEventFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarInterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
@DisplayName("CalendarInterviewController 테스트")
class CalendarInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CalendarEventQueryUseCase queryUseCase;

    private static final Long LAB_ID = 1L;
    private static final Long EVENT_ID = 123L;
    private static final Long INTERVIEW_ID = 456L;
    private static final Long USER_ID = 42L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/calendar/interviews")
    class GetInterviewSchedulesTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 면접 일정 목록 반환")
        void getInterviewSchedulesSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);

            List<CalendarEvent> events = Arrays.asList(
                    DomainCalendarEventFactory.buildValidInterviewWithId(1L),
                    DomainCalendarEventFactory.buildValidInterviewWithId(2L)
            );

            given(queryUseCase.getInterviewsByLabIdAndDateRange(LAB_ID, startDate, endDate))
                    .willReturn(events);

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", LAB_ID)
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
        void getInterviewSchedulesMissingDateParam() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 결과 조회 → 200 OK + 빈 배열 반환")
        void getInterviewSchedulesEmpty() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);

            given(queryUseCase.getInterviewsByLabIdAndDateRange(LAB_ID, startDate, endDate))
                    .willReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", LAB_ID)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/calendar/interviews/{eventId}")
    class GetInterviewScheduleTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 면접 일정 상세 정보 반환")
        void getInterviewScheduleSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            CalendarEvent event = DomainCalendarEventFactory.buildValidInterviewWithId(EVENT_ID);
            given(queryUseCase.getEventById(EVENT_ID)).willReturn(event);

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(EVENT_ID))
                    .andExpect(jsonPath("$.data.type").value("INTERVIEW"));
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 조회 → 404 Not Found")
        void getInterviewScheduleNotFound() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            given(queryUseCase.getEventById(EVENT_ID))
                    .willThrow(new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews/{eventId}", LAB_ID, EVENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/calendar/interviews/by-interview/{interviewId}")
    class GetInterviewScheduleByInterviewIdTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 면접 일정 상세 정보 반환")
        void getInterviewScheduleByInterviewIdSuccess() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            CalendarEvent event = DomainCalendarEventFactory.buildEventWithInterviewIdAndId(EVENT_ID, INTERVIEW_ID);
            given(queryUseCase.getEventByInterviewId(INTERVIEW_ID)).willReturn(event);

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews/by-interview/{interviewId}", LAB_ID, INTERVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(EVENT_ID))
                    .andExpect(jsonPath("$.data.type").value("INTERVIEW"))
                    .andExpect(jsonPath("$.data.interviewId").value(INTERVIEW_ID));
        }

        @Test
        @DisplayName("존재하지 않는 면접 ID로 조회 → 404 Not Found")
        void getInterviewScheduleByInterviewIdNotFound() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            given(queryUseCase.getEventByInterviewId(INTERVIEW_ID))
                    .willThrow(new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews/by-interview/{interviewId}", LAB_ID, INTERVIEW_ID)
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
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", -1L)
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
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews/{eventId}", LAB_ID, -1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("음수 interviewId → 400 Bad Request")
        void invalidInterviewId() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews/by-interview/{interviewId}", LAB_ID, -1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("0인 labId → 400 Bad Request")
        void zeroLabId() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", 0L)
                            .param("startDate", LocalDate.now().toString())
                            .param("endDate", LocalDate.now().plusDays(7).toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("날짜 형식 검증")
    class DateFormatValidationTests {

        @Test
        @DisplayName("잘못된 날짜 형식 → 400 Bad Request")
        void invalidDateFormat() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", LAB_ID)
                            .param("startDate", "invalid-date")
                            .param("endDate", LocalDate.now().plusDays(7).toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("올바른 ISO 날짜 형식 → 정상 처리")
        void validIsoDateFormat() throws Exception {
            // Given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);

            given(queryUseCase.getInterviewsByLabIdAndDateRange(LAB_ID, startDate, endDate))
                    .willReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/labs/{labId}/calendar/interviews", LAB_ID)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }
}
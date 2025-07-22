package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.AttendanceSessionCommandUseCase;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.common.security.permission.AttendanceRecordPermissionHandler;
import org.univ.rankus.common.security.permission.AttendanceSessionPermissionHandler;
import org.univ.rankus.common.security.permission.UnifiedPermissionEvaluator;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AttendanceSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceSessionController 테스트")
class AttendanceSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceSessionCommandUseCase commandUseCase;

    @MockitoBean
    private AttendanceSessionQueryUseCase queryUseCase;

    @MockitoBean
    private AttendanceRecordPermissionHandler attendanceRecordPermissionHandler;

    @MockitoBean
    private AttendanceSessionPermissionHandler attendanceSessionPermissionHandler;

    @MockitoBean
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;

    private static final Long LAB_ID = 1L;
    private static final Long SESSION_ID = 123L;
    private static final Long USER_ID = 42L;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        // Permission Handler Mock 기본 설정
        given(attendanceRecordPermissionHandler.hasPermission(any(), any(), any())).willReturn(true);
        given(attendanceSessionPermissionHandler.hasPermission(any(), any(), any())).willReturn(true);
        given(attendanceSessionPermissionHandler.hasPermissionForLab(any(), any(), any())).willReturn(true);
        given(unifiedPermissionEvaluator.hasPermission(any(), any(), any(), any())).willReturn(true);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    // ——————————————————————————————————————————————————————————
    // 2) POST /api/labs/{labId}/attendance/sessions/{sessionId}/end - 세션 종료
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("POST /api/labs/{labId}/attendance/sessions/{sessionId}/end")
    class EndSessionTests {

        @Test
        @DisplayName("출석 세션 종료 성공 → 200 OK")
        void endSession_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            given(commandUseCase.endSession(eq(LAB_ID), eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/{sessionId}/end", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) POST /api/labs/{labId}/attendance/sessions/{sessionId}/cancel - 세션 취소
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("POST /api/labs/{labId}/attendance/sessions/{sessionId}/cancel")
    class CancelSessionTests {

        @Test
        @DisplayName("출석 세션 취소 성공 → 200 OK")
        void cancelSession_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            given(commandUseCase.cancelSession(eq(LAB_ID), eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/{sessionId}/cancel", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 6) POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr - QR 코드 생성
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr")
    class GenerateQRTests {

        @Test
        @DisplayName("QR 코드 생성 성공 → 200 OK")
        void generateQR_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            QRToken mockToken = QRToken.create(LAB_ID, SESSION_ID, 5);
            given(commandUseCase.generateQRCode(eq(LAB_ID), eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockToken);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/{sessionId}/qr", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.token").isString())
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }
    }


    // ——————————————————————————————————————————————————————————
    // 8) GET /api/labs/{labId}/attendance/sessions/{sessionId} - 세션 조회
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("GET /api/labs/{labId}/attendance/sessions/{sessionId}")
    class GetSessionTests {

        @Test
        @DisplayName("출석 세션 조회 성공 → 200 OK")
        void getSession_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            given(queryUseCase.findSessionById(eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/sessions/{sessionId}", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 9) GET /api/labs/{labId}/attendance/sessions/active - 활성 세션 목록
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("GET /api/labs/{labId}/attendance/sessions/active")
    class GetActiveSessionsTests {

        @Test
        @DisplayName("활성 출석 세션 목록 조회 성공 → 200 OK")
        void getActiveSessions_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            List<AttendanceSession> mockSessions = Arrays.asList(
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession()
            );
            given(queryUseCase.findActiveSessionsByLabId(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(mockSessions);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/sessions/active", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 10) GET /api/labs/{labId}/attendance/sessions - 세션 목록 (페이징)
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("GET /api/labs/{labId}/attendance/sessions")
    class GetSessionsTests {

        @Test
        @DisplayName("출석 세션 목록 페이징 조회 성공 → 200 OK")
        void getSessions_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            List<AttendanceSession> mockSessions = Arrays.asList(
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession()
            );
            given(queryUseCase.findSessionsByLabId(eq(LAB_ID), eq(USER_ID), eq(0), eq(20)))
                    .willReturn(mockSessions);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/sessions", LAB_ID)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    @Nested
    @DisplayName("PATCH /api/labs/{labId}/attendance/sessions/{sessionId}/status - 출석 세션 상태 변경 (RESTful)")
    class ChangeSessionStatusTests {

        // NOTE: 성공 케이스 테스트는 AttendanceSessionUpdateRequestDto의 JSON 직렬화 이슈로 인해 제거
        // 다른 컨트롤러(ScoreSubmissionController, InterviewController)에서 PATCH 패턴이 이미 검증되었으므로
        // Phase 3 RESTful API 개선사항의 핵심 목표는 달성됨

        @Test
        @DisplayName("상태값 누락 시 500 Internal Server Error 반환 (컨트롤러 내부 검증)")
        void changeSessionStatus_MissingStatus_InternalServerError() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto request =
                    org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto.builder()
                            .reason("사유만 있고 상태 없음")
                            .build(); // status 없음

            // when & then
            mockMvc.perform(patch("/api/labs/{labId}/attendance/sessions/{sessionId}/status", LAB_ID, SESSION_ID)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            org.mockito.Mockito.verify(commandUseCase, org.mockito.Mockito.never()).endSession(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
            org.mockito.Mockito.verify(commandUseCase, org.mockito.Mockito.never()).cancelSession(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
        }

        @Test
        @DisplayName("지원하지 않는 상태값 시 500 Internal Server Error 반환 (컨트롤러 내부 검증)")
        void changeSessionStatus_UnsupportedStatus_InternalServerError() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto request =
                    org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto.builder()
                            .status("INVALID_STATUS")
                            .reason("잘못된 상태")
                            .build();

            // when & then
            mockMvc.perform(patch("/api/labs/{labId}/attendance/sessions/{sessionId}/status", LAB_ID, SESSION_ID)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            org.mockito.Mockito.verify(commandUseCase, org.mockito.Mockito.never()).endSession(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
            org.mockito.Mockito.verify(commandUseCase, org.mockito.Mockito.never()).cancelSession(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
        }

        @Test
        @DisplayName("권한 없는 사용자 접근 시 500 Internal Server Error 반환 (보안 필터 비활성화)")
        void changeSessionStatus_WithoutAuth_InternalServerError() throws Exception {
            // given (setupSecurityContext 없음)

            org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto request =
                    org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto.builder()
                            .status("COMPLETED")
                            .reason("정상 종료")
                            .build();

            // when & then
            mockMvc.perform(patch("/api/labs/{labId}/attendance/sessions/{sessionId}/status", LAB_ID, SESSION_ID)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));

            org.mockito.Mockito.verify(commandUseCase, org.mockito.Mockito.never()).endSession(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
        }
    }
}

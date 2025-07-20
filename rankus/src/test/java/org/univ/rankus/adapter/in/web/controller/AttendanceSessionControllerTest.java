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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}

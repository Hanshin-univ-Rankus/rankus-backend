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
import org.springframework.http.MediaType;
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
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceSessionController.class)
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
    // 1) POST /api/labs/{labId}/attendance/sessions - 출석 세션 생성
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("POST /api/labs/{labId}/attendance/sessions")
    class CreateSessionTests {

        @Test
        @DisplayName("출석 세션 생성 성공 → 201 Created")
        void createSession_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            Map<String, Object> request = new HashMap<>();
            request.put("title", "오전 출석");
            request.put("qrValidityMinutes", 5);

            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            given(commandUseCase.createSession(eq(LAB_ID), eq("오전 출석"), eq(5), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 세션 생성 시 400 Bad Request")
        void createSession_badRequest() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            Map<String, Object> request = new HashMap<>();
            request.put("title", ""); // 빈 제목
            request.put("qrValidityMinutes", 0); // 잘못된 유효시간

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증 없이 세션 생성 시 500 Internal Server Error")
        void createSession_unauthenticated() throws Exception {
            // given
            Map<String, Object> request = new HashMap<>();
            request.put("title", "오전 출석");
            request.put("qrValidityMinutes", 5);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));
        }
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
            given(commandUseCase.endSession(eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/{sessionId}/end", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }

        @Test
        @DisplayName("존재하지 않는 세션 종료 시 404 Not Found")
        void endSession_sessionNotFound() throws Exception {
            // given
            setupSecurityContext(USER_ID);
            Long nonExistentSessionId = 999L;

            doThrow(new AttendanceNotFoundException(AttendanceErrorCode.SESSION_NOT_FOUND))
                    .when(commandUseCase).endSession(eq(nonExistentSessionId), eq(USER_ID));

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/{sessionId}/end", LAB_ID, nonExistentSessionId))
                    .andExpect(status().isNotFound()) // AttendanceNotFoundException은 404로 매핑
                    .andExpect(jsonPath("$.code").value("ATT_404"));
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
            given(commandUseCase.cancelSession(eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/{sessionId}/cancel", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) PUT /api/labs/{labId}/attendance/sessions/{sessionId}/title - 제목 수정
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("PUT /api/labs/{labId}/attendance/sessions/{sessionId}/title")
    class UpdateTitleTests {

        @Test
        @DisplayName("세션 제목 수정 성공 → 200 OK")
        void updateTitle_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            Map<String, Object> request = new HashMap<>();
            request.put("title", "수정된 출석");

            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            given(commandUseCase.updateSessionTitle(eq(SESSION_ID), eq("수정된 출석"), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/sessions/{sessionId}/title", LAB_ID, SESSION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) PUT /api/labs/{labId}/attendance/sessions/{sessionId}/qr-validity - QR 유효시간 수정
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("PUT /api/labs/{labId}/attendance/sessions/{sessionId}/qr-validity")
    class UpdateQRValidityTests {

        @Test
        @DisplayName("QR 유효시간 수정 성공 → 200 OK")
        void updateQRValidity_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            Map<String, Object> request = new HashMap<>();
            request.put("qrValidityMinutes", 10);

            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            given(commandUseCase.updateQRValidityMinutes(eq(SESSION_ID), eq(10), eq(USER_ID)))
                    .willReturn(mockSession);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/sessions/{sessionId}/qr-validity", LAB_ID, SESSION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
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
            given(commandUseCase.generateQRCode(eq(SESSION_ID), eq(USER_ID)))
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
    // 7) POST /api/labs/{labId}/attendance/sessions/check - 출석 체크
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("POST /api/labs/{labId}/attendance/sessions/check")
    class CheckAttendanceTests {

        @Test
        @DisplayName("출석 체크 성공 → 200 OK")
        void checkAttendance_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            Map<String, Object> request = new HashMap<>();
            request.put("qrToken", "1-123-1640995200");

            AttendanceRecord mockRecord = DomainAttendanceFactory.buildValidRecord();
            given(commandUseCase.checkAttendance(eq("1-123-1640995200"), eq(USER_ID)))
                    .willReturn(mockRecord);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/check", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.userId").value(mockRecord.getUserId()));
        }

        @Test
        @DisplayName("잘못된 QR 토큰으로 출석 체크 시 400 Bad Request")
        void checkAttendance_invalidToken() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            Map<String, Object> request = new HashMap<>();
            request.put("qrToken", "invalid-token");

            doThrow(new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID))
                    .when(commandUseCase).checkAttendance(eq("invalid-token"), eq(USER_ID));

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/attendance/sessions/check", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("ATT_009"));
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

    // ——————————————————————————————————————————————————————————
    // 11) GET /api/labs/{labId}/attendance/sessions/{sessionId}/statistics - 통계
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("GET /api/labs/{labId}/attendance/sessions/{sessionId}/statistics")
    class GetStatisticsTests {

        @Test
        @DisplayName("출석 세션 통계 조회 성공 → 200 OK")
        void getStatistics_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            AttendanceSession.AttendanceStatistics mockStats =
                    AttendanceSession.AttendanceStatistics.from(10L, 8L, 1L, 1L);
            given(queryUseCase.getSessionStatistics(eq(SESSION_ID), eq(USER_ID)))
                    .willReturn(mockStats);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/sessions/{sessionId}/statistics", LAB_ID, SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalMembers").value(10))
                    .andExpect(jsonPath("$.data.presentCount").value(8))
                    .andExpect(jsonPath("$.data.lateCount").value(1))
                    .andExpect(jsonPath("$.data.absentCount").value(1));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 12) GET /api/labs/{labId}/attendance/sessions/my-sessions - 내 참여 세션
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("GET /api/labs/{labId}/attendance/sessions/my-sessions")
    class GetMySessionsTests {

        @Test
        @DisplayName("내가 참여한 출석 세션 목록 조회 성공 → 200 OK")
        void getMySessions_success() throws Exception {
            // given
            setupSecurityContext(USER_ID);

            List<AttendanceSession> mockSessions = Arrays.asList(
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession()
            );
            given(queryUseCase.findSessionsByUserId(eq(USER_ID), eq(0), eq(20)))
                    .willReturn(mockSessions);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/sessions/my-sessions", LAB_ID)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }
}
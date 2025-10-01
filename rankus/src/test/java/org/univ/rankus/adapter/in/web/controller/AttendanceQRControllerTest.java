package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.MediaType;
import org.univ.rankus.application.port.in.AttendanceSessionCommandUseCase;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.service.qr.QRTokenCryptoService;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;
import org.univ.rankus.domain.model.attendance.SecureQRToken;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.willThrow;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;

/**
 * (16) resolve API 통합(WebMvc) 테스트 - 기본 케이스
 */
@WebMvcTest(controllers = AttendanceQRController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceQRControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QRTokenCryptoService qrTokenCryptoService;

    @MockitoBean
    private AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;

    @MockitoBean
    private AttendanceSessionCommandUseCase attendanceSessionCommandUseCase;

    private AttendanceSession buildSession(Long labId, Long sessionId, SessionStatus status, int validityMinutes) {
        // AttendanceSession.create 사용 (labId, createdBy, title, validity)
        AttendanceSession session = AttendanceSession.create(labId, 1L, "테스트 세션", validityMinutes);
        // 세션 ID 강제 설정을 위해 리플렉션 사용 (테스트 편의)
        try {
            var f = AttendanceSession.class.getDeclaredField("sessionId");
            f.setAccessible(true);
            f.set(session, sessionId);
        } catch (Exception ignored) {}
        // 상태가 ACTIVE 아닌 경우 변경을 위해 status 필드를 리플렉션 (테스트 목적)
        if (status != SessionStatus.ACTIVE) {
            try {
                var sf = AttendanceSession.class.getDeclaredField("status");
                sf.setAccessible(true);
                sf.set(session, status);
            } catch (Exception ignored) {}
        }
        return session;
    }

    private void setAuthUser(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(principal, null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("GET /api/attendance/qr/resolve")
    class ResolveTests {

        @Test
        @DisplayName("SECURE 토큰 유효 → valid=true, reason=OK")
        void resolve_secure_valid() throws Exception {
            String token = "SECURE_TOKEN_ABC";
            Long labId = 11L; Long sessionId = 99L;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = now.plusMinutes(5);

            SecureQRToken.QRTokenPayload payload = new SecureQRToken.QRTokenPayload(labId, sessionId, now.toString(), expires.toString(), "nonce");
            given(qrTokenCryptoService.decrypt(token)).willReturn(payload);
            given(attendanceSessionQueryUseCase.findSessionByIdPublic(sessionId))
                    .willReturn(buildSession(labId, sessionId, SessionStatus.ACTIVE, 5));

            ResultActions ra = mockMvc.perform(get("/api/attendance/qr/resolve")
                    .param("token", token));

            ra.andExpect(status().isOk())
              .andExpect(jsonPath("$.data.valid").value(true))
              .andExpect(jsonPath("$.data.reason").value("OK"))
              .andExpect(jsonPath("$.data.type").value("SECURE"))
              .andExpect(jsonPath("$.data.sessionId").value(sessionId));
        }

        @Test
        @DisplayName("Secure 복호화 실패 + Legacy 파싱 실패 → valid=false, reason=INVALID")
        void resolve_invalid() throws Exception {
            String token = "BAD_TOKEN";
            // Secure decrypt throw
            given(qrTokenCryptoService.decrypt(token)).willThrow(new RuntimeException("decrypt fail"));
            // Legacy 파싱도 실패시키기 위해 AttendanceValidationException 던지는 상황 시뮬 필요 없음 (컨트롤러 내부 legacy fromString 에서 실패) → 별도 mock 불필요
            // 호출
            ResultActions ra = mockMvc.perform(get("/api/attendance/qr/resolve")
                    .param("token", token));

            ra.andExpect(status().isOk())
              .andExpect(jsonPath("$.data.valid").value(false))
              .andExpect(jsonPath("$.data.reason").value("INVALID"));
        }

        @Test
        @DisplayName("SECURE 토큰이지만 세션이 COMPLETED → valid=false, reason=SESSION_INACTIVE")
        void resolve_secure_inactive() throws Exception {
            String token = "SECURE_TOKEN_INACTIVE";
            Long labId = 7L; Long sessionId = 123L;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = now.plusMinutes(5);
            SecureQRToken.QRTokenPayload payload = new SecureQRToken.QRTokenPayload(labId, sessionId, now.toString(), expires.toString(), "nonce");
            given(qrTokenCryptoService.decrypt(token)).willReturn(payload);
            given(attendanceSessionQueryUseCase.findSessionByIdPublic(sessionId))
                    .willReturn(buildSession(labId, sessionId, SessionStatus.COMPLETED, 5));

            ResultActions ra = mockMvc.perform(get("/api/attendance/qr/resolve")
                    .param("token", token));

            ra.andExpect(status().isOk())
              .andExpect(jsonPath("$.data.valid").value(false))
              .andExpect(jsonPath("$.data.reason").value("SESSION_INACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/attendance/check")
    class GlobalCheckTests {
        @Test
        @DisplayName("글로벌 출석 체크 성공")
        void globalCheck_success() throws Exception {
            String token = "SECURE_TOKEN_SUCCESS";
            Long labId = 10L; Long sessionId = 500L; Long userId = 77L;
            setAuthUser(userId);
            // decrypt labId
            SecureQRToken.QRTokenPayload payload = new SecureQRToken.QRTokenPayload(labId, sessionId, LocalDateTime.now().toString(), LocalDateTime.now().plusMinutes(5).toString(), "nonce");
            given(qrTokenCryptoService.decrypt(token)).willReturn(payload);
            // AttendanceRecord mock
            AttendanceRecord record = mock(AttendanceRecord.class);
            AttendanceSession session = buildSession(labId, sessionId, SessionStatus.ACTIVE, 5);
            given(record.getRecordId()).willReturn(1000L);
            given(record.getAttendanceSession()).willReturn(session);
            given(record.getUserId()).willReturn(userId);
            given(record.getCheckedAt()).willReturn(LocalDateTime.now());
            given(attendanceSessionCommandUseCase.checkAttendance(labId, token, userId)).willReturn(record);

            mockMvc.perform(post("/api/attendance/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"" + token + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.sessionId").value(sessionId))
                    .andExpect(jsonPath("$.data.userId").value(userId));
        }

        @Test
        @DisplayName("글로벌 출석 체크 - 이미 출석 (ALREADY_CHECKED_IN)")
        void globalCheck_already() throws Exception {
            String token = "SECURE_TOKEN_ALREADY";
            Long labId = 10L; Long sessionId = 501L; Long userId = 78L;
            setAuthUser(userId);
            SecureQRToken.QRTokenPayload payload = new SecureQRToken.QRTokenPayload(labId, sessionId, LocalDateTime.now().toString(), LocalDateTime.now().plusMinutes(5).toString(), "nonce");
            given(qrTokenCryptoService.decrypt(token)).willReturn(payload);
            willThrow(new org.univ.rankus.domain.model.attendance.exception.AttendancePermissionException(AttendanceErrorCode.ALREADY_CHECKED_IN))
                    .given(attendanceSessionCommandUseCase).checkAttendance(labId, token, userId);

            mockMvc.perform(post("/api/attendance/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"" + token + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(AttendanceErrorCode.ALREADY_CHECKED_IN.getMessage()));
        }

        @Test
        @DisplayName("글로벌 출석 체크 - 만료된 토큰")
        void globalCheck_expired() throws Exception {
            String token = "SECURE_TOKEN_EXPIRED";
            Long labId = 10L; Long sessionId = 502L; Long userId = 79L;
            setAuthUser(userId);
            SecureQRToken.QRTokenPayload payload = new SecureQRToken.QRTokenPayload(labId, sessionId, LocalDateTime.now().minusMinutes(10).toString(), LocalDateTime.now().minusMinutes(5).toString(), "nonce");
            // decrypt 는 성공하지만 check 단계 전에 서비스에서 만료 예외 발생 시뮬레이션
            given(qrTokenCryptoService.decrypt(token)).willReturn(payload);
            willThrow(new org.univ.rankus.domain.model.attendance.exception.AttendancePermissionException(AttendanceErrorCode.QR_TOKEN_EXPIRED))
                    .given(attendanceSessionCommandUseCase).checkAttendance(labId, token, userId);

            mockMvc.perform(post("/api/attendance/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\":\"" + token + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(AttendanceErrorCode.QR_TOKEN_EXPIRED.getMessage()));
        }
    }
}

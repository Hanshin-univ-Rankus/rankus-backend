package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
import org.univ.rankus.adapter.in.web.dto.request.AttendanceStatusUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AttendanceRecordResponseDto;
import org.univ.rankus.application.port.in.AttendanceRecordCommandUseCase;
import org.univ.rankus.application.port.in.AttendanceRecordQueryUseCase;
import org.univ.rankus.common.security.permission.AttendanceRecordPermissionHandler;
import org.univ.rankus.common.security.permission.AttendanceSessionPermissionHandler;
import org.univ.rankus.common.security.permission.UnifiedPermissionEvaluator;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceRecordController 테스트")
class AttendanceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceRecordCommandUseCase commandUseCase;

    @MockitoBean
    private AttendanceRecordQueryUseCase queryUseCase;

    @MockitoBean
    private AttendanceRecordPermissionHandler attendanceRecordPermissionHandler;

    @MockitoBean
    private AttendanceSessionPermissionHandler attendanceSessionPermissionHandler;

    @MockitoBean
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;

    private static final Long RECORD_ID = 1L;
    private static final Long SESSION_ID = 1L;
    private static final Long USER_ID = 2L;

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

    @Test
    @DisplayName("PUT /api/attendance/records/{recordId}/present > 출석 처리 성공 → 200 OK")
    void markAsPresent_Success() throws Exception {
        // given
        setupSecurityContext(USER_ID);
        AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecordWithId(RECORD_ID);
        record.markAsPresent(USER_ID, "테스트 수동 출석 처리");
        AttendanceRecordResponseDto response = AttendanceRecordResponseDto.from(record);
        
        AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(AttendanceStatus.PRESENT, "테스트 출석 처리");

        given(commandUseCase.markAsPresent(eq(RECORD_ID), eq(USER_ID), eq("테스트 출석 처리"))).willReturn(record);
        given(attendanceRecordPermissionHandler.hasPermission(any(), eq(RECORD_ID), eq("MANAGE"))).willReturn(true);

        // when & then
        mockMvc.perform(put("/api/attendance/records/{recordId}/present", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("PUT /api/attendance/records/{recordId}/late > 지각 처리 성공 → 200 OK")
    void markAsLate_Success() throws Exception {
        // given
        setupSecurityContext(USER_ID);
        AttendanceRecord record = DomainAttendanceFactory.buildValidRecordWithId(RECORD_ID);
        record.markAsLate(USER_ID, "테스트 수동 지각 처리");
        AttendanceRecordResponseDto response = AttendanceRecordResponseDto.from(record);
        
        AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(AttendanceStatus.LATE, "테스트 지각 처리");

        given(commandUseCase.markAsLate(eq(RECORD_ID), eq(USER_ID), eq("테스트 지각 처리"))).willReturn(record);
        given(attendanceRecordPermissionHandler.hasPermission(any(), eq(RECORD_ID), eq("MANAGE"))).willReturn(true);

        // when & then
        mockMvc.perform(put("/api/attendance/records/{recordId}/late", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("PUT /api/attendance/records/{recordId}/absent > 결석 처리 성공 → 200 OK")
    void markAsAbsent_Success() throws Exception {
        // given
        setupSecurityContext(USER_ID);
        AttendanceRecord record = DomainAttendanceFactory.buildValidRecordWithId(RECORD_ID);
        record.markAsAbsent(USER_ID, "테스트 수동 결석 처리");
        AttendanceRecordResponseDto response = AttendanceRecordResponseDto.from(record);
        
        AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(AttendanceStatus.ABSENT, "테스트 결석 처리");

        given(commandUseCase.markAsAbsent(eq(RECORD_ID), eq(USER_ID), eq("테스트 결석 처리"))).willReturn(record);
        given(attendanceRecordPermissionHandler.hasPermission(any(), eq(RECORD_ID), eq("MANAGE"))).willReturn(true);

        // when & then
        mockMvc.perform(put("/api/attendance/records/{recordId}/absent", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("존재하지 않는 출석 기록 조작 시 404 Not Found")
    void markAsPresent_NotFound() throws Exception {
        // given
        setupSecurityContext(USER_ID);
        AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(AttendanceStatus.PRESENT, "테스트 출석 처리");
        
        given(commandUseCase.markAsPresent(eq(999L), eq(USER_ID), eq("테스트 출석 처리")))
                .willThrow(new AttendanceNotFoundException(999L));

        // when & then
        mockMvc.perform(put("/api/attendance/records/{recordId}/present", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("인증 없는 요청 시 500 Internal Server Error (필터 비활성화)")
    void unauthenticatedRequest_InternalServerError() throws Exception {
        // given
        AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(AttendanceStatus.PRESENT, "테스트 출석 처리");
        
        // when & then - 필터가 비활성화되어 userDetails가 null이 되어 500 발생
        mockMvc.perform(put("/api/attendance/records/{recordId}/present", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
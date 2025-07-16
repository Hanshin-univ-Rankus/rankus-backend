package org.univ.rankus.adapter.in.web.lab.attendance;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateRequest;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateResponse;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.LabAttendanceManagementResponse;
import org.univ.rankus.application.port.in.command.BulkUpdateAttendanceCommand;
import org.univ.rankus.application.port.in.query.GetLabAttendanceManagementQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.common.security.LabMemberPermissionEvaluator;
import org.univ.rankus.common.security.permission.UnifiedPermissionEvaluator;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.attendance.SessionStatus;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.user.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LabAttendanceManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabAttendanceManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetLabAttendanceManagementQuery getLabAttendanceManagementQuery;

    @MockitoBean
    private BulkUpdateAttendanceCommand bulkUpdateAttendanceCommand;

    @MockitoBean
    private LabMemberPermissionEvaluator labMemberPermissionEvaluator;

    @MockitoBean
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;

    private static final Long LAB_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Long RECORD_ID_1 = 101L;
    private static final Long RECORD_ID_2 = 102L;


    @Nested
    @DisplayName("GET /api/labs/{labId}/attendance/management")
    class GetLabAttendanceManagementTests {

        @Test
        @DisplayName("관리자 권한으로 랩실 출석 관리 통합 뷰 조회 → 200 OK")
        void getLabAttendanceManagement_AdminRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            LabAttendanceManagementResponse response = createMockAttendanceManagementResponse();

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabAttendanceManagementQuery.getLabAttendanceManagement(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/management", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 출석 관리 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.sessions").isArray())
                    .andExpect(jsonPath("$.data.sessions.length()").value(2))
                    .andExpect(jsonPath("$.data.sessions[0].sessionId").value(1L))
                    .andExpect(jsonPath("$.data.sessions[0].title").value("AI 세미나"))
                    .andExpect(jsonPath("$.data.sessions[0].totalParticipants").value(10))
                    .andExpect(jsonPath("$.data.sessions[0].presentCount").value(8))
                    .andExpect(jsonPath("$.data.sessions[0].attendanceRate").value(80.0))
                    .andExpect(jsonPath("$.data.overallStats.totalSessions").value(2))
                    .andExpect(jsonPath("$.data.overallStats.overallAttendanceRate").value(85.0))
                    .andExpect(jsonPath("$.data.overallStats.activeMembers").value(15));
        }

        @Test
        @DisplayName("교수 권한으로 랩실 출석 관리 통합 뷰 조회 → 200 OK")
        void getLabAttendanceManagement_ProfessorRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.PROFESSOR, null);

            LabAttendanceManagementResponse response = createMockAttendanceManagementResponse();

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabAttendanceManagementQuery.getLabAttendanceManagement(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/management", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 출석 관리 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.sessions").isArray())
                    .andExpect(jsonPath("$.data.overallStats").exists());
        }

        @Test
        @DisplayName("랩 리더 권한으로 소속 랩실 출석 관리 통합 뷰 조회 → 200 OK")
        void getLabAttendanceManagement_LabLeaderRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_LEADER, LAB_ID);

            LabAttendanceManagementResponse response = createMockAttendanceManagementResponse();

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabAttendanceManagementQuery.getLabAttendanceManagement(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/management", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 출석 관리 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.sessions").isArray());
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 조회 → 404 Not Found")
        void getLabAttendanceManagement_LabNotFound_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabAttendanceManagementQuery.getLabAttendanceManagement(eq(LAB_ID), eq(USER_ID)))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/management", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"));
        }

        @Test
        @DisplayName("권한 없는 사용자 조회 → 403 Forbidden")
        void getLabAttendanceManagement_PermissionDenied_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.STUDENT, null);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(false);
            given(getLabAttendanceManagementQuery.getLabAttendanceManagement(eq(LAB_ID), eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/attendance/management", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("LAB_007"));
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/attendance/bulk-update")
    class BulkUpdateAttendanceTests {

        @Test
        @DisplayName("관리자 권한으로 출석 일괄 수정 → 200 OK + 완전 성공")
        void bulkUpdateAttendance_AdminRole_CompleteSuccess() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            BulkAttendanceUpdateRequest request = createBulkUpdateRequest();
            BulkAttendanceUpdateResponse response = createSuccessfulBulkUpdateResponse();

            given(labMemberPermissionEvaluator.canManageLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(bulkUpdateAttendanceCommand.bulkUpdateAttendance(eq(LAB_ID), any(BulkAttendanceUpdateRequest.class), eq(USER_ID)))
                    .willReturn(response);

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("출석 정보를 성공적으로 일괄 수정했습니다."))
                    .andExpect(jsonPath("$.data.totalUpdates").value(2))
                    .andExpect(jsonPath("$.data.successfulUpdates").value(2))
                    .andExpect(jsonPath("$.data.failedUpdates").value(0))
                    .andExpect(jsonPath("$.data.results").isArray())
                    .andExpect(jsonPath("$.data.results.length()").value(2))
                    .andExpect(jsonPath("$.data.results[0].success").value(true))
                    .andExpect(jsonPath("$.data.results[1].success").value(true));
        }

        @Test
        @DisplayName("교수 권한으로 출석 일괄 수정 → 200 OK + 부분 성공")
        void bulkUpdateAttendance_ProfessorRole_PartialSuccess() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.PROFESSOR, null);

            BulkAttendanceUpdateRequest request = createBulkUpdateRequest();
            BulkAttendanceUpdateResponse response = createPartialSuccessBulkUpdateResponse();

            given(labMemberPermissionEvaluator.canManageLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(bulkUpdateAttendanceCommand.bulkUpdateAttendance(eq(LAB_ID), any(BulkAttendanceUpdateRequest.class), eq(USER_ID)))
                    .willReturn(response);

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("출석 정보를 성공적으로 일괄 수정했습니다."))
                    .andExpect(jsonPath("$.data.totalUpdates").value(2))
                    .andExpect(jsonPath("$.data.successfulUpdates").value(1))
                    .andExpect(jsonPath("$.data.failedUpdates").value(1))
                    .andExpect(jsonPath("$.data.results[0].success").value(true))
                    .andExpect(jsonPath("$.data.results[1].success").value(false))
                    .andExpect(jsonPath("$.data.results[1].errorMessage").value("Status is already PRESENT"));
        }

        @Test
        @DisplayName("랩 매니저 권한으로 소속 랩실 출석 일괄 수정 → 200 OK")
        void bulkUpdateAttendance_LabManagerRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MANAGER, LAB_ID);

            BulkAttendanceUpdateRequest request = createBulkUpdateRequest();
            BulkAttendanceUpdateResponse response = createSuccessfulBulkUpdateResponse();

            given(labMemberPermissionEvaluator.canManageLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(bulkUpdateAttendanceCommand.bulkUpdateAttendance(eq(LAB_ID), any(BulkAttendanceUpdateRequest.class), eq(USER_ID)))
                    .willReturn(response);

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("출석 정보를 성공적으로 일괄 수정했습니다."))
                    .andExpect(jsonPath("$.data.totalUpdates").value(2))
                    .andExpect(jsonPath("$.data.successfulUpdates").value(2))
                    .andExpect(jsonPath("$.data.failedUpdates").value(0));
        }

        @Test
        @DisplayName("빈 수정 목록으로 요청 → 400 Bad Request")
        void bulkUpdateAttendance_EmptyUpdatesList_ValidationError() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            BulkAttendanceUpdateRequest request = new BulkAttendanceUpdateRequest();
            request.setUpdates(List.of()); // 빈 목록
            request.setReason("테스트 사유");

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"));
        }

        @Test
        @DisplayName("사유 없이 요청 → 400 Bad Request")
        void bulkUpdateAttendance_NoReason_ValidationError() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            BulkAttendanceUpdateRequest request = new BulkAttendanceUpdateRequest();
            request.setUpdates(Arrays.asList(
                    createAttendanceUpdateItem(RECORD_ID_1, AttendanceStatus.PRESENT)
            ));
            request.setReason(null); // 사유 없음

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"));
        }

        @Test
        @DisplayName("권한 없는 사용자 수정 → 403 Forbidden")
        void bulkUpdateAttendance_PermissionDenied_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MEMBER, LAB_ID);

            BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

            given(labMemberPermissionEvaluator.canManageLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(false);
            given(bulkUpdateAttendanceCommand.bulkUpdateAttendance(eq(LAB_ID), any(BulkAttendanceUpdateRequest.class), eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_ATTENDANCE_MANAGE_PERMISSION_DENIED));

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("LAB_012"));
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 수정 → 404 Not Found")
        void bulkUpdateAttendance_LabNotFound_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

            given(labMemberPermissionEvaluator.canManageLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(bulkUpdateAttendanceCommand.bulkUpdateAttendance(eq(LAB_ID), any(BulkAttendanceUpdateRequest.class), eq(USER_ID)))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/attendance/bulk-update", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"));
        }
    }

    // Helper methods
    private CustomUserDetails createMockUserDetails(Long userId, Role role, Long labId) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);
        given(userDetails.getRole()).willReturn(role);
        given(userDetails.getLabId()).willReturn(labId);
        return userDetails;
    }


    private LabAttendanceManagementResponse createMockAttendanceManagementResponse() {
        LabAttendanceManagementResponse.AttendanceSessionSummary session1 = 
                new LabAttendanceManagementResponse.AttendanceSessionSummary(
                        1L, "AI 세미나", LocalDateTime.now().minusDays(1), 
                        LocalDateTime.now().minusDays(1).plusHours(2),
                        SessionStatus.COMPLETED, 10, 8, 1, 1, 80.0, 
                        LocalDateTime.now().minusDays(1), "관리자"
                );

        LabAttendanceManagementResponse.AttendanceSessionSummary session2 = 
                new LabAttendanceManagementResponse.AttendanceSessionSummary(
                        2L, "DB 스터디", LocalDateTime.now().minusDays(2), 
                        LocalDateTime.now().minusDays(2).plusHours(1),
                        SessionStatus.COMPLETED, 12, 11, 0, 1, 91.7, 
                        LocalDateTime.now().minusDays(2), "관리자"
                );

        LabAttendanceManagementResponse.AttendanceOverallStats overallStats = 
                new LabAttendanceManagementResponse.AttendanceOverallStats(
                        2, 22, 19, 1, 2, 85.0, 15, LocalDateTime.now().minusDays(1)
                );

        return new LabAttendanceManagementResponse(
                Arrays.asList(session1, session2), 
                overallStats
        );
    }

    private BulkAttendanceUpdateRequest createBulkUpdateRequest() {
        BulkAttendanceUpdateRequest request = new BulkAttendanceUpdateRequest();
        request.setUpdates(Arrays.asList(
                createAttendanceUpdateItem(RECORD_ID_1, AttendanceStatus.PRESENT),
                createAttendanceUpdateItem(RECORD_ID_2, AttendanceStatus.LATE)
        ));
        request.setReason("출석 점검 후 일괄 수정");
        return request;
    }

    private BulkAttendanceUpdateRequest.AttendanceUpdateItem createAttendanceUpdateItem(Long recordId, AttendanceStatus status) {
        BulkAttendanceUpdateRequest.AttendanceUpdateItem item = new BulkAttendanceUpdateRequest.AttendanceUpdateItem();
        item.setRecordId(recordId);
        item.setNewStatus(status);
        return item;
    }

    private BulkAttendanceUpdateResponse createSuccessfulBulkUpdateResponse() {
        BulkAttendanceUpdateResponse.UpdateResult result1 = new BulkAttendanceUpdateResponse.UpdateResult(
                RECORD_ID_1, true, AttendanceStatus.ABSENT, AttendanceStatus.PRESENT, null
        );
        BulkAttendanceUpdateResponse.UpdateResult result2 = new BulkAttendanceUpdateResponse.UpdateResult(
                RECORD_ID_2, true, AttendanceStatus.ABSENT, AttendanceStatus.LATE, null
        );

        return new BulkAttendanceUpdateResponse(
                2, 2, 0, Arrays.asList(result1, result2)
        );
    }

    private BulkAttendanceUpdateResponse createPartialSuccessBulkUpdateResponse() {
        BulkAttendanceUpdateResponse.UpdateResult result1 = new BulkAttendanceUpdateResponse.UpdateResult(
                RECORD_ID_1, true, AttendanceStatus.ABSENT, AttendanceStatus.PRESENT, null
        );
        BulkAttendanceUpdateResponse.UpdateResult result2 = new BulkAttendanceUpdateResponse.UpdateResult(
                RECORD_ID_2, false, AttendanceStatus.PRESENT, AttendanceStatus.PRESENT, "Status is already PRESENT"
        );

        return new BulkAttendanceUpdateResponse(
                2, 1, 1, Arrays.asList(result1, result2)
        );
    }
}
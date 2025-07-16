package org.univ.rankus.adapter.in.web.lab.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.lab.statistics.dto.LabComprehensiveStatsResponse;
import org.univ.rankus.application.port.in.lab.statistics.GetLabComprehensiveStatsQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.common.security.LabMemberPermissionEvaluator;
import org.univ.rankus.common.security.permission.UnifiedPermissionEvaluator;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.user.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LabComprehensiveStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabComprehensiveStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetLabComprehensiveStatsQuery getLabComprehensiveStatsQuery;

    @MockitoBean
    private LabMemberPermissionEvaluator labMemberPermissionEvaluator;

    @MockitoBean
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;

    private static final Long LAB_ID = 1L;
    private static final Long USER_ID = 42L;


    @Nested
    @DisplayName("GET /api/labs/comprehensive-stats")
    class GetLabComprehensiveStatsTests {

        @Test
        @DisplayName("관리자 권한으로 전체 랩실 종합 통계 조회 → 200 OK")
        void getLabComprehensiveStats_AdminRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            LabComprehensiveStatsResponse response = createMockComprehensiveStatsResponse();

            given(getLabComprehensiveStatsQuery.getLabComprehensiveStats(eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/comprehensive-stats")
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("전체 랩실 종합 통계를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.labStats").isArray())
                    .andExpect(jsonPath("$.data.labStats.length()").value(2))
                    .andExpect(jsonPath("$.data.labStats[0].labId").value(1L))
                    .andExpect(jsonPath("$.data.labStats[0].labName").value("AI 연구실"))
                    .andExpect(jsonPath("$.data.labStats[0].category").value("AI"))
                    .andExpect(jsonPath("$.data.labStats[0].professorName").value("김교수"))
                    .andExpect(jsonPath("$.data.labStats[0].totalMembers").value(15))
                    .andExpect(jsonPath("$.data.labStats[0].averageAttendanceRate").value(85.5))
                    .andExpect(jsonPath("$.data.labStats[0].totalScore").value(1250))
                    .andExpect(jsonPath("$.data.overallSummary.totalLabs").value(2))
                    .andExpect(jsonPath("$.data.overallSummary.totalMembers").value(27))
                    .andExpect(jsonPath("$.data.overallSummary.averageAttendanceRate").value(82.75))
                    .andExpect(jsonPath("$.data.overallSummary.mostActiveLabByAttendance.labId").value(1L))
                    .andExpect(jsonPath("$.data.overallSummary.mostActiveLabByScore.labId").value(1L))
                    .andExpect(jsonPath("$.data.overallSummary.largestLab.labId").value(1L));
        }

        @Test
        @DisplayName("교수 권한으로 전체 랩실 종합 통계 조회 → 200 OK")
        void getLabComprehensiveStats_ProfessorRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.PROFESSOR, null);

            LabComprehensiveStatsResponse response = createMockComprehensiveStatsResponse();

            given(getLabComprehensiveStatsQuery.getLabComprehensiveStats(eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/comprehensive-stats")
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("전체 랩실 종합 통계를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.labStats").isArray())
                    .andExpect(jsonPath("$.data.overallSummary").exists());
        }

        @Test
        @DisplayName("일반 사용자 권한으로 전체 랩실 종합 통계 조회 → 403 Forbidden")
        void getLabComprehensiveStats_RegularUserRole_Forbidden() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MEMBER, LAB_ID);

            given(getLabComprehensiveStatsQuery.getLabComprehensiveStats(eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_STATISTICS_VIEW_PERMISSION_DENIED));

            // when & then
            mockMvc.perform(get("/api/labs/comprehensive-stats")
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("LAB_013"));
        }

        @Test
        @DisplayName("학생 권한으로 전체 랩실 종합 통계 조회 → 403 Forbidden")
        void getLabComprehensiveStats_StudentRole_Forbidden() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.STUDENT, null);

            given(getLabComprehensiveStatsQuery.getLabComprehensiveStats(eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_STATISTICS_VIEW_PERMISSION_DENIED));

            // when & then
            mockMvc.perform(get("/api/labs/comprehensive-stats")
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("LAB_013"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 → 404 Not Found")
        void getLabComprehensiveStats_UserNotFound_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            given(getLabComprehensiveStatsQuery.getLabComprehensiveStats(eq(USER_ID)))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/comprehensive-stats")
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/detailed-stats")
    class GetLabDetailedStatsTests {

        @Test
        @DisplayName("관리자 권한으로 특정 랩실 상세 통계 조회 → 200 OK")
        void getLabDetailedStats_AdminRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            LabComprehensiveStatsResponse.LabStats labStats = createMockLabStats(1L, "AI 연구실", LabCategory.AI);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabComprehensiveStatsQuery.getLabDetailedStats(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(labStats);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/detailed-stats", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 상세 통계를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.labId").value(1L))
                    .andExpect(jsonPath("$.data.labName").value("AI 연구실"))
                    .andExpect(jsonPath("$.data.category").value("AI"))
                    .andExpect(jsonPath("$.data.professorName").value("김교수"))
                    .andExpect(jsonPath("$.data.totalMembers").value(15))
                    .andExpect(jsonPath("$.data.labLeaderCount").value(1))
                    .andExpect(jsonPath("$.data.labManagerCount").value(2))
                    .andExpect(jsonPath("$.data.labMemberCount").value(12))
                    .andExpect(jsonPath("$.data.averageAttendanceRate").value(85.5))
                    .andExpect(jsonPath("$.data.totalScore").value(1250))
                    .andExpect(jsonPath("$.data.totalSessions").value(20))
                    .andExpect(jsonPath("$.data.totalScoreSubmissions").value(45))
                    .andExpect(jsonPath("$.data.totalApplications").value(30))
                    .andExpect(jsonPath("$.data.totalNotices").value(15))
                    .andExpect(jsonPath("$.data.totalInterviews").value(25));
        }

        @Test
        @DisplayName("교수 권한으로 특정 랩실 상세 통계 조회 → 200 OK")
        void getLabDetailedStats_ProfessorRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.PROFESSOR, null);

            LabComprehensiveStatsResponse.LabStats labStats = createMockLabStats(1L, "AI 연구실", LabCategory.AI);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabComprehensiveStatsQuery.getLabDetailedStats(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(labStats);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/detailed-stats", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 상세 통계를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.labId").value(1L))
                    .andExpect(jsonPath("$.data.labName").value("AI 연구실"));
        }

        @Test
        @DisplayName("랩 리더 권한으로 소속 랩실 상세 통계 조회 → 200 OK")
        void getLabDetailedStats_LabLeaderRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_LEADER, LAB_ID);

            LabComprehensiveStatsResponse.LabStats labStats = createMockLabStats(1L, "AI 연구실", LabCategory.AI);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabComprehensiveStatsQuery.getLabDetailedStats(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(labStats);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/detailed-stats", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 상세 통계를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.labId").value(1L))
                    .andExpect(jsonPath("$.data.totalMembers").value(15));
        }

        @Test
        @DisplayName("일반 랩원 권한으로 소속 랩실 상세 통계 조회 → 200 OK")
        void getLabDetailedStats_LabMemberRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MEMBER, LAB_ID);

            LabComprehensiveStatsResponse.LabStats labStats = createMockLabStats(1L, "AI 연구실", LabCategory.AI);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabComprehensiveStatsQuery.getLabDetailedStats(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(labStats);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/detailed-stats", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 상세 통계를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.labId").value(1L));
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 조회 → 404 Not Found")
        void getLabDetailedStats_LabNotFound_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabComprehensiveStatsQuery.getLabDetailedStats(eq(LAB_ID), eq(USER_ID)))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/detailed-stats", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"));
        }

        @Test
        @DisplayName("권한 없는 사용자 조회 → 403 Forbidden")
        void getLabDetailedStats_PermissionDenied_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.STUDENT, null);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(false);
            given(getLabComprehensiveStatsQuery.getLabDetailedStats(eq(LAB_ID), eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/detailed-stats", LAB_ID)
                            .principal(new TestingAuthenticationToken(principal, null)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("LAB_007"));
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


    private LabComprehensiveStatsResponse createMockComprehensiveStatsResponse() {
        LabComprehensiveStatsResponse.LabStats labStats1 = createMockLabStats(1L, "AI 연구실", LabCategory.AI);
        LabComprehensiveStatsResponse.LabStats labStats2 = createMockLabStats(2L, "DB 연구실", LabCategory.DB);

        List<LabComprehensiveStatsResponse.LabStats> labStatsList = Arrays.asList(labStats1, labStats2);

        LabComprehensiveStatsResponse.OverallSummary overallSummary = new LabComprehensiveStatsResponse.OverallSummary(
                2, // totalLabs
                27, // totalMembers
                35, // totalSessions
                70, // totalScoreSubmissions
                50, // totalApplications
                25, // totalNotices
                40, // totalInterviews
                82.75, // averageAttendanceRate
                2, // activeLabs
                labStats1, // mostActiveLabByAttendance
                labStats1, // mostActiveLabByScore
                labStats1  // largestLab
        );

        return new LabComprehensiveStatsResponse(labStatsList, overallSummary);
    }

    private LabComprehensiveStatsResponse.LabStats createMockLabStats(Long labId, String labName, LabCategory category) {
        LocalDateTime now = LocalDateTime.now();
        return new LabComprehensiveStatsResponse.LabStats(
                labId, // labId
                labName, // labName
                category, // category
                "김교수", // professorName
                1, // ranking
                15, // totalMembers
                1, // labLeaderCount
                2, // labManagerCount
                12, // labMemberCount
                15, // activeMembers
                20, // totalSessions
                85.5, // averageAttendanceRate
                now.minusDays(1), // lastSessionDate
                45, // totalScoreSubmissions
                1250, // totalScore
                40, // approvedSubmissions
                5, // pendingSubmissions
                30, // totalApplications
                8, // pendingApplications
                22, // approvedApplications
                15, // totalNotices
                now.minusDays(2), // lastNoticeDate
                25, // totalInterviews
                20, // completedInterviews
                5 // pendingInterviews
        );
    }
}
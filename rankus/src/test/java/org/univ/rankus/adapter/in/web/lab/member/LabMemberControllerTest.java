package org.univ.rankus.adapter.in.web.lab.member;

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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;
import org.univ.rankus.application.port.in.lab.member.GetLabMembersQuery;
import org.univ.rankus.common.security.LabMemberPermissionEvaluator;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.common.security.permission.UnifiedPermissionEvaluator;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LabMemberController.class)
@AutoConfigureMockMvc
class LabMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetLabMembersQuery getLabMembersQuery;

    @MockitoBean
    private LabMemberPermissionEvaluator labMemberPermissionEvaluator;

    @MockitoBean
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;

    private static final Long LAB_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Long MEMBER_ID = 123L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/members")
    class GetLabMembersTests {

        @Test
        @DisplayName("관리자 권한으로 랩실 멤버 목록 조회 → 200 OK + 상세 정보 포함")
        void getLabMembers_AdminRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);
            setupSecurityContext(principal);

            User member1 = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));
            User member2 = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));
            List<User> members = Arrays.asList(member1, member2);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMembers(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(members);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members", LAB_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_ADMIN"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 목록을 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(member1.getId()))
                    .andExpect(jsonPath("$.data[0].name").value(member1.getName()))
                    .andExpect(jsonPath("$.data[0].email").value(member1.getEmail())) // 관리자는 이메일 볼 수 있음
                    .andExpect(jsonPath("$.data[1].id").value(member2.getId()))
                    .andExpect(jsonPath("$.data[1].name").value(member2.getName()));
        }

        @Test
        @DisplayName("일반 랩원 권한으로 랩실 멤버 목록 조회 → 200 OK + 기본 정보만")
        void getLabMembers_LabMemberRole_BasicInfoOnly() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MEMBER, LAB_ID);
            setupSecurityContext(principal);

            User member1 = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));
            User member2 = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));
            List<User> members = Arrays.asList(member1, member2);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMembers(eq(LAB_ID), eq(USER_ID)))
                    .willReturn(members);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members", LAB_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_LAB_MEMBER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 목록을 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(member1.getId()))
                    .andExpect(jsonPath("$.data[0].name").value(member1.getName()))
                    .andExpect(jsonPath("$.data[0].email").doesNotExist()) // 일반 랩원은 이메일 못 봄
                    .andExpect(jsonPath("$.data[0].phoneNumber").doesNotExist()); // 일반 랩원은 전화번호 못 봄
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 조회 → 404 Not Found")
        void getLabMembers_LabNotFound_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);
            setupSecurityContext(principal);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMembers(eq(LAB_ID), eq(USER_ID)))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members", LAB_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_ADMIN"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"));
        }

        @Test
        @DisplayName("권한 없는 사용자 조회 → 403 Forbidden")
        void getLabMembers_PermissionDenied_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.STUDENT, null);
            setupSecurityContext(principal);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(false);
            given(getLabMembersQuery.getLabMembers(eq(LAB_ID), eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members", LAB_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_STUDENT"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("LAB_007"));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/members/{memberId}")
    class GetLabMemberTests {

        @Test
        @DisplayName("교수 권한으로 특정 랩실 멤버 조회 → 200 OK + 상세 정보 포함")
        void getLabMember_ProfessorRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.PROFESSOR, null);
            setupSecurityContext(principal);

            User member = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMember(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willReturn(member);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_PROFESSOR"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.id").value(member.getId()))
                    .andExpect(jsonPath("$.data.name").value(member.getName()))
                    .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                    .andExpect(jsonPath("$.data.phoneNumber").value(member.getPhoneNumber()));
        }

        @Test
        @DisplayName("랩 리더 권한으로 동일 랩실 멤버 조회 → 200 OK + 상세 정보 포함")
        void getLabMember_LabLeaderRole_SameLabMember_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_LEADER, LAB_ID);
            setupSecurityContext(principal);

            User member = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMember(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willReturn(member);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_LAB_LEADER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.id").value(member.getId()))
                    .andExpect(jsonPath("$.data.name").value(member.getName()))
                    .andExpect(jsonPath("$.data.email").value(member.getEmail()));
        }

        @Test
        @DisplayName("일반 랩원 권한으로 동일 랩실 멤버 조회 → 200 OK + 기본 정보만")
        void getLabMember_LabMemberRole_BasicInfoOnly() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MEMBER, LAB_ID);
            setupSecurityContext(principal);

            User member = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMember(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willReturn(member);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_LAB_MEMBER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.id").value(member.getId()))
                    .andExpect(jsonPath("$.data.name").value(member.getName()))
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.phoneNumber").doesNotExist());
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회 → 404 Not Found")
        void getLabMember_MemberNotFound_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);
            setupSecurityContext(principal);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMember(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willThrow(new LabValidationException(LabErrorCode.LAB_MEMBER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_ADMIN"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("LAB_009"));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/members/{memberId}/detail")
    class GetLabMemberDetailTests {

        @Test
        @DisplayName("관리자 권한으로 랩실 멤버 상세 조회 → 200 OK + 활동 통계 포함")
        void getLabMemberDetail_AdminRole_Success() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.ADMIN, null);
            setupSecurityContext(principal);

            LabMemberDetailResponse.LabActivityStats stats = new LabMemberDetailResponse.LabActivityStats(
                    10, 8, 1, 1, 80.0, 5, 450, 10
            );
            LabMemberDetailResponse response = createMockLabMemberDetailResponse(stats);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMemberDetail(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}/detail", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_ADMIN"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 상세 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.id").value(MEMBER_ID))
                    .andExpect(jsonPath("$.data.name").value("테스트 멤버"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.activityStats").exists())
                    .andExpect(jsonPath("$.data.activityStats.totalAttendanceSessions").value(10))
                    .andExpect(jsonPath("$.data.activityStats.attendanceRate").value(80.0))
                    .andExpect(jsonPath("$.data.activityStats.scoreSubmissions").value(5));
        }

        @Test
        @DisplayName("일반 랩원 권한으로 랩실 멤버 상세 조회 → 200 OK + 기본 정보만")
        void getLabMemberDetail_LabMemberRole_BasicInfoOnly() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.LAB_MEMBER, LAB_ID);
            setupSecurityContext(principal);

            LabMemberDetailResponse response = createMockLabMemberDetailResponse(null);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(true);
            given(getLabMembersQuery.getLabMemberDetail(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}/detail", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_LAB_MEMBER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 멤버 상세 정보를 성공적으로 조회했습니다."))
                    .andExpect(jsonPath("$.data.id").value(MEMBER_ID))
                    .andExpect(jsonPath("$.data.name").value("테스트 멤버"))
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.activityStats").doesNotExist());
        }

        @Test
        @DisplayName("권한 없는 사용자 조회 → 403 Forbidden")
        void getLabMemberDetail_PermissionDenied_ThrowsException() throws Exception {
            // given
            CustomUserDetails principal = createMockUserDetails(USER_ID, Role.STUDENT, null);
            setupSecurityContext(principal);

            given(labMemberPermissionEvaluator.canViewLabMembers(eq(LAB_ID), eq(USER_ID))).willReturn(false);
            given(getLabMembersQuery.getLabMemberDetail(eq(LAB_ID), eq(MEMBER_ID), eq(USER_ID)))
                    .willThrow(new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/members/{memberId}/detail", LAB_ID, MEMBER_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    new TestingAuthenticationToken(principal, null, "ROLE_STUDENT"))))
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

    private void setupSecurityContext(CustomUserDetails principal) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private LabMemberDetailResponse createMockLabMemberDetailResponse(LabMemberDetailResponse.LabActivityStats stats) {
        return new LabMemberDetailResponse(
                MEMBER_ID,
                "테스트 멤버",
                "2021001234",
                3,
                Role.LAB_MEMBER,
                org.univ.rankus.domain.model.user.EnrollmentStatus.ENROLLED,
                java.time.LocalDateTime.now(),
                stats != null ? "test@example.com" : null,
                stats != null ? "010-1234-5678" : null,
                stats
        );
    }
}
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.request.ScoreSubmissionApprovalRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.ScoreSubmissionCreateRequestDto;
import org.univ.rankus.application.port.in.command.ScoreSubmissionCommandUseCase;
import org.univ.rankus.application.port.in.query.ScoreSubmissionQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.ranking.policy.DuplicateCheckPolicy;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScoreSubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ScoreSubmissionController 테스트")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScoreSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScoreSubmissionCommandUseCase scoreSubmissionCommandUseCase;

    @MockitoBean
    private ScoreSubmissionQueryUseCase scoreSubmissionQueryUseCase;

    @MockitoBean
    private org.univ.rankus.application.port.in.command.FileUploadUseCase fileUploadUseCase;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 테스트용 SecurityContext 설정 헬퍼 메서드
     *
     * @param userId 사용자 ID
     */
    private void setupSecurityContext(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        authentication.setAuthenticated(true); // 중요: 인증 상태를 true로 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/score-submissions - 점수 신청 생성")
    class CreateScoreSubmissionTests {

        @Test
        @DisplayName("유효한 요청으로 점수 신청 생성 성공")
        void createScoreSubmission_ValidRequest_Success() throws Exception {
            // given: principal 세팅
            Long userId = 1L;
            setupSecurityContext(userId);

            // given
            ScoreSubmissionCreateRequestDto request = ScoreSubmissionCreateRequestDto.builder()
                    .labId(1L)
                    .category(ScoreCategory.RESEARCH_SCI_PAPER)
                    .achievementDescription("SCI 논문 게재 성과")
                    .achievementDate(LocalDate.now().minusDays(30))
                    .proofFileUrl("https://example.com/proof.pdf")
                    .applicationReason("연구 성과 인정 신청")
                    .relatedLink("https://example.com/paper")
                    .visibility(VisibilityLevel.PUBLIC)
                    .build();

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L);
            given(scoreSubmissionCommandUseCase.submitScore(
                    anyLong(), eq(1L), eq(ScoreCategory.RESEARCH_SCI_PAPER),
                    eq("SCI 논문 게재 성과"), eq(request.getAchievementDate()),
                    eq("https://example.com/proof.pdf"), eq("연구 성과 인정 신청"),
                    eq("https://example.com/paper"), eq(VisibilityLevel.PUBLIC)
            )).willReturn(submission);

            // when & then
            mockMvc.perform(post("/api/score-submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.category").value("RESEARCH_SCI_PAPER"))
                    .andExpect(jsonPath("$.data.achievementDescription").value("SCI 논문 게재 성과"));

            verify(scoreSubmissionCommandUseCase).submitScore(
                    anyLong(), eq(1L), eq(ScoreCategory.RESEARCH_SCI_PAPER),
                    eq("SCI 논문 게재 성과"), eq(request.getAchievementDate()),
                    eq("https://example.com/proof.pdf"), eq("연구 성과 인정 신청"),
                    eq("https://example.com/paper"), eq(VisibilityLevel.PUBLIC)
            );
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 Bad Request 반환")
        void createScoreSubmission_MissingRequiredFields_BadRequest() throws Exception {
            // given: principal 세팅
            setupSecurityContext(1L);

            // given
            ScoreSubmissionCreateRequestDto request = ScoreSubmissionCreateRequestDto.builder()
                    .labId(1L)
                    // category 누락
                    .achievementDescription("성과 내용")
                    .achievementDate(LocalDate.now().minusDays(30))
                    .proofFileUrl("https://example.com/proof.pdf")
                    .visibility(VisibilityLevel.PUBLIC)
                    .build();

            // when & then
            mockMvc.perform(post("/api/score-submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(scoreSubmissionCommandUseCase, never()).submitScore(
                    anyLong(), anyLong(), any(), anyString(), any(), anyString(), anyString(), anyString(), any()
            );
        }

        @Test
        @DisplayName("인증 없이 요청 시 500 Internal Server Error 반환 (보안 필터 비활성화)")
        void createScoreSubmission_WithoutAuth_InternalServerError() throws Exception {
            // given
            ScoreSubmissionCreateRequestDto request = ScoreSubmissionCreateRequestDto.builder()
                    .labId(1L)
                    .category(ScoreCategory.RESEARCH_SCI_PAPER)
                    .achievementDescription("SCI 논문 게재 성과")
                    .achievementDate(LocalDate.now().minusDays(30))
                    .proofFileUrl("https://example.com/proof.pdf")
                    .visibility(VisibilityLevel.PUBLIC)
                    .build();

            // when & then - 보안 필터가 비활성화되어 있어 userDetails가 null이 되어 500 에러 발생
            mockMvc.perform(post("/api/score-submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));

            verify(scoreSubmissionCommandUseCase, never()).submitScore(
                    anyLong(), anyLong(), any(), anyString(), any(), anyString(), anyString(), anyString(), any()
            );
        }
    }

    @Nested
    @DisplayName("GET /api/score-submissions/{submissionId} - 점수 신청 상세 조회")
    class GetScoreSubmissionTests {

        @Test
        @DisplayName("존재하는 점수 신청 조회 성공")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void getScoreSubmission_ExistingSubmission_Success() throws Exception {
            // given
            Long submissionId = 1L;
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
            given(scoreSubmissionQueryUseCase.findSubmissionById(submissionId))
                    .willReturn(submission);

            // when & then
            mockMvc.perform(get("/api/score-submissions/{submissionId}", submissionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(submissionId))
                    .andExpect(jsonPath("$.data.category").value("RESEARCH_SCI_PAPER"));

            verify(scoreSubmissionQueryUseCase).findSubmissionById(submissionId);
        }

        @Test
        @DisplayName("존재하지 않는 점수 신청 조회 시 404 Not Found 반환")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void getScoreSubmission_NonExistingSubmission_NotFound() throws Exception {
            // given
            Long submissionId = 999L;
            given(scoreSubmissionQueryUseCase.findSubmissionById(submissionId))
                    .willThrow(new RankingValidationException(RankingErrorCode.SUBMISSION_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/score-submissions/{submissionId}", submissionId))
                    .andExpect(status().isNotFound());

            verify(scoreSubmissionQueryUseCase).findSubmissionById(submissionId);
        }
    }

    @Nested
    @DisplayName("GET /api/score-submissions/my - 내 점수 신청 목록 조회")
    class GetMyScoreSubmissionsTests {

        @Test
        @DisplayName("내 점수 신청 목록 조회 성공")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void getMyScoreSubmissions_Success() throws Exception {
            // given: principal 세팅
            Long userId = 1L;
            setupSecurityContext(userId);

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L),
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(2L)
            );
            Page<ScoreSubmission> submissionPage = new PageImpl<>(submissions, PageRequest.of(0, 20), submissions.size());
            given(scoreSubmissionQueryUseCase.findSubmissionsByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(submissionPage);

            // when & then
            mockMvc.perform(get("/api/score-submissions/my")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2));

            verify(scoreSubmissionQueryUseCase).findSubmissionsByUserId(eq(userId), any(Pageable.class));
        }

        @Test
        @DisplayName("인증 없이 요청 시 500 Internal Server Error 반환 (보안 필터 비활성화)")
        void getMyScoreSubmissions_WithoutAuth_InternalServerError() throws Exception {
            // when & then - 보안 필터가 비활성화되어 있어 userDetails가 null이 되어 500 에러 발생
            mockMvc.perform(get("/api/score-submissions/my"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));

            verify(scoreSubmissionQueryUseCase, never()).findSubmissionsByUserId(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("POST /api/score-submissions/{submissionId}/approve - 점수 신청 승인")
    class ApproveScoreSubmissionTests {

        @Test
        @DisplayName("LAB_MANAGER 역할로 점수 신청 승인 성공")
        void approveScoreSubmission_AsLabManager_Success() throws Exception {
            // given: principal 세팅
            Long userId = 2L;
            setupSecurityContext(userId);

            Long submissionId = 1L;
            doNothing().when(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), eq(userId));

            // when & then
            mockMvc.perform(post("/api/score-submissions/{submissionId}/approve", submissionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));

            verify(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), eq(userId));
        }

        @Test
        @DisplayName("LAB_LEADER 역할로 점수 신청 승인 성공")
        void approveScoreSubmission_AsLabLeader_Success() throws Exception {
            // given: principal 세팅
            Long userId = 3L;
            setupSecurityContext(userId);

            Long submissionId = 1L;
            doNothing().when(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), eq(userId));

            // when & then
            mockMvc.perform(post("/api/score-submissions/{submissionId}/approve", submissionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));

            verify(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), eq(userId));
        }

        @Test
        @DisplayName("STUDENT 역할로 승인 시도 시 403 Forbidden 반환")
        @WithMockUser(username = "student@hs.ac.kr", roles = "STUDENT")
        void approveScoreSubmission_AsStudent_Forbidden() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(1L); // SecurityContext 설정

            // 권한 부족 시 예외 발생하도록 설정
            doThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"))
                    .when(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), anyLong());

            // when & then
            mockMvc.perform(post("/api/score-submissions/{submissionId}/approve", submissionId))
                    .andExpect(status().isForbidden());

            verify(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), anyLong());
        }

        @Test
        @DisplayName("권한 없는 사용자가 승인 시도 시 403 Forbidden 반환")
        @WithMockUser(username = "manager@hs.ac.kr", roles = "LAB_MANAGER")
        void approveScoreSubmission_InsufficientPermission_Forbidden() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(2L); // SecurityContext 설정
            doThrow(new RankingValidationException(RankingErrorCode.INSUFFICIENT_PERMISSION))
                    .when(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), anyLong());

            // when & then
            mockMvc.perform(post("/api/score-submissions/{submissionId}/approve", submissionId))
                    .andExpect(status().isForbidden());

            verify(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), anyLong());
        }
    }

    @Nested
    @DisplayName("POST /api/score-submissions/{submissionId}/reject - 점수 신청 거부")
    class RejectScoreSubmissionTests {

        @Test
        @DisplayName("유효한 거부 사유로 점수 신청 거부 성공")
        @WithMockUser(username = "manager@hs.ac.kr", roles = "LAB_MANAGER")
        void rejectScoreSubmission_ValidReason_Success() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(2L); // SecurityContext 설정
            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .rejectionReason("증빙서류 부족")
                    .build();

            doNothing().when(scoreSubmissionCommandUseCase)
                    .rejectSubmission(eq(submissionId), anyLong(), eq("증빙서류 부족"));

            // when & then
            mockMvc.perform(post("/api/score-submissions/{submissionId}/reject", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));

            verify(scoreSubmissionCommandUseCase).rejectSubmission(eq(submissionId), anyLong(), eq("증빙서류 부족"));
        }

        @Test
        @DisplayName("STUDENT 역할로 거부 시도 시 403 Forbidden 반환")
        @WithMockUser(username = "student@hs.ac.kr", roles = "STUDENT")
        void rejectScoreSubmission_AsStudent_Forbidden() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(1L); // SecurityContext 설정
            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .rejectionReason("증빙서류 부족")
                    .build();

            // 권한 부족 시 예외 발생하도록 설정
            doThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"))
                    .when(scoreSubmissionCommandUseCase).rejectSubmission(eq(submissionId), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/score-submissions/{submissionId}/reject", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(scoreSubmissionCommandUseCase).rejectSubmission(eq(submissionId), anyLong(), eq("증빙서류 부족"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/score-submissions/{submissionId} - 점수 신청 삭제")
    class DeleteScoreSubmissionTests {

        @Test
        @DisplayName("본인이 신청한 점수 삭제 성공")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void deleteScoreSubmission_OwnSubmission_Success() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(1L); // SecurityContext 설정
            doNothing().when(scoreSubmissionCommandUseCase).deleteSubmission(eq(submissionId), anyLong());

            // when & then
            mockMvc.perform(delete("/api/score-submissions/{submissionId}", submissionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));

            verify(scoreSubmissionCommandUseCase).deleteSubmission(eq(submissionId), anyLong());
        }

        @Test
        @DisplayName("타인이 신청한 점수 삭제 시도 시 403 Forbidden 반환")
        @WithMockUser(username = "other@hs.ac.kr", roles = "STUDENT")
        void deleteScoreSubmission_NotOwner_Forbidden() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(2L); // SecurityContext 설정
            doThrow(new RankingValidationException(RankingErrorCode.NOT_OWNER))
                    .when(scoreSubmissionCommandUseCase).deleteSubmission(eq(submissionId), anyLong());

            // when & then
            mockMvc.perform(delete("/api/score-submissions/{submissionId}", submissionId))
                    .andExpect(status().isForbidden());

            verify(scoreSubmissionCommandUseCase).deleteSubmission(eq(submissionId), anyLong());
        }

        @Test
        @DisplayName("이미 처리된 점수 삭제 시도 시 409 Conflict 반환")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void deleteScoreSubmission_AlreadyProcessed_Conflict() throws Exception {
            // given
            Long submissionId = 1L;
            setupSecurityContext(1L); // SecurityContext 설정
            doThrow(new RankingValidationException(RankingErrorCode.ALREADY_PROCESSED))
                    .when(scoreSubmissionCommandUseCase).deleteSubmission(eq(submissionId), anyLong());

            // when & then
            mockMvc.perform(delete("/api/score-submissions/{submissionId}", submissionId))
                    .andExpect(status().isConflict());

            verify(scoreSubmissionCommandUseCase).deleteSubmission(eq(submissionId), anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/score-submissions/check-duplicates - 중복 검사")
    class CheckDuplicatesTests {

        @Test
        @DisplayName("중복 검사 성공")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void checkDuplicates_Success() throws Exception {
            // given: principal 세팅
            Long userId = 1L;
            setupSecurityContext(userId);

            LocalDate achievementDate = LocalDate.now().minusDays(30);
            ScoreCategory category = ScoreCategory.RESEARCH_SCI_PAPER;

            DuplicateCheckPolicy.DuplicateCheckResult result = new DuplicateCheckPolicy.DuplicateCheckResult(
                    false, false, List.of()
            );

            given(scoreSubmissionQueryUseCase.checkDuplicates(eq(userId), eq(achievementDate), eq(category)))
                    .willReturn(result);

            // when & then
            mockMvc.perform(get("/api/score-submissions/check-duplicates")
                            .param("achievementDate", achievementDate.toString())
                            .param("category", category.name()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.hasDuplicates").value(false));

            verify(scoreSubmissionQueryUseCase).checkDuplicates(eq(userId), eq(achievementDate), eq(category));
        }

        @Test
        @DisplayName("인증 없이 중복 검사 시도 시 500 Internal Server Error 반환 (보안 필터 비활성화)")
        void checkDuplicates_WithoutAuth_InternalServerError() throws Exception {
            // given
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            ScoreCategory category = ScoreCategory.RESEARCH_SCI_PAPER;

            // when & then - 보안 필터가 비활성화되어 있어 userDetails가 null이 되어 500 에러 발생
            mockMvc.perform(get("/api/score-submissions/check-duplicates")
                            .param("achievementDate", achievementDate.toString())
                            .param("category", category.name()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));

            verify(scoreSubmissionQueryUseCase, never()).checkDuplicates(anyLong(), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/score-submissions/my-total-score - 내 총 점수 조회")
    class GetMyTotalScoreTests {

        @Test
        @DisplayName("내 총 점수 조회 성공")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void getMyTotalScore_Success() throws Exception {
            // given
            setupSecurityContext(1L); // SecurityContext 설정
            int totalScore = 150;
            given(scoreSubmissionQueryUseCase.calculateUserTotalScore(anyLong()))
                    .willReturn(totalScore);

            // when & then
            mockMvc.perform(get("/api/score-submissions/my-total-score"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").value(totalScore));

            verify(scoreSubmissionQueryUseCase).calculateUserTotalScore(anyLong());
        }

        @Test
        @DisplayName("인증 없이 요청 시 500 Internal Server Error 반환 (보안 필터 비활성화)")
        void getMyTotalScore_WithoutAuth_InternalServerError() throws Exception {
            // when & then - 보안 필터가 비활성화되어 있어 userDetails가 null이 되어 500 에러 발생
            mockMvc.perform(get("/api/score-submissions/my-total-score"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));

            verify(scoreSubmissionQueryUseCase, never()).calculateUserTotalScore(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/score-submissions/my-lab-score/{labId} - 랩실 내 내 점수 조회")
    class GetMyLabScoreTests {

        @Test
        @DisplayName("랩실 내 내 점수 조회 성공")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void getMyLabScore_Success() throws Exception {
            // given
            setupSecurityContext(1L); // SecurityContext 설정
            Long labId = 1L;
            int labScore = 100;
            given(scoreSubmissionQueryUseCase.calculateUserScoreInLab(anyLong(), eq(labId)))
                    .willReturn(labScore);

            // when & then
            mockMvc.perform(get("/api/score-submissions/my-lab-score/{labId}", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").value(labScore));

            verify(scoreSubmissionQueryUseCase).calculateUserScoreInLab(anyLong(), eq(labId));
        }

        @Test
        @DisplayName("존재하지 않는 랩실 조회 시 404 Not Found 반환")
        @WithMockUser(username = "user@hs.ac.kr", roles = "STUDENT")
        void getMyLabScore_LabNotFound_NotFound() throws Exception {
            // given
            setupSecurityContext(1L); // SecurityContext 설정
            Long labId = 999L;
            given(scoreSubmissionQueryUseCase.calculateUserScoreInLab(anyLong(), eq(labId)))
                    .willThrow(new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/score-submissions/my-lab-score/{labId}", labId))
                    .andExpect(status().isNotFound());

            verify(scoreSubmissionQueryUseCase).calculateUserScoreInLab(anyLong(), eq(labId));
        }
    }

    @Nested
    @DisplayName("PATCH /api/score-submissions/{submissionId}/status - 점수 신청 상태 변경 (RESTful)")
    class ChangeSubmissionStatusTests {

        @Test
        @DisplayName("점수 신청 승인 성공")
        @WithMockUser(username = "admin@hs.ac.kr", roles = "ADMIN")
        void changeSubmissionStatus_ApproveSubmission_Success() throws Exception {
            // given
            setupSecurityContext(1L);
            Long submissionId = 1L;

            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .status("APPROVED")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/score-submissions/{submissionId}/status", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("점수 신청이 승인되었습니다"));

            verify(scoreSubmissionCommandUseCase).approveSubmission(eq(submissionId), anyLong());
        }

        @Test
        @DisplayName("점수 신청 거부 성공")
        @WithMockUser(username = "admin@hs.ac.kr", roles = "ADMIN")
        void changeSubmissionStatus_RejectSubmission_Success() throws Exception {
            // given
            setupSecurityContext(1L);
            Long submissionId = 1L;
            String rejectionReason = "증빙자료가 불충분합니다";

            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .status("REJECTED")
                    .rejectionReason(rejectionReason)
                    .build();

            // when & then
            mockMvc.perform(patch("/api/score-submissions/{submissionId}/status", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("점수 신청이 거부되었습니다"));

            verify(scoreSubmissionCommandUseCase).rejectSubmission(eq(submissionId), anyLong(), eq(rejectionReason));
        }

        @Test
        @DisplayName("상태값 누락 시 500 Internal Server Error 반환 (컨트롤러 내부 검증)")
        @WithMockUser(username = "admin@hs.ac.kr", roles = "ADMIN")
        void changeSubmissionStatus_MissingStatus_InternalServerError() throws Exception {
            // given
            setupSecurityContext(1L);
            Long submissionId = 1L;

            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .build(); // status 없음

            // when & then
            mockMvc.perform(patch("/api/score-submissions/{submissionId}/status", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            verify(scoreSubmissionCommandUseCase, never()).approveSubmission(anyLong(), anyLong());
            verify(scoreSubmissionCommandUseCase, never()).rejectSubmission(anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("거부 시 사유 누락 시 500 Internal Server Error 반환 (컨트롤러 내부 검증)")
        @WithMockUser(username = "admin@hs.ac.kr", roles = "ADMIN")
        void changeSubmissionStatus_RejectWithoutReason_InternalServerError() throws Exception {
            // given
            setupSecurityContext(1L);
            Long submissionId = 1L;

            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .status("REJECTED")
                    .build(); // rejectionReason 없음

            // when & then
            mockMvc.perform(patch("/api/score-submissions/{submissionId}/status", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            verify(scoreSubmissionCommandUseCase, never()).rejectSubmission(anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("지원하지 않는 상태값 시 500 Internal Server Error 반환 (컨트롤러 내부 검증)")
        @WithMockUser(username = "admin@hs.ac.kr", roles = "ADMIN")
        void changeSubmissionStatus_UnsupportedStatus_InternalServerError() throws Exception {
            // given
            setupSecurityContext(1L);
            Long submissionId = 1L;

            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .status("INVALID_STATUS")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/score-submissions/{submissionId}/status", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));

            verify(scoreSubmissionCommandUseCase, never()).approveSubmission(anyLong(), anyLong());
            verify(scoreSubmissionCommandUseCase, never()).rejectSubmission(anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("권한 없는 사용자 접근 시 500 Internal Server Error 반환 (보안 필터 비활성화)")
        void changeSubmissionStatus_WithoutAuth_InternalServerError() throws Exception {
            // given
            Long submissionId = 1L;

            ScoreSubmissionApprovalRequestDto request = ScoreSubmissionApprovalRequestDto.builder()
                    .status("APPROVED")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/score-submissions/{submissionId}/status", submissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("GLOBAL_002"));

            verify(scoreSubmissionCommandUseCase, never()).approveSubmission(anyLong(), anyLong());
        }
    }
}
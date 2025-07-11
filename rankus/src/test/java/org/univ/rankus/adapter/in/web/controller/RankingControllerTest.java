package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@DisplayName("RankingController 테스트")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RankingQueryUseCase rankingQueryUseCase;

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
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(principal, null));
    }

    @Nested
    @DisplayName("전체 랩실 랭킹 조회 테스트")
    class GetLabRankingsTest {

        @Test
        @DisplayName("전체 랩실 랭킹 조회 성공")
        @WithMockUser(roles = "STUDENT")
        void getLabRankings_Success() throws Exception {
            // given
            List<RankingQueryUseCase.LabRankingResult> rankings = Arrays.asList(
                    createLabRankingResult(1L, "AI랩", 1500, 1),
                    createLabRankingResult(2L, "웹개발랩", 1200, 2),
                    createLabRankingResult(3L, "보안랩", 1000, 3)
            );

            Pageable pageable = PageRequest.of(0, 20);
            Page<RankingQueryUseCase.LabRankingResult> rankingPage = new PageImpl<>(rankings, pageable, rankings.size());

            given(rankingQueryUseCase.getLabRankings(any(Pageable.class))).willReturn(rankingPage);

            // when & then
            mockMvc.perform(get("/api/rankings/labs")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.data.content[0].labId").value(1))
                    .andExpect(jsonPath("$.data.content[0].labName").value("AI랩"))
                    .andExpect(jsonPath("$.data.content[0].totalScore").value(1500))
                    .andExpect(jsonPath("$.data.content[0].rank").value(1));

            verify(rankingQueryUseCase).getLabRankings(any(Pageable.class));
        }

        @Test
        @DisplayName("페이징 파라미터를 사용한 랭킹 조회")
        @WithMockUser(roles = "STUDENT")
        void getLabRankings_WithPagination_Success() throws Exception {
            // given
            List<RankingQueryUseCase.LabRankingResult> rankings = Arrays.asList(
                    createLabRankingResult(4L, "네트워크랩", 800, 4),
                    createLabRankingResult(5L, "모바일랩", 600, 5)
            );

            Pageable pageable = PageRequest.of(1, 2);
            Page<RankingQueryUseCase.LabRankingResult> rankingPage = new PageImpl<>(rankings, pageable, 10);

            given(rankingQueryUseCase.getLabRankings(any(Pageable.class))).willReturn(rankingPage);

            // when & then
            mockMvc.perform(get("/api/rankings/labs")
                            .param("page", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(10))
                    .andExpect(jsonPath("$.data.totalPages").value(5));

            verify(rankingQueryUseCase).getLabRankings(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("특정 랩실 랭킹 조회 테스트")
    class GetLabRankingTest {

        @Test
        @DisplayName("특정 랩실 랭킹 조회 성공")
        @WithMockUser(roles = "STUDENT")
        void getLabRanking_Success() throws Exception {
            // given
            Long labId = 1L;
            RankingQueryUseCase.LabRankingResult ranking = createLabRankingResult(labId, "AI랩", 1500, 1);

            given(rankingQueryUseCase.getLabRanking(labId)).willReturn(ranking);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.labId").value(1))
                    .andExpect(jsonPath("$.data.labName").value("AI랩"))
                    .andExpect(jsonPath("$.data.totalScore").value(1500))
                    .andExpect(jsonPath("$.data.rank").value(1));

            verify(rankingQueryUseCase).getLabRanking(labId);
        }

        @Test
        @DisplayName("존재하지 않는 랩실 랭킹 조회시 404 반환")
        @WithMockUser(roles = "STUDENT")
        void getLabRanking_NotFound_404() throws Exception {
            // given
            Long labId = 999L;
            given(rankingQueryUseCase.getLabRanking(labId))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}", labId))
                    .andExpect(status().isNotFound());

            verify(rankingQueryUseCase).getLabRanking(labId);
        }
    }

    @Nested
    @DisplayName("랩실 상위 기여자 조회 테스트")
    class GetTopContributorsTest {

        @Test
        @DisplayName("상위 기여자 조회 성공")
        @WithMockUser(roles = "STUDENT")
        void getTopContributors_Success() throws Exception {
            // given
            Long labId = 1L;
            List<RankingQueryUseCase.UserContribution> contributors = Arrays.asList(
                    createUserContribution(1L, "김철수", 500),
                    createUserContribution(2L, "이영희", 400),
                    createUserContribution(3L, "박민수", 300)
            );

            given(rankingQueryUseCase.getTopContributors(labId, 5)).willReturn(contributors);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}/contributors", labId)
                            .param("limit", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].userId").value(1))
                    .andExpect(jsonPath("$.data[0].userName").value("김철수"))
                    .andExpect(jsonPath("$.data[0].contributionScore").value(500));

            verify(rankingQueryUseCase).getTopContributors(labId, 5);
        }

        @Test
        @DisplayName("기본 limit으로 상위 기여자 조회")
        @WithMockUser(roles = "STUDENT")
        void getTopContributors_DefaultLimit_Success() throws Exception {
            // given
            Long labId = 1L;
            List<RankingQueryUseCase.UserContribution> contributors = Arrays.asList(
                    createUserContribution(1L, "김철수", 500)
            );

            given(rankingQueryUseCase.getTopContributors(labId, 5)).willReturn(contributors);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}/contributors", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(rankingQueryUseCase).getTopContributors(labId, 5);
        }
    }

    @Nested
    @DisplayName("내 랩실 랭킹 조회 테스트")
    class GetMyLabRankingsTest {

        @Test
        @DisplayName("내 랩실 랭킹 조회 성공")
        void getMyLabRankings_Success() throws Exception {
            // given
            Long userId = 1L;
            setupSecurityContext(userId);

            List<RankingQueryUseCase.LabRankingResult> rankings = Arrays.asList(
                    createLabRankingResult(1L, "AI랩", 1500, 1),
                    createLabRankingResult(2L, "웹개발랩", 1200, 2)
            );

            given(rankingQueryUseCase.getUserLabRankings(userId)).willReturn(rankings);

            // when & then
            mockMvc.perform(get("/api/rankings/my-labs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(rankingQueryUseCase).getUserLabRankings(userId);
        }
    }

    @Nested
    @DisplayName("랩실 총 점수 조회 테스트")
    class GetLabTotalScoreTest {

        @Test
        @DisplayName("랩실 총 점수 조회 성공")
        @WithMockUser(roles = "STUDENT")
        void getLabTotalScore_Success() throws Exception {
            // given
            Long labId = 1L;
            int totalScore = 1500;

            given(rankingQueryUseCase.calculateLabTotalScore(labId)).willReturn(totalScore);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}/total-score", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").value(1500));

            verify(rankingQueryUseCase).calculateLabTotalScore(labId);
        }

        @Test
        @DisplayName("존재하지 않는 랩실의 총 점수 조회시 404 반환")
        @WithMockUser(roles = "STUDENT")
        void getLabTotalScore_NotFound_404() throws Exception {
            // given
            Long labId = 999L;
            given(rankingQueryUseCase.calculateLabTotalScore(labId))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}/total-score", labId))
                    .andExpect(status().isNotFound());

            verify(rankingQueryUseCase).calculateLabTotalScore(labId);
        }
    }

    @Nested
    @DisplayName("내 기여도 조회 테스트")
    class GetMyContributionTest {

        @Test
        @DisplayName("랩실에서 내 기여도 조회 성공")
        void getMyContributionInLab_Success() throws Exception {
            // given
            Long labId = 1L;
            Long userId = 1L;
            setupSecurityContext(userId);

            int contribution = 500;

            given(rankingQueryUseCase.calculateUserContributionInLab(userId, labId)).willReturn(contribution);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/{labId}/my-contribution", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").value(500));

            verify(rankingQueryUseCase).calculateUserContributionInLab(userId, labId);
        }
    }

    @Nested
    @DisplayName("점수 범위별 랩실 수 조회 테스트")
    class GetLabsCountInScoreRangeTest {

        @Test
        @DisplayName("점수 범위별 랩실 수 조회 성공")
        @WithMockUser(roles = "STUDENT")
        void getLabsCountInScoreRange_Success() throws Exception {
            // given
            int minScore = 1000;
            int maxScore = 2000;
            long count = 5L;

            given(rankingQueryUseCase.countLabsInScoreRange(minScore, maxScore)).willReturn(count);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/count")
                            .param("minScore", String.valueOf(minScore))
                            .param("maxScore", String.valueOf(maxScore)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(rankingQueryUseCase).countLabsInScoreRange(minScore, maxScore);
        }

        @Test
        @DisplayName("기본 점수 범위로 랩실 수 조회")
        @WithMockUser(roles = "STUDENT")
        void getLabsCountInScoreRange_DefaultRange_Success() throws Exception {
            // given
            long count = 10L;

            given(rankingQueryUseCase.countLabsInScoreRange(0, 1000)).willReturn(count);

            // when & then
            mockMvc.perform(get("/api/rankings/labs/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").value(10));

            verify(rankingQueryUseCase).countLabsInScoreRange(0, 1000);
        }
    }

    @Nested
    @DisplayName("인증 없는 접근 테스트")
    class UnauthorizedAccessTest {

        @Test
        @DisplayName("인증 없이 랭킹 조회시 정상 응답 (필터 비활성화)")
        void getLabRankings_Unauthenticated_WithFiltersDisabled() throws Exception {
            // given
            List<RankingQueryUseCase.LabRankingResult> rankings = Arrays.asList(
                    createLabRankingResult(1L, "AI랩", 1500, 1)
            );

            Pageable pageable = PageRequest.of(0, 20);
            Page<RankingQueryUseCase.LabRankingResult> rankingPage = new PageImpl<>(rankings, pageable, rankings.size());

            given(rankingQueryUseCase.getLabRankings(any(Pageable.class))).willReturn(rankingPage);

            // when & then
            mockMvc.perform(get("/api/rankings/labs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }

        @Test
        @DisplayName("인증 없이 내 랩실 랭킹 조회시 500 반환 (인증 정보 없음)")
        void getMyLabRankings_Unauthenticated_InternalServerError() throws Exception {
            // when & then (CustomUserDetails가 null이므로 500 에러 발생)
            mockMvc.perform(get("/api/rankings/my-labs"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // Helper methods for creating test data
    private RankingQueryUseCase.LabRankingResult createLabRankingResult(Long labId, String labName, int totalScore, int rank) {
        return new RankingQueryUseCase.LabRankingResult(labId, labName, totalScore, rank, Arrays.asList(), Arrays.asList());
    }

    private RankingQueryUseCase.UserContribution createUserContribution(Long userId, String userName, int contributionScore) {
        return new RankingQueryUseCase.UserContribution(userId, userName, contributionScore, 5);
    }
}
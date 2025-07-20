package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.command.VoteCommandUseCase;
import org.univ.rankus.application.port.in.query.VoteQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = VoteController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = org.univ.rankus.common.security.jwt.JwtAuthenticationFilter.class)
        },
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = org.univ.rankus.common.exception.GlobalExceptionHandler.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class VoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VoteCommandUseCase commandUseCase;

    @MockitoBean
    private VoteQueryUseCase queryUseCase;

    @MockitoBean
    private org.univ.rankus.common.security.permission.VotePermissionHandler votePermissionHandler;

    private static final Long LAB_ID = 1L;
    private static final Long VOTE_ID = 123L;
    private static final Long USER_ID = 42L;
    private static final Long OPTION_ID = 456L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        authentication.setAuthenticated(true); // 🔑 핵심: 인증 상태 명시
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/votes")
    class GetLabVotesTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 페이징된 투표 목록")
        void getLabVotesSuccess() throws Exception {
            // given
            List<Vote> votes = createMockVotes();
            Page<Vote> votePage = new PageImpl<>(votes, PageRequest.of(0, 20), votes.size());
            given(queryUseCase.findVotesByLabId(eq(LAB_ID), any(Pageable.class)))
                    .willReturn(votePage);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/votes", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("투표 목록 조회 성공"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/votes/all")
    class GetAllLabVotesTests {

        @Test
        @DisplayName("전체 투표 조회 → 200 OK")
        void getAllLabVotesSuccess() throws Exception {
            // given
            List<Vote> votes = createMockVotes();
            given(queryUseCase.findVotesByLabId(LAB_ID)).willReturn(votes);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/votes/all", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("전체 투표 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/votes/status/{status}")
    class GetVotesByStatusTests {

        @Test
        @DisplayName("상태별 투표 조회 → 200 OK")
        void getVotesByStatusSuccess() throws Exception {
            // given
            List<Vote> activeVotes = List.of(createMockVote(VoteStatus.ACTIVE));
            given(queryUseCase.findVotesByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE))
                    .willReturn(activeVotes);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/votes/status/{status}", LAB_ID, "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("상태별 투표 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/votes/active")
    class GetActiveVotesTests {

        @Test
        @DisplayName("활성 투표 조회 → 200 OK")
        void getActiveVotesSuccess() throws Exception {
            // given
            List<Vote> activeVotes = List.of(createMockVote(VoteStatus.ACTIVE));
            given(queryUseCase.findActiveVotesByLabId(LAB_ID)).willReturn(activeVotes);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/votes/active", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("활성 투표 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/votes/{voteId}")
    class GetVoteTests {

        @Test
        @DisplayName("투표 상세 조회 → 200 OK")
        void getVoteSuccess() throws Exception {
            // given
            Vote mockVote = createMockVote(VoteStatus.ACTIVE);
            given(queryUseCase.findVoteById(VOTE_ID)).willReturn(mockVote);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/votes/{voteId}", LAB_ID, VOTE_ID))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("투표 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(VOTE_ID))
                    .andExpect(jsonPath("$.data.title").value("테스트 투표"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("존재하지 않는 투표 조회 → 404 Not Found")
        void getVoteNotFound() throws Exception {
            // given
            given(queryUseCase.findVoteById(VOTE_ID))
                    .willThrow(new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/votes/{voteId}", LAB_ID, VOTE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("VOTE_015"))
                    .andExpect(jsonPath("$.message").value("해당 투표를 찾을 수 없습니다."));
        }
    }


    @Nested
    @DisplayName("PATCH /api/labs/{labId}/votes/{voteId}/close")
    class CloseVoteTests {

        @Test
        @DisplayName("투표 종료 → 200 OK")
        void closeVoteSuccess() throws Exception {
            // given
            Vote closedVote = createMockVote(VoteStatus.CLOSED);
            given(commandUseCase.closeVote(VOTE_ID)).willReturn(closedVote);

            // when & then
            mockMvc.perform(patch("/api/labs/{labId}/votes/{voteId}/close", LAB_ID, VOTE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("투표 종료 성공"))
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/labs/{labId}/votes/{voteId}/cancel")
    class CancelVoteTests {

        @Test
        @DisplayName("투표 취소 → 200 OK")
        void cancelVoteSuccess() throws Exception {
            // given
            Vote canceledVote = createMockVote(VoteStatus.CANCELED);
            given(commandUseCase.cancelVote(VOTE_ID)).willReturn(canceledVote);

            // when & then
            mockMvc.perform(patch("/api/labs/{labId}/votes/{voteId}/cancel", LAB_ID, VOTE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("투표 취소 성공"))
                    .andExpect(jsonPath("$.data.status").value("CANCELED"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/votes/{voteId}")
    class DeleteVoteTests {

        @Test
        @DisplayName("투표 삭제 → 200 OK")
        void deleteVoteSuccess() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/labs/{labId}/votes/{voteId}", LAB_ID, VOTE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("투표 삭제 성공"));
        }

        @Test
        @DisplayName("존재하지 않는 투표 삭제 → 404 Not Found")
        void deleteVoteNotFound() throws Exception {
            // given
            doThrow(new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND))
                    .when(commandUseCase).deleteVote(VOTE_ID);

            // when & then
            mockMvc.perform(delete("/api/labs/{labId}/votes/{voteId}", LAB_ID, VOTE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("VOTE_015"));
        }
    }

    // Helper methods
    private List<Vote> createMockVotes() {
        return Arrays.asList(
                createMockVote(VoteStatus.ACTIVE),
                createMockVote(VoteStatus.CLOSED)
        );
    }

    private Vote createMockVote(VoteStatus status) {
        Vote vote = mock(Vote.class);
        User creator = DomainUserFactory.buildStudentUser();
        Lab lab = DomainLabFactory.buildAiLab();

        given(vote.getId()).willReturn(VOTE_ID);
        given(vote.getTitle()).willReturn("테스트 투표");
        given(vote.getDescription()).willReturn("테스트 투표 설명");
        given(vote.getStatus()).willReturn(status);
        given(vote.getCreator()).willReturn(creator);
        given(vote.getLab()).willReturn(lab);

        return vote;
    }
}
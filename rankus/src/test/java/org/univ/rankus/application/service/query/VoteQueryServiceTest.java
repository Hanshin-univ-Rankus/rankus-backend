package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.application.port.out.VoteOptionRepositoryPort;
import org.univ.rankus.application.port.out.VoteParticipationRepositoryPort;
import org.univ.rankus.application.port.out.VoteRepositoryPort;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteOption;
import org.univ.rankus.domain.model.vote.VoteParticipation;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VoteQueryService 단위 테스트")
class VoteQueryServiceTest {

    @Mock
    private VoteRepositoryPort voteRepositoryPort;

    @Mock
    private VoteOptionRepositoryPort voteOptionRepositoryPort;

    @Mock
    private VoteParticipationRepositoryPort voteParticipationRepositoryPort;

    @InjectMocks
    private VoteQueryService service;

    private static final Long VOTE_ID = 123L;
    private static final Long LAB_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Long CREATOR_ID = 99L;

    @Nested
    @DisplayName("findVoteById 메서드는")
    class FindVoteByIdTests {

        @Test
        @DisplayName("존재하는 투표를 반환한다")
        void findVoteByIdSuccess() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));

            // when
            Vote result = service.findVoteById(VOTE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(vote);
            verify(voteRepositoryPort).findById(VOTE_ID);
        }

        @Test
        @DisplayName("존재하지 않는 투표 조회 시 VoteNotFoundException을 던진다")
        void findVoteByIdNotFound() {
            // given
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findVoteById(VOTE_ID))
                    .isInstanceOf(VoteNotFoundException.class)
                    .satisfies(ex -> {
                        VoteNotFoundException e = (VoteNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
        }
    }

    @Nested
    @DisplayName("findVotesByLabId 메서드는")
    class FindVotesByLabIdTests {

        @Test
        @DisplayName("랩실의 모든 투표를 최신순으로 반환한다")
        void findVotesByLabIdSuccess() {
            // given
            List<Vote> votes = Arrays.asList(
                    DomainVoteFactory.buildValidVote(),
                    DomainVoteFactory.buildValidVote()
            );
            when(voteRepositoryPort.findByLabIdOrderByCreatedAtDesc(LAB_ID)).thenReturn(votes);

            // when
            List<Vote> result = service.findVotesByLabId(LAB_ID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(votes);
            verify(voteRepositoryPort).findByLabIdOrderByCreatedAtDesc(LAB_ID);
        }

        @Test
        @DisplayName("투표가 없는 경우 빈 리스트를 반환한다")
        void findVotesByLabIdEmpty() {
            // given
            when(voteRepositoryPort.findByLabIdOrderByCreatedAtDesc(LAB_ID)).thenReturn(Collections.emptyList());

            // when
            List<Vote> result = service.findVotesByLabId(LAB_ID);

            // then
            assertThat(result).isEmpty();
            verify(voteRepositoryPort).findByLabIdOrderByCreatedAtDesc(LAB_ID);
        }
    }

    @Nested
    @DisplayName("findVotesByLabId with Pageable 메서드는")
    class FindVotesByLabIdPageableTests {

        @Test
        @DisplayName("랩실의 투표를 페이지별로 반환한다")
        void findVotesByLabIdPageableSuccess() {
            // given
            List<Vote> votes = Arrays.asList(DomainVoteFactory.buildValidVote());
            Page<Vote> votePage = new PageImpl<>(votes, PageRequest.of(0, 10), 1);
            Pageable pageable = PageRequest.of(0, 10);
            when(voteRepositoryPort.findByLabIdOrderByCreatedAtDesc(LAB_ID, pageable)).thenReturn(votePage);

            // when
            Page<Vote> result = service.findVotesByLabId(LAB_ID, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(voteRepositoryPort).findByLabIdOrderByCreatedAtDesc(LAB_ID, pageable);
        }
    }

    @Nested
    @DisplayName("findVotesByLabIdAndStatus 메서드는")
    class FindVotesByLabIdAndStatusTests {

        @Test
        @DisplayName("특정 상태의 투표를 반환한다")
        void findVotesByLabIdAndStatusSuccess() {
            // given
            List<Vote> activeVotes = Arrays.asList(DomainVoteFactory.buildActiveVote());
            when(voteRepositoryPort.findByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE)).thenReturn(activeVotes);

            // when
            List<Vote> result = service.findVotesByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE);

            // then
            assertThat(result).hasSize(1);
            verify(voteRepositoryPort).findByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE);
        }

        @Test
        @DisplayName("해당 상태의 투표가 없는 경우 빈 리스트를 반환한다")
        void findVotesByLabIdAndStatusEmpty() {
            // given
            when(voteRepositoryPort.findByLabIdAndStatus(LAB_ID, VoteStatus.CLOSED)).thenReturn(Collections.emptyList());

            // when
            List<Vote> result = service.findVotesByLabIdAndStatus(LAB_ID, VoteStatus.CLOSED);

            // then
            assertThat(result).isEmpty();
            verify(voteRepositoryPort).findByLabIdAndStatus(LAB_ID, VoteStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("findVotesByCreatorId 메서드는")
    class FindVotesByCreatorIdTests {

        @Test
        @DisplayName("특정 사용자가 생성한 투표를 반환한다")
        void findVotesByCreatorIdSuccess() {
            // given
            List<Vote> creatorVotes = Arrays.asList(
                    DomainVoteFactory.buildValidVote(),
                    DomainVoteFactory.buildValidVote()
            );
            when(voteRepositoryPort.findByCreatorId(CREATOR_ID)).thenReturn(creatorVotes);

            // when
            List<Vote> result = service.findVotesByCreatorId(CREATOR_ID);

            // then
            assertThat(result).hasSize(2);
            verify(voteRepositoryPort).findByCreatorId(CREATOR_ID);
        }
    }

    @Nested
    @DisplayName("findActiveVotesByLabId 메서드는")
    class FindActiveVotesByLabIdTests {

        @Test
        @DisplayName("랩실의 활성 투표를 반환한다")
        void findActiveVotesByLabIdSuccess() {
            // given
            List<Vote> activeVotes = Arrays.asList(DomainVoteFactory.buildActiveVote());
            when(voteRepositoryPort.findActiveVotesByLabId(LAB_ID)).thenReturn(activeVotes);

            // when
            List<Vote> result = service.findActiveVotesByLabId(LAB_ID);

            // then
            assertThat(result).hasSize(1);
            verify(voteRepositoryPort).findActiveVotesByLabId(LAB_ID);
        }
    }

    @Nested
    @DisplayName("findVoteOptionsByVoteId 메서드는")
    class FindVoteOptionsByVoteIdTests {

        @Test
        @DisplayName("투표의 선택지를 순서대로 반환한다")
        void findVoteOptionsByVoteIdSuccess() {
            // given
            List<VoteOption> options = Arrays.asList(
                    DomainVoteFactory.buildValidVoteOption(),
                    DomainVoteFactory.buildValidVoteOption()
            );
            when(voteOptionRepositoryPort.findByVoteIdOrderByOptionOrder(VOTE_ID)).thenReturn(options);

            // when
            List<VoteOption> result = service.findVoteOptionsByVoteId(VOTE_ID);

            // then
            assertThat(result).hasSize(2);
            verify(voteOptionRepositoryPort).findByVoteIdOrderByOptionOrder(VOTE_ID);
        }
    }

    @Nested
    @DisplayName("findVoteParticipationsByVoteId 메서드는")
    class FindVoteParticipationsByVoteIdTests {

        @Test
        @DisplayName("투표의 참여 기록을 반환한다")
        void findVoteParticipationsByVoteIdSuccess() {
            // given
            List<VoteParticipation> participations = Arrays.asList(
                    DomainVoteFactory.buildValidVoteParticipation()
            );
            when(voteParticipationRepositoryPort.findByVoteId(VOTE_ID)).thenReturn(participations);

            // when
            List<VoteParticipation> result = service.findVoteParticipationsByVoteId(VOTE_ID);

            // then
            assertThat(result).hasSize(1);
            verify(voteParticipationRepositoryPort).findByVoteId(VOTE_ID);
        }
    }

    @Nested
    @DisplayName("findVoteParticipationByVoteIdAndUserId 메서드는")
    class FindVoteParticipationByVoteIdAndUserIdTests {

        @Test
        @DisplayName("사용자의 특정 투표 참여 기록을 반환한다")
        void findVoteParticipationByVoteIdAndUserIdSuccess() {
            // given
            VoteParticipation participation = DomainVoteFactory.buildValidVoteParticipation();
            when(voteParticipationRepositoryPort.findByVoteIdAndUserId(VOTE_ID, USER_ID))
                    .thenReturn(Optional.of(participation));

            // when
            VoteParticipation result = service.findVoteParticipationByVoteIdAndUserId(VOTE_ID, USER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(participation);
            verify(voteParticipationRepositoryPort).findByVoteIdAndUserId(VOTE_ID, USER_ID);
        }

        @Test
        @DisplayName("참여 기록이 없는 경우 null을 반환한다")
        void findVoteParticipationByVoteIdAndUserIdNotFound() {
            // given
            when(voteParticipationRepositoryPort.findByVoteIdAndUserId(VOTE_ID, USER_ID))
                    .thenReturn(Optional.empty());

            // when
            VoteParticipation result = service.findVoteParticipationByVoteIdAndUserId(VOTE_ID, USER_ID);

            // then
            assertThat(result).isNull();
            verify(voteParticipationRepositoryPort).findByVoteIdAndUserId(VOTE_ID, USER_ID);
        }
    }

    @Nested
    @DisplayName("hasUserParticipated 메서드는")
    class HasUserParticipatedTests {

        @Test
        @DisplayName("사용자가 투표에 참여한 경우 true를 반환한다")
        void hasUserParticipatedTrue() {
            // given
            when(voteParticipationRepositoryPort.existsByVoteIdAndUserId(VOTE_ID, USER_ID)).thenReturn(true);

            // when
            boolean result = service.hasUserParticipated(VOTE_ID, USER_ID);

            // then
            assertThat(result).isTrue();
            verify(voteParticipationRepositoryPort).existsByVoteIdAndUserId(VOTE_ID, USER_ID);
        }

        @Test
        @DisplayName("사용자가 투표에 참여하지 않은 경우 false를 반환한다")
        void hasUserParticipatedFalse() {
            // given
            when(voteParticipationRepositoryPort.existsByVoteIdAndUserId(VOTE_ID, USER_ID)).thenReturn(false);

            // when
            boolean result = service.hasUserParticipated(VOTE_ID, USER_ID);

            // then
            assertThat(result).isFalse();
            verify(voteParticipationRepositoryPort).existsByVoteIdAndUserId(VOTE_ID, USER_ID);
        }
    }

    @Nested
    @DisplayName("countVotesByLabId 메서드는")
    class CountVotesByLabIdTests {

        @Test
        @DisplayName("랩실의 총 투표 수를 반환한다")
        void countVotesByLabIdSuccess() {
            // given
            when(voteRepositoryPort.countByLabId(LAB_ID)).thenReturn(5L);

            // when
            long result = service.countVotesByLabId(LAB_ID);

            // then
            assertThat(result).isEqualTo(5L);
            verify(voteRepositoryPort).countByLabId(LAB_ID);
        }

        @Test
        @DisplayName("투표가 없는 경우 0을 반환한다")
        void countVotesByLabIdZero() {
            // given
            when(voteRepositoryPort.countByLabId(LAB_ID)).thenReturn(0L);

            // when
            long result = service.countVotesByLabId(LAB_ID);

            // then
            assertThat(result).isEqualTo(0L);
            verify(voteRepositoryPort).countByLabId(LAB_ID);
        }
    }

    @Nested
    @DisplayName("countVotesByLabIdAndStatus 메서드는")
    class CountVotesByLabIdAndStatusTests {

        @Test
        @DisplayName("특정 상태의 투표 수를 반환한다")
        void countVotesByLabIdAndStatusSuccess() {
            // given
            when(voteRepositoryPort.countByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE)).thenReturn(3L);

            // when
            long result = service.countVotesByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE);

            // then
            assertThat(result).isEqualTo(3L);
            verify(voteRepositoryPort).countByLabIdAndStatus(LAB_ID, VoteStatus.ACTIVE);
        }

        @Test
        @DisplayName("해당 상태의 투표가 없는 경우 0을 반환한다")
        void countVotesByLabIdAndStatusZero() {
            // given
            when(voteRepositoryPort.countByLabIdAndStatus(LAB_ID, VoteStatus.CANCELED)).thenReturn(0L);

            // when
            long result = service.countVotesByLabIdAndStatus(LAB_ID, VoteStatus.CANCELED);

            // then
            assertThat(result).isEqualTo(0L);
            verify(voteRepositoryPort).countByLabIdAndStatus(LAB_ID, VoteStatus.CANCELED);
        }
    }
}
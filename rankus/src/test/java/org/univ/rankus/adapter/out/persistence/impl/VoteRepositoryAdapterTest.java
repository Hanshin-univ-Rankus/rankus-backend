package org.univ.rankus.adapter.out.persistence.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataVoteRepository;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteRepositoryAdapter 테스트")
class VoteRepositoryAdapterTest {

    @Mock
    private SpringDataVoteRepository springDataVoteRepository;

    @InjectMocks
    private VoteRepositoryAdapter voteRepositoryAdapter;

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTests {

        @Test
        @DisplayName("save() - SpringDataRepository로 정상 위임")
        void save_DelegatesToSpringDataRepository() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            Vote savedVote = DomainVoteFactory.buildValidVote();
            given(springDataVoteRepository.save(vote)).willReturn(savedVote);

            // when
            Vote result = voteRepositoryAdapter.save(vote);

            // then
            assertThat(result).isEqualTo(savedVote);
            verify(springDataVoteRepository).save(vote);
        }

        @Test
        @DisplayName("findById() - SpringDataRepository로 정상 위임")
        void findById_DelegatesToSpringDataRepository() {
            // given
            Long voteId = 1L;
            Vote vote = DomainVoteFactory.buildValidVote();
            given(springDataVoteRepository.findById(voteId))
                    .willReturn(Optional.of(vote));

            // when
            Optional<Vote> result = voteRepositoryAdapter.findById(voteId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(vote);
            verify(springDataVoteRepository).findById(voteId);
        }

        @Test
        @DisplayName("findById() - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void findById_NonExistentId_ReturnsEmptyOptional() {
            // given
            Long nonExistentId = 999L;
            given(springDataVoteRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            // when
            Optional<Vote> result = voteRepositoryAdapter.findById(nonExistentId);

            // then
            assertThat(result).isEmpty();
            verify(springDataVoteRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("deleteById() - SpringDataRepository로 정상 위임")
        void deleteById_DelegatesToSpringDataRepository() {
            // given
            Long voteId = 1L;

            // when
            voteRepositoryAdapter.deleteById(voteId);

            // then
            verify(springDataVoteRepository).deleteById(voteId);
        }

        @Test
        @DisplayName("existsById() - SpringDataRepository로 정상 위임")
        void existsById_DelegatesToSpringDataRepository() {
            // given
            Long voteId = 1L;
            given(springDataVoteRepository.existsById(voteId)).willReturn(true);

            // when
            boolean result = voteRepositoryAdapter.existsById(voteId);

            // then
            assertThat(result).isTrue();
            verify(springDataVoteRepository).existsById(voteId);
        }
    }

    @Nested
    @DisplayName("랩실별 조회 테스트")
    class LabQueryTests {

        @Test
        @DisplayName("findByLabId() - 랩실 ID로 조회")
        void findByLabId_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            List<Vote> votes = Arrays.asList(
                    DomainVoteFactory.buildValidVote(),
                    DomainVoteFactory.buildValidVote()
            );
            given(springDataVoteRepository.findByLabId(labId)).willReturn(votes);

            // when
            List<Vote> result = voteRepositoryAdapter.findByLabId(labId);

            // then
            assertThat(result).isEqualTo(votes);
            assertThat(result).hasSize(2);
            verify(springDataVoteRepository).findByLabId(labId);
        }

        @Test
        @DisplayName("findByLabIdOrderByCreatedAtDesc() - 랩실 ID로 최신순 조회")
        void findByLabIdOrderByCreatedAtDesc_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            List<Vote> votes = Arrays.asList(
                    DomainVoteFactory.buildValidVote(),
                    DomainVoteFactory.buildValidVote()
            );
            given(springDataVoteRepository.findByLabIdOrderByCreatedAtDesc(labId)).willReturn(votes);

            // when
            List<Vote> result = voteRepositoryAdapter.findByLabIdOrderByCreatedAtDesc(labId);

            // then
            assertThat(result).isEqualTo(votes);
            verify(springDataVoteRepository).findByLabIdOrderByCreatedAtDesc(labId);
        }

        @Test
        @DisplayName("findByLabId() with Pageable - 랩실 ID로 페이징 조회")
        void findByLabId_WithPageable_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<Vote> votes = Arrays.asList(DomainVoteFactory.buildValidVote());
            Page<Vote> votePage = new PageImpl<>(votes, pageable, votes.size());
            given(springDataVoteRepository.findByLabIdOrderByCreatedAtDesc(labId, pageable))
                    .willReturn(votePage);

            // when
            Page<Vote> result = voteRepositoryAdapter.findByLabId(labId, pageable);

            // then
            assertThat(result).isEqualTo(votePage);
            verify(springDataVoteRepository).findByLabIdOrderByCreatedAtDesc(labId, pageable);
        }

        @Test
        @DisplayName("findByLabIdOrderByCreatedAtDesc() with Pageable - 랩실 ID로 최신순 페이징 조회")
        void findByLabIdOrderByCreatedAtDesc_WithPageable_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<Vote> votes = Arrays.asList(DomainVoteFactory.buildValidVote());
            Page<Vote> votePage = new PageImpl<>(votes, pageable, votes.size());
            given(springDataVoteRepository.findByLabIdOrderByCreatedAtDesc(labId, pageable))
                    .willReturn(votePage);

            // when
            Page<Vote> result = voteRepositoryAdapter.findByLabIdOrderByCreatedAtDesc(labId, pageable);

            // then
            assertThat(result).isEqualTo(votePage);
            verify(springDataVoteRepository).findByLabIdOrderByCreatedAtDesc(labId, pageable);
        }

        @Test
        @DisplayName("findActiveVotesByLabId() - 랩실의 활성 투표 조회")
        void findActiveVotesByLabId_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            List<Vote> activeVotes = Arrays.asList(DomainVoteFactory.buildActiveVote());
            given(springDataVoteRepository.findActiveVotesByLabId(labId)).willReturn(activeVotes);

            // when
            List<Vote> result = voteRepositoryAdapter.findActiveVotesByLabId(labId);

            // then
            assertThat(result).isEqualTo(activeVotes);
            verify(springDataVoteRepository).findActiveVotesByLabId(labId);
        }

        @Test
        @DisplayName("countByLabId() - 랩실의 투표 수 조회")
        void countByLabId_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            long expectedCount = 5L;
            given(springDataVoteRepository.countByLabId(labId)).willReturn(expectedCount);

            // when
            long result = voteRepositoryAdapter.countByLabId(labId);

            // then
            assertThat(result).isEqualTo(expectedCount);
            verify(springDataVoteRepository).countByLabId(labId);
        }
    }

    @Nested
    @DisplayName("상태별 조회 테스트")
    class StatusQueryTests {

        @Test
        @DisplayName("findByLabIdAndStatus() - 랩실 ID와 상태로 조회")
        void findByLabIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            VoteStatus status = VoteStatus.ACTIVE;
            List<Vote> votes = Arrays.asList(DomainVoteFactory.buildActiveVote());
            given(springDataVoteRepository.findByLabIdAndStatus(labId, status)).willReturn(votes);

            // when
            List<Vote> result = voteRepositoryAdapter.findByLabIdAndStatus(labId, status);

            // then
            assertThat(result).isEqualTo(votes);
            verify(springDataVoteRepository).findByLabIdAndStatus(labId, status);
        }

        @Test
        @DisplayName("findByLabIdAndStatus() with Pageable - 랩실 ID와 상태로 페이징 조회")
        void findByLabIdAndStatus_WithPageable_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            VoteStatus status = VoteStatus.CLOSED;
            Pageable pageable = PageRequest.of(0, 10);
            List<Vote> votes = Arrays.asList(DomainVoteFactory.buildClosedVote());
            Page<Vote> votePage = new PageImpl<>(votes, pageable, votes.size());
            given(springDataVoteRepository.findByLabIdAndStatusOrderByCreatedAtDesc(labId, status, pageable))
                    .willReturn(votePage);

            // when
            Page<Vote> result = voteRepositoryAdapter.findByLabIdAndStatus(labId, status, pageable);

            // then
            assertThat(result).isEqualTo(votePage);
            verify(springDataVoteRepository).findByLabIdAndStatusOrderByCreatedAtDesc(labId, status, pageable);
        }

        @Test
        @DisplayName("countByLabIdAndStatus() - 랩실 ID와 상태별 투표 수 조회")
        void countByLabIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            VoteStatus status = VoteStatus.ACTIVE;
            long expectedCount = 3L;
            given(springDataVoteRepository.countByLabIdAndStatus(labId, status)).willReturn(expectedCount);

            // when
            long result = voteRepositoryAdapter.countByLabIdAndStatus(labId, status);

            // then
            assertThat(result).isEqualTo(expectedCount);
            verify(springDataVoteRepository).countByLabIdAndStatus(labId, status);
        }
    }

    @Nested
    @DisplayName("생성자별 조회 테스트")
    class CreatorQueryTests {

        @Test
        @DisplayName("findByCreatorId() - 생성자 ID로 조회")
        void findByCreatorId_DelegatesToSpringDataRepository() {
            // given
            Long creatorId = 1L;
            List<Vote> votes = Arrays.asList(
                    DomainVoteFactory.buildValidVote(),
                    DomainVoteFactory.buildValidVote()
            );
            given(springDataVoteRepository.findByCreatorId(creatorId)).willReturn(votes);

            // when
            List<Vote> result = voteRepositoryAdapter.findByCreatorId(creatorId);

            // then
            assertThat(result).isEqualTo(votes);
            verify(springDataVoteRepository).findByCreatorId(creatorId);
        }

        @Test
        @DisplayName("countByCreatorId() - 생성자별 투표 수 조회")
        void countByCreatorId_DelegatesToSpringDataRepository() {
            // given
            Long creatorId = 1L;
            long expectedCount = 7L;
            given(springDataVoteRepository.countByCreatorId(creatorId)).willReturn(expectedCount);

            // when
            long result = voteRepositoryAdapter.countByCreatorId(creatorId);

            // then
            assertThat(result).isEqualTo(expectedCount);
            verify(springDataVoteRepository).countByCreatorId(creatorId);
        }
    }

    @Nested
    @DisplayName("메서드 파라미터 전달 테스트")
    class ParameterPassingTests {

        @Test
        @DisplayName("save() - 파라미터 정확히 전달")
        void save_PassesParametersCorrectly() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            Vote savedVote = DomainVoteFactory.buildValidVote();
            given(springDataVoteRepository.save(any(Vote.class))).willReturn(savedVote);

            // when
            voteRepositoryAdapter.save(vote);

            // then
            ArgumentCaptor<Vote> captor = ArgumentCaptor.forClass(Vote.class);
            verify(springDataVoteRepository).save(captor.capture());
            assertThat(captor.getValue()).isEqualTo(vote);
        }

        @Test
        @DisplayName("findByLabIdAndStatus() with Pageable - 파라미터 정확히 전달")
        void findByLabIdAndStatus_WithPageable_PassesParametersCorrectly() {
            // given
            Long labId = 1L;
            VoteStatus status = VoteStatus.ACTIVE;
            Pageable pageable = PageRequest.of(0, 10);
            given(springDataVoteRepository.findByLabIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of()));

            // when
            voteRepositoryAdapter.findByLabIdAndStatus(labId, status, pageable);

            // then
            ArgumentCaptor<Long> labIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<VoteStatus> statusCaptor = ArgumentCaptor.forClass(VoteStatus.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(springDataVoteRepository).findByLabIdAndStatusOrderByCreatedAtDesc(
                    labIdCaptor.capture(), statusCaptor.capture(), pageableCaptor.capture()
            );

            assertThat(labIdCaptor.getValue()).isEqualTo(labId);
            assertThat(statusCaptor.getValue()).isEqualTo(status);
            assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        }

        @Test
        @DisplayName("countByLabIdAndStatus() - 파라미터 정확히 전달")
        void countByLabIdAndStatus_PassesParametersCorrectly() {
            // given
            Long labId = 1L;
            VoteStatus status = VoteStatus.CLOSED;
            given(springDataVoteRepository.countByLabIdAndStatus(any(), any())).willReturn(0L);

            // when
            voteRepositoryAdapter.countByLabIdAndStatus(labId, status);

            // then
            ArgumentCaptor<Long> labIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<VoteStatus> statusCaptor = ArgumentCaptor.forClass(VoteStatus.class);
            verify(springDataVoteRepository).countByLabIdAndStatus(
                    labIdCaptor.capture(), statusCaptor.capture()
            );

            assertThat(labIdCaptor.getValue()).isEqualTo(labId);
            assertThat(statusCaptor.getValue()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("반환값 처리 테스트")
    class ReturnValueHandlingTests {

        @Test
        @DisplayName("findByLabId() - 빈 리스트 반환 처리")
        void findByLabId_HandlesEmptyList() {
            // given
            Long labId = 1L;
            given(springDataVoteRepository.findByLabId(labId)).willReturn(List.of());

            // when
            List<Vote> result = voteRepositoryAdapter.findByLabId(labId);

            // then
            assertThat(result).isEmpty();
            verify(springDataVoteRepository).findByLabId(labId);
        }

        @Test
        @DisplayName("countByLabId() - 0 반환 처리")
        void countByLabId_HandlesZeroCount() {
            // given
            Long labId = 1L;
            given(springDataVoteRepository.countByLabId(labId)).willReturn(0L);

            // when
            long result = voteRepositoryAdapter.countByLabId(labId);

            // then
            assertThat(result).isEqualTo(0L);
            verify(springDataVoteRepository).countByLabId(labId);
        }

        @Test
        @DisplayName("existsById() - false 반환 처리")
        void existsById_HandlesFalseReturn() {
            // given
            Long voteId = 1L;
            given(springDataVoteRepository.existsById(voteId)).willReturn(false);

            // when
            boolean result = voteRepositoryAdapter.existsById(voteId);

            // then
            assertThat(result).isFalse();
            verify(springDataVoteRepository).existsById(voteId);
        }
    }
}
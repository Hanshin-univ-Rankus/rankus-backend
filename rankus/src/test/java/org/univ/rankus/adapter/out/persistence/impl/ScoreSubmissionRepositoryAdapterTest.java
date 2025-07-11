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
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataScoreSubmissionRepository;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreSubmissionRepositoryAdapter 테스트")
class ScoreSubmissionRepositoryAdapterTest {

    @Mock
    private SpringDataScoreSubmissionRepository springDataScoreSubmissionRepository;

    @InjectMocks
    private ScoreSubmissionRepositoryAdapter scoreSubmissionRepositoryAdapter;

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTests {

        @Test
        @DisplayName("save() - SpringDataRepository로 정상 위임")
        void save_DelegatesToSpringDataRepository() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
            ScoreSubmission savedSubmission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L);
            given(springDataScoreSubmissionRepository.save(submission)).willReturn(savedSubmission);

            // when
            ScoreSubmission result = scoreSubmissionRepositoryAdapter.save(submission);

            // then
            assertThat(result).isEqualTo(savedSubmission);
            verify(springDataScoreSubmissionRepository).save(submission);
        }

        @Test
        @DisplayName("findById() - SpringDataRepository로 정상 위임")
        void findById_DelegatesToSpringDataRepository() {
            // given
            Long submissionId = 1L;
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
            given(springDataScoreSubmissionRepository.findById(submissionId))
                    .willReturn(Optional.of(submission));

            // when
            Optional<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findById(submissionId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(submission);
            verify(springDataScoreSubmissionRepository).findById(submissionId);
        }

        @Test
        @DisplayName("findById() - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void findById_NonExistentId_ReturnsEmptyOptional() {
            // given
            Long nonExistentId = 999L;
            given(springDataScoreSubmissionRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            // when
            Optional<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findById(nonExistentId);

            // then
            assertThat(result).isEmpty();
            verify(springDataScoreSubmissionRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("delete() - SpringDataRepository로 정상 위임")
        void delete_DelegatesToSpringDataRepository() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L);

            // when
            scoreSubmissionRepositoryAdapter.delete(submission);

            // then
            verify(springDataScoreSubmissionRepository).delete(submission);
        }
    }

    @Nested
    @DisplayName("사용자별 조회 테스트")
    class UserQueryTests {

        @Test
        @DisplayName("findByUserId() - 사용자 ID로 페이징 조회")
        void findByUserId_WithPagination_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L),
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(2L)
            );
            Page<ScoreSubmission> submissionPage = new PageImpl<>(submissions, pageable, submissions.size());
            given(springDataScoreSubmissionRepository.findByUserIdOrderBySubmittedAtDesc(userId, pageable))
                    .willReturn(submissionPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByUserId(userId, pageable);

            // then
            assertThat(result).isEqualTo(submissionPage);
            assertThat(result.getContent()).hasSize(2);
            verify(springDataScoreSubmissionRepository).findByUserIdOrderBySubmittedAtDesc(userId, pageable);
        }

        @Test
        @DisplayName("findByUserIdAndStatus() - 사용자 ID와 상태로 조회")
        void findByUserIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );
            given(springDataScoreSubmissionRepository.findByUserIdAndStatusOrderBySubmittedAtDesc(userId, status))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByUserIdAndStatus(userId, status);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByUserIdAndStatusOrderBySubmittedAtDesc(userId, status);
        }
    }

    @Nested
    @DisplayName("랩실별 조회 테스트")
    class LabQueryTests {

        @Test
        @DisplayName("findByLabId() - 랩실 ID로 페이징 조회")
        void findByLabId_WithPagination_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> submissionPage = new PageImpl<>(submissions, pageable, submissions.size());
            given(springDataScoreSubmissionRepository.findByLabIdOrderBySubmittedAtDesc(labId, pageable))
                    .willReturn(submissionPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByLabId(labId, pageable);

            // then
            assertThat(result).isEqualTo(submissionPage);
            verify(springDataScoreSubmissionRepository).findByLabIdOrderBySubmittedAtDesc(labId, pageable);
        }

        @Test
        @DisplayName("findByLabIdAndStatus() - 랩실 ID와 상태로 페이징 조회")
        void findByLabIdAndStatus_WithPagination_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.PENDING;
            Pageable pageable = PageRequest.of(0, 10);
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> submissionPage = new PageImpl<>(submissions, pageable, submissions.size());
            given(springDataScoreSubmissionRepository.findByLabIdAndStatusOrderBySubmittedAtDesc(labId, status, pageable))
                    .willReturn(submissionPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByLabIdAndStatus(labId, status, pageable);

            // then
            assertThat(result).isEqualTo(submissionPage);
            verify(springDataScoreSubmissionRepository).findByLabIdAndStatusOrderBySubmittedAtDesc(labId, status, pageable);
        }

        @Test
        @DisplayName("findByLabIdAndStatus() - 랩실 ID와 상태로 목록 조회")
        void findByLabIdAndStatus_AsList_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );
            given(springDataScoreSubmissionRepository.findByLabIdAndStatusOrderBySubmittedAtDesc(labId, status))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByLabIdAndStatus(labId, status);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByLabIdAndStatusOrderBySubmittedAtDesc(labId, status);
        }
    }

    @Nested
    @DisplayName("중복 검사 테스트")
    class DuplicateCheckTests {

        @Test
        @DisplayName("findByUserIdAndAchievementDate() - 사용자 ID와 취득일자로 조회")
        void findByUserIdAndAchievementDate_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            given(springDataScoreSubmissionRepository.findByUserIdAndAchievementDateOrderBySubmittedAtDesc(userId, achievementDate))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByUserIdAndAchievementDate(userId, achievementDate);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByUserIdAndAchievementDateOrderBySubmittedAtDesc(userId, achievementDate);
        }

        @Test
        @DisplayName("findByUserIdAndAchievementDateAndCategory() - 사용자 ID, 취득일자, 카테고리로 조회")
        void findByUserIdAndAchievementDateAndCategory_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            ScoreCategory category = ScoreCategory.RESEARCH_SCI_PAPER;
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            given(springDataScoreSubmissionRepository.findByUserIdAndAchievementDateAndCategoryOrderBySubmittedAtDesc(userId, achievementDate, category))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByUserIdAndAchievementDateAndCategory(userId, achievementDate, category);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByUserIdAndAchievementDateAndCategoryOrderBySubmittedAtDesc(userId, achievementDate, category);
        }

        @Test
        @DisplayName("existsByUserIdAndLabIdAndAchievementDateAndCategory() - 존재 여부 검사")
        void existsByUserIdAndLabIdAndAchievementDateAndCategory_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            Long labId = 1L;
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            ScoreCategory category = ScoreCategory.RESEARCH_SCI_PAPER;
            given(springDataScoreSubmissionRepository.existsByUserIdAndLabIdAndAchievementDateAndCategory(userId, labId, achievementDate, category))
                    .willReturn(true);

            // when
            boolean result = scoreSubmissionRepositoryAdapter.existsByUserIdAndLabIdAndAchievementDateAndCategory(userId, labId, achievementDate, category);

            // then
            assertThat(result).isTrue();
            verify(springDataScoreSubmissionRepository).existsByUserIdAndLabIdAndAchievementDateAndCategory(userId, labId, achievementDate, category);
        }
    }

    @Nested
    @DisplayName("만료 처리 테스트")
    class ExpirationTests {

        @Test
        @DisplayName("findByStatusAndExpiresAtBefore() - 만료 시간 이전 조회")
        void findByStatusAndExpiresAtBefore_DelegatesToSpringDataRepository() {
            // given
            SubmissionStatus status = SubmissionStatus.PENDING;
            LocalDateTime expiresAt = LocalDateTime.now();
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildExpiredSubmission()
            );
            given(springDataScoreSubmissionRepository.findByStatusAndExpiresAtBeforeOrderBySubmittedAtDesc(status, expiresAt))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByStatusAndExpiresAtBefore(status, expiresAt);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByStatusAndExpiresAtBeforeOrderBySubmittedAtDesc(status, expiresAt);
        }

        @Test
        @DisplayName("findByStatusAndExpiresAtBetween() - 만료 시간 범위 조회")
        void findByStatusAndExpiresAtBetween_DelegatesToSpringDataRepository() {
            // given
            SubmissionStatus status = SubmissionStatus.PENDING;
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildExpiredSubmission()
            );
            given(springDataScoreSubmissionRepository.findByStatusAndExpiresAtBetweenOrderBySubmittedAtDesc(status, from, to))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByStatusAndExpiresAtBetween(status, from, to);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByStatusAndExpiresAtBetweenOrderBySubmittedAtDesc(status, from, to);
        }
    }

    @Nested
    @DisplayName("점수 집계 테스트")
    class ScoreAggregationTests {

        @Test
        @DisplayName("sumScoresByLabIdAndStatus() - 랩실 ID와 상태별 점수 합계")
        void sumScoresByLabIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            Integer expectedSum = 150;
            given(springDataScoreSubmissionRepository.sumScoresByLabIdAndStatus(labId, status))
                    .willReturn(expectedSum);

            // when
            Integer result = scoreSubmissionRepositoryAdapter.sumScoresByLabIdAndStatus(labId, status);

            // then
            assertThat(result).isEqualTo(expectedSum);
            verify(springDataScoreSubmissionRepository).sumScoresByLabIdAndStatus(labId, status);
        }

        @Test
        @DisplayName("sumScoresByUserIdAndLabIdAndStatus() - 사용자 ID, 랩실 ID, 상태별 점수 합계")
        void sumScoresByUserIdAndLabIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            Integer expectedSum = 100;
            given(springDataScoreSubmissionRepository.sumScoresByUserIdAndLabIdAndStatus(userId, labId, status))
                    .willReturn(expectedSum);

            // when
            Integer result = scoreSubmissionRepositoryAdapter.sumScoresByUserIdAndLabIdAndStatus(userId, labId, status);

            // then
            assertThat(result).isEqualTo(expectedSum);
            verify(springDataScoreSubmissionRepository).sumScoresByUserIdAndLabIdAndStatus(userId, labId, status);
        }

        @Test
        @DisplayName("sumScoresByUserIdAndStatus() - 사용자 ID와 상태별 점수 합계")
        void sumScoresByUserIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            Integer expectedSum = 200;
            given(springDataScoreSubmissionRepository.sumScoresByUserIdAndStatus(userId, status))
                    .willReturn(expectedSum);

            // when
            Integer result = scoreSubmissionRepositoryAdapter.sumScoresByUserIdAndStatus(userId, status);

            // then
            assertThat(result).isEqualTo(expectedSum);
            verify(springDataScoreSubmissionRepository).sumScoresByUserIdAndStatus(userId, status);
        }

        @Test
        @DisplayName("sumScoresByLabIdAndStatus() - NULL 값 처리")
        void sumScoresByLabIdAndStatus_HandlesNullValue() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            given(springDataScoreSubmissionRepository.sumScoresByLabIdAndStatus(labId, status))
                    .willReturn(null);

            // when
            Integer result = scoreSubmissionRepositoryAdapter.sumScoresByLabIdAndStatus(labId, status);

            // then
            assertThat(result).isNull();
            verify(springDataScoreSubmissionRepository).sumScoresByLabIdAndStatus(labId, status);
        }
    }

    @Nested
    @DisplayName("상태별 조회 테스트")
    class StatusQueryTests {

        @Test
        @DisplayName("findByStatus() - 상태로 페이징 조회")
        void findByStatus_WithPagination_DelegatesToSpringDataRepository() {
            // given
            SubmissionStatus status = SubmissionStatus.PENDING;
            Pageable pageable = PageRequest.of(0, 10);
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> submissionPage = new PageImpl<>(submissions, pageable, submissions.size());
            given(springDataScoreSubmissionRepository.findByStatusOrderBySubmittedAtDesc(status, pageable))
                    .willReturn(submissionPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByStatus(status, pageable);

            // then
            assertThat(result).isEqualTo(submissionPage);
            verify(springDataScoreSubmissionRepository).findByStatusOrderBySubmittedAtDesc(status, pageable);
        }

        @Test
        @DisplayName("findByUserIdAndLabIdAndStatus() - 사용자 ID, 랩실 ID, 상태로 조회")
        void findByUserIdAndLabIdAndStatus_DelegatesToSpringDataRepository() {
            // given
            Long userId = 1L;
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );
            given(springDataScoreSubmissionRepository.findByUserIdAndLabIdAndStatusOrderBySubmittedAtDesc(userId, labId, status))
                    .willReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionRepositoryAdapter.findByUserIdAndLabIdAndStatus(userId, labId, status);

            // then
            assertThat(result).isEqualTo(submissions);
            verify(springDataScoreSubmissionRepository).findByUserIdAndLabIdAndStatusOrderBySubmittedAtDesc(userId, labId, status);
        }
    }

    @Nested
    @DisplayName("메서드 파라미터 전달 테스트")
    class ParameterPassingTests {

        @Test
        @DisplayName("save() - 파라미터 정확히 전달")
        void save_PassesParametersCorrectly() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
            ScoreSubmission savedSubmission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L);
            given(springDataScoreSubmissionRepository.save(any(ScoreSubmission.class))).willReturn(savedSubmission);

            // when
            scoreSubmissionRepositoryAdapter.save(submission);

            // then
            ArgumentCaptor<ScoreSubmission> captor = ArgumentCaptor.forClass(ScoreSubmission.class);
            verify(springDataScoreSubmissionRepository).save(captor.capture());
            assertThat(captor.getValue()).isEqualTo(submission);
        }

        @Test
        @DisplayName("findByLabIdAndStatus() - 파라미터 정확히 전달")
        void findByLabIdAndStatus_PassesParametersCorrectly() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            Pageable pageable = PageRequest.of(0, 10);
            given(springDataScoreSubmissionRepository.findByLabIdAndStatusOrderBySubmittedAtDesc(any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of()));

            // when
            scoreSubmissionRepositoryAdapter.findByLabIdAndStatus(labId, status, pageable);

            // then
            ArgumentCaptor<Long> labIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<SubmissionStatus> statusCaptor = ArgumentCaptor.forClass(SubmissionStatus.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(springDataScoreSubmissionRepository).findByLabIdAndStatusOrderBySubmittedAtDesc(
                    labIdCaptor.capture(), statusCaptor.capture(), pageableCaptor.capture()
            );

            assertThat(labIdCaptor.getValue()).isEqualTo(labId);
            assertThat(statusCaptor.getValue()).isEqualTo(status);
            assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        }
    }
}
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase.LabRankingResult;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase.UserContribution;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.policy.RankingPolicy;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RankingQueryService 테스트")
class RankingQueryServiceTest {

    @Mock
    private ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private RankingPolicy rankingPolicy;

    @InjectMocks
    private RankingQueryService rankingQueryService;

    @Nested
    @DisplayName("랩실 랭킹 조회 시")
    class GetLabRankingsTests {

        @Test
        @DisplayName("모든 랩실의 랭킹 정보를 페이지네이션으로 조회할 수 있다")
        void getLabRankings_ValidPageable_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
            Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
            List<Lab> labs = Arrays.asList(lab1, lab2);

            List<ScoreSubmission> lab1Submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );
            List<ScoreSubmission> lab2Submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );

            List<Integer> sortedScores = Arrays.asList(150, 100);

            when(labRepositoryPort.findAll()).thenReturn(labs);
            when(labRepositoryPort.findById(1L)).thenReturn(Optional.of(lab1)); // 추가된 Mock
            when(labRepositoryPort.findById(2L)).thenReturn(Optional.of(lab2)); // 추가된 Mock
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(1L, SubmissionStatus.APPROVED))
                    .thenReturn(lab1Submissions);
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(2L, SubmissionStatus.APPROVED))
                    .thenReturn(lab2Submissions);
            when(rankingPolicy.calculateLabTotalScore(lab1Submissions)).thenReturn(150);
            when(rankingPolicy.calculateLabTotalScore(lab2Submissions)).thenReturn(100);
            when(rankingPolicy.calculateRankWithTies(150, sortedScores)).thenReturn(1);
            when(rankingPolicy.calculateRankWithTies(100, sortedScores)).thenReturn(2);

            // when
            Page<LabRankingResult> result = rankingQueryService.getLabRankings(pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            LabRankingResult firstRanking = result.getContent().get(0);
            assertThat(firstRanking.getLabId()).isEqualTo(1L);
            assertThat(firstRanking.getTotalScore()).isEqualTo(150);
            assertThat(firstRanking.getRank()).isEqualTo(1);

            LabRankingResult secondRanking = result.getContent().get(1);
            assertThat(secondRanking.getLabId()).isEqualTo(2L);
            assertThat(secondRanking.getTotalScore()).isEqualTo(100);
            assertThat(secondRanking.getRank()).isEqualTo(2);

            verify(labRepositoryPort).findAll();
            verify(rankingPolicy, times(2)).calculateLabTotalScore(any());
            verify(rankingPolicy, times(2)).calculateRankWithTies(anyInt(), any());
        }

        @Test
        @DisplayName("랩실이 없을 때 빈 페이지를 반환한다")
        void getLabRankings_NoLabs_ReturnsEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(labRepositoryPort.findAll()).thenReturn(List.of());

            // when
            Page<LabRankingResult> result = rankingQueryService.getLabRankings(pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(labRepositoryPort).findAll();
        }
    }

    @Nested
    @DisplayName("특정 랩실 랭킹 조회 시")
    class GetLabRankingTests {

        @Test
        @DisplayName("유효한 랩실 ID로 랭킹 정보를 조회할 수 있다")
        void getLabRanking_ValidLabId_Success() {
            // given
            Long labId = 1L;
            Lab lab = DomainLabFactory.buildValidLabWithId(labId);

            List<ScoreSubmission> approvedSubmissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );

            Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
            List<Lab> allLabs = Arrays.asList(lab, lab2);

            List<Integer> allScores = Arrays.asList(150, 100);

            when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
            when(labRepositoryPort.findById(2L)).thenReturn(Optional.of(lab2)); // 추가된 Mock
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.APPROVED))
                    .thenReturn(approvedSubmissions);
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(2L, SubmissionStatus.APPROVED))
                    .thenReturn(List.of()); // 추가된 Mock
            when(rankingPolicy.calculateLabTotalScore(approvedSubmissions)).thenReturn(150);
            when(rankingPolicy.calculateLabTotalScore(List.of())).thenReturn(100); // 추가된 Mock
            when(labRepositoryPort.findAll()).thenReturn(allLabs);
            when(rankingPolicy.calculateRankWithTies(150, allScores)).thenReturn(1);

            // when
            LabRankingResult result = rankingQueryService.getLabRanking(labId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getLabId()).isEqualTo(labId);
            assertThat(result.getLabName()).isEqualTo(lab.getName());
            assertThat(result.getTotalScore()).isEqualTo(150);
            assertThat(result.getRank()).isEqualTo(1);

            verify(labRepositoryPort, atLeast(1)).findById(labId); // getLabRanking + getTopContributors에서 여러 번 호출
            verify(scoreSubmissionRepositoryPort, atLeast(1)).findByLabIdAndStatus(labId, SubmissionStatus.APPROVED);
            verify(rankingPolicy, atLeast(1)).calculateLabTotalScore(approvedSubmissions);
            verify(rankingPolicy).calculateRankWithTies(150, allScores);
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 조회 시 예외가 발생한다")
        void getLabRanking_LabNotFound_ThrowsException() {
            // given
            Long labId = 999L;
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rankingQueryService.getLabRanking(labId))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException exception = (LabNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort, never()).findByLabIdAndStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("상위 기여자 조회 시")
    class GetTopContributorsTests {

        @Test
        @DisplayName("랩실의 상위 기여자 목록을 조회할 수 있다")
        void getTopContributors_ValidLabId_Success() {
            // given
            Long labId = 1L;
            int limit = 5;

            Lab lab = DomainLabFactory.buildValidLabWithId(labId);
            User user1 = DomainUserFactory.buildValidUserWithId(1L);
            User user2 = DomainUserFactory.buildValidUserWithId(2L);

            List<ScoreSubmission> approvedSubmissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildSubmissionWithUserAndScore(user1, 100),
                    DomainScoreSubmissionFactory.buildSubmissionWithUserAndScore(user1, 50),
                    DomainScoreSubmissionFactory.buildSubmissionWithUserAndScore(user2, 35) // 35점으로 수정
            );

            when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.APPROVED))
                    .thenReturn(approvedSubmissions);

            // when
            List<UserContribution> result = rankingQueryService.getTopContributors(labId, limit);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);

            UserContribution firstContributor = result.get(0);
            assertThat(firstContributor.getUserId()).isEqualTo(1L);
            assertThat(firstContributor.getContributionScore()).isEqualTo(150);
            assertThat(firstContributor.getApprovedSubmissionCount()).isEqualTo(2);

            UserContribution secondContributor = result.get(1);
            assertThat(secondContributor.getUserId()).isEqualTo(2L);
            assertThat(secondContributor.getContributionScore()).isEqualTo(35); // 35점으로 수정
            assertThat(secondContributor.getApprovedSubmissionCount()).isEqualTo(1);

            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort).findByLabIdAndStatus(labId, SubmissionStatus.APPROVED);
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 조회 시 예외가 발생한다")
        void getTopContributors_LabNotFound_ThrowsException() {
            // given
            Long labId = 999L;
            int limit = 5;
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rankingQueryService.getTopContributors(labId, limit))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException exception = (LabNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort, never()).findByLabIdAndStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("사용자 랩실 랭킹 조회 시")
    class GetUserLabRankingsTests {

        @Test
        @DisplayName("사용자가 속한 랩실의 랭킹 정보를 조회할 수 있다")
        void getUserLabRankings_UserWithLab_Success() {
            // given
            Long userId = 1L;
            Long labId = 1L;

            Lab lab = DomainLabFactory.buildValidLabWithId(labId);
            User user = DomainUserFactory.buildValidUserWithId(userId);
            user.assignLab(lab);

            List<ScoreSubmission> approvedSubmissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab)); // 추가된 Mock
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.APPROVED))
                    .thenReturn(approvedSubmissions);
            when(rankingPolicy.calculateLabTotalScore(approvedSubmissions)).thenReturn(150);
            when(labRepositoryPort.findAll()).thenReturn(List.of(lab));
            when(rankingPolicy.calculateRankWithTies(eq(150), any())).thenReturn(1);

            // when
            List<LabRankingResult> result = rankingQueryService.getUserLabRankings(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);

            LabRankingResult ranking = result.get(0);
            assertThat(ranking.getLabId()).isEqualTo(labId);
            assertThat(ranking.getTotalScore()).isEqualTo(150);
            assertThat(ranking.getRank()).isEqualTo(1);

            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("랩실에 속하지 않은 사용자는 빈 목록을 반환한다")
        void getUserLabRankings_UserWithoutLab_ReturnsEmptyList() {
            // given
            Long userId = 1L;
            User user = DomainUserFactory.buildValidUserWithId(userId);

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

            // when
            List<LabRankingResult> result = rankingQueryService.getUserLabRankings(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시 예외가 발생한다")
        void getUserLabRankings_UserNotFound_ThrowsException() {
            // given
            Long userId = 999L;
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rankingQueryService.getUserLabRankings(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
        }
    }

    @Nested
    @DisplayName("랩실 총점 계산 시")
    class CalculateLabTotalScoreTests {

        @Test
        @DisplayName("유효한 랩실 ID로 총점을 계산할 수 있다")
        void calculateLabTotalScore_ValidLabId_Success() {
            // given
            Long labId = 1L;
            Lab lab = DomainLabFactory.buildValidLabWithId(labId);

            List<ScoreSubmission> approvedSubmissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );

            when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.APPROVED))
                    .thenReturn(approvedSubmissions);
            when(rankingPolicy.calculateLabTotalScore(approvedSubmissions)).thenReturn(150);

            // when
            int result = rankingQueryService.calculateLabTotalScore(labId);

            // then
            assertThat(result).isEqualTo(150);

            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort).findByLabIdAndStatus(labId, SubmissionStatus.APPROVED);
            verify(rankingPolicy).calculateLabTotalScore(approvedSubmissions);
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 계산 시 예외가 발생한다")
        void calculateLabTotalScore_LabNotFound_ThrowsException() {
            // given
            Long labId = 999L;
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rankingQueryService.calculateLabTotalScore(labId))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException exception = (LabNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort, never()).findByLabIdAndStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("사용자 랩실 기여도 계산 시")
    class CalculateUserContributionInLabTests {

        @Test
        @DisplayName("유효한 사용자와 랩실 ID로 기여도를 계산할 수 있다")
        void calculateUserContributionInLab_ValidIds_Success() {
            // given
            Long userId = 1L;
            Long labId = 1L;

            User user = DomainUserFactory.buildValidUserWithId(userId);
            Lab lab = DomainLabFactory.buildValidLabWithId(labId);

            List<ScoreSubmission> userSubmissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildSubmissionWithUserAndScore(user, 100)
            );

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
            when(scoreSubmissionRepositoryPort.findByUserIdAndLabIdAndStatus(userId, labId, SubmissionStatus.APPROVED))
                    .thenReturn(userSubmissions);
            when(rankingPolicy.calculateUserContribution(userId, labId, userSubmissions)).thenReturn(100);

            // when
            int result = rankingQueryService.calculateUserContributionInLab(userId, labId);

            // then
            assertThat(result).isEqualTo(100);

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort).findByUserIdAndLabIdAndStatus(userId, labId, SubmissionStatus.APPROVED);
            verify(rankingPolicy).calculateUserContribution(userId, labId, userSubmissions);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 계산 시 예외가 발생한다")
        void calculateUserContributionInLab_UserNotFound_ThrowsException() {
            // given
            Long userId = 999L;
            Long labId = 1L;
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rankingQueryService.calculateUserContributionInLab(userId, labId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 계산 시 예외가 발생한다")
        void calculateUserContributionInLab_LabNotFound_ThrowsException() {
            // given
            Long userId = 1L;
            Long labId = 999L;

            User user = DomainUserFactory.buildValidUserWithId(userId);
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rankingQueryService.calculateUserContributionInLab(userId, labId))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException exception = (LabNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort, never()).findByUserIdAndLabIdAndStatus(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("점수 범위 내 랩실 수 조회 시")
    class CountLabsInScoreRangeTests {

        @Test
        @DisplayName("점수 범위 내 랩실 수를 조회할 수 있다")
        void countLabsInScoreRange_ValidRange_Success() {
            // given
            int minScore = 50;
            int maxScore = 150;

            Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
            Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
            Lab lab3 = DomainLabFactory.buildValidLabWithId(3L);

            List<Lab> allLabs = Arrays.asList(lab1, lab2, lab3);

            when(labRepositoryPort.findAll()).thenReturn(allLabs);
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(1L, SubmissionStatus.APPROVED))
                    .thenReturn(List.of());
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(2L, SubmissionStatus.APPROVED))
                    .thenReturn(List.of());
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(3L, SubmissionStatus.APPROVED))
                    .thenReturn(List.of());
            when(rankingPolicy.calculateLabTotalScore(any())).thenReturn(100, 200, 75);

            // when
            long result = rankingQueryService.countLabsInScoreRange(minScore, maxScore);

            // then
            assertThat(result).isEqualTo(2); // 100점, 75점 랩실 2개

            verify(labRepositoryPort).findAll();
            verify(rankingPolicy, times(3)).calculateLabTotalScore(any());
        }

        @Test
        @DisplayName("범위 내 랩실이 없을 때 0을 반환한다")
        void countLabsInScoreRange_NoLabsInRange_ReturnsZero() {
            // given
            int minScore = 200;
            int maxScore = 300;

            Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
            List<Lab> allLabs = Arrays.asList(lab1);

            when(labRepositoryPort.findAll()).thenReturn(allLabs);
            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(1L, SubmissionStatus.APPROVED))
                    .thenReturn(List.of());
            when(rankingPolicy.calculateLabTotalScore(any())).thenReturn(100); // 범위 밖

            // when
            long result = rankingQueryService.countLabsInScoreRange(minScore, maxScore);

            // then
            assertThat(result).isEqualTo(0);

            verify(labRepositoryPort).findAll();
            verify(rankingPolicy).calculateLabTotalScore(any());
        }
    }

    @Nested
    @DisplayName("랭킹 업데이트 필요 랩실 조회 시")
    class FindLabsNeedingRankingUpdateTests {

        @Test
        @DisplayName("모든 랩실 목록을 반환한다")
        void findLabsNeedingRankingUpdate_ReturnsAllLabs() {
            // given
            Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
            Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
            List<Lab> allLabs = Arrays.asList(lab1, lab2);

            when(labRepositoryPort.findAll()).thenReturn(allLabs);

            // when
            List<Lab> result = rankingQueryService.findLabsNeedingRankingUpdate();

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(lab1, lab2);

            verify(labRepositoryPort).findAll();
        }

        @Test
        @DisplayName("랩실이 없을 때 빈 목록을 반환한다")
        void findLabsNeedingRankingUpdate_NoLabs_ReturnsEmptyList() {
            // given
            when(labRepositoryPort.findAll()).thenReturn(List.of());

            // when
            List<Lab> result = rankingQueryService.findLabsNeedingRankingUpdate();

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(labRepositoryPort).findAll();
        }
    }
}
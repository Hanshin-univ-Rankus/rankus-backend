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
import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.ranking.policy.DuplicateCheckPolicy;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScoreSubmissionQueryServiceTest {

    @Mock
    private ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private DuplicateCheckPolicy duplicateCheckPolicy;

    @InjectMocks
    private ScoreSubmissionQueryService scoreSubmissionQueryService;

    // ——————————————————————————————————————————————————————————
    // 헬퍼 메서드: 반복되는 모킹 로직을 한곳에 모았습니다.
    // ——————————————————————————————————————————————————————————

    private User givenExistingUser(Long userId) {
        User user = DomainUserFactory.buildValidUserWithId(userId);
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        return user;
    }

    private ScoreSubmission givenExistingSubmission(Long submissionId) {
        ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
        when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
        return submission;
    }

    private Page<ScoreSubmission> createPageOfSubmissions(List<ScoreSubmission> submissions, Pageable pageable) {
        return new PageImpl<>(submissions, pageable, submissions.size());
    }

    // ——————————————————————————————————————————————————————————
    // 1) findSubmissionById 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findSubmissionById 메서드는")
    class FindSubmissionByIdTests {

        @Test
        @DisplayName("존재하는 점수 신청 ID로 조회하면 점수 신청을 반환한다")
        void findSubmissionById_existingId_success() {
            // given
            Long submissionId = 1L;
            ScoreSubmission expectedSubmission = givenExistingSubmission(submissionId);

            // when
            ScoreSubmission result = scoreSubmissionQueryService.findSubmissionById(submissionId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(submissionId);
            assertThat(result).isSameAs(expectedSubmission);

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
        }

        @Test
        @DisplayName("존재하지 않는 점수 신청 ID로 조회하면 RankingValidationException을 던진다")
        void findSubmissionById_nonExistingId_throwsException() {
            // given
            Long submissionId = 999L;
            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionQueryService.findSubmissionById(submissionId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException e = (RankingValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.SUBMISSION_NOT_FOUND);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 2) findSubmissionsByUserId 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findSubmissionsByUserId 메서드는")
    class FindSubmissionsByUserIdTests {

        @Test
        @DisplayName("존재하는 사용자 ID로 조회하면 해당 사용자의 점수 신청 목록을 반환한다")
        void findSubmissionsByUserId_existingUserId_success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            givenExistingUser(userId);
            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L),
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(2L)
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByUserId(userId, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByUserId(userId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).findByUserId(userId, pageable);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회하면 UserNotFoundException을 던진다")
        void findSubmissionsByUserId_nonExistingUserId_throwsException() {
            // given
            Long userId = 999L;
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionQueryService.findSubmissionsByUserId(userId, pageable))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort, never()).findByUserId(any(), any());
        }

        @Test
        @DisplayName("점수 신청이 없는 사용자 ID로 조회하면 빈 페이지를 반환한다")
        void findSubmissionsByUserId_noSubmissions_returnsEmptyPage() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            givenExistingUser(userId);
            Page<ScoreSubmission> emptyPage = createPageOfSubmissions(Collections.emptyList(), pageable);

            when(scoreSubmissionRepositoryPort.findByUserId(userId, pageable)).thenReturn(emptyPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByUserId(userId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).findByUserId(userId, pageable);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) findSubmissionsByLabId 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findSubmissionsByLabId 메서드는")
    class FindSubmissionsByLabIdTests {

        @Test
        @DisplayName("랩실 ID로 조회하면 해당 랩실의 점수 신청 목록을 반환한다")
        void findSubmissionsByLabId_existingLabId_success() {
            // given
            Long labId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L),
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(2L)
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByLabId(labId, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByLabId(labId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);

            verify(scoreSubmissionRepositoryPort).findByLabId(labId, pageable);
        }

        @Test
        @DisplayName("점수 신청이 없는 랩실 ID로 조회하면 빈 페이지를 반환한다")
        void findSubmissionsByLabId_noSubmissions_returnsEmptyPage() {
            // given
            Long labId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            Page<ScoreSubmission> emptyPage = createPageOfSubmissions(Collections.emptyList(), pageable);

            when(scoreSubmissionRepositoryPort.findByLabId(labId, pageable)).thenReturn(emptyPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByLabId(labId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(scoreSubmissionRepositoryPort).findByLabId(labId, pageable);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) findSubmissionsByStatus 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findSubmissionsByStatus 메서드는")
    class FindSubmissionsByStatusTests {

        @Test
        @DisplayName("상태로 조회하면 해당 상태의 점수 신청 목록을 반환한다")
        void findSubmissionsByStatus_validStatus_success() {
            // given
            SubmissionStatus status = SubmissionStatus.PENDING;
            Pageable pageable = PageRequest.of(0, 10);

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByStatus(status, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByStatus(status, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(scoreSubmissionRepositoryPort).findByStatus(status, pageable);
        }

        @Test
        @DisplayName("해당 상태의 점수 신청이 없으면 빈 페이지를 반환한다")
        void findSubmissionsByStatus_noSubmissions_returnsEmptyPage() {
            // given
            SubmissionStatus status = SubmissionStatus.REJECTED;
            Pageable pageable = PageRequest.of(0, 10);

            Page<ScoreSubmission> emptyPage = createPageOfSubmissions(Collections.emptyList(), pageable);

            when(scoreSubmissionRepositoryPort.findByStatus(status, pageable)).thenReturn(emptyPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByStatus(status, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(scoreSubmissionRepositoryPort).findByStatus(status, pageable);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) findSubmissionsByLabIdAndStatus 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findSubmissionsByLabIdAndStatus 메서드는")
    class FindSubmissionsByLabIdAndStatusTests {

        @Test
        @DisplayName("랩실 ID와 상태로 조회하면 해당 조건의 점수 신청 목록을 반환한다")
        void findSubmissionsByLabIdAndStatus_validCondition_success() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.APPROVED;
            Pageable pageable = PageRequest.of(0, 10);

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildApprovedSubmission()
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, status, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByLabIdAndStatus(labId, status, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(scoreSubmissionRepositoryPort).findByLabIdAndStatus(labId, status, pageable);
        }

        @Test
        @DisplayName("조건에 맞는 점수 신청이 없으면 빈 페이지를 반환한다")
        void findSubmissionsByLabIdAndStatus_noMatches_returnsEmptyPage() {
            // given
            Long labId = 1L;
            SubmissionStatus status = SubmissionStatus.REJECTED;
            Pageable pageable = PageRequest.of(0, 10);

            Page<ScoreSubmission> emptyPage = createPageOfSubmissions(Collections.emptyList(), pageable);

            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, status, pageable)).thenReturn(emptyPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsByLabIdAndStatus(labId, status, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(scoreSubmissionRepositoryPort).findByLabIdAndStatus(labId, status, pageable);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 6) findPendingSubmissionsForApprover 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findPendingSubmissionsForApprover 메서드는")
    class FindPendingSubmissionsForApproverTests {

        @Test
        @DisplayName("관리자 권한으로 조회하면 모든 PENDING 상태의 점수 신청을 반환한다")
        void findPendingSubmissionsForApprover_adminUser_success() {
            // given
            Long approverId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            User admin = DomainUserFactory.buildValidUserWithId(approverId);
            ReflectionTestUtils.setField(admin, "role", Role.ADMIN);

            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(admin));

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByStatus(SubmissionStatus.PENDING, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findPendingSubmissionsForApprover(approverId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionRepositoryPort).findByStatus(SubmissionStatus.PENDING, pageable);
        }

        @Test
        @DisplayName("교수 권한으로 조회하면 모든 PENDING 상태의 점수 신청을 반환한다")
        void findPendingSubmissionsForApprover_professorUser_success() {
            // given
            Long approverId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            User professor = DomainUserFactory.buildValidUserWithId(approverId);
            ReflectionTestUtils.setField(professor, "role", Role.PROFESSOR);

            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(professor));

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByStatus(SubmissionStatus.PENDING, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findPendingSubmissionsForApprover(approverId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionRepositoryPort).findByStatus(SubmissionStatus.PENDING, pageable);
        }

        @Test
        @DisplayName("랩장 권한으로 조회하면 자신의 랩실 PENDING 상태의 점수 신청을 반환한다")
        void findPendingSubmissionsForApprover_labLeaderUser_success() {
            // given
            Long approverId = 1L;
            Long labId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            User labLeader = DomainUserFactory.buildValidUserWithId(approverId);
            ReflectionTestUtils.setField(labLeader, "role", Role.LAB_LEADER);
            ReflectionTestUtils.setField(labLeader, "lab", DomainLabFactory.buildValidLabWithId(labId));

            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(labLeader));

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            Page<ScoreSubmission> expectedPage = createPageOfSubmissions(submissions, pageable);

            when(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.PENDING, pageable)).thenReturn(expectedPage);

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findPendingSubmissionsForApprover(approverId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionRepositoryPort).findByLabIdAndStatus(labId, SubmissionStatus.PENDING, pageable);
        }

        @Test
        @DisplayName("랩실 소속이 없는 사용자로 조회하면 빈 페이지를 반환한다")
        void findPendingSubmissionsForApprover_userWithoutLab_returnsEmptyPage() {
            // given
            Long approverId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            User userWithoutLab = DomainUserFactory.buildValidUserWithId(approverId);
            ReflectionTestUtils.setField(userWithoutLab, "role", Role.LAB_LEADER);
            ReflectionTestUtils.setField(userWithoutLab, "lab", null);

            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(userWithoutLab));

            // when
            Page<ScoreSubmission> result = scoreSubmissionQueryService.findPendingSubmissionsForApprover(approverId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionRepositoryPort, never()).findByLabIdAndStatus(anyLong(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 승인자 ID로 조회하면 UserNotFoundException을 던진다")
        void findPendingSubmissionsForApprover_nonExistingApprover_throwsException() {
            // given
            Long approverId = 999L;
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionQueryService.findPendingSubmissionsForApprover(approverId, pageable))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionRepositoryPort, never()).findByStatus(any(), any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 7) checkDuplicates 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("checkDuplicates 메서드는")
    class CheckDuplicatesTests {

        @Test
        @DisplayName("존재하는 사용자 ID로 중복 체크하면 중복 체크 결과를 반환한다")
        void checkDuplicates_existingUserId_success() {
            // given
            Long userId = 1L;
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            ScoreCategory category = ScoreCategory.ACADEMIC_ACHIEVEMENT;

            givenExistingUser(userId);

            List<ScoreSubmission> existingSubmissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );
            DuplicateCheckPolicy.DuplicateCheckResult expectedResult =
                    new DuplicateCheckPolicy.DuplicateCheckResult(false, false, Collections.emptyList());

            when(scoreSubmissionRepositoryPort.findByUserIdAndAchievementDate(userId, achievementDate))
                    .thenReturn(existingSubmissions);
            when(duplicateCheckPolicy.checkDuplicates(userId, achievementDate, category, existingSubmissions))
                    .thenReturn(expectedResult);

            // when
            DuplicateCheckPolicy.DuplicateCheckResult result = scoreSubmissionQueryService.checkDuplicates(userId, achievementDate, category);

            // then
            assertThat(result).isNotNull();
            assertThat(result.hasDateDuplicates()).isFalse();
            assertThat(result.hasExactDuplicates()).isFalse();
            assertThat(result.getDuplicateSubmissions()).isEmpty();

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).findByUserIdAndAchievementDate(userId, achievementDate);
            verify(duplicateCheckPolicy).checkDuplicates(userId, achievementDate, category, existingSubmissions);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 중복 체크하면 UserNotFoundException을 던진다")
        void checkDuplicates_nonExistingUserId_throwsException() {
            // given
            Long userId = 999L;
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            ScoreCategory category = ScoreCategory.ACADEMIC_ACHIEVEMENT;

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionQueryService.checkDuplicates(userId, achievementDate, category))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort, never()).findByUserIdAndAchievementDate(anyLong(), any());
            verify(duplicateCheckPolicy, never()).checkDuplicates(anyLong(), any(), any(), any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 8) findSubmissionsExpiringWithin 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findSubmissionsExpiringWithin 메서드는")
    class FindSubmissionsExpiringWithinTests {

        @Test
        @DisplayName("특정 기간 내에 만료되는 점수 신청을 반환한다")
        void findSubmissionsExpiringWithin_validDays_success() {
            // given
            int days = 7;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureTime = now.plusDays(days);

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L)
            );

            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBetween(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsExpiringWithin(days);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);

            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBetween(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("만료 예정인 점수 신청이 없으면 빈 리스트를 반환한다")
        void findSubmissionsExpiringWithin_noExpiringSubmissions_returnsEmptyList() {
            // given
            int days = 7;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureTime = now.plusDays(days);

            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBetween(
                    SubmissionStatus.PENDING, now, futureTime)).thenReturn(Collections.emptyList());

            // when
            List<ScoreSubmission> result = scoreSubmissionQueryService.findSubmissionsExpiringWithin(days);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBetween(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 9) findExpiredSubmissions 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("findExpiredSubmissions 메서드는")
    class FindExpiredSubmissionsTests {

        @Test
        @DisplayName("만료된 점수 신청을 반환한다")
        void findExpiredSubmissions_hasExpiredSubmissions_success() {
            // given
            LocalDateTime now = LocalDateTime.now();

            List<ScoreSubmission> submissions = Arrays.asList(
                    DomainScoreSubmissionFactory.buildExpiredSubmission()
            );

            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class))).thenReturn(submissions);

            // when
            List<ScoreSubmission> result = scoreSubmissionQueryService.findExpiredSubmissions();

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);

            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("만료된 점수 신청이 없으면 빈 리스트를 반환한다")
        void findExpiredSubmissions_noExpiredSubmissions_returnsEmptyList() {
            // given
            LocalDateTime now = LocalDateTime.now();

            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBefore(
                    SubmissionStatus.PENDING, now)).thenReturn(Collections.emptyList());

            // when
            List<ScoreSubmission> result = scoreSubmissionQueryService.findExpiredSubmissions();

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class));
        }
    }

    // ——————————————————————————————————————————————————————————
    // 10) calculateUserScoreInLab 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("calculateUserScoreInLab 메서드는")
    class CalculateUserScoreInLabTests {

        @Test
        @DisplayName("존재하는 사용자와 랩실 ID로 점수 계산하면 총점을 반환한다")
        void calculateUserScoreInLab_existingUserAndLab_success() {
            // given
            Long userId = 1L;
            Long labId = 1L;
            Integer expectedScore = 150;

            givenExistingUser(userId);

            when(scoreSubmissionRepositoryPort.sumScoresByUserIdAndLabIdAndStatus(
                    userId, labId, SubmissionStatus.APPROVED)).thenReturn(expectedScore);

            // when
            int result = scoreSubmissionQueryService.calculateUserScoreInLab(userId, labId);

            // then
            assertThat(result).isEqualTo(expectedScore);

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).sumScoresByUserIdAndLabIdAndStatus(
                    userId, labId, SubmissionStatus.APPROVED);
        }

        @Test
        @DisplayName("점수가 없는 사용자의 경우 0을 반환한다")
        void calculateUserScoreInLab_noScores_returnsZero() {
            // given
            Long userId = 1L;
            Long labId = 1L;

            givenExistingUser(userId);

            when(scoreSubmissionRepositoryPort.sumScoresByUserIdAndLabIdAndStatus(
                    userId, labId, SubmissionStatus.APPROVED)).thenReturn(null);

            // when
            int result = scoreSubmissionQueryService.calculateUserScoreInLab(userId, labId);

            // then
            assertThat(result).isEqualTo(0);

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).sumScoresByUserIdAndLabIdAndStatus(
                    userId, labId, SubmissionStatus.APPROVED);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 점수 계산하면 UserNotFoundException을 던진다")
        void calculateUserScoreInLab_nonExistingUserId_throwsException() {
            // given
            Long userId = 999L;
            Long labId = 1L;

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionQueryService.calculateUserScoreInLab(userId, labId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort, never()).sumScoresByUserIdAndLabIdAndStatus(anyLong(), anyLong(), any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 11) calculateUserTotalScore 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("calculateUserTotalScore 메서드는")
    class CalculateUserTotalScoreTests {

        @Test
        @DisplayName("존재하는 사용자 ID로 총점 계산하면 총점을 반환한다")
        void calculateUserTotalScore_existingUserId_success() {
            // given
            Long userId = 1L;
            Integer expectedScore = 300;

            givenExistingUser(userId);

            when(scoreSubmissionRepositoryPort.sumScoresByUserIdAndStatus(
                    userId, SubmissionStatus.APPROVED)).thenReturn(expectedScore);

            // when
            int result = scoreSubmissionQueryService.calculateUserTotalScore(userId);

            // then
            assertThat(result).isEqualTo(expectedScore);

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).sumScoresByUserIdAndStatus(
                    userId, SubmissionStatus.APPROVED);
        }

        @Test
        @DisplayName("점수가 없는 사용자의 경우 0을 반환한다")
        void calculateUserTotalScore_noScores_returnsZero() {
            // given
            Long userId = 1L;

            givenExistingUser(userId);

            when(scoreSubmissionRepositoryPort.sumScoresByUserIdAndStatus(
                    userId, SubmissionStatus.APPROVED)).thenReturn(null);

            // when
            int result = scoreSubmissionQueryService.calculateUserTotalScore(userId);

            // then
            assertThat(result).isEqualTo(0);

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort).sumScoresByUserIdAndStatus(
                    userId, SubmissionStatus.APPROVED);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 총점 계산하면 UserNotFoundException을 던진다")
        void calculateUserTotalScore_nonExistingUserId_throwsException() {
            // given
            Long userId = 999L;

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionQueryService.calculateUserTotalScore(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(scoreSubmissionRepositoryPort, never()).sumScoresByUserIdAndStatus(anyLong(), any());
        }
    }
}
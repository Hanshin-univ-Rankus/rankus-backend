package org.univ.rankus.domain.model.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScoreCategory Enum 테스트")
class ScoreCategoryTest {

    @Test
    @DisplayName("모든 ScoreCategory 값이 정의되어 있다")
    void allScoreCategoryValues_AreDefined() {
        // given & when
        ScoreCategory[] categories = ScoreCategory.values();

        // then
        assertThat(categories).hasSize(17);
        assertThat(categories).contains(
                // 학업 성과
                ScoreCategory.ACADEMIC_ACHIEVEMENT,
                ScoreCategory.SCHOLARSHIP,
                ScoreCategory.GPA_MAINTENANCE,

                // 대회 수상
                ScoreCategory.CONTEST_INTERNAL_WINNER,
                ScoreCategory.CONTEST_INTERNAL_RUNNER_UP,
                ScoreCategory.CONTEST_EXTERNAL_WINNER,
                ScoreCategory.CONTEST_EXTERNAL_RUNNER_UP,

                // 자격증
                ScoreCategory.CERTIFICATION_NATIONAL,
                ScoreCategory.CERTIFICATION_INTERNATIONAL,
                ScoreCategory.CERTIFICATION_PRIVATE,

                // 교육 및 세미나
                ScoreCategory.EDUCATION_COMPLETION,
                ScoreCategory.SEMINAR_PARTICIPATION,

                // 연구 성과
                ScoreCategory.RESEARCH_SCI_PAPER,
                ScoreCategory.RESEARCH_GENERAL_PAPER,
                ScoreCategory.RESEARCH_PATENT,

                // 기타 활동
                ScoreCategory.VOLUNTEER_ACTIVITY,
                ScoreCategory.EXTRACURRICULAR_ACTIVITY
        );
    }

    @ParameterizedTest
    @EnumSource(ScoreCategory.class)
    @DisplayName("모든 ScoreCategory는 양수의 기본 점수를 가진다")
    void allScoreCategories_HavePositiveDefaultScore(ScoreCategory category) {
        // when
        int defaultScore = category.getDefaultScore();

        // then
        assertThat(defaultScore).isPositive();
    }

    @ParameterizedTest
    @EnumSource(ScoreCategory.class)
    @DisplayName("모든 ScoreCategory는 비어있지 않은 표시 이름을 가진다")
    void allScoreCategories_HaveNonEmptyDisplayName(ScoreCategory category) {
        // when
        String displayName = category.getDisplayName();

        // then
        assertThat(displayName).isNotBlank();
    }

    @Test
    @DisplayName("학업 성과 카테고리들의 기본 점수가 올바르다")
    void academicAchievementCategories_HaveCorrectDefaultScores() {
        // when & then
        assertThat(ScoreCategory.ACADEMIC_ACHIEVEMENT.getDefaultScore()).isEqualTo(5);
        assertThat(ScoreCategory.SCHOLARSHIP.getDefaultScore()).isEqualTo(15);
        assertThat(ScoreCategory.GPA_MAINTENANCE.getDefaultScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("대회 수상 카테고리들의 기본 점수가 올바르다")
    void contestCategories_HaveCorrectDefaultScores() {
        // when & then
        assertThat(ScoreCategory.CONTEST_INTERNAL_WINNER.getDefaultScore()).isEqualTo(30);
        assertThat(ScoreCategory.CONTEST_INTERNAL_RUNNER_UP.getDefaultScore()).isEqualTo(20);
        assertThat(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDefaultScore()).isEqualTo(40);
        assertThat(ScoreCategory.CONTEST_EXTERNAL_RUNNER_UP.getDefaultScore()).isEqualTo(35);
    }

    @Test
    @DisplayName("자격증 카테고리들의 기본 점수가 올바르다")
    void certificationCategories_HaveCorrectDefaultScores() {
        // when & then
        assertThat(ScoreCategory.CERTIFICATION_NATIONAL.getDefaultScore()).isEqualTo(20);
        assertThat(ScoreCategory.CERTIFICATION_INTERNATIONAL.getDefaultScore()).isEqualTo(25);
        assertThat(ScoreCategory.CERTIFICATION_PRIVATE.getDefaultScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("교육 및 세미나 카테고리들의 기본 점수가 올바르다")
    void educationCategories_HaveCorrectDefaultScores() {
        // when & then
        assertThat(ScoreCategory.EDUCATION_COMPLETION.getDefaultScore()).isEqualTo(10);
        assertThat(ScoreCategory.SEMINAR_PARTICIPATION.getDefaultScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("연구 성과 카테고리들의 기본 점수가 올바르다")
    void researchCategories_HaveCorrectDefaultScores() {
        // when & then
        assertThat(ScoreCategory.RESEARCH_SCI_PAPER.getDefaultScore()).isEqualTo(100);
        assertThat(ScoreCategory.RESEARCH_GENERAL_PAPER.getDefaultScore()).isEqualTo(50);
        assertThat(ScoreCategory.RESEARCH_PATENT.getDefaultScore()).isEqualTo(30);
    }

    @Test
    @DisplayName("기타 활동 카테고리들의 기본 점수가 올바르다")
    void otherActivityCategories_HaveCorrectDefaultScores() {
        // when & then
        assertThat(ScoreCategory.VOLUNTEER_ACTIVITY.getDefaultScore()).isEqualTo(5);
        assertThat(ScoreCategory.EXTRACURRICULAR_ACTIVITY.getDefaultScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("연구 성과 카테고리들이 가장 높은 점수를 가진다")
    void researchCategories_HaveHighestScores() {
        // when
        int sciPaperScore = ScoreCategory.RESEARCH_SCI_PAPER.getDefaultScore();
        int generalPaperScore = ScoreCategory.RESEARCH_GENERAL_PAPER.getDefaultScore();
        int patentScore = ScoreCategory.RESEARCH_PATENT.getDefaultScore();

        // then
        assertThat(sciPaperScore).isEqualTo(100); // 최고 점수
        assertThat(generalPaperScore).isEqualTo(50);
        assertThat(patentScore).isEqualTo(30);

        // 다른 카테고리들보다 높은 점수인지 확인
        assertThat(sciPaperScore).isGreaterThan(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDefaultScore());
        assertThat(generalPaperScore).isGreaterThan(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDefaultScore());
        assertThat(patentScore).isGreaterThan(ScoreCategory.CERTIFICATION_INTERNATIONAL.getDefaultScore());
    }

    @Test
    @DisplayName("교외 대회 수상이 교내 대회 수상보다 높은 점수를 가진다")
    void externalContest_HasHigherScoreThanInternal() {
        // when
        int externalWinnerScore = ScoreCategory.CONTEST_EXTERNAL_WINNER.getDefaultScore();
        int internalWinnerScore = ScoreCategory.CONTEST_INTERNAL_WINNER.getDefaultScore();
        int externalRunnerUpScore = ScoreCategory.CONTEST_EXTERNAL_RUNNER_UP.getDefaultScore();
        int internalRunnerUpScore = ScoreCategory.CONTEST_INTERNAL_RUNNER_UP.getDefaultScore();

        // then
        assertThat(externalWinnerScore).isGreaterThan(internalWinnerScore);
        assertThat(externalRunnerUpScore).isGreaterThan(internalRunnerUpScore);
    }

    @Test
    @DisplayName("우승이 우수상보다 높은 점수를 가진다")
    void winner_HasHigherScoreThanRunnerUp() {
        // when & then
        assertThat(ScoreCategory.CONTEST_INTERNAL_WINNER.getDefaultScore())
                .isGreaterThan(ScoreCategory.CONTEST_INTERNAL_RUNNER_UP.getDefaultScore());
        assertThat(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDefaultScore())
                .isGreaterThan(ScoreCategory.CONTEST_EXTERNAL_RUNNER_UP.getDefaultScore());
    }

    @Test
    @DisplayName("국제 자격증이 국가 자격증보다 높은 점수를 가진다")
    void internationalCertification_HasHigherScoreThanNational() {
        // when
        int internationalScore = ScoreCategory.CERTIFICATION_INTERNATIONAL.getDefaultScore();
        int nationalScore = ScoreCategory.CERTIFICATION_NATIONAL.getDefaultScore();
        int privateScore = ScoreCategory.CERTIFICATION_PRIVATE.getDefaultScore();

        // then
        assertThat(internationalScore).isGreaterThan(nationalScore);
        assertThat(nationalScore).isGreaterThan(privateScore);
    }

    @Test
    @DisplayName("표시 이름이 한국어로 되어 있다")
    void displayNames_AreInKorean() {
        // when & then
        assertThat(ScoreCategory.ACADEMIC_ACHIEVEMENT.getDisplayName()).isEqualTo("학업 성과 (A+)");
        assertThat(ScoreCategory.SCHOLARSHIP.getDisplayName()).isEqualTo("장학금 수혜");
        assertThat(ScoreCategory.RESEARCH_SCI_PAPER.getDisplayName()).isEqualTo("SCI 논문 게재");
        assertThat(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDisplayName()).isEqualTo("교외 대회 대상");
        assertThat(ScoreCategory.CERTIFICATION_INTERNATIONAL.getDisplayName()).isEqualTo("국제 자격증");
        assertThat(ScoreCategory.VOLUNTEER_ACTIVITY.getDisplayName()).isEqualTo("봉사 활동");
    }

    @Test
    @DisplayName("점수 분포가 합리적이다")
    void scoreDistribution_IsReasonable() {
        // when
        int minScore = ScoreCategory.ACADEMIC_ACHIEVEMENT.getDefaultScore(); // 5점
        int maxScore = ScoreCategory.RESEARCH_SCI_PAPER.getDefaultScore(); // 100점

        // then
        assertThat(minScore).isEqualTo(5);
        assertThat(maxScore).isEqualTo(100);

        // 점수가 5점 이하인 항목들 (저점수 그룹)
        assertThat(ScoreCategory.ACADEMIC_ACHIEVEMENT.getDefaultScore()).isLessThanOrEqualTo(5);
        assertThat(ScoreCategory.SEMINAR_PARTICIPATION.getDefaultScore()).isLessThanOrEqualTo(5);
        assertThat(ScoreCategory.VOLUNTEER_ACTIVITY.getDefaultScore()).isLessThanOrEqualTo(5);

        // 점수가 50점 이상인 항목들 (고점수 그룹)
        assertThat(ScoreCategory.RESEARCH_SCI_PAPER.getDefaultScore()).isGreaterThanOrEqualTo(50);
        assertThat(ScoreCategory.RESEARCH_GENERAL_PAPER.getDefaultScore()).isGreaterThanOrEqualTo(50);

        // 점수가 30점 이상인 항목들 (중점수 그룹)
        assertThat(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDefaultScore()).isGreaterThanOrEqualTo(30);
    }

    @Test
    @DisplayName("모든 카테고리의 점수가 100점을 초과하지 않는다")
    void allCategories_ScoreDoesNotExceed100() {
        // when & then
        for (ScoreCategory category : ScoreCategory.values()) {
            assertThat(category.getDefaultScore()).isLessThanOrEqualTo(100);
        }
    }

    @Test
    @DisplayName("카테고리 이름과 표시 이름이 일관성을 가진다")
    void categoryNames_AreConsistentWithDisplayNames() {
        // when & then
        assertThat(ScoreCategory.RESEARCH_SCI_PAPER.name()).contains("RESEARCH");
        assertThat(ScoreCategory.RESEARCH_SCI_PAPER.getDisplayName()).contains("논문");

        assertThat(ScoreCategory.CONTEST_EXTERNAL_WINNER.name()).contains("CONTEST");
        assertThat(ScoreCategory.CONTEST_EXTERNAL_WINNER.getDisplayName()).contains("대회");

        assertThat(ScoreCategory.CERTIFICATION_INTERNATIONAL.name()).contains("CERTIFICATION");
        assertThat(ScoreCategory.CERTIFICATION_INTERNATIONAL.getDisplayName()).contains("자격증");

        assertThat(ScoreCategory.VOLUNTEER_ACTIVITY.name()).contains("VOLUNTEER");
        assertThat(ScoreCategory.VOLUNTEER_ACTIVITY.getDisplayName()).contains("봉사");
    }
}
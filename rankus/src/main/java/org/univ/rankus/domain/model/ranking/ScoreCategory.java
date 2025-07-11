package org.univ.rankus.domain.model.ranking;

/**
 * 점수 카테고리를 정의한 enum
 * 각 카테고리는 기본 점수를 가지며, 점수 신청 시 기준이 됩니다.
 */
public enum ScoreCategory {
    // 학업 성과
    ACADEMIC_ACHIEVEMENT(5, "학업 성과 (A+)"),
    SCHOLARSHIP(15, "장학금 수혜"),
    GPA_MAINTENANCE(10, "GPA 4.0 유지"),

    // 대회 수상
    CONTEST_INTERNAL_WINNER(30, "교내 대회 대상"),
    CONTEST_INTERNAL_RUNNER_UP(20, "교내 대회 우수상"),
    CONTEST_EXTERNAL_WINNER(40, "교외 대회 대상"),
    CONTEST_EXTERNAL_RUNNER_UP(35, "교외 대회 우수상"),

    // 자격증
    CERTIFICATION_NATIONAL(20, "국가 자격증"),
    CERTIFICATION_INTERNATIONAL(25, "국제 자격증"),
    CERTIFICATION_PRIVATE(10, "민간 자격증"),

    // 교육 및 세미나
    EDUCATION_COMPLETION(10, "교육 과정 수료"),
    SEMINAR_PARTICIPATION(5, "세미나 참여"),

    // 연구 성과
    RESEARCH_SCI_PAPER(100, "SCI 논문 게재"),
    RESEARCH_GENERAL_PAPER(50, "일반 논문 게재"),
    RESEARCH_PATENT(30, "특허 출원/등록"),

    // 기타 활동
    VOLUNTEER_ACTIVITY(5, "봉사 활동"),
    EXTRACURRICULAR_ACTIVITY(10, "교내 활동");

    private final int defaultScore;
    private final String displayName;

    ScoreCategory(int defaultScore, String displayName) {
        this.defaultScore = defaultScore;
        this.displayName = displayName;
    }

    public int getDefaultScore() {
        return defaultScore;
    }

    public String getDisplayName() {
        return displayName;
    }
}
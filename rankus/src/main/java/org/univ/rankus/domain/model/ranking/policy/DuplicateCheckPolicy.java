package org.univ.rankus.domain.model.ranking.policy;

import org.springframework.stereotype.Component;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 중복 검사 정책을 정의하는 도메인 서비스
 */
@Component
public class DuplicateCheckPolicy {

    /**
     * 중복 검사 결과를 담는 값 객체
     */
    public static class DuplicateCheckResult {
        private final boolean hasDateDuplicates;
        private final boolean hasExactDuplicates;
        private final List<ScoreSubmission> duplicateSubmissions;

        public DuplicateCheckResult(boolean hasDateDuplicates, boolean hasExactDuplicates,
                                    List<ScoreSubmission> duplicateSubmissions) {
            this.hasDateDuplicates = hasDateDuplicates;
            this.hasExactDuplicates = hasExactDuplicates;
            this.duplicateSubmissions = duplicateSubmissions;
        }

        public boolean hasDateDuplicates() {
            return hasDateDuplicates;
        }

        public boolean hasExactDuplicates() {
            return hasExactDuplicates;
        }

        public List<ScoreSubmission> getDuplicateSubmissions() {
            return duplicateSubmissions;
        }

        public boolean hasDuplicates() {
            return hasDateDuplicates || hasExactDuplicates;
        }
    }

    /**
     * 중복 검사 실행
     */
    public DuplicateCheckResult checkDuplicates(Long userId, LocalDate achievementDate,
                                                ScoreCategory category, List<ScoreSubmission> existingSubmissions) {
        // 같은 날짜의 신청 검사
        List<ScoreSubmission> sameDateSubmissions = existingSubmissions.stream()
                .filter(submission -> submission.getUser().getId().equals(userId))
                .filter(submission -> submission.getAchievementDate().equals(achievementDate))
                .collect(Collectors.toList());

        // 같은 날짜 + 같은 카테고리의 신청 검사
        List<ScoreSubmission> exactDuplicates = sameDateSubmissions.stream()
                .filter(submission -> submission.getCategory() == category)
                .collect(Collectors.toList());

        boolean hasDateDuplicates = !sameDateSubmissions.isEmpty();
        boolean hasExactDuplicates = !exactDuplicates.isEmpty();

        return new DuplicateCheckResult(hasDateDuplicates, hasExactDuplicates, sameDateSubmissions);
    }

    /**
     * 경고 레벨 결정
     */
    public enum DuplicateWarningLevel {
        NONE("중복 없음"),
        DATE_DUPLICATE("날짜 중복 경고"),
        EXACT_DUPLICATE("완전 중복 경고");

        private final String description;

        DuplicateWarningLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 중복 경고 레벨 결정
     */
    public DuplicateWarningLevel determineDuplicateWarningLevel(DuplicateCheckResult result) {
        if (result.hasExactDuplicates()) {
            return DuplicateWarningLevel.EXACT_DUPLICATE;
        } else if (result.hasDateDuplicates()) {
            return DuplicateWarningLevel.DATE_DUPLICATE;
        } else {
            return DuplicateWarningLevel.NONE;
        }
    }

    /**
     * 중복 허용 여부 결정
     */
    public boolean isDuplicateAllowed(DuplicateCheckResult result) {
        // 완전 중복(날짜 + 카테고리)은 허용하지 않음
        return !result.hasExactDuplicates();
    }

    /**
     * 중복 신청 시 UI 우선순위 결정
     */
    public int calculateDisplayPriority(DuplicateCheckResult result) {
        if (result.hasExactDuplicates()) {
            return 1; // 최우선 (빨간색 테두리)
        } else if (result.hasDateDuplicates()) {
            return 2; // 2순위 (노란색 경고)
        } else {
            return 3; // 일반
        }
    }

    /**
     * 중복 방지 메시지 생성
     */
    public String generateDuplicateMessage(DuplicateCheckResult result) {
        if (result.hasExactDuplicates()) {
            return "동일한 날짜와 카테고리의 신청이 이미 존재합니다.";
        } else if (result.hasDateDuplicates()) {
            return "동일한 날짜의 다른 신청이 존재합니다.";
        } else {
            return "";
        }
    }

    /**
     * 중복 신청 건수 제한 확인
     */
    public boolean isWithinDuplicateLimit(DuplicateCheckResult result) {
        // 같은 날짜의 신청은 최대 5개까지 허용
        return result.getDuplicateSubmissions().size() < 5;
    }
}
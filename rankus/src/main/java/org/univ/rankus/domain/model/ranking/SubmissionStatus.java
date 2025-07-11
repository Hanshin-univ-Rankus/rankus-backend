package org.univ.rankus.domain.model.ranking;

/**
 * 점수 신청 상태를 정의한 enum
 */
public enum SubmissionStatus {
    PENDING("심사 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨");

    private final String displayName;

    SubmissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 상태 변경 가능 여부 확인
     */
    public boolean canTransitionTo(SubmissionStatus newStatus) {
        return this == PENDING && (newStatus == APPROVED || newStatus == REJECTED);
    }
}
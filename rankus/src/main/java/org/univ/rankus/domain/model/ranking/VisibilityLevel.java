package org.univ.rankus.domain.model.ranking;

/**
 * 점수 신청 공개 범위를 정의한 enum
 */
public enum VisibilityLevel {
    PUBLIC("전체 공개"),
    LAB_ONLY("랩실 내 공개"),
    PRIVATE("비공개");

    private final String displayName;

    VisibilityLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 공개 범위 확인
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isLabOnly() {
        return this == LAB_ONLY;
    }

    public boolean isPrivate() {
        return this == PRIVATE;
    }
}
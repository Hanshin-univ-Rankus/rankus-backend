package org.univ.rankus.domain.model.user;

/**
 * 학생의 휴학/재학 상태를 나타내는 열거형
 */
public enum EnrollmentStatus {
    /**
     * 재학 상태
     */
    ENROLLED("재학"),

    /**
     * 휴학 상태
     */
    ON_LEAVE("휴학");

    private final String description;

    EnrollmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
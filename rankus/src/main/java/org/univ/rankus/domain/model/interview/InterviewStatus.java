package org.univ.rankus.domain.model.interview;

/**
 * 면접 상태 열거형
 * - 면접 설정의 전체적인 상태 관리
 * - 면접 활성화/비활성화/종료 상태 구분
 */
public enum InterviewStatus {
    /**
     * 비활성화 상태
     * - 면접 설정이 생성되었지만 아직 활성화되지 않음
     * - 지원자들이 지원할 수 없는 상태
     */
    INACTIVE("비활성화"),

    /**
     * 활성화 상태
     * - 면접이 활성화되어 지원자들이 지원할 수 있는 상태
     * - 면접 슬롯 예약 가능
     */
    ACTIVE("활성화"),

    /**
     * 종료 상태
     * - 면접이 완전히 종료된 상태
     * - 더 이상 지원이나 상태 변경이 불가능
     */
    CLOSED("종료");

    private final String description;

    InterviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 면접이 활성화되어 있는지 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 면접이 종료되었는지 확인
     */
    public boolean isClosed() {
        return this == CLOSED;
    }

    /**
     * 면접 상태 변경이 가능한지 확인
     */
    public boolean canChangeStatus() {
        return this != CLOSED;
    }
}
package org.univ.rankus.domain.model.interview;

/**
 * 면접 슬롯 상태 열거형
 * - 개별 면접 슬롯의 예약 상태 관리
 * - 슬롯별 예약 가능/불가능 상태 구분
 */
public enum SlotStatus {
    /**
     * 예약 가능 상태
     * - 슬롯에 여유 자리가 있어 예약 가능한 상태
     * - 정상적인 예약 처리가 가능
     */
    AVAILABLE("예약 가능"),

    /**
     * 예약 마감 상태
     * - 슬롯의 최대 지원자 수에 도달한 상태
     * - 더 이상 예약을 받을 수 없음
     */
    FULL("예약 마감"),

    /**
     * 취소된 상태
     * - 관리자에 의해 슬롯이 취소된 상태
     * - 예약을 받을 수 없으며, 기존 예약도 무효화됨
     */
    CANCELLED("취소됨");

    private final String description;

    SlotStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 예약 가능한 상태인지 확인
     */
    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    /**
     * 예약이 마감된 상태인지 확인
     */
    public boolean isFull() {
        return this == FULL;
    }

    /**
     * 취소된 상태인지 확인
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * 예약 처리가 가능한 상태인지 확인
     */
    public boolean canAcceptReservation() {
        return this == AVAILABLE;
    }
}
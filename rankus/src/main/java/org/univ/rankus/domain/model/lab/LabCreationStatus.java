package org.univ.rankus.domain.model.lab;

/**
 * 랩실 생성 신청 상태
 */
public enum LabCreationStatus {
    PENDING,   // 심사 대기
    APPROVED,  // 승인됨
    REJECTED   // 거절됨
}
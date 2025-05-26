package org.univ.rankus.domain.model.lab;

/**
 * 랩실 가입 신청 상태
 */
public enum ApplicationStatus {
    PENDING,   // 대기 중
    APPROVED,  // 승인됨
    REJECTED   // 거절됨
}

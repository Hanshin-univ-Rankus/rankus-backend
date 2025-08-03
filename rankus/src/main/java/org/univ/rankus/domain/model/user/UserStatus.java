package org.univ.rankus.domain.model.user;

public enum UserStatus {
    PENDING, // 이메일 인증 대기 중
    ACTIVE,  // 활성 상태
    DORMANT, // 휴면 상태
    BANNED   // 정지 상태
}

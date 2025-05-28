package org.univ.rankus.domain.model.user;

public enum Role {
    STUDENT,    // 일반 학생 (기본)
    LAB_LEADER, // 랩장
    LAB_MANAGER, // 랩 관리 담당
    LAB_MEMBER, // 랩원
    PROFESSOR,  // 담당 교수
    ADMIN       // 시스템 관리자
}

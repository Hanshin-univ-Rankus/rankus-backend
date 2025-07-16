package org.univ.rankus.adapter.in.web.lab.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.univ.rankus.domain.model.user.EnrollmentStatus;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * 랩원 상세 프로필 응답 DTO
 */
@Getter
@AllArgsConstructor
public class LabMemberDetailResponse {

    private Long id;
    private String name;
    private String studentNumber;
    private Integer grade;
    private Role role;
    private EnrollmentStatus enrollmentStatus;
    private LocalDateTime joinedAt;

    // 권한에 따라 노출되는 추가 정보
    private String email;
    private String phoneNumber;

    // 랩실 활동 통계
    private LabActivityStats activityStats;

    /**
     * 랩실 활동 통계 내부 클래스
     */
    @Getter
    @AllArgsConstructor
    public static class LabActivityStats {
        private int totalAttendanceSessions;
        private int attendedSessions;
        private int absentSessions;
        private int lateSessions;
        private double attendanceRate;
        private int scoreSubmissions;
        private int totalScore;
        private int rankingContribution;
    }

    /**
     * 기본 정보만 포함하는 생성자 (일반 랩원용)
     */
    public static LabMemberDetailResponse fromUserBasic(User user) {
        return new LabMemberDetailResponse(
                user.getId(),
                user.getName(),
                user.getStudentNumber(),
                user.getGrade(),
                user.getRole(),
                user.getEnrollmentStatus(),
                user.getCreatedAt(),
                null, // 이메일 비공개
                null, // 연락처 비공개
                null  // 활동 통계 비공개
        );
    }

    /**
     * 상세 정보를 포함하는 생성자 (관리자용)
     */
    public static LabMemberDetailResponse fromUserDetailed(User user, LabActivityStats stats) {
        return new LabMemberDetailResponse(
                user.getId(),
                user.getName(),
                user.getStudentNumber(),
                user.getGrade(),
                user.getRole(),
                user.getEnrollmentStatus(),
                user.getCreatedAt(),
                user.getEmail(),
                user.getPhoneNumber(),
                stats
        );
    }
}
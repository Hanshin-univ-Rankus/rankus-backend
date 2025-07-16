package org.univ.rankus.adapter.in.web.lab.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.univ.rankus.domain.model.user.EnrollmentStatus;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * 랩실 멤버 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
public class LabMemberResponse {

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

    /**
     * 기본 정보만 포함하는 생성자 (일반 랩원용)
     */
    public static LabMemberResponse fromUserBasic(User user) {
        return new LabMemberResponse(
                user.getId(),
                user.getName(),
                user.getStudentNumber(),
                user.getGrade(),
                user.getRole(),
                user.getEnrollmentStatus(),
                user.getCreatedAt(),
                null, // 이메일 비공개
                null  // 연락처 비공개
        );
    }

    /**
     * 상세 정보를 포함하는 생성자 (관리자용)
     */
    public static LabMemberResponse fromUserDetailed(User user) {
        return new LabMemberResponse(
                user.getId(),
                user.getName(),
                user.getStudentNumber(),
                user.getGrade(),
                user.getRole(),
                user.getEnrollmentStatus(),
                user.getCreatedAt(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
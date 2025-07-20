package org.univ.rankus.adapter.in.web.lab.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "랩실 멤버 응답 정보")
public class LabMemberResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "학번", example = "20240001")
    private String studentNumber;

    @Schema(description = "학년", example = "3")
    private Integer grade;

    @Schema(description = "사용자 역할")
    private Role role;

    @Schema(description = "재학 상태")
    private EnrollmentStatus enrollmentStatus;

    @Schema(description = "랩실 가입일시")
    private LocalDateTime joinedAt;

    // 권한에 따라 노출되는 추가 정보
    @Schema(description = "이메일 주소", nullable = true, example = "hong@example.com")
    private String email;

    @Schema(description = "전화번호", nullable = true, example = "010-1234-5678")
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
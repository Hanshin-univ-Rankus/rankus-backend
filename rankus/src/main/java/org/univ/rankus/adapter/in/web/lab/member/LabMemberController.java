package org.univ.rankus.adapter.in.web.lab.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;
import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberResponse;
import org.univ.rankus.application.port.in.lab.member.GetLabMembersQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.util.List;

/**
 * 랩실 멤버 조회 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/{labId}/members")
@RequiredArgsConstructor
@Tag(name = "Lab Members", description = "랩실 멤버 조회 API")
public class LabMemberController {

    private final GetLabMembersQuery getLabMembersQuery;

    @Operation(summary = "랩실 멤버 목록 조회", description = "특정 랩실의 모든 멤버를 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<List<LabMemberResponse>>> getLabMembers(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        List<User> members = getLabMembersQuery.getLabMembers(labId, requesterId);

        // 권한에 따라 응답 데이터 차등화
        List<LabMemberResponse> responses = members.stream()
                .map(member -> shouldShowDetailedInfo(userDetails, member)
                        ? LabMemberResponse.fromUserDetailed(member)
                        : LabMemberResponse.fromUserBasic(member))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses, "랩실 멤버 목록을 성공적으로 조회했습니다."));
    }

    @Operation(summary = "랩실 멤버 상세 조회", description = "특정 랩실의 멤버 상세 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<LabMemberResponse>> getLabMember(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        User member = getLabMembersQuery.getLabMember(labId, memberId, requesterId);

        // 권한에 따라 응답 데이터 차등화
        LabMemberResponse response = shouldShowDetailedInfo(userDetails, member)
                ? LabMemberResponse.fromUserDetailed(member)
                : LabMemberResponse.fromUserBasic(member);

        return ResponseEntity.ok(ApiResponse.success(response, "랩실 멤버 정보를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "랩실 멤버 상세 프로필 조회", description = "특정 랩실의 멤버 상세 프로필과 활동 통계를 조회합니다.")
    @GetMapping("/{memberId}/detail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<LabMemberDetailResponse>> getLabMemberDetail(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        LabMemberDetailResponse response = getLabMembersQuery.getLabMemberDetail(labId, memberId, requesterId);

        return ResponseEntity.ok(ApiResponse.success(response, "랩실 멤버 상세 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 상세 정보 노출 여부 결정
     * - ADMIN, PROFESSOR: 모든 정보 노출
     * - LAB_LEADER, LAB_MANAGER: 소속 랩실 멤버에 대해서만 상세 정보 노출
     */
    private boolean shouldShowDetailedInfo(CustomUserDetails userDetails, User member) {
        Role role = userDetails.getRole();

        // ADMIN, PROFESSOR는 모든 정보 조회 가능
        if (role == Role.ADMIN || role == Role.PROFESSOR) {
            return true;
        }

        // LAB_LEADER, LAB_MANAGER는 소속 랩실 멤버에 대해서만 상세 정보 조회 가능
        if (role == Role.LAB_LEADER || role == Role.LAB_MANAGER) {
            return userDetails.getLabId() != null
                    && member.getLab() != null
                    && userDetails.getLabId().equals(member.getLab().getId());
        }

        return false;
    }
}
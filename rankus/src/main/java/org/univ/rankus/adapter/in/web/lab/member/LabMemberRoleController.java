package org.univ.rankus.adapter.in.web.lab.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.lab.member.dto.RoleManagementRequest;
import org.univ.rankus.application.port.in.lab.member.ManageLabMemberRoleCommand;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

/**
 * 랩실 멤버 역할 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/{labId}/members")
@RequiredArgsConstructor
@Tag(name = "Lab Member Role Management", description = "랩실 멤버 역할 관리 API")
public class LabMemberRoleController {

    private final ManageLabMemberRoleCommand manageLabMemberRoleCommand;

    @Operation(summary = "랩매니저 권한 부여", description = "랩원을 랩매니저로 승급시킵니다.")
    @PostMapping("/promote-manager")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canManageLabMembers(#labId, authentication)")
    public ResponseEntity<Void> promoteToManager(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Valid @RequestBody RoleManagementRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long managerId = userDetails.getUserId();

        manageLabMemberRoleCommand.promoteToManager(labId, request.getMemberId(), managerId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "랩매니저 권한 회수", description = "랩매니저를 랩원으로 강등시킵니다.")
    @PostMapping("/demote-manager")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canManageLabMembers(#labId, authentication)")
    public ResponseEntity<Void> demoteToMember(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Valid @RequestBody RoleManagementRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long managerId = userDetails.getUserId();

        manageLabMemberRoleCommand.demoteToMember(labId, request.getMemberId(), managerId);

        return ResponseEntity.ok().build();
    }
}
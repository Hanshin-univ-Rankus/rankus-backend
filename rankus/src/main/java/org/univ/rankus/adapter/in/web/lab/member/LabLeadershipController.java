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
import org.univ.rankus.adapter.in.web.lab.member.dto.TransferLeadershipRequest;
import org.univ.rankus.application.port.in.lab.member.TransferLabLeadershipCommand;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

/**
 * 랩장 위임 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/{labId}")
@RequiredArgsConstructor
@Tag(name = "Lab Leadership", description = "랩장 위임 API")
public class LabLeadershipController {

    private final TransferLabLeadershipCommand transferLabLeadershipCommand;

    @Operation(summary = "랩장 위임", description = "현재 랩장이 다른 멤버에게 랩장 권한을 위임합니다.")
    @PostMapping("/transfer-leadership")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canTransferLabLeadership(#labId, authentication.principal.userId)")
    public ResponseEntity<Void> transferLeadership(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Valid @RequestBody TransferLeadershipRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentLeaderId = userDetails.getUserId();

        transferLabLeadershipCommand.transferLeadership(labId, request.getNewLeaderId(), currentLeaderId);

        return ResponseEntity.ok().build();
    }
}
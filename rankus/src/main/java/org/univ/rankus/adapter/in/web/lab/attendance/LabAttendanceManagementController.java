package org.univ.rankus.adapter.in.web.lab.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateRequest;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateResponse;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.LabAttendanceManagementResponse;
import org.univ.rankus.application.port.in.command.BulkUpdateAttendanceCommand;
import org.univ.rankus.application.port.in.query.GetLabAttendanceManagementQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

/**
 * 랩실 출석 관리 통합 뷰 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/{labId}/attendance")
@RequiredArgsConstructor
@Tag(name = "Lab Attendance Management", description = "랩실 출석 관리 통합 API")
public class LabAttendanceManagementController {

    private final GetLabAttendanceManagementQuery getLabAttendanceManagementQuery;
    private final BulkUpdateAttendanceCommand bulkUpdateAttendanceCommand;

    @Operation(summary = "랩실 출석 관리 통합 뷰", description = "특정 랩실의 출석 관리 통합 뷰를 조회합니다.")
    @GetMapping("/management")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<LabAttendanceManagementResponse>> getLabAttendanceManagement(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        LabAttendanceManagementResponse response = getLabAttendanceManagementQuery
                .getLabAttendanceManagement(labId, requesterId);

        return ResponseEntity.ok(ApiResponse.success(response, "랩실 출석 관리 정보를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "출석 일괄 수정", description = "여러 출석 기록을 한 번에 수정합니다.")
    @PutMapping("/bulk-update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canManageLabMembers(#labId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<BulkAttendanceUpdateResponse>> bulkUpdateAttendance(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Valid @RequestBody BulkAttendanceUpdateRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long managerId = userDetails.getUserId();

        BulkAttendanceUpdateResponse response = bulkUpdateAttendanceCommand
                .bulkUpdateAttendance(labId, request, managerId);

        return ResponseEntity.ok(ApiResponse.success(response, "출석 정보를 성공적으로 일괄 수정했습니다."));
    }
}
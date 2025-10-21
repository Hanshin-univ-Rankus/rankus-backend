package org.univ.rankus.adapter.in.web.lab.statistics;

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
import org.univ.rankus.adapter.in.web.lab.statistics.dto.LabComprehensiveStatsResponse;
import org.univ.rankus.application.port.in.lab.statistics.GetLabComprehensiveStatsQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

/**
 * 랩실 종합 통계 컨트롤러
 */
@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
@Tag(name = "Lab Comprehensive Statistics", description = "랩실 종합 통계 API")
public class LabComprehensiveStatsController {

    private final GetLabComprehensiveStatsQuery getLabComprehensiveStatsQuery;

    @Operation(summary = "전체 랩실 종합 통계", description = "모든 랩실의 종합 통계를 조회합니다. (ADMIN, PROFESSOR만 접근 가능)")
    @GetMapping("/comprehensive-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<LabComprehensiveStatsResponse>> getLabComprehensiveStats(
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        LabComprehensiveStatsResponse response = getLabComprehensiveStatsQuery
                .getLabComprehensiveStats(requesterId);

        return ResponseEntity.ok(ApiResponse.success(response, "전체 랩실 종합 통계를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "특정 랩실 상세 통계", description = "특정 랩실의 상세 통계를 조회합니다.")
    @GetMapping("/{labId}/detailed-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication)")
    public ResponseEntity<ApiResponse<LabComprehensiveStatsResponse.LabStats>> getLabDetailedStats(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        LabComprehensiveStatsResponse.LabStats response = getLabComprehensiveStatsQuery
                .getLabDetailedStats(labId, requesterId);

        return ResponseEntity.ok(ApiResponse.success(response, "랩실 상세 통계를 성공적으로 조회했습니다."));
    }
}
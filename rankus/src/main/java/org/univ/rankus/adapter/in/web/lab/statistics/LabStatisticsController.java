package org.univ.rankus.adapter.in.web.lab.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.lab.statistics.dto.LabComprehensiveStatsResponse;
import org.univ.rankus.application.port.in.lab.statistics.GetLabComprehensiveStatsQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

/**
 * 랩실 통계 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/statistics")
@RequiredArgsConstructor
@Tag(name = "Lab Statistics", description = "랩실 통계 API")
public class LabStatisticsController {

    private final GetLabComprehensiveStatsQuery getLabComprehensiveStatsQuery;

    @Operation(summary = "전체 랩실 종합 통계", description = "모든 랩실의 종합 통계를 조회합니다. (ADMIN, PROFESSOR만 접근 가능)")
    @GetMapping("/comprehensive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<LabComprehensiveStatsResponse> getLabComprehensiveStats(
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        LabComprehensiveStatsResponse response = getLabComprehensiveStatsQuery
                .getLabComprehensiveStats(requesterId);

        return ResponseEntity.ok(response);
    }
}
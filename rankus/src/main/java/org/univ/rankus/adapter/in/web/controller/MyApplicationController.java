package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabApplicationResponseDto;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;
import org.univ.rankus.domain.model.lab.application.LabApplication;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "MyApplications", description = "내 랩실 가입 신청 모아보기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/applications")
public class MyApplicationController {

    private final LabApplicationQueryUseCase queryUseCase;

    @Operation(
            summary = "내 가입 신청 목록 조회",
            description = "본인이 제출한 랩실 가입 신청 목록을 조회합니다. 랩실 ID 및 상태로 필터링할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LabApplicationResponseDto>>> listMyApplications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "특정 랩실로 필터링할 ID", example = "1")
            @RequestParam(name = "labId", required = false) @Positive Long labId,
            @Parameter(description = "상태로 필터링", schema = @Schema(allowableValues = {"PENDING", "APPROVED", "REJECTED"}))
            @RequestParam(name = "status", required = false) ApplicationStatus status
    ) {
        List<LabApplication> apps = queryUseCase.listMyApplications(principal.getUserId(), labId, status);
        List<LabApplicationResponseDto> dtos = apps.stream()
                .map(LabApplicationResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "내 신청 목록 조회 성공"));
    }
}


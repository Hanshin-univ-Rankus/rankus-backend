package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.LabApplicationRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabApplicationResponseDto;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "LabApplication", description = "랩실 가입 신청 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/applications")
public class LabApplicationController {

    private final LabApplicationCommandUseCase commandUseCase;
    private final LabApplicationQueryUseCase queryUseCase;

    @Operation(summary = "랩실 가입 신청", description = "로그인 사용자가 해당 랩실에 가입 신청을 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "가입 신청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabApplicationResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "입력 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "랩실 또는 사용자 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class})
                    )
            )
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> applyToLab(
            @PathVariable @Positive Long labId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid LabApplicationRequestDto dto
    ) {
        LabApplication created = commandUseCase.applyToLab(
                labId,
                principal.getUserId(),
                dto.interviewTime()
        );
        LabApplicationResponseDto respDto = LabApplicationResponseDto.from(created);
        ApiResponse<LabApplicationResponseDto> body = ApiResponse.created(respDto, "가입 신청 성공");
        URI location = URI.create("/api/labs/" + labId + "/applications/" + created.getId());
        return ResponseEntity.created(location)
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    @Operation(summary = "가입 신청 취소", description = "자신의 가입 신청서를 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "취소 성공", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            )
    })
    @DeleteMapping("/{appId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'DELETE')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> cancelApplication(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long appId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        commandUseCase.cancelApplication(appId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "신청서 목록 조회", description = "해당 랩실의 모든 가입 신청서를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabApplicationResponseDto.class})
                    )
            )
    })
    @GetMapping
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'LabApplication', 'VIEW')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<LabApplicationResponseDto>>> listApplications(
            @PathVariable @Positive Long labId
    ) {
        List<LabApplicationResponseDto> dtos = queryUseCase.listApplicationsByLab(labId).stream()
                .map(LabApplicationResponseDto::from)
                .collect(Collectors.toList());
        ApiResponse<List<LabApplicationResponseDto>> body = ApiResponse.success(dtos, "신청서 목록 조회 성공");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "신청서 단건 조회", description = "특정 가입 신청서를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabApplicationResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "신청서 정보 없음", content = @Content
            )
    })
    @GetMapping("/{appId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'VIEW')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> getApplication(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long appId
    ) {
        LabApplicationResponseDto dto = LabApplicationResponseDto.from(queryUseCase.getApplicationById(appId));
        ApiResponse<LabApplicationResponseDto> body = ApiResponse.success(dto, "신청서 조회 성공");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "신청서 승인", description = "가입 신청서를 승인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "승인 성공", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            )
    })
    @PutMapping("/{appId}/approve")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'APPROVE')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> approveApplication(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long appId
    ) {
        commandUseCase.approveApplication(appId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "신청서 거절", description = "가입 신청서를 거절합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "거절 성공", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            )
    })
    @PutMapping("/{appId}/reject")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'REJECT')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long appId
    ) {
        commandUseCase.rejectApplication(appId);
        return ResponseEntity.noContent().build();
    }
}
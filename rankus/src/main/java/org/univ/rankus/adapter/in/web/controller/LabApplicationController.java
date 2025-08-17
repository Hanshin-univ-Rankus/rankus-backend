package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.univ.rankus.adapter.in.web.dto.request.LabApplicationSlotRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabApplicationResponseDto;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.application.LabApplication;

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

    @Operation(
            summary = "랩실 가입 신청 (레거시)",
            description = """
                    로그인한 사용자가 특정 랩실에 가입 신청을 합니다. (구형 시간 기반 방식)
                                        
                    ## 기능 설명
                    - **레거시 API**: 면접 시스템 도입 전 호환성을 위해 유지
                    - 직접 면접 시간을 지정하여 신청
                    - 새로운 면접 시스템 사용 시 `/slot-based` 엔드포인트 권장
                                        
                    ## 신청 절차
                    1. 원하는 면접 시간 입력
                    2. 랩실 지원 신청서 제출
                    3. 랩장/매니저의 승인 대기
                    4. 승인 시 랩실 멤버로 등록
                                        
                    ## 주의사항
                    - 이미 해당 랩실에 신청한 경우 중복 신청 불가
                    - 면접 시간은 미래 시점이어야 함
                    """
    )
    @RequestBody(
            description = "랩실 지원 신청 정보",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LabApplicationRequestDto.class),
                    examples = @ExampleObject(
                            name = "지원 신청 예시",
                            summary = "일반적인 랩실 지원 신청",
                            value = """
                                    {
                                      "interviewTime": "2024-02-15T14:30:00"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "가입 신청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "가입 신청 성공",
                                              "data": {
                                                "id": 1,
                                                "userId": 1,
                                                "labId": 1,
                                                "interviewTime": "2024-02-15T14:30:00",
                                                "status": "PENDING",
                                                "appliedAt": "2024-01-15T10:30:00"
                                              },
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력 검증 실패 또는 면접 시스템 사용 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "검증 실패",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "이 랩실은 면접 시스템을 사용합니다. /slot-based 엔드포인트를 이용해주세요",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "랩실 또는 사용자 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "리소스 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 랩실을 찾을 수 없습니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복 신청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "중복 신청",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "이미 해당 랩실에 신청하셨습니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> applyToLab(
            @Parameter(description = "지원할 랩실의 ID", required = true, example = "1")
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
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @GetMapping
    @PreAuthorize("@labApplicationPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW')")
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
                            schema = @Schema(implementation = ApiResponse.class)
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

    // ====== 새로운 슬롯 기반 지원 API ======

    @Operation(summary = "랩실 가입 신청 (슬롯 기반)", description = "면접 슬롯을 선택하여 랩실에 가입 신청을 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "가입 신청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "입력 검증 실패 또는 슬롯 예약 불가",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "중복 신청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/slot-based")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> applyToLabWithSlot(
            @PathVariable @Positive Long labId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid LabApplicationSlotRequestDto dto
    ) {
        LabApplication created = commandUseCase.applyToLabWithSlot(
                labId,
                principal.getUserId(),
                dto.slotId()
        );
        LabApplicationResponseDto respDto = LabApplicationResponseDto.from(created);
        ApiResponse<LabApplicationResponseDto> body = ApiResponse.created(respDto, "가입 신청 성공");
        URI location = URI.create("/api/labs/" + labId + "/applications/" + created.getId());
        return ResponseEntity.created(location)
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}
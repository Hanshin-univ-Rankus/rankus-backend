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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.LabCreationRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabCreationRequestRejectDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabCreationRequestResponseDto;
import org.univ.rankus.application.port.in.command.LabCreationRequestCommandUseCase;
import org.univ.rankus.application.port.in.query.LabCreationRequestQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.LabCreationRequest;

import java.net.URI;
import java.util.List;

@Tag(name = "LabCreationRequest", description = "랩실 생성 신청 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lab-creation-requests")
public class LabCreationRequestController {

    private final LabCreationRequestCommandUseCase commandUseCase;
    private final LabCreationRequestQueryUseCase queryUseCase;

    @Operation(summary = "랩실 생성 신청", description = "로그인 사용자가 새로운 랩실 생성을 신청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "랩실 생성 신청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabCreationRequestResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "입력 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "중복 신청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class})
                    )
            )
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabCreationRequestResponseDto>> createLabCreationRequest(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid LabCreationRequestDto dto
    ) {
        LabCreationRequest created = commandUseCase.createLabCreationRequest(
                dto.requestedLabName(),
                dto.requestedCategory(),
                dto.requestedDescription(),
                principal.getUserId()
        );
        LabCreationRequestResponseDto responseDto = LabCreationRequestResponseDto.from(created);
        ApiResponse<LabCreationRequestResponseDto> body = ApiResponse.created(responseDto, "랩실 생성 신청 성공");
        URI location = URI.create("/api/lab-creation-requests/" + created.getId());
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "내 랩실 생성 신청 목록 조회", description = "현재 로그인한 사용자의 랩실 생성 신청 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabCreationRequestResponseDto.class})
                    )
            )
    })
    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LabCreationRequestResponseDto>>> getCurrentUserLabCreationRequests(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        List<LabCreationRequest> requests = queryUseCase.getLabCreationRequestsByRequester(principal.getUserId());
        List<LabCreationRequestResponseDto> dtos = LabCreationRequestResponseDto.fromList(requests);
        ApiResponse<List<LabCreationRequestResponseDto>> body = ApiResponse.success(dtos, "내 신청 목록 조회 성공");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "랩실 생성 신청 단건 조회", description = "특정 랩실 생성 신청을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabCreationRequestResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "신청 정보 없음", content = @Content
            )
    })
    @GetMapping("/{requestId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'VIEW')")
    public ResponseEntity<ApiResponse<LabCreationRequestResponseDto>> getLabCreationRequest(
            @PathVariable @Positive Long requestId
    ) {
        LabCreationRequest request = queryUseCase.getLabCreationRequestById(requestId);
        LabCreationRequestResponseDto dto = LabCreationRequestResponseDto.from(request);
        ApiResponse<LabCreationRequestResponseDto> body = ApiResponse.success(dto, "랩실 생성 신청 조회 성공");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "랩실 생성 신청 취소", description = "자신의 랩실 생성 신청을 취소합니다. (PENDING 상태에서만 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "취소 성공", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "취소할 수 없는 상태", content = @Content
            )
    })
    @DeleteMapping("/{requestId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'DELETE')")
    public ResponseEntity<Void> cancelLabCreationRequest(
            @PathVariable @Positive Long requestId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        commandUseCase.cancelLabCreationRequest(requestId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    // === 관리자 전용 API ===

    @Operation(summary = "[관리자] 모든 랩실 생성 신청 조회", description = "관리자가 모든 랩실 생성 신청 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabCreationRequestResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            )
    })
    @GetMapping("/admin/all")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<LabCreationRequestResponseDto>>> getAllLabCreationRequests() {
        List<LabCreationRequest> requests = queryUseCase.getAllLabCreationRequests();
        List<LabCreationRequestResponseDto> dtos = LabCreationRequestResponseDto.fromList(requests);
        ApiResponse<List<LabCreationRequestResponseDto>> body = ApiResponse.success(dtos, "전체 신청 목록 조회 성공");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "[관리자] 대기 중인 랩실 생성 신청 조회", description = "관리자가 승인 대기 중인 랩실 생성 신청 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabCreationRequestResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            )
    })
    @GetMapping("/admin/pending")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<LabCreationRequestResponseDto>>> getPendingLabCreationRequests() {
        List<LabCreationRequest> requests = queryUseCase.getPendingLabCreationRequests();
        List<LabCreationRequestResponseDto> dtos = LabCreationRequestResponseDto.fromList(requests);
        ApiResponse<List<LabCreationRequestResponseDto>> body = ApiResponse.success(dtos, "대기 중인 신청 목록 조회 성공");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "[관리자] 랩실 생성 신청 승인", description = "관리자가 랩실 생성 신청을 승인합니다. 승인 시 실제 랩실이 생성되고 신청자가 랩장으로 할당됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "승인 성공", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "승인할 수 없는 상태", content = @Content
            )
    })
    @PutMapping("/{requestId}/approve")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'APPROVE')")
    public ResponseEntity<Void> approveLabCreationRequest(
            @PathVariable @Positive Long requestId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        commandUseCase.approveLabCreationRequest(requestId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[관리자] 랩실 생성 신청 거절", description = "관리자가 랩실 생성 신청을 거절합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "거절 성공", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "거절할 수 없는 상태", content = @Content
            )
    })
    @PutMapping("/{requestId}/reject")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'REJECT')")
    public ResponseEntity<Void> rejectLabCreationRequest(
            @PathVariable @Positive Long requestId,
            @RequestBody @Valid LabCreationRequestRejectDto dto,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        commandUseCase.rejectLabCreationRequest(requestId, principal.getUserId(), dto.rejectionReason());
        return ResponseEntity.noContent().build();
    }
}
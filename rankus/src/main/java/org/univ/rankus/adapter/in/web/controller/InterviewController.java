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
import org.univ.rankus.adapter.in.web.dto.request.*;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.InterviewResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.InterviewSlotResponseDto;
import org.univ.rankus.application.port.in.command.InterviewCommandUseCase;
import org.univ.rankus.application.port.in.query.InterviewQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;

import java.net.URI;
import java.util.List;

/**
 * Interview 관리 API Controller
 * - 면접 생성, 활성화, 슬롯 관리 등
 * - 랩장/매니저 권한 필요
 */
@Tag(name = "Interview", description = "면접 관리 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/interviews")
@SecurityRequirement(name = "bearerAuth")
public class InterviewController {

    private final InterviewCommandUseCase commandUseCase;
    private final InterviewQueryUseCase queryUseCase;

    @Operation(summary = "면접 생성", description = "랩실에 새로운 면접을 생성합니다. (랩장/매니저 권한 필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "면접 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, InterviewResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "입력 검증 실패 또는 이미 활성화된 면접 존재",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> createInterview(
            @PathVariable @Positive Long labId,
            @Valid @RequestBody InterviewCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Interview interview = commandUseCase.createInterview(
                labId,
                request.getStartDate(),
                request.getEndDate(),
                request.getDurationMinutes(),
                request.getMaxApplicantsPerSlot()
        );

        InterviewResponseDto responseDto = InterviewResponseDto.from(interview);
        URI location = URI.create("/api/labs/" + labId + "/interviews/" + interview.getId());
        
        return ResponseEntity.created(location)
                .body(ApiResponse.created(responseDto));
    }

    @Operation(summary = "랩실 면접 목록 조회", description = "특정 랩실의 모든 면접을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewResponseDto>>> getInterviewsByLab(
            @PathVariable @Positive Long labId) {

        List<Interview> interviews = queryUseCase.getInterviewsByLabId(labId);
        List<InterviewResponseDto> responseDtos = InterviewResponseDto.fromList(interviews);

        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }

    @Operation(summary = "면접 상세 조회", description = "특정 면접의 상세 정보를 조회합니다.")
    @GetMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> getInterview(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId) {

        Interview interview = queryUseCase.getInterviewById(interviewId);
        InterviewResponseDto responseDto = InterviewResponseDto.from(interview);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 활성화", description = "면접을 활성화하여 지원을 받을 수 있도록 합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/activate")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> activateInterview(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Interview interview = commandUseCase.activateInterview(interviewId);
        InterviewResponseDto responseDto = InterviewResponseDto.from(interview);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 비활성화", description = "면접을 비활성화하여 지원을 중단합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/deactivate")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> deactivateInterview(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Interview interview = commandUseCase.deactivateInterview(interviewId);
        InterviewResponseDto responseDto = InterviewResponseDto.from(interview);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 종료", description = "면접을 종료합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/close")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> closeInterview(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Interview interview = commandUseCase.closeInterview(interviewId);
        InterviewResponseDto responseDto = InterviewResponseDto.from(interview);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 수정", description = "면접 설정을 수정합니다. (랩장/매니저 권한 필요)")
    @PutMapping("/{interviewId}")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> updateInterview(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @Valid @RequestBody InterviewUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Interview interview = commandUseCase.updateInterview(
                interviewId,
                request.getStartDate(),
                request.getEndDate(),
                request.getDurationMinutes(),
                request.getMaxApplicantsPerSlot()
        );

        InterviewResponseDto responseDto = InterviewResponseDto.from(interview);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 삭제", description = "면접을 삭제합니다. (랩장/매니저 권한 필요)")
    @DeleteMapping("/{interviewId}")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        commandUseCase.deleteInterview(interviewId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ====== 면접 슬롯 관련 API ======

    @Operation(summary = "면접 슬롯 생성", description = "면접에 새로운 시간 슬롯을 생성합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/slots")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewSlotResponseDto>> createInterviewSlot(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @Valid @RequestBody InterviewSlotCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        InterviewSlot slot = commandUseCase.createInterviewSlot(
                interviewId,
                request.getStartTime(),
                request.getEndTime(),
                request.getMaxApplicants()
        );

        InterviewSlotResponseDto responseDto = InterviewSlotResponseDto.from(slot);
        URI location = URI.create("/api/labs/" + labId + "/interviews/" + interviewId + "/slots/" + slot.getId());
        
        return ResponseEntity.created(location)
                .body(ApiResponse.created(responseDto));
    }

    @Operation(summary = "면접 슬롯 일괄 생성", description = "면접에 여러 시간 슬롯을 한 번에 생성합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/slots/batch")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<List<InterviewSlotResponseDto>>> createMultipleInterviewSlots(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @Valid @RequestBody InterviewSlotBatchCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<InterviewCommandUseCase.SlotCreationInfo> slotInfos = request.getSlots().stream()
                .map(dto -> new InterviewCommandUseCase.SlotCreationInfo(
                        dto.getStartTime(),
                        dto.getEndTime(),
                        dto.getMaxApplicants()
                ))
                .toList();

        List<InterviewSlot> slots = commandUseCase.createMultipleInterviewSlots(interviewId, slotInfos);
        List<InterviewSlotResponseDto> responseDtos = InterviewSlotResponseDto.fromList(slots);

        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }

    @Operation(summary = "면접 슬롯 목록 조회", description = "특정 면접의 모든 슬롯을 조회합니다.")
    @GetMapping("/{interviewId}/slots")
    public ResponseEntity<ApiResponse<List<InterviewSlotResponseDto>>> getInterviewSlots(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId) {

        List<InterviewSlot> slots = queryUseCase.getSlotsByInterviewIdOrderByTime(interviewId);
        List<InterviewSlotResponseDto> responseDtos = InterviewSlotResponseDto.fromList(slots);

        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }

    @Operation(summary = "예약 가능한 슬롯 조회", description = "특정 면접의 예약 가능한 슬롯만 조회합니다.")
    @GetMapping("/{interviewId}/slots/available")
    public ResponseEntity<ApiResponse<List<InterviewSlotResponseDto>>> getAvailableSlots(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId) {

        List<InterviewSlot> slots = queryUseCase.getAvailableSlotsByInterviewId(interviewId);
        List<InterviewSlotResponseDto> responseDtos = InterviewSlotResponseDto.fromList(slots);

        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }

    @Operation(summary = "면접 슬롯 취소", description = "면접 슬롯을 취소합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/slots/{slotId}/cancel")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewSlotResponseDto>> cancelInterviewSlot(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @PathVariable @Positive Long slotId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        InterviewSlot slot = commandUseCase.cancelInterviewSlot(slotId);
        InterviewSlotResponseDto responseDto = InterviewSlotResponseDto.from(slot);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 슬롯 재활성화", description = "취소된 면접 슬롯을 재활성화합니다. (랩장/매니저 권한 필요)")
    @PostMapping("/{interviewId}/slots/{slotId}/reactivate")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<InterviewSlotResponseDto>> reactivateInterviewSlot(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @PathVariable @Positive Long slotId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        InterviewSlot slot = commandUseCase.reactivateInterviewSlot(slotId);
        InterviewSlotResponseDto responseDto = InterviewSlotResponseDto.from(slot);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "면접 슬롯 삭제", description = "면접 슬롯을 삭제합니다. (랩장/매니저 권한 필요)")
    @DeleteMapping("/{interviewId}/slots/{slotId}")
    @PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<Void>> deleteInterviewSlot(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @PathVariable @Positive Long slotId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        commandUseCase.deleteInterviewSlot(slotId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
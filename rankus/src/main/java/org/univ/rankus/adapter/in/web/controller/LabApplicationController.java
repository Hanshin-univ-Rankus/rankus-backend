package org.univ.rankus.adapter.in.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.LabApplicationRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabApplicationResponseDto;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/applications")
public class LabApplicationController {

    private final LabApplicationCommandUseCase commandUseCase;
    private final LabApplicationQueryUseCase queryUseCase;

    /**
     * POST /api/v1/labs/{labId}/applications
     * - 랩 가입 신청 (로그인 사용자)
     * - 201 Created + Location 헤더 + ApiResponse<LabApplicationResponseDto>
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> applyToLab(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid LabApplicationRequestDto dto
    ) {
        LabApplication created = commandUseCase.applyToLab(
                labId,
                principal.getUserId(),
                dto.interviewTime()
        );

        LabApplicationResponseDto respDto = LabApplicationResponseDto.from(created);

        ApiResponse<LabApplicationResponseDto> body = ApiResponse.<LabApplicationResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("가입 신청 성공")
                .data(respDto)
                .build();

        URI location = URI.create("/api/v1/labs/" + labId + "/applications/" + created.getId());
        return ResponseEntity
                .created(location)
                .body(body);
    }

    /**
     * DELETE /api/v1/labs/{labId}/applications/{appId}
     * - 자신의 신청서 취소
     * - 204 No Content
     */
    @DeleteMapping("/{appId}")
    @PreAuthorize("hasPermission(#appId, 'LabApplication', 'cancel')")
    public ResponseEntity<Void> cancelApplication(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "신청서 ID는 양수여야 합니다.") Long appId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        commandUseCase.cancelApplication(appId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/labs/{labId}/applications
     * - 특정 랩실의 모든 신청서 조회 (리더/매니저)
     * - 200 OK + ApiResponse<List<LabApplicationResponseDto>>
     */
    @GetMapping
    @PreAuthorize("hasPermission(#labId, 'LabApplication', 'view')")
    public ResponseEntity<ApiResponse<List<LabApplicationResponseDto>>> listApplications(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId
    ) {
        List<LabApplication> apps = queryUseCase.listApplicationsByLab(labId);
        List<LabApplicationResponseDto> dtos = apps.stream()
                .map(LabApplicationResponseDto::from)
                .collect(Collectors.toList());

        ApiResponse<List<LabApplicationResponseDto>> body = ApiResponse.<List<LabApplicationResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("신청서 목록 조회 성공")
                .data(dtos)
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/v1/labs/{labId}/applications/{appId}
     * - 단일 신청서 조회 (리더/매니저)
     * - 200 OK + ApiResponse<LabApplicationResponseDto>
     */
    @GetMapping("/{appId}")
    @PreAuthorize("hasPermission(#labId, 'LabApplication', 'view')")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> getApplication(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "신청서 ID는 양수여야 합니다.") Long appId
    ) {
        LabApplication app = queryUseCase.getApplicationById(appId);
        LabApplicationResponseDto dto = LabApplicationResponseDto.from(app);

        ApiResponse<LabApplicationResponseDto> body = ApiResponse.<LabApplicationResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("신청서 조회 성공")
                .data(dto)
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * PUT /api/v1/labs/{labId}/applications/{appId}/approve
     * - 신청서 승인 (리더/매니저)
     * - 204 No Content
     */
    @PutMapping("/{appId}/approve")
    @PreAuthorize("hasPermission(#appId, 'LabApplication', 'approve')")
    public ResponseEntity<Void> approveApplication(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "신청서 ID는 양수여야 합니다.") Long appId
    ) {
        commandUseCase.approveApplication(appId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/labs/{labId}/applications/{appId}/reject
     * - 신청서 거절 (리더/매니저)
     * - 204 No Content
     */
    @PutMapping("/{appId}/reject")
    @PreAuthorize("hasPermission(#appId, 'LabApplication', 'reject')")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "신청서 ID는 양수여야 합니다.") Long appId
    ) {
        commandUseCase.rejectApplication(appId);
        return ResponseEntity.noContent().build();
    }
}
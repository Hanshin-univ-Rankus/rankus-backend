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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.LabNoticeCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabNoticeUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabNoticeResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.PageResponse;
import org.univ.rankus.application.port.in.command.LabNoticeCommandUseCase;
import org.univ.rankus.application.port.in.query.LabNoticeQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/notices")
@Tag(name = "LabNotice", description = "랩실 공지사항 API")
public class LabNoticeController {

    private final LabNoticeQueryUseCase labNoticeQueryUseCase;
    private final LabNoticeCommandUseCase labNoticeCommandUseCase;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 공지사항 목록 조회", description = "특정 랩실의 공지사항을 페이징하여 조회합니다. 고정 공지 → 최신순으로 정렬됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "공지사항 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "조회 권한 없음", content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<PageResponse<LabNoticeResponseDto>>> getLabNotices(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<LabNotice> noticePage = labNoticeQueryUseCase.getNoticesByLabId(labId, pageable);
        PageResponse<LabNoticeResponseDto> pageResponse = PageResponse.of(noticePage, LabNoticeResponseDto::from);

        return ResponseEntity.ok(ApiResponse.success(pageResponse, "공지사항 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 공지사항 전체 목록 조회", description = "특정 랩실의 모든 공지사항을 조회합니다.")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<List<LabNoticeResponseDto>>> getAllLabNotices(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId
    ) {
        List<LabNotice> notices = labNoticeQueryUseCase.getNoticesByLabId(labId);
        List<LabNoticeResponseDto> responseList = LabNoticeResponseDto.fromList(notices);

        return ResponseEntity.ok(ApiResponse.success(responseList, "전체 공지사항 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "특정 타입 공지사항 조회", description = "특정 랩실의 특정 타입(일반/긴급) 공지사항을 조회합니다.")
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<List<LabNoticeResponseDto>>> getNoticesByType(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable NoticeType type
    ) {
        List<LabNotice> notices = labNoticeQueryUseCase.getNoticesByLabIdAndType(labId, type);
        List<LabNoticeResponseDto> responseList = LabNoticeResponseDto.fromList(notices);

        String message = type == NoticeType.URGENT ? "긴급 공지사항 조회 성공" : "일반 공지사항 조회 성공";
        return ResponseEntity.ok(ApiResponse.success(responseList, message));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "고정 공지사항 조회", description = "특정 랩실의 고정된 공지사항만 조회합니다.")
    @GetMapping("/pinned")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<List<LabNoticeResponseDto>>> getPinnedNotices(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId
    ) {
        List<LabNotice> notices = labNoticeQueryUseCase.getPinnedNoticesByLabId(labId);
        List<LabNoticeResponseDto> responseList = LabNoticeResponseDto.fromList(notices);

        return ResponseEntity.ok(ApiResponse.success(responseList, "고정 공지사항 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다.")
    @GetMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'VIEW')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> getNotice(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "공지사항 ID는 양수여야 합니다") Long noticeId
    ) {
        LabNotice notice = labNoticeQueryUseCase.getNoticeById(noticeId);
        LabNoticeResponseDto responseDto = LabNoticeResponseDto.from(notice);

        return ResponseEntity.ok(ApiResponse.success(responseDto, "공지사항 상세 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "공지사항 생성", description = "새로운 공지사항을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "공지사항 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "생성 권한 없음", content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_NOTICES')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> createNotice(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @Valid @RequestBody LabNoticeCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LabNotice notice = labNoticeCommandUseCase.createNotice(
                request.getTitle(),
                request.getContent(),
                userDetails.getUserId(),
                labId,
                request.getType(),
                request.isPinned()
        );

        LabNoticeResponseDto responseDto = LabNoticeResponseDto.from(notice);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(responseDto, "공지사항이 성공적으로 생성되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    @PutMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'UPDATE')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> updateNotice(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "공지사항 ID는 양수여야 합니다") Long noticeId,
            @Valid @RequestBody LabNoticeUpdateRequestDto request
    ) {
        LabNotice updatedNotice = labNoticeCommandUseCase.updateNotice(
                noticeId,
                request.getTitle(),
                request.getContent(),
                request.getType(),
                request.isPinned()
        );

        LabNoticeResponseDto responseDto = LabNoticeResponseDto.from(updatedNotice);

        return ResponseEntity.ok(ApiResponse.success(responseDto, "공지사항이 성공적으로 수정되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "공지사항 고정 토글", description = "공지사항의 고정 상태를 토글합니다.")
    @PatchMapping("/{noticeId}/pin")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'UPDATE')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> togglePinNotice(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "공지사항 ID는 양수여야 합니다") Long noticeId
    ) {
        LabNotice updatedNotice = labNoticeCommandUseCase.togglePinNotice(noticeId);
        LabNoticeResponseDto responseDto = LabNoticeResponseDto.from(updatedNotice);

        String message = updatedNotice.isPinned() ? "공지사항이 고정되었습니다" : "공지사항 고정이 해제되었습니다";

        return ResponseEntity.ok(ApiResponse.success(responseDto, message));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "공지사항 ID는 양수여야 합니다") Long noticeId
    ) {
        labNoticeCommandUseCase.deleteNotice(noticeId);

        return ResponseEntity.ok(ApiResponse.deleted("공지사항이 성공적으로 삭제되었습니다"));
    }
}
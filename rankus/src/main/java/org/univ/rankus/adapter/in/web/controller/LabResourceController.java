package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.adapter.in.web.dto.request.LabResourceCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabResourceUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.PageResponse;
import org.univ.rankus.application.port.in.command.LabResourceCommandUseCase;
import org.univ.rankus.application.port.in.query.LabResourceQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 랩실 자료실 Controller
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/resources")
@Tag(name = "LabResource", description = "랩실 자료실 API")
public class LabResourceController {

    private final LabResourceQueryUseCase labResourceQueryUseCase;
    private final LabResourceCommandUseCase labResourceCommandUseCase;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 목록 조회", description = "특정 랩실의 자료를 페이징하여 조회합니다. 카테고리 및 검색어로 필터링 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "자료 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "조회 권한 없음", content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LabResourceResponseDto>>> getLabResources(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @RequestParam(required = false) @Parameter(description = "자료 카테고리") ResourceCategory category,
            @RequestParam(required = false) @Parameter(description = "검색어") String search,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Page<LabResourceResponseDto> resourcePage = labResourceQueryUseCase.getLabResources(
                labId, category, search, pageable, currentUser.getUserId());
        PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage, dto -> dto);

        return ResponseEntity.ok(ApiResponse.success(pageResponse, "자료 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 전체 목록 조회", description = "특정 랩실의 모든 자료를 조회합니다.")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LabResourceResponseDto>>> getAllLabResources(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<LabResourceResponseDto> resources = labResourceQueryUseCase.getAllLabResources(labId, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(resources, "전체 자료 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 상세 조회", description = "특정 랩실 자료의 상세 정보를 조회합니다.")
    @GetMapping("/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabResourceResponseDto>> getLabResource(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "자료 ID는 양수여야 합니다") Long resourceId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        LabResourceResponseDto resource = labResourceQueryUseCase.getLabResource(resourceId, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(resource, "자료 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 생성", description = "새로운 랩실 자료를 업로드합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "자료 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "생성 권한 없음", content = @Content
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabResourceResponseDto>> createLabResource(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @Valid @RequestPart @Parameter(description = "자료 생성 요청") LabResourceCreateRequestDto request,
            @RequestPart @Parameter(description = "업로드할 파일") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        LabResourceResponseDto response = labResourceCommandUseCase.createLabResource(
                labId, request, file, currentUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "자료 생성 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 수정", description = "기존 랩실 자료의 정보를 수정합니다.")
    @PutMapping("/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabResourceResponseDto>> updateLabResource(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "자료 ID는 양수여야 합니다") Long resourceId,
            @Valid @RequestBody LabResourceUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        LabResourceResponseDto response = labResourceCommandUseCase.updateLabResource(
                resourceId, request, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(response, "자료 수정 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 삭제", description = "랩실 자료를 삭제합니다.")
    @DeleteMapping("/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteLabResource(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "자료 ID는 양수여야 합니다") Long resourceId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        labResourceCommandUseCase.deleteLabResource(resourceId, currentUser.getUserId());

        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "자료 파일 다운로드", description = "랩실 자료 파일을 다운로드합니다. 다운로드 횟수가 증가합니다.")
    @GetMapping("/{resourceId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadLabResource(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "자료 ID는 양수여야 합니다") Long resourceId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        // 먼저 자료 정보를 가져와서 파일명을 확인
        LabResourceResponseDto resource = labResourceQueryUseCase.getLabResource(resourceId, currentUser.getUserId());
        Resource fileResource = labResourceQueryUseCase.downloadLabResource(resourceId, currentUser.getUserId());

        // 파일명 인코딩 (한글 지원)
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(resource.getFileName(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            encodedFileName = resource.getFileName();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(fileResource);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "자료 공개 여부 토글", description = "자료의 공개/비공개 상태를 토글합니다.")
    @PatchMapping("/{resourceId}/toggle-public")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabResourceResponseDto>> toggleResourcePublic(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "자료 ID는 양수여야 합니다") Long resourceId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        LabResourceResponseDto response = labResourceCommandUseCase.toggleResourcePublic(
                resourceId, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(response, "자료 공개 상태 변경 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "자료 카테고리 목록 조회", description = "사용 가능한 자료 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ResourceCategory>>> getResourceCategories() {
        List<ResourceCategory> categories = labResourceQueryUseCase.getResourceCategories();

        return ResponseEntity.ok(ApiResponse.success(categories, "카테고리 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 자료 통계 조회", description = "랩실의 자료 통계 정보를 조회합니다.")
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabResourceQueryUseCase.LabResourceStatsDto>> getLabResourceStats(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        LabResourceQueryUseCase.LabResourceStatsDto stats = labResourceQueryUseCase.getLabResourceStats(
                labId, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(stats, "자료 통계 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "사용자별 업로드 자료 조회", description = "특정 사용자가 업로드한 자료 목록을 조회합니다.")
    @GetMapping("/my-uploads")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LabResourceResponseDto>>> getMyUploads(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Page<LabResourceResponseDto> resourcePage = labResourceQueryUseCase.getResourcesByUploader(
                currentUser.getUserId(), pageable);
        PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage, dto -> dto);

        return ResponseEntity.ok(ApiResponse.success(pageResponse, "내 업로드 자료 조회 성공"));
    }
}
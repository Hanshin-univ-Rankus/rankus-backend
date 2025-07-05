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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.LabImageRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabImageResponseDto;
import org.univ.rankus.application.port.in.command.LabImageCommandUseCase;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.domain.model.lab.core.LabImage;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LabImage 관련 REST API 컨트롤러
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/images")
@Tag(name = "LabImage", description = "랩실 이미지 관리 API")
public class LabImageController {

    private final LabImageCommandUseCase commandUseCase;
    private final LabImageQueryUseCase queryUseCase;

    /**
     * POST /api/labs/{labId}/images
     * - 새로운 이미지를 등록합니다. (랩 리더·매니저만)
     * - 201 Created + Location 헤더 + ApiResponse<LabImageResponseDto> 반환
     */
    @PostMapping
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'LabImage', 'CREATE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "이미지 등록", description = "새로운 랩실 이미지를 등록합니다. (랩 리더·매니저만)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "이미지 등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabImageResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(allOf = {ApiResponse.class}))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(allOf = {ApiResponse.class}))
            )
    })
    public ResponseEntity<ApiResponse<LabImageResponseDto>> addImage(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @RequestBody @Valid LabImageRequestDto requestDto
    ) {
        // 1) 이미지 등록
        LabImage saved = commandUseCase.addImage(
                labId,
                requestDto.getImageUrl(),
                requestDto.getType()
        );

        // 2) DTO 변환
        LabImageResponseDto dto = LabImageResponseDto.from(
                saved.getId(),
                saved.getLab().getId(),
                saved.getImageUrl(),
                saved.getType()
        );

        // 3) ApiResponse 래핑
        ApiResponse<LabImageResponseDto> body = ApiResponse.created(dto, "이미지 등록 성공");

        // 4) Location 헤더
        URI location = URI.create("/api/labs/" + labId + "/images/" + saved.getId());

        return ResponseEntity
                .created(location)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    /**
     * GET /api/labs/{labId}/images
     * - 특정 랩실에 속한 모든 이미지를 조회합니다. (모두 접근 가능)
     * - 200 OK + ApiResponse<List<LabImageResponseDto>> 반환
     */
    @GetMapping
    @Operation(summary = "이미지 목록 조회", description = "특정 랩실에 속한 모든 이미지를 조회합니다. (모두 접근 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이미지 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, List.class, LabImageResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(allOf = {ApiResponse.class}))
            )
    })
    public ResponseEntity<ApiResponse<List<LabImageResponseDto>>> listImages(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId
    ) {
        List<LabImage> images = queryUseCase.listImagesByLab(labId);

        List<LabImageResponseDto> dtos = images.stream()
                .map(img -> LabImageResponseDto.from(
                        img.getId(),
                        img.getLab().getId(),
                        img.getImageUrl(),
                        img.getType()
                ))
                .collect(Collectors.toList());

        ApiResponse<List<LabImageResponseDto>> body = ApiResponse.success(dtos, "이미지 목록 조회 성공");

        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/labs/{labId}/images/{imageId}
     * - 200 OK + ApiResponse<LabImageResponseDto> 반환 (모두 접근 가능)
     */
    @GetMapping("/{imageId}")
    @Operation(summary = "이미지 조회", description = "특정 랩실 이미지 정보를 조회합니다. (모두 접근 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이미지 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(allOf = {ApiResponse.class, LabImageResponseDto.class})
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(allOf = {ApiResponse.class}))
            )
    })
    public ResponseEntity<ApiResponse<LabImageResponseDto>> getImage(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "이미지 ID는 양수여야 합니다.") Long imageId
    ) {
        LabImage img = queryUseCase.getImageById(imageId);

        LabImageResponseDto dto = LabImageResponseDto.from(
                img.getId(),
                img.getLab().getId(),
                img.getImageUrl(),
                img.getType()
        );

        ApiResponse<LabImageResponseDto> body = ApiResponse.success(dto, "이미지 조회 성공");

        return ResponseEntity.ok(body);
    }

    /**
     * DELETE /api/labs/{labId}/images/{imageId}
     * - 특정 이미지를 삭제합니다. (랩 리더·매니저만)
     * - 204 No Content 반환
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #imageId, 'LabImage', 'DELETE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "이미지 삭제", description = "랩실 이미지를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    })
    public ResponseEntity<Void> deleteImage(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "이미지 ID는 양수여야 합니다.") Long imageId
    ) {
        commandUseCase.deleteImage(labId, imageId);
        return ResponseEntity.noContent().build();
    }
}
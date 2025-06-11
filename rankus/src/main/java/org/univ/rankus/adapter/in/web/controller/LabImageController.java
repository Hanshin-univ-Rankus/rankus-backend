package org.univ.rankus.adapter.in.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.LabImageRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabImageResponseDto;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.application.port.in.command.LabImageCommandUseCase;
import org.univ.rankus.domain.model.lab.LabImage;

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
public class LabImageController {

    private final LabImageCommandUseCase commandUseCase;
    private final LabImageQueryUseCase queryUseCase;

    /**
     * POST /api/v1/labs/{labId}/images
     * - 새로운 이미지를 등록합니다. (랩 리더·매니저만)
     * - 201 Created + Location 헤더 + ApiResponse<LabImageResponseDto> 반환
     */
    @PostMapping
    @PreAuthorize("hasPermission(#labId, 'LabImage', 'create')")
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
        ApiResponse<LabImageResponseDto> body = ApiResponse.<LabImageResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("이미지 등록 성공")
                .data(dto)
                .build();

        // 4) Location 헤더
        URI location = URI.create("/api/v1/labs/" + labId + "/images/" + saved.getId());

        return ResponseEntity
                .created(location)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    /**
     * GET /api/v1/labs/{labId}/images
     * - 특정 랩실에 속한 모든 이미지를 조회합니다. (모두 접근 가능)
     * - 200 OK + ApiResponse<List<LabImageResponseDto>> 반환
     */
    @GetMapping
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

        ApiResponse<List<LabImageResponseDto>> body = ApiResponse.<List<LabImageResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("이미지 목록 조회 성공")
                .data(dtos)
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/v1/labs/{labId}/images/{imageId}
     * - 200 OK + ApiResponse<LabImageResponseDto> 반환 (모두 접근 가능)
     */
    @GetMapping("/{imageId}")
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

        ApiResponse<LabImageResponseDto> body = ApiResponse.<LabImageResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("이미지 조회 성공")
                .data(dto)
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * DELETE /api/v1/labs/{labId}/images/{imageId}
     * - 특정 이미지를 삭제합니다. (랩 리더·매니저만)
     * - 204 No Content 반환
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasPermission(#imageId, 'LabImage', 'delete')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId,
            @PathVariable @Positive(message = "이미지 ID는 양수여야 합니다.") Long imageId
    ) {
        commandUseCase.deleteImage(labId, imageId);
        return ResponseEntity.noContent().build();
    }
}
package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.application.port.in.LabImageUseCase;
import org.univ.rankus.adapter.in.web.dto.LabImageRequestDto;
import org.univ.rankus.adapter.in.web.dto.LabImageResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/labs/{labId}/images")
@Tag(name = "LabImage", description = "랩실 이미지 관리 API")
public class LabImageController {

    private final LabImageUseCase imageUseCase;

    public LabImageController(LabImageUseCase imageUseCase) {
        this.imageUseCase = imageUseCase;
    }

    @Operation(summary = "랩실 이미지 등록", description = "랩실에 이미지를 등록합니다.")
    @PostMapping
    public ResponseEntity<LabImageResponseDto> registerImage(
            @PathVariable Long labId,
            @Valid @RequestBody LabImageRequestDto req
    ) {
        var img = imageUseCase.registerImage(labId, req.getImageUrl(), req.getType());
        return ResponseEntity.ok(LabImageResponseDto.from(img));
    }

    @Operation(summary = "랩실 이미지 목록 조회", description = "랩실에 등록된 모든 이미지를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<LabImageResponseDto>> listImages(
            @PathVariable Long labId
    ) {
        List<LabImageResponseDto> dtos = imageUseCase.listImages(labId).stream()
                .map(LabImageResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

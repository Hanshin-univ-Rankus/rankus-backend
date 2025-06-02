package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.application.port.in.LabPromotionUseCase;
import org.univ.rankus.adapter.in.web.dto.LabResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/labs")
@Tag(name = "Lab API", description = "랩실 홍보 관련 API")
public class LabController {

    private final LabPromotionUseCase useCase;

    public LabController(LabPromotionUseCase useCase) {
        this.useCase = useCase;
    }

    @Operation(summary = "랩실 목록 조회", description = "전체 랩실을 랭킹 순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<LabResponseDto>> listLabs() {
        List<LabResponseDto> result = useCase.listLabs()
                .stream()
                .map(LabResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "랩실 상세 조회", description = "ID로 특정 랩실 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<LabResponseDto> getLabById(@PathVariable Long id) {
        LabResponseDto dto = LabResponseDto.from(useCase.getLabById(id));
        return ResponseEntity.ok(dto);
    }
}

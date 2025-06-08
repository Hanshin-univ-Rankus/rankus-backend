package org.univ.rankus.adapter.in.web.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabResponseDto;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.domain.model.lab.Lab;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lab 관련 REST API 컨트롤러
 *
 * - GET /api/v1/labs         : 인증된 사용자 모두 접근 가능
 * - GET /api/v1/labs/{labId} : 인증된 사용자 모두 접근 가능
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs")
public class LabPromotionController {

    // 읽기 전용 Query 인터페이스 주입
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;

    /**
     * GET /api/v1/labs
     * - 모든 랩실을 랭킹 내림차순으로 조회합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LabResponseDto>>> listLabs() {
        // 1) 도메인에서 데이터 조회
        List<Lab> labs = labPromotionQueryUseCase.listLabs();

        // 2) DTO 변환
        List<LabResponseDto> dtoList = labs.stream()
                .map(LabResponseDto::from)
                .collect(Collectors.toList());

        // 3) ApiResponse 빌드
        ApiResponse<List<LabResponseDto>> body = ApiResponse.<List<LabResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("랩실 목록 조회 성공")
                .data(dtoList)
                .build();

        // 4) 200 OK + ApiResponse 바디 반환
        return ResponseEntity
                .ok(body);
    }

    /**
     * GET /api/v1/labs/{labId}
     * - ID로 특정 랩실을 조회합니다.
     */
    @GetMapping("/{labId}")
    public ResponseEntity<ApiResponse<LabResponseDto>> getLab(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId
    ) {
        // 1) 도메인에서 단일 객체 조회
        Lab lab = labPromotionQueryUseCase.getLabById(labId);

        // 2) DTO 변환
        LabResponseDto dto = LabResponseDto.from(lab);

        // 3) ApiResponse 빌드
        ApiResponse<LabResponseDto> body = ApiResponse.<LabResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("랩실 정보 조회 성공")
                .data(dto)
                .build();

        // 4) 200 OK + ApiResponse 바디 반환
        return ResponseEntity
                .ok(body);
    }
}
package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.LabApplicationRequestDto;
import org.univ.rankus.adapter.in.web.dto.LabApplicationResponseDto;
import org.univ.rankus.application.port.in.LabApplicationUseCase;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(path = "/api/labs/{labId}/applications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "랩실 지원 API", description = "랩실 지원서 등록 및 조회 API")
public class LabApplicationController {

    private final LabApplicationUseCase applicationUseCase;

    public LabApplicationController(LabApplicationUseCase applicationUseCase) {
        this.applicationUseCase = applicationUseCase;
    }

    @Operation(
        summary = "랩실 지원서 등록",
        description = "특정 랩실에 지원서를 등록합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지원서 등록 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 입력값"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 랩실")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LabApplicationResponseDto> registerApplication(
            @Parameter(description = "랩실 ID", required = true) @PathVariable Long labId,
            @Valid @RequestBody LabApplicationRequestDto request
    ) {
        LabApplication app = applicationUseCase.registerApplication(
                labId,
                request.getUserId(),
                request.getUserName(),
                request.getInterviewTime()
        );
        return ResponseEntity.ok(LabApplicationResponseDto.from(app));
    }

    @Operation(
        summary = "랩실 지원서 목록 조회",
        description = "특정 랩실에 등록된 모든 지원서를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 랩실")
    })
    @GetMapping
    public ResponseEntity<List<LabApplicationResponseDto>> listApplications(
            @Parameter(description = "랩실 ID", required = true) @PathVariable Long labId
    ) {
        List<LabApplication> list = applicationUseCase.listApplications(labId);
        List<LabApplicationResponseDto> dtos = list.stream()
                .map(LabApplicationResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

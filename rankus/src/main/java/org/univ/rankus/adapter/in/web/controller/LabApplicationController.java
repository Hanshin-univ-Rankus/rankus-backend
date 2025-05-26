package org.univ.rankus.adapter.in.web.controller;


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
public class LabApplicationController {

    private final LabApplicationUseCase applicationUseCase;

    public LabApplicationController(LabApplicationUseCase applicationUseCase) {
        this.applicationUseCase = applicationUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LabApplicationResponseDto> registerApplication(
            @PathVariable Long labId,
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

    @GetMapping
    public ResponseEntity<List<LabApplicationResponseDto>> listApplications(
            @PathVariable Long labId
    ) {
        List<LabApplication> list = applicationUseCase.listApplications(labId);
        List<LabApplicationResponseDto> dtos = list.stream()
                .map(LabApplicationResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

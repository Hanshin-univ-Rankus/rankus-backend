package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * LabApplication 신청 시 요청 파라미터를 받기 위한 DTO
 */
public record LabApplicationRequestDto(
        @NotNull(message = "인터뷰 시간은 필수입니다.")
        @Future(message = "인터뷰 시간은 미래여야 합니다.")
        LocalDateTime interviewTime
) { }
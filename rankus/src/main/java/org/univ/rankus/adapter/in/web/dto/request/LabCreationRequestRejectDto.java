package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 랩실 생성 신청 거절 시 요청 파라미터를 받기 위한 DTO
 */
public record LabCreationRequestRejectDto(
        @Size(max = 500, message = "거절 사유는 500자 이하여야 합니다")
        String rejectionReason
) {
}
package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 출석 체크 요청 DTO
 */
@Data
@NoArgsConstructor
public class AttendanceCheckRequestDto {

    @NotBlank(message = "QR 토큰은 필수입니다")
    private String qrToken;

    public AttendanceCheckRequestDto(String qrToken) {
        this.qrToken = qrToken;
    }
}
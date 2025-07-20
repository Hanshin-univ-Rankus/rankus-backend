package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출석 세션 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionCreateRequestDto {

    @NotNull(message = "제목은 필수입니다")
    @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다")
    private String title;

    @Min(value = 1, message = "QR 유효시간은 최소 1분입니다")
    @Max(value = 10, message = "QR 유효시간은 최대 10분입니다")
    private Integer qrValidityMinutes;
}
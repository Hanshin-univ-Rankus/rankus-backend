package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 캘린더 이벤트 생성 시 요청 파라미터를 받기 위한 DTO
 * - 일반 일정(SCHEDULE) 생성용
 * - 면접 일정(INTERVIEW)은 Interview 시스템에서 자동으로 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;

    @NotNull(message = "날짜는 필수입니다")
    @Future(message = "날짜는 미래여야 합니다")
    private LocalDate eventDate;
}
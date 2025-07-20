package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "투표 생성 요청 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteCreateRequestDto {

    @Schema(
            description = "투표 제목",
            example = "다음 주 랩실 회식 장소 투표",
            maxLength = 200,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "투표 제목은 필수입니다")
    @Size(max = 200, message = "투표 제목은 200자 이하여야 합니다")
    private String title;

    @Schema(
            description = "투표 설명 (선택사항)",
            example = "2024년 2월 랩실 회식 장소를 결정하기 위한 투표입니다. 많은 참여 부탁드립니다!",
            maxLength = 1000,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 1000, message = "투표 설명은 1000자 이하여야 합니다")
    private String description;

    @Schema(
            description = "투표 마감일시 (현재 시간 이후)",
            example = "2024-02-10T23:59:59",
            type = "string",
            format = "date-time",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "투표 마감일은 필수입니다")
    private LocalDateTime deadline;

    @Schema(
            description = "투표 선택지 목록 (2-5개)",
            example = "[\"한식당 (삼겹살)\", \"중식당 (짜장면)\", \"일식당 (초밥)\", \"치킨집 (후라이드)\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "투표 선택지는 필수입니다")
    @Size(min = 2, max = 5, message = "투표 선택지는 2개 이상 5개 이하여야 합니다")
    private List<@NotBlank(message = "투표 선택지 내용은 필수입니다") @Size(max = 100, message = "투표 선택지는 100자 이하여야 합니다") String> optionTexts;
}
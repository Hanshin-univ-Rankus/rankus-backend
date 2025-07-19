package org.univ.rankus.adapter.in.web.dto.request;

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

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteCreateRequestDto {

    @NotBlank(message = "투표 제목은 필수입니다")
    @Size(max = 200, message = "투표 제목은 200자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "투표 설명은 1000자 이하여야 합니다")
    private String description;

    @NotNull(message = "투표 마감일은 필수입니다")
    private LocalDateTime deadline;

    @NotEmpty(message = "투표 선택지는 필수입니다")
    @Size(min = 2, max = 5, message = "투표 선택지는 2개 이상 5개 이하여야 합니다")
    private List<@NotBlank(message = "투표 선택지 내용은 필수입니다") @Size(max = 100, message = "투표 선택지는 100자 이하여야 합니다") String> optionTexts;
}
package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSubmissionCreateRequestDto {

    @NotNull(message = "랩실 ID는 필수입니다")
    private Long labId;

    @NotNull(message = "점수 카테고리는 필수입니다")
    private ScoreCategory category;

    @NotBlank(message = "성과 내용은 필수입니다")
    @Size(max = 500, message = "성과 내용은 500자 이하여야 합니다")
    private String achievementDescription;

    @NotNull(message = "취득일자는 필수입니다")
    @PastOrPresent(message = "취득일자는 미래일 수 없습니다")
    private LocalDate achievementDate;

    @NotBlank(message = "증빙서류는 필수입니다")
    @Size(max = 500, message = "증빙서류 URL은 500자 이하여야 합니다")
    private String proofFileUrl;

    @Size(max = 200, message = "신청 사유는 200자 이하여야 합니다")
    private String applicationReason;

    @Size(max = 500, message = "관련 링크는 500자 이하여야 합니다")
    private String relatedLink;

    @NotNull(message = "공개 범위는 필수입니다")
    @Builder.Default
    private VisibilityLevel visibility = VisibilityLevel.PUBLIC;
}
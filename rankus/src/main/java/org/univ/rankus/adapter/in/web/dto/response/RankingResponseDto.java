package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "랩실 랭킹 응답 정보")
public class RankingResponseDto {

    @Schema(description = "랩실 ID", example = "1")
    private Long labId;

    @Schema(description = "랩실 이름", example = "AI 연구실")
    private String labName;

    @Schema(description = "총 점수", example = "850")
    private int totalScore;

    @Schema(description = "순위", example = "1")
    private int rank;

    @Schema(description = "최근 점수 제출 목록")
    private List<ScoreSubmissionResponseDto> recentSubmissions;

    @Schema(description = "상위 기여자 목록")
    private List<UserContributionResponseDto> topContributors;

    public static RankingResponseDto from(RankingQueryUseCase.LabRankingResult result) {
        return RankingResponseDto.builder()
                .labId(result.getLabId())
                .labName(result.getLabName())
                .totalScore(result.getTotalScore())
                .rank(result.getRank())
                .recentSubmissions(result.getRecentSubmissions().stream()
                        .map(ScoreSubmissionResponseDto::from)
                        .toList())
                .topContributors(result.getTopContributors().stream()
                        .map(UserContributionResponseDto::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 기여도 응답 정보")
    public static class UserContributionResponseDto {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;

        @Schema(description = "기여 점수", example = "120")
        private int contributionScore;

        @Schema(description = "승인된 제출 수", example = "5")
        private int approvedSubmissionCount;

        public static UserContributionResponseDto from(RankingQueryUseCase.UserContribution contribution) {
            return UserContributionResponseDto.builder()
                    .userId(contribution.getUserId())
                    .userName(contribution.getUserName())
                    .contributionScore(contribution.getContributionScore())
                    .approvedSubmissionCount(contribution.getApprovedSubmissionCount())
                    .build();
        }
    }
}
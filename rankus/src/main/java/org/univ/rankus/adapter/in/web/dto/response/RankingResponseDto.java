package org.univ.rankus.adapter.in.web.dto.response;

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
public class RankingResponseDto {

    private Long labId;
    private String labName;
    private int totalScore;
    private int rank;
    private List<ScoreSubmissionResponseDto> recentSubmissions;
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
    public static class UserContributionResponseDto {
        private Long userId;
        private String userName;
        private int contributionScore;
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
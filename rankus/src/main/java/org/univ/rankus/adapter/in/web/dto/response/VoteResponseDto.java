package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "투표 응답 정보")
public class VoteResponseDto {
    @Schema(description = "투표 ID", example = "1")
    private final Long id;

    @Schema(description = "투표 제목", example = "연구실 정기 미팅 시간 투표")
    private final String title;

    @Schema(description = "투표 설명", example = "정기 미팅 시간을 결정하기 위한 투표입니다.")
    private final String description;

    @Schema(description = "투표 상태")
    private final VoteStatus status;

    @Schema(description = "투표 마감 시간")
    private final LocalDateTime deadline;

    @Schema(description = "총 투표 수", example = "15")
    private final Integer totalVotes;

    @Schema(description = "생성일시")
    private final LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private final LocalDateTime updatedAt;

    // 생성자 정보
    @Schema(description = "투표 생성자 ID", example = "1")
    private final Long creatorId;

    @Schema(description = "투표 생성자 이름", example = "홍길동")
    private final String creatorName;

    // 랩실 정보
    @Schema(description = "랩실 ID", example = "1")
    private final Long labId;

    @Schema(description = "랩실 이름", example = "AI 연구실")
    private final String labName;

    // 투표 선택지들
    @Schema(description = "투표 선택지 목록")
    private final List<VoteOptionResponseDto> options;

    // 추가 정보
    @Schema(description = "참여 가능 여부", example = "true")
    private final boolean canParticipate;

    @Schema(description = "만료 여부", example = "false")
    private final boolean isExpired;

    public static VoteResponseDto from(Vote vote) {
        return VoteResponseDto.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .status(vote.getStatus())
                .deadline(vote.getDeadline())
                .totalVotes(vote.getTotalVotes())
                .createdAt(vote.getCreatedAt())
                .updatedAt(vote.getUpdatedAt())
                .creatorId(vote.getCreator() != null ? vote.getCreator().getId() : null)
                .creatorName(vote.getCreator() != null ? vote.getCreator().getName() : null)
                .labId(vote.getLab() != null ? vote.getLab().getId() : null)
                .labName(vote.getLab() != null ? vote.getLab().getName() : null)
                .options(VoteOptionResponseDto.fromList(vote.getOptions()))
                .canParticipate(vote.canParticipate())
                .isExpired(vote.isExpired())
                .build();
    }

    public static List<VoteResponseDto> fromList(List<Vote> votes) {
        return votes.stream()
                .map(VoteResponseDto::from)
                .collect(Collectors.toList());
    }
}
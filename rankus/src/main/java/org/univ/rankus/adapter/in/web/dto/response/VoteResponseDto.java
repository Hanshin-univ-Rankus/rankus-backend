package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class VoteResponseDto {
    private final Long id;
    private final String title;
    private final String description;
    private final VoteStatus status;
    private final LocalDateTime deadline;
    private final Integer totalVotes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 생성자 정보
    private final Long creatorId;
    private final String creatorName;

    // 랩실 정보
    private final Long labId;
    private final String labName;

    // 투표 선택지들
    private final List<VoteOptionResponseDto> options;

    // 추가 정보
    private final boolean canParticipate;
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
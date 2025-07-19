package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.vote.VoteParticipation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class VoteParticipationResponseDto {
    private final Long id;
    private final Long voteId;
    private final String voteTitle;
    private final Long userId;
    private final String userName;
    private final Long selectedOptionId;
    private final String selectedOptionText;
    private final LocalDateTime participatedAt;

    public static VoteParticipationResponseDto from(VoteParticipation participation) {
        return VoteParticipationResponseDto.builder()
                .id(participation.getId())
                .voteId(participation.getVote() != null ? participation.getVote().getId() : null)
                .voteTitle(participation.getVote() != null ? participation.getVote().getTitle() : null)
                .userId(participation.getUser() != null ? participation.getUser().getId() : null)
                .userName(participation.getUser() != null ? participation.getUser().getName() : null)
                .selectedOptionId(participation.getSelectedOption() != null ? participation.getSelectedOption().getId() : null)
                .selectedOptionText(participation.getSelectedOption() != null ? participation.getSelectedOption().getOptionText() : null)
                .participatedAt(participation.getCreatedAt())
                .build();
    }

    public static List<VoteParticipationResponseDto> fromList(List<VoteParticipation> participations) {
        return participations.stream()
                .map(VoteParticipationResponseDto::from)
                .collect(Collectors.toList());
    }
}
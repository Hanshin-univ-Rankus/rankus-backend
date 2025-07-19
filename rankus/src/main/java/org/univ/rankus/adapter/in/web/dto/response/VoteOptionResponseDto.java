package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.vote.VoteOption;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class VoteOptionResponseDto {
    private final Long id;
    private final String optionText;
    private final Integer optionOrder;
    private final Integer voteCount;

    public static VoteOptionResponseDto from(VoteOption voteOption) {
        return VoteOptionResponseDto.builder()
                .id(voteOption.getId())
                .optionText(voteOption.getOptionText())
                .optionOrder(voteOption.getOptionOrder())
                .voteCount(voteOption.getVoteCount())
                .build();
    }

    public static List<VoteOptionResponseDto> fromList(List<VoteOption> voteOptions) {
        return voteOptions.stream()
                .map(VoteOptionResponseDto::from)
                .collect(Collectors.toList());
    }
}
package org.univ.rankus.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.ranking.policy.DuplicateCheckPolicy;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckResponseDto {

    private boolean hasDateDuplicates;
    private boolean hasExactDuplicates;
    private boolean hasDuplicates;
    private String warningLevel;
    private String warningMessage;
    private int displayPriority;
    private boolean duplicateAllowed;
    private List<ScoreSubmissionResponseDto> duplicateSubmissions;

    public static DuplicateCheckResponseDto from(DuplicateCheckPolicy.DuplicateCheckResult result) {
        DuplicateCheckPolicy policy = new DuplicateCheckPolicy();
        DuplicateCheckPolicy.DuplicateWarningLevel warningLevel = policy.determineDuplicateWarningLevel(result);

        return DuplicateCheckResponseDto.builder()
                .hasDateDuplicates(result.hasDateDuplicates())
                .hasExactDuplicates(result.hasExactDuplicates())
                .hasDuplicates(result.hasDuplicates())
                .warningLevel(warningLevel.name())
                .warningMessage(policy.generateDuplicateMessage(result))
                .displayPriority(policy.calculateDisplayPriority(result))
                .duplicateAllowed(policy.isDuplicateAllowed(result))
                .duplicateSubmissions(result.getDuplicateSubmissions().stream()
                        .map(ScoreSubmissionResponseDto::from)
                        .toList())
                .build();
    }
}
package org.univ.rankus.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSubmissionResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private Long labId;
    private String labName;
    private ScoreCategory category;
    private String categoryDisplayName;
    private String achievementDescription;
    private LocalDate achievementDate;
    private String proofFileUrl;
    private String applicationReason;
    private String relatedLink;
    private SubmissionStatus status;
    private String statusDisplayName;
    private VisibilityLevel visibility;
    private String visibilityDisplayName;
    private Long approvedBy;
    private String approverName;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime expiresAt;
    private boolean correctionUsed;
    private int correctionCount;
    private String rejectionReason;
    private int score;
    private boolean expired;
    private boolean canBeApproved;

    public static ScoreSubmissionResponseDto from(ScoreSubmission submission) {
        return ScoreSubmissionResponseDto.builder()
                .id(submission.getId())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getName())
                .labId(submission.getLab().getId())
                .labName(submission.getLab().getName())
                .category(submission.getCategory())
                .categoryDisplayName(submission.getCategory().getDisplayName())
                .achievementDescription(submission.getAchievementDescription())
                .achievementDate(submission.getAchievementDate())
                .proofFileUrl(submission.getProofFileUrl())
                .applicationReason(submission.getApplicationReason())
                .relatedLink(submission.getRelatedLink())
                .status(submission.getStatus())
                .statusDisplayName(submission.getStatus().getDisplayName())
                .visibility(submission.getVisibility())
                .visibilityDisplayName(submission.getVisibility().getDisplayName())
                .approvedBy(submission.getApprovedBy())
                .approverName(null) // 필요시 조회
                .approvedAt(submission.getApprovedAt())
                .submittedAt(submission.getSubmittedAt())
                .expiresAt(submission.getExpiresAt())
                .correctionUsed(submission.isCorrectionUsed())
                .correctionCount(submission.getCorrectionCount())
                .rejectionReason(submission.getRejectionReason())
                .score(submission.getScore())
                .expired(submission.isExpired())
                .canBeApproved(submission.canBeApproved())
                .build();
    }
}
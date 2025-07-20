package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "점수 제출 응답 정보")
public class ScoreSubmissionResponseDto {

    @Schema(description = "제출 ID", example = "1")
    private Long id;

    @Schema(description = "제출자 ID", example = "1")
    private Long userId;

    @Schema(description = "제출자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "랩실 ID", example = "1")
    private Long labId;

    @Schema(description = "랩실 이름", example = "AI 연구실")
    private String labName;

    @Schema(description = "점수 카테고리")
    private ScoreCategory category;

    @Schema(description = "카테고리 표시명", example = "논문 게재")
    private String categoryDisplayName;

    @Schema(description = "성과 설명", example = "SCI 저널에 논문 게재")
    private String achievementDescription;

    @Schema(description = "성과 달성일")
    private LocalDate achievementDate;

    @Schema(description = "증빙 파일 URL", nullable = true)
    private String proofFileUrl;

    @Schema(description = "신청 사유", example = "연구 성과로 점수 신청합니다.")
    private String applicationReason;

    @Schema(description = "관련 링크", nullable = true)
    private String relatedLink;

    @Schema(description = "제출 상태")
    private SubmissionStatus status;

    @Schema(description = "상태 표시명", example = "승인 대기")
    private String statusDisplayName;

    @Schema(description = "공개 수준")
    private VisibilityLevel visibility;

    @Schema(description = "공개 수준 표시명", example = "랩실 내 공개")
    private String visibilityDisplayName;

    @Schema(description = "승인자 ID", nullable = true)
    private Long approvedBy;

    @Schema(description = "승인자 이름", nullable = true)
    private String approverName;

    @Schema(description = "승인일시", nullable = true)
    private LocalDateTime approvedAt;

    @Schema(description = "제출일시")
    private LocalDateTime submittedAt;

    @Schema(description = "만료일시", nullable = true)
    private LocalDateTime expiresAt;

    @Schema(description = "수정 사용 여부", example = "false")
    private boolean correctionUsed;

    @Schema(description = "수정 횟수", example = "0")
    private int correctionCount;

    @Schema(description = "거부 사유", nullable = true)
    private String rejectionReason;

    @Schema(description = "점수", example = "100")
    private int score;

    @Schema(description = "만료 여부", example = "false")
    private boolean expired;

    @Schema(description = "승인 가능 여부", example = "true")
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
package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;

import java.time.LocalDate;

public interface ScoreSubmissionCommandUseCase {

    /**
     * 점수 신청 생성
     */
    ScoreSubmission submitScore(Long userId, Long labId, ScoreCategory category,
                                String achievementDescription, LocalDate achievementDate,
                                String proofFileUrl, String applicationReason,
                                String relatedLink, VisibilityLevel visibility);

    /**
     * 점수 신청 승인
     */
    void approveSubmission(Long submissionId, Long approverId);

    /**
     * 점수 신청 거부
     */
    void rejectSubmission(Long submissionId, Long approverId, String rejectionReason);

    /**
     * 점수 상태 정정 (승인 ↔ 거부)
     */
    void correctSubmissionStatus(Long submissionId, Long correcterId);

    /**
     * 점수 신청 삭제 (신청자 본인만 가능)
     */
    void deleteSubmission(Long submissionId, Long userId);

    /**
     * 만료된 점수 신청 자동 거부 (스케줄러용)
     */
    void processExpiredSubmissions();
}
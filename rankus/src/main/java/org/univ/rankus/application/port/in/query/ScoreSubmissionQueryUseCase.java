package org.univ.rankus.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.policy.DuplicateCheckPolicy;

import java.time.LocalDate;
import java.util.List;

public interface ScoreSubmissionQueryUseCase {

    /**
     * 점수 신청 ID로 조회
     */
    ScoreSubmission findSubmissionById(Long submissionId);

    /**
     * 사용자의 점수 신청 목록 조회
     */
    Page<ScoreSubmission> findSubmissionsByUserId(Long userId, Pageable pageable);

    /**
     * 랩실의 점수 신청 목록 조회
     */
    Page<ScoreSubmission> findSubmissionsByLabId(Long labId, Pageable pageable);

    /**
     * 상태별 점수 신청 목록 조회
     */
    Page<ScoreSubmission> findSubmissionsByStatus(SubmissionStatus status, Pageable pageable);

    /**
     * 랩실 + 상태별 점수 신청 목록 조회
     */
    Page<ScoreSubmission> findSubmissionsByLabIdAndStatus(Long labId, SubmissionStatus status, Pageable pageable);

    /**
     * 승인 대기 중인 점수 신청 목록 조회 (승인자별)
     */
    Page<ScoreSubmission> findPendingSubmissionsForApprover(Long approverId, Pageable pageable);

    /**
     * 중복 검사 실행
     */
    DuplicateCheckPolicy.DuplicateCheckResult checkDuplicates(Long userId, LocalDate achievementDate,
                                                              ScoreCategory category);

    /**
     * 만료 예정 점수 신청 목록 조회
     */
    List<ScoreSubmission> findSubmissionsExpiringWithin(int days);

    /**
     * 만료된 점수 신청 목록 조회
     */
    List<ScoreSubmission> findExpiredSubmissions();

    /**
     * 사용자의 특정 랩실에서의 승인된 점수 총합 조회
     */
    int calculateUserScoreInLab(Long userId, Long labId);

    /**
     * 사용자의 전체 승인된 점수 총합 조회
     */
    int calculateUserTotalScore(Long userId);
}
package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.ScoreSubmissionCommandUseCase;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.ranking.policy.ScoreSubmissionPolicy;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional  // 쓰기 트랜잭션 관리
public class ScoreSubmissionCommandService implements ScoreSubmissionCommandUseCase {

    private final ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final ScoreSubmissionPolicy scoreSubmissionPolicy;

    @Override
    public ScoreSubmission submitScore(Long userId, Long labId, ScoreCategory category,
                                       String achievementDescription, LocalDate achievementDate,
                                       String proofFileUrl, String applicationReason,
                                       String relatedLink, VisibilityLevel visibility) {
        // 1) 사용자 검증
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2) 랩실 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3) 점수 신청 생성
        ScoreSubmission submission = new ScoreSubmission(
                user, lab, category, achievementDescription, achievementDate,
                proofFileUrl, applicationReason, relatedLink, visibility
        );

        // 4) 점수 신청 저장
        return scoreSubmissionRepositoryPort.save(submission);
    }

    @Override
    public void approveSubmission(Long submissionId, Long approverId) {
        // 1) 점수 신청 조회
        ScoreSubmission submission = scoreSubmissionRepositoryPort.findById(submissionId)
                .orElseThrow(() -> new RankingValidationException(RankingErrorCode.SUBMISSION_NOT_FOUND));

        // 2) 승인자 검증
        User approver = userRepositoryPort.findById(approverId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 승인 권한 확인
        if (!scoreSubmissionPolicy.canApprove(approver, submission)) {
            throw new RankingValidationException(RankingErrorCode.INSUFFICIENT_PERMISSION);
        }

        // 4) 승인 처리
        submission.approve(approverId);
        scoreSubmissionRepositoryPort.save(submission);
    }

    @Override
    public void rejectSubmission(Long submissionId, Long approverId, String rejectionReason) {
        // 1) 점수 신청 조회
        ScoreSubmission submission = scoreSubmissionRepositoryPort.findById(submissionId)
                .orElseThrow(() -> new RankingValidationException(RankingErrorCode.SUBMISSION_NOT_FOUND));

        // 2) 승인자 검증
        User approver = userRepositoryPort.findById(approverId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 승인 권한 확인
        if (!scoreSubmissionPolicy.canApprove(approver, submission)) {
            throw new RankingValidationException(RankingErrorCode.INSUFFICIENT_PERMISSION);
        }

        // 4) 거부 처리
        submission.reject(approverId, rejectionReason);
        scoreSubmissionRepositoryPort.save(submission);
    }

    @Override
    public void correctSubmissionStatus(Long submissionId, Long correcterId) {
        // 1) 점수 신청 조회
        ScoreSubmission submission = scoreSubmissionRepositoryPort.findById(submissionId)
                .orElseThrow(() -> new RankingValidationException(RankingErrorCode.SUBMISSION_NOT_FOUND));

        // 2) 정정자 검증
        User correcter = userRepositoryPort.findById(correcterId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 정정 권한 확인
        if (!scoreSubmissionPolicy.canApprove(correcter, submission)) {
            throw new RankingValidationException(RankingErrorCode.INSUFFICIENT_PERMISSION);
        }

        // 4) 정정 가능 여부 확인
        if (!scoreSubmissionPolicy.canCorrect(submission)) {
            throw new RankingValidationException(RankingErrorCode.CORRECTION_LIMIT_EXCEEDED);
        }

        // 5) 정정 처리 (상태 전환)
        SubmissionStatus newStatus = submission.getStatus() == SubmissionStatus.APPROVED
                ? SubmissionStatus.REJECTED : SubmissionStatus.APPROVED;

        submission.correctStatus(newStatus, correcterId);
        scoreSubmissionRepositoryPort.save(submission);
    }

    @Override
    public void deleteSubmission(Long submissionId, Long userId) {
        // 1) 점수 신청 조회
        ScoreSubmission submission = scoreSubmissionRepositoryPort.findById(submissionId)
                .orElseThrow(() -> new RankingValidationException(RankingErrorCode.SUBMISSION_NOT_FOUND));

        // 2) 소유자 확인
        if (!submission.isOwnedBy(userId)) {
            throw new RankingValidationException(RankingErrorCode.NOT_OWNER);
        }

        // 3) 삭제 가능 여부 확인 (PENDING 상태만 삭제 가능)
        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw new RankingValidationException(RankingErrorCode.ALREADY_PROCESSED);
        }

        // 4) 삭제 처리
        scoreSubmissionRepositoryPort.delete(submission);
    }

    @Override
    public void processExpiredSubmissions() {
        // 1) 만료된 점수 신청 조회
        List<ScoreSubmission> expiredSubmissions = scoreSubmissionRepositoryPort
                .findByStatusAndExpiresAtBefore(SubmissionStatus.PENDING, LocalDateTime.now());

        // 2) 자동 거부 처리
        for (ScoreSubmission submission : expiredSubmissions) {
            if (scoreSubmissionPolicy.shouldAutoReject(submission)) {
                submission.rejectBySystem("시스템 자동 거부 (6개월 기한 만료)");
                scoreSubmissionRepositoryPort.save(submission);
            }
        }
    }
}
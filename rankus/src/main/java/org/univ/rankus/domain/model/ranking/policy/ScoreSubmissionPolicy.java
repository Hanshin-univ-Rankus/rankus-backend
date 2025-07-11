package org.univ.rankus.domain.model.ranking.policy;

import org.springframework.stereotype.Component;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 점수 신청 관련 정책을 정의하는 도메인 서비스
 */
@Component
public class ScoreSubmissionPolicy {

    /**
     * 승인 기한 정책 (6개월)
     */
    public static final int APPROVAL_DEADLINE_MONTHS = 6;

    /**
     * 정정 가능 횟수 정책
     */
    public static final int MAX_CORRECTION_COUNT = 1;

    /**
     * 자동 거부 기한 확인
     */
    public boolean shouldAutoReject(ScoreSubmission submission) {
        return submission.getStatus() == SubmissionStatus.PENDING &&
                submission.isExpired();
    }

    /**
     * 승인 권한 확인
     */
    public boolean canApprove(User approver, ScoreSubmission submission) {
        // 본인이 신청한 점수는 승인 불가
        if (submission.getUser().getId().equals(approver.getId())) {
            return false;
        }

        // ADMIN과 PROFESSOR는 모든 랩실의 점수 승인 가능
        if (approver.getRole() == Role.ADMIN || approver.getRole() == Role.PROFESSOR) {
            return true;
        }

        // 해당 랩실의 LAB_LEADER, LAB_MANAGER만 승인 가능
        return (approver.getRole() == Role.LAB_LEADER || approver.getRole() == Role.LAB_MANAGER)
                && approver.getLab() != null
                && approver.getLab().equals(submission.getLab());
    }

    /**
     * 정정 가능 여부 확인
     */
    public boolean canCorrect(ScoreSubmission submission) {
        return submission.getCorrectionCount() < MAX_CORRECTION_COUNT;
    }

    /**
     * 승인 → 거부 정정 가능 여부
     */
    public boolean canCorrectApprovalToRejection(ScoreSubmission submission) {
        return submission.getStatus() == SubmissionStatus.APPROVED && canCorrect(submission);
    }

    /**
     * 거부 → 승인 정정 가능 여부
     */
    public boolean canCorrectRejectionToApproval(ScoreSubmission submission) {
        return submission.getStatus() == SubmissionStatus.REJECTED &&
                !submission.isCorrectionUsed() && canCorrect(submission);
    }

    /**
     * 만료 예정 알림 기준 (만료 1주일 전)
     */
    public boolean shouldSendExpirationWarning(ScoreSubmission submission) {
        LocalDateTime warningTime = submission.getExpiresAt().minusWeeks(1);
        return LocalDateTime.now().isAfter(warningTime) &&
                submission.getStatus() == SubmissionStatus.PENDING;
    }

    /**
     * 벌크 처리 가능 여부
     */
    public boolean canBulkProcess(List<ScoreSubmission> submissions, User processor) {
        return submissions.stream()
                .allMatch(submission -> canApprove(processor, submission));
    }

    /**
     * 재신청 가능 여부 (거부된 경우)
     */
    public boolean canResubmit(ScoreSubmission submission) {
        return submission.getStatus() == SubmissionStatus.REJECTED;
    }

    /**
     * 점수 신청 만료 시간 계산
     */
    public LocalDateTime calculateExpirationTime(LocalDateTime submissionTime) {
        return submissionTime.plusMonths(APPROVAL_DEADLINE_MONTHS);
    }
}
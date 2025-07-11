package org.univ.rankus.domain.model.ranking;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ScoreSubmission 엔티티 - 점수 신청 정보를 나타내는 도메인 모델
 */
@Getter
@Entity
@Table(name = "score_submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 점수 신청 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 신청자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab; // 신청 랩실

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScoreCategory category; // 점수 카테고리

    @Column(nullable = false, length = 500)
    private String achievementDescription; // 성과 내용

    @Column(nullable = false)
    private LocalDate achievementDate; // 취득일자

    @Column(nullable = false, length = 500)
    private String proofFileUrl; // 증빙서류 URL

    @Column(length = 200)
    private String applicationReason; // 신청 사유 (선택)

    @Column(length = 500)
    private String relatedLink; // 관련 링크 (선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status; // 신청 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisibilityLevel visibility; // 공개 범위

    @Column(name = "approved_by")
    private Long approvedBy; // 승인자 ID

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 승인 시각

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt; // 신청 시각

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 만료 시각 (신청 후 6개월)

    @Column(name = "correction_used", nullable = false)
    private boolean correctionUsed; // 정정 사용 여부

    @Column(name = "correction_count", nullable = false)
    private int correctionCount; // 정정 횟수

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason; // 거부 사유

    /**
     * 생성자: 점수 신청 생성
     */
    public ScoreSubmission(User user, Lab lab, ScoreCategory category,
                           String achievementDescription, LocalDate achievementDate,
                           String proofFileUrl, String applicationReason,
                           String relatedLink, VisibilityLevel visibility) {
        this.user = validateUser(user);
        this.lab = validateLab(lab);
        this.category = validateCategory(category);
        this.achievementDescription = validateAchievementDescription(achievementDescription);
        this.achievementDate = validateAchievementDate(achievementDate);
        this.proofFileUrl = validateProofFileUrl(proofFileUrl);
        this.applicationReason = validateApplicationReason(applicationReason);
        this.relatedLink = validateRelatedLink(relatedLink);
        this.visibility = validateVisibility(visibility);
        this.status = SubmissionStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMonths(6);
        this.correctionUsed = false;
        this.correctionCount = 0;
    }

    /**
     * 사용자 유효성 검증
     */
    private User validateUser(User user) {
        if (user == null) {
            throw new RankingValidationException(RankingErrorCode.USER_REQUIRED);
        }
        return user;
    }

    /**
     * 랩실 유효성 검증
     */
    private Lab validateLab(Lab lab) {
        if (lab == null) {
            throw new RankingValidationException(RankingErrorCode.LAB_REQUIRED);
        }
        return lab;
    }

    /**
     * 카테고리 유효성 검증
     */
    private ScoreCategory validateCategory(ScoreCategory category) {
        if (category == null) {
            throw new RankingValidationException(RankingErrorCode.CATEGORY_REQUIRED);
        }
        return category;
    }

    /**
     * 성과 내용 유효성 검증
     */
    private String validateAchievementDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new RankingValidationException(RankingErrorCode.ACHIEVEMENT_DESCRIPTION_REQUIRED);
        }
        String trimmed = description.trim();
        if (trimmed.length() > 500) {
            throw new RankingValidationException(RankingErrorCode.ACHIEVEMENT_DESCRIPTION_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 취득일자 유효성 검증
     */
    private LocalDate validateAchievementDate(LocalDate date) {
        if (date == null) {
            throw new RankingValidationException(RankingErrorCode.ACHIEVEMENT_DATE_REQUIRED);
        }
        if (date.isAfter(LocalDate.now())) {
            throw new RankingValidationException(RankingErrorCode.ACHIEVEMENT_DATE_FUTURE);
        }
        return date;
    }

    /**
     * 증빙서류 URL 유효성 검증
     */
    private String validateProofFileUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new RankingValidationException(RankingErrorCode.PROOF_FILE_URL_REQUIRED);
        }
        return url.trim();
    }

    /**
     * 신청 사유 유효성 검증
     */
    private String validateApplicationReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return null;
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 200) {
            throw new RankingValidationException(RankingErrorCode.APPLICATION_REASON_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 관련 링크 유효성 검증
     */
    private String validateRelatedLink(String link) {
        if (!StringUtils.hasText(link)) {
            return null;
        }
        String trimmed = link.trim();
        try {
            new URL(trimmed);
            return trimmed;
        } catch (Exception e) {
            throw new RankingValidationException(RankingErrorCode.RELATED_LINK_INVALID);
        }
    }

    /**
     * 공개 범위 유효성 검증
     */
    private VisibilityLevel validateVisibility(VisibilityLevel visibility) {
        if (visibility == null) {
            throw new RankingValidationException(RankingErrorCode.VISIBILITY_REQUIRED);
        }
        return visibility;
    }

    /**
     * 점수 신청 승인
     */
    public void approve(Long approverId) {
        validateApprovalPermission(approverId);

        if (!this.status.canTransitionTo(SubmissionStatus.APPROVED)) {
            throw new RankingValidationException(RankingErrorCode.INVALID_STATUS_TRANSITION);
        }

        this.status = SubmissionStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    /**
     * 점수 신청 거부
     */
    public void reject(Long approverId, String reason) {
        validateApprovalPermission(approverId);

        if (!this.status.canTransitionTo(SubmissionStatus.REJECTED)) {
            throw new RankingValidationException(RankingErrorCode.INVALID_STATUS_TRANSITION);
        }

        this.status = SubmissionStatus.REJECTED;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * 시스템 자동 거부 (만료 등)
     */
    public void rejectBySystem(String reason) {
        if (!this.status.canTransitionTo(SubmissionStatus.REJECTED)) {
            throw new RankingValidationException(RankingErrorCode.INVALID_STATUS_TRANSITION);
        }

        this.status = SubmissionStatus.REJECTED;
        this.approvedBy = -1L; // 시스템 ID
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * 승인 권한 검증
     */
    private void validateApprovalPermission(Long approverId) {
        if (approverId == null) {
            throw new RankingValidationException(RankingErrorCode.INSUFFICIENT_PERMISSION);
        }

        if (this.user.getId().equals(approverId)) {
            throw new RankingValidationException(RankingErrorCode.CANNOT_APPROVE_OWN_SUBMISSION);
        }
    }

    /**
     * 점수 정정 (승인 ↔ 거부)
     */
    public void correctStatus(SubmissionStatus newStatus, Long correcterId) {
        validateCorrectionPermission(correcterId);

        if (this.correctionCount >= 1) {
            throw new RankingValidationException(RankingErrorCode.CORRECTION_LIMIT_EXCEEDED);
        }

        // 거절 → 승인: 정정 내역이 없을 때만 가능
        if (this.status == SubmissionStatus.REJECTED && newStatus == SubmissionStatus.APPROVED) {
            if (this.correctionUsed) {
                throw new RankingValidationException(RankingErrorCode.CORRECTION_NOT_ALLOWED);
            }
        }

        this.status = newStatus;
        this.approvedBy = correcterId;
        this.approvedAt = LocalDateTime.now();
        this.correctionUsed = true;
        this.correctionCount++;
    }

    /**
     * 정정 권한 검증
     */
    private void validateCorrectionPermission(Long correcterId) {
        if (correcterId == null) {
            throw new RankingValidationException(RankingErrorCode.INSUFFICIENT_PERMISSION);
        }
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 승인 가능 여부 확인
     */
    public boolean canBeApproved() {
        return this.status == SubmissionStatus.PENDING && !isExpired();
    }

    /**
     * 소유자 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    /**
     * 점수 값 반환
     */
    public int getScore() {
        return this.category.getDefaultScore();
    }

    /**
     * 사용자가 해당 랩실의 점수 승인 권한을 가지는지 확인
     */
    public boolean canUserApprove(User approver) {
        // 본인이 신청한 점수는 승인 불가
        if (this.user.getId().equals(approver.getId())) {
            return false;
        }

        // ADMIN과 PROFESSOR는 모든 랩실의 점수 승인 가능
        if (approver.getRole() == Role.ADMIN || approver.getRole() == Role.PROFESSOR) {
            return true;
        }

        // 해당 랩실의 LAB_LEADER, LAB_MANAGER만 승인 가능
        return (approver.getRole() == Role.LAB_LEADER || approver.getRole() == Role.LAB_MANAGER)
                && approver.getLab() != null
                && approver.getLab().equals(this.lab);
    }
}
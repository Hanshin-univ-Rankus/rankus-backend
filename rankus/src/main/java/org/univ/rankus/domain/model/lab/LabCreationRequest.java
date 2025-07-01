package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.exception.LabCreationRequestErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabCreationRequestValidationException;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * LabCreationRequest 엔티티
 * 랩실 생성 신청을 관리하는 도메인 모델
 */
@Getter
@Entity
@Table(name = "lab_creation_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabCreationRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 신청 고유 ID

    // 신청할 랩실 이름 (필수, 최대 10자)
    @Column(nullable = false, length = 10)
    private String requestedLabName;

    // 신청할 랩실 카테고리 (필수)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LabCategory requestedCategory;

    // 신청할 랩실 설명 (선택, 최대 255자)
    @Column(length = 255)
    private String requestedDescription;

    // 신청자 (필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    // 신청 상태 (기본값: PENDING)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LabCreationStatus status;

    // 처리 시간 (승인/거절 시점)
    private LocalDateTime processedAt;

    // 처리자 (승인/거절한 관리자 또는 교수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private User processedBy;

    // 거절 사유 (거절 시에만 입력, 최대 500자)
    @Column(length = 500)
    private String rejectionReason;

    /**
     * 생성자: 필수 필드 검증 후 세팅
     */
    public LabCreationRequest(String requestedLabName, LabCategory requestedCategory,
                              String requestedDescription, User requester) {
        this.requestedLabName = validateRequestedLabName(requestedLabName);
        this.requestedCategory = validateRequestedCategory(requestedCategory);
        this.requestedDescription = validateRequestedDescription(requestedDescription);
        this.requester = validateRequester(requester);
        this.status = LabCreationStatus.PENDING;
    }


    /**
     * 신청 승인
     *
     * @param approver 승인자 (ADMIN 또는 PROFESSOR)
     */
    public void approve(User approver) {
        validateCanChangeStatus();
        validateApprover(approver);

        this.status = LabCreationStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = approver;
        this.rejectionReason = null; // 승인 시에는 거절 사유 제거
    }

    /**
     * 신청 거절
     *
     * @param rejector 거절자 (ADMIN 또는 PROFESSOR)
     * @param reason   거절 사유
     */
    public void reject(User rejector, String reason) {
        validateCanChangeStatus();
        validateApprover(rejector);

        this.status = LabCreationStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = rejector;
        this.rejectionReason = validateRejectionReason(reason);
    }

    /**
     * 상태 변경 가능 여부 검증 (PENDING 상태에서만 변경 가능)
     */
    private void validateCanChangeStatus() {
        if (this.status != LabCreationStatus.PENDING) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
            );
        }
    }

    /**
     * 승인자/거절자 권한 검증
     */
    private void validateApprover(User approver) {
        if (approver == null) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL
            );
        }
        // ADMIN 또는 PROFESSOR만 승인/거절 가능
        if (!approver.getRole().name().equals("ADMIN") && !approver.getRole().name().equals("PROFESSOR")) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL
            );
        }
    }

    /**
     * 신청자 소유 여부 확인
     */
    public boolean isOwnedBy(User user) {
        return this.requester != null && this.requester.equals(user);
    }

    /**
     * 신청자 소유 여부 확인 (ID 기반)
     */
    public boolean isOwnedBy(Long userId) {
        return this.requester != null && this.requester.getId().equals(userId);
    }

    /**
     * 상태 확인 메서드들
     */
    public boolean isPending() {
        return this.status == LabCreationStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == LabCreationStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == LabCreationStatus.REJECTED;
    }

    /**
     * 신청할 랩실 이름 검증
     */
    private String validateRequestedLabName(String requestedLabName) {
        if (!StringUtils.hasText(requestedLabName)) {
            throw new LabCreationRequestValidationException(LabCreationRequestErrorCode.REQUESTED_LAB_NAME_REQUIRED);
        }
        String trimmed = requestedLabName.trim();
        if (trimmed.length() > 10) {
            throw new LabCreationRequestValidationException(LabCreationRequestErrorCode.REQUESTED_LAB_NAME_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 신청할 랩실 카테고리 검증
     */
    private LabCategory validateRequestedCategory(LabCategory requestedCategory) {
        if (requestedCategory == null) {
            throw new LabCreationRequestValidationException(LabCreationRequestErrorCode.REQUESTED_CATEGORY_REQUIRED);
        }
        return requestedCategory;
    }

    /**
     * 신청할 랩실 설명 검증
     */
    private String validateRequestedDescription(String requestedDescription) {
        if (requestedDescription == null) {
            return null;
        }
        String trimmed = requestedDescription.trim();
        if (trimmed.length() > 255) {
            throw new LabCreationRequestValidationException(LabCreationRequestErrorCode.REQUESTED_DESCRIPTION_TOO_LONG);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 신청자 검증
     */
    private User validateRequester(User requester) {
        if (requester == null) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.REQUESTER_REQUIRED
            );
        }
        return requester;
    }

    /**
     * 거절 사유 검증
     */
    private String validateRejectionReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 500) {
            throw new LabCreationRequestValidationException(LabCreationRequestErrorCode.REJECTION_REASON_TOO_LONG);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }
}
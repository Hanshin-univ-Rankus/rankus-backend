package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.exception.*;
import org.univ.rankus.domain.model.user.User;  // 추가: User 엔티티 직접 참조
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "lab_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존과 동일하게 Lab을 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    // ★ User 엔티티를 직접 참조하도록 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "interview_time", nullable = false)
    private LocalDateTime interviewTime;  // 면접 예정 시간 (필수)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;     // 신청 상태 (PENDING, APPROVED, REJECTED)

    /**
     * 변경된 생성자:
     *  - 기존: (Lab lab, Long userId, String userName, LocalDateTime interviewTime)
     *  - 삭제: userId, userName → 대신 User 엔티티 전체를 넘겨받음
     */
    public LabApplication(Lab lab, User user, LocalDateTime interviewTime) {
        if (lab == null) {
            throw new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND);
        }
        this.lab = lab;

        if (user == null) {
            throw new UserNotFoundException(UserErrorCode.USER_NOT_FOUND);
        }
        this.user = user;

        if (interviewTime == null || interviewTime.isBefore(LocalDateTime.now())) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.INVALID_INTERVIEW_TIME);
        }
        this.interviewTime = interviewTime;


        this.status = ApplicationStatus.PENDING;  // 기본 상태

        // (필요 시) 도메인 내부에서 “자기 랩에 이미 신청했는지” 검증 로직을 추가할 수도 있습니다.
    }

    // interviewTime 검증을 원한다면 private 메서드로 분리해도 무방합니다.
    // 예:
    // private LocalDateTime validateInterviewTime(LocalDateTime interviewTime) { … }

    public void approve() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED);
        }
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED);
        }
        this.status = ApplicationStatus.REJECTED;
    }

    public boolean isOwnedBy(Long userId) {
        return user != null && user.getId().equals(userId);
    }
}
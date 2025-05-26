package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 랩실 가입 신청 도메인 엔티티
 */
@Getter
@Entity
@Table(name = "lab_applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 리플렉션용
public class LabApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lab ↔ LabApplication : 1:N
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    // 아직 User 엔티티 미도입 상태를 가정, 단순 userId로 참조
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    // 면접 예정 시간
    @Column(name = "interview_time", nullable = false)
    private LocalDateTime interviewTime;

    /**
     * 생성자: 필수 파라미터 검증 + 기본 상태 설정
     */
    public LabApplication(Lab lab, Long userId, LocalDateTime interviewTime) {
        this.lab = Objects.requireNonNull(lab, "Lab은 필수입니다.");
        this.userId = Objects.requireNonNull(userId, "userId는 필수입니다.");
        this.interviewTime = Objects.requireNonNull(interviewTime, "interviewTime은 필수입니다.");
        this.status = ApplicationStatus.PENDING; // 초기 상태
    }

    /** 신청을 승인 처리 */
    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    /** 신청을 거절 처리 */
    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
}

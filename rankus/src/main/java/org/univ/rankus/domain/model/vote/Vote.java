package org.univ.rankus.domain.model.vote;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vote 엔티티 - 투표 정보를 나타내는 도메인 모델
 */
@Getter
@Entity
@Table(name = "votes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 투표 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // 투표 생성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab; // 투표가 속한 랩실

    @Column(nullable = false, length = 200)
    private String title; // 투표 제목

    @Column(length = 1000)
    private String description; // 투표 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoteStatus status; // 투표 상태

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline; // 투표 마감일

    @Column(name = "total_votes", nullable = false)
    private Integer totalVotes; // 총 투표 수

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteOption> options = new ArrayList<>(); // 투표 선택지들

    /**
     * 생성자: 투표 생성
     */
    public Vote(User creator, Lab lab, String title, String description, LocalDateTime deadline) {
        this.creator = validateCreator(creator);
        this.lab = validateLab(lab);
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.deadline = validateDeadline(deadline);
        this.status = VoteStatus.ACTIVE;
        this.totalVotes = 0;
    }

    /**
     * 생성자: 투표 생성 (설명 없음)
     */
    public Vote(User creator, Lab lab, String title, LocalDateTime deadline) {
        this(creator, lab, title, null, deadline);
    }

    /**
     * 생성자 유효성 검증
     */
    private User validateCreator(User creator) {
        if (creator == null) {
            throw new VoteValidationException(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED);
        }
        return creator;
    }

    /**
     * 랩실 유효성 검증
     */
    private Lab validateLab(Lab lab) {
        if (lab == null) {
            throw new VoteValidationException(VoteErrorCode.VOTE_NOT_FOUND);
        }
        return lab;
    }

    /**
     * 제목 유효성 검증
     */
    private String validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new VoteValidationException(VoteErrorCode.VOTE_TITLE_REQUIRED);
        }
        String trimmed = title.trim();
        if (trimmed.length() > 200) {
            throw new VoteValidationException(VoteErrorCode.VOTE_TITLE_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 설명 유효성 검증
     */
    private String validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > 1000) {
            throw new VoteValidationException(VoteErrorCode.VOTE_DESCRIPTION_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 마감일 유효성 검증
     */
    private LocalDateTime validateDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            throw new VoteValidationException(VoteErrorCode.VOTE_DEADLINE_REQUIRED);
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new VoteValidationException(VoteErrorCode.VOTE_DEADLINE_PAST);
        }
        return deadline;
    }

    /**
     * 투표 선택지 추가
     */
    public void addOption(String optionText, Integer optionOrder) {
        if (this.options.size() >= 5) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTIONS_TOO_MANY);
        }
        VoteOption option = new VoteOption(this, optionText, optionOrder);
        this.options.add(option);
    }

    /**
     * 투표 선택지들 설정 (한번에)
     */
    public void setOptions(List<String> optionTexts) {
        if (optionTexts == null || optionTexts.size() < 2) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTIONS_REQUIRED);
        }
        if (optionTexts.size() > 5) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTIONS_TOO_MANY);
        }

        this.options.clear();
        for (int i = 0; i < optionTexts.size(); i++) {
            addOption(optionTexts.get(i), i + 1);
        }
    }

    /**
     * 투표 종료
     */
    public void close() {
        if (!this.status.canTransitionTo(VoteStatus.CLOSED)) {
            throw new VoteValidationException(VoteErrorCode.VOTE_INVALID_STATUS_TRANSITION);
        }
        this.status = VoteStatus.CLOSED;
    }

    /**
     * 투표 취소
     */
    public void cancel() {
        if (!this.status.canTransitionTo(VoteStatus.CANCELED)) {
            throw new VoteValidationException(VoteErrorCode.VOTE_INVALID_STATUS_TRANSITION);
        }
        this.status = VoteStatus.CANCELED;
    }

    /**
     * 투표 참여 증가
     */
    public void incrementTotalVotes() {
        this.totalVotes++;
    }

    /**
     * 투표 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.deadline);
    }

    /**
     * 투표 참여 가능 여부 확인
     */
    public boolean canParticipate() {
        return this.status.isActive() && !isExpired();
    }

    /**
     * 투표 삭제 가능 여부 확인
     */
    public boolean canBeDeleted() {
        return this.totalVotes == 0;
    }

    /**
     * 소유자 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.creator.getId().equals(userId);
    }

    /**
     * 사용자가 해당 투표의 관리 권한을 가지는지 확인
     */
    public boolean canUserManage(User user) {
        // 투표 생성자는 항상 관리 가능
        if (this.creator.getId().equals(user.getId())) {
            return true;
        }

        // ADMIN과 PROFESSOR는 모든 투표 관리 가능
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR) {
            return true;
        }

        // 해당 랩실의 LAB_LEADER, LAB_MANAGER만 관리 가능
        return (user.getRole() == Role.LAB_LEADER || user.getRole() == Role.LAB_MANAGER)
                && user.getLab() != null
                && user.getLab().equals(this.lab);
    }

    /**
     * 사용자가 해당 투표에 참여할 수 있는지 확인
     */
    public boolean canUserParticipate(User user) {
        // 투표가 참여 가능한 상태인지 확인
        if (!canParticipate()) {
            return false;
        }

        // 해당 랩실 멤버만 참여 가능
        return user.getLab() != null && user.getLab().equals(this.lab);
    }
}
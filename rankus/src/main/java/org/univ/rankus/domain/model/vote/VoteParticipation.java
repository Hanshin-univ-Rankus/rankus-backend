package org.univ.rankus.domain.model.vote;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;

/**
 * VoteParticipation 엔티티 - 투표 참여 기록을 나타내는 도메인 모델
 */
@Getter
@Entity
@Table(name = "vote_participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vote_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteParticipation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 투표 참여 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote; // 투표

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 참여자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", nullable = false)
    private VoteOption selectedOption; // 선택한 옵션

    /**
     * 생성자: 투표 참여 기록 생성
     */
    public VoteParticipation(Vote vote, User user, VoteOption selectedOption) {
        this.vote = validateVote(vote);
        this.user = validateUser(user);
        this.selectedOption = validateSelectedOption(selectedOption);

        // 비즈니스 로직: 투표 참여 시 선택지 투표 수 증가
        this.selectedOption.incrementVoteCount();
    }

    /**
     * 투표 유효성 검증
     */
    private Vote validateVote(Vote vote) {
        if (vote == null) {
            throw new VoteValidationException(VoteErrorCode.VOTE_NOT_FOUND);
        }
        return vote;
    }

    /**
     * 사용자 유효성 검증
     */
    private User validateUser(User user) {
        if (user == null) {
            throw new VoteValidationException(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED);
        }
        return user;
    }

    /**
     * 선택 옵션 유효성 검증
     */
    private VoteOption validateSelectedOption(VoteOption selectedOption) {
        if (selectedOption == null) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTION_NOT_FOUND);
        }
        return selectedOption;
    }

    /**
     * 참여자 확인
     */
    public boolean isParticipatedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    /**
     * 투표 소속 확인
     */
    public boolean belongsToVote(Long voteId) {
        return this.vote.getId().equals(voteId);
    }
}
package org.univ.rankus.domain.model.vote;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;

/**
 * VoteOption 엔티티 - 투표 선택지 정보를 나타내는 도메인 모델
 */
@Getter
@Entity
@Table(name = "vote_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 투표 선택지 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote; // 소속 투표

    @Column(nullable = false, length = 100)
    private String optionText; // 선택지 내용

    @Column(nullable = false)
    private Integer optionOrder; // 선택지 순서

    @Column(nullable = false)
    private Integer voteCount; // 투표 수

    /**
     * 생성자: 투표 선택지 생성
     */
    public VoteOption(Vote vote, String optionText, Integer optionOrder) {
        this.vote = validateVote(vote);
        this.optionText = validateOptionText(optionText);
        this.optionOrder = validateOptionOrder(optionOrder);
        this.voteCount = 0;
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
     * 선택지 내용 유효성 검증
     */
    private String validateOptionText(String optionText) {
        if (!StringUtils.hasText(optionText)) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTION_TEXT_REQUIRED);
        }
        String trimmed = optionText.trim();
        if (trimmed.length() > 100) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTION_TEXT_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 선택지 순서 유효성 검증
     */
    private Integer validateOptionOrder(Integer optionOrder) {
        if (optionOrder == null || optionOrder < 1) {
            throw new VoteValidationException(VoteErrorCode.VOTE_OPTIONS_REQUIRED);
        }
        return optionOrder;
    }

    /**
     * 투표 수 증가
     */
    public void incrementVoteCount() {
        this.voteCount++;
    }

    /**
     * 투표 수 감소
     */
    public void decrementVoteCount() {
        if (this.voteCount > 0) {
            this.voteCount--;
        }
    }
}
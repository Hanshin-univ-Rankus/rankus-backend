package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteParticipation;

import java.time.LocalDateTime;
import java.util.List;

public interface VoteCommandUseCase {

    /**
     * 새로운 투표를 생성합니다.
     *
     * @param title       투표 제목
     * @param description 투표 설명
     * @param creatorId   생성자 ID
     * @param labId       랩실 ID
     * @param deadline    투표 마감일
     * @param optionTexts 투표 선택지 목록
     * @return 생성된 투표
     */
    Vote createVote(String title, String description, Long creatorId, Long labId, LocalDateTime deadline, List<String> optionTexts);

    /**
     * 설명 없이 투표를 생성합니다.
     *
     * @param title       투표 제목
     * @param creatorId   생성자 ID
     * @param labId       랩실 ID
     * @param deadline    투표 마감일
     * @param optionTexts 투표 선택지 목록
     * @return 생성된 투표
     */
    Vote createVote(String title, Long creatorId, Long labId, LocalDateTime deadline, List<String> optionTexts);

    /**
     * 투표를 삭제합니다.
     *
     * @param voteId 투표 ID
     */
    void deleteVote(Long voteId);

    /**
     * 투표를 종료합니다.
     *
     * @param voteId 투표 ID
     * @return 종료된 투표
     */
    Vote closeVote(Long voteId);

    /**
     * 투표를 취소합니다.
     *
     * @param voteId 투표 ID
     * @return 취소된 투표
     */
    Vote cancelVote(Long voteId);

    /**
     * 투표에 참여합니다.
     *
     * @param voteId           투표 ID
     * @param userId           참여자 ID
     * @param selectedOptionId 선택한 옵션 ID
     * @return 투표 참여 기록
     */
    VoteParticipation participateInVote(Long voteId, Long userId, Long selectedOptionId);
}
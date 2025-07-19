package org.univ.rankus.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteOption;
import org.univ.rankus.domain.model.vote.VoteParticipation;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.util.List;

public interface VoteQueryUseCase {

    /**
     * 투표 ID로 투표를 조회합니다.
     *
     * @param voteId 투표 ID
     * @return 투표 정보
     */
    Vote findVoteById(Long voteId);

    /**
     * 랩실의 모든 투표를 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 투표 목록
     */
    List<Vote> findVotesByLabId(Long labId);

    /**
     * 랩실의 투표를 페이지별로 조회합니다.
     *
     * @param labId    랩실 ID
     * @param pageable 페이지 정보
     * @return 페이지별 투표 목록
     */
    Page<Vote> findVotesByLabId(Long labId, Pageable pageable);

    /**
     * 랩실의 특정 상태 투표를 조회합니다.
     *
     * @param labId  랩실 ID
     * @param status 투표 상태
     * @return 투표 목록
     */
    List<Vote> findVotesByLabIdAndStatus(Long labId, VoteStatus status);

    /**
     * 사용자가 생성한 투표를 조회합니다.
     *
     * @param creatorId 생성자 ID
     * @return 투표 목록
     */
    List<Vote> findVotesByCreatorId(Long creatorId);

    /**
     * 랩실의 활성 투표를 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 활성 투표 목록
     */
    List<Vote> findActiveVotesByLabId(Long labId);

    /**
     * 투표의 선택지를 조회합니다.
     *
     * @param voteId 투표 ID
     * @return 선택지 목록
     */
    List<VoteOption> findVoteOptionsByVoteId(Long voteId);

    /**
     * 투표의 참여 기록을 조회합니다.
     *
     * @param voteId 투표 ID
     * @return 참여 기록 목록
     */
    List<VoteParticipation> findVoteParticipationsByVoteId(Long voteId);

    /**
     * 사용자의 특정 투표 참여 기록을 조회합니다.
     *
     * @param voteId 투표 ID
     * @param userId 사용자 ID
     * @return 참여 기록 (없으면 null)
     */
    VoteParticipation findVoteParticipationByVoteIdAndUserId(Long voteId, Long userId);

    /**
     * 사용자가 투표에 참여했는지 확인합니다.
     *
     * @param voteId 투표 ID
     * @param userId 사용자 ID
     * @return 참여 여부
     */
    boolean hasUserParticipated(Long voteId, Long userId);

    /**
     * 랩실의 투표 개수를 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 투표 개수
     */
    long countVotesByLabId(Long labId);

    /**
     * 랩실의 특정 상태 투표 개수를 조회합니다.
     *
     * @param labId  랩실 ID
     * @param status 투표 상태
     * @return 투표 개수
     */
    long countVotesByLabIdAndStatus(Long labId, VoteStatus status);
}
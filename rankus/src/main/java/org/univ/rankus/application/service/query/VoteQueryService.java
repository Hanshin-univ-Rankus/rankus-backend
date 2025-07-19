package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.VoteQueryUseCase;
import org.univ.rankus.application.port.out.VoteOptionRepositoryPort;
import org.univ.rankus.application.port.out.VoteParticipationRepositoryPort;
import org.univ.rankus.application.port.out.VoteRepositoryPort;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteOption;
import org.univ.rankus.domain.model.vote.VoteParticipation;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteQueryService implements VoteQueryUseCase {

    private final VoteRepositoryPort voteRepositoryPort;
    private final VoteOptionRepositoryPort voteOptionRepositoryPort;
    private final VoteParticipationRepositoryPort voteParticipationRepositoryPort;

    @Override
    public Vote findVoteById(Long voteId) {
        return voteRepositoryPort.findById(voteId)
                .orElseThrow(() -> new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND));
    }

    @Override
    public List<Vote> findVotesByLabId(Long labId) {
        return voteRepositoryPort.findByLabIdOrderByCreatedAtDesc(labId);
    }

    @Override
    public Page<Vote> findVotesByLabId(Long labId, Pageable pageable) {
        return voteRepositoryPort.findByLabIdOrderByCreatedAtDesc(labId, pageable);
    }

    @Override
    public List<Vote> findVotesByLabIdAndStatus(Long labId, VoteStatus status) {
        return voteRepositoryPort.findByLabIdAndStatus(labId, status);
    }

    @Override
    public List<Vote> findVotesByCreatorId(Long creatorId) {
        return voteRepositoryPort.findByCreatorId(creatorId);
    }

    @Override
    public List<Vote> findActiveVotesByLabId(Long labId) {
        return voteRepositoryPort.findActiveVotesByLabId(labId);
    }

    @Override
    public List<VoteOption> findVoteOptionsByVoteId(Long voteId) {
        return voteOptionRepositoryPort.findByVoteIdOrderByOptionOrder(voteId);
    }

    @Override
    public List<VoteParticipation> findVoteParticipationsByVoteId(Long voteId) {
        return voteParticipationRepositoryPort.findByVoteId(voteId);
    }

    @Override
    public VoteParticipation findVoteParticipationByVoteIdAndUserId(Long voteId, Long userId) {
        return voteParticipationRepositoryPort.findByVoteIdAndUserId(voteId, userId).orElse(null);
    }

    @Override
    public boolean hasUserParticipated(Long voteId, Long userId) {
        return voteParticipationRepositoryPort.existsByVoteIdAndUserId(voteId, userId);
    }

    @Override
    public long countVotesByLabId(Long labId) {
        return voteRepositoryPort.countByLabId(labId);
    }

    @Override
    public long countVotesByLabIdAndStatus(Long labId, VoteStatus status) {
        return voteRepositoryPort.countByLabIdAndStatus(labId, status);
    }
}
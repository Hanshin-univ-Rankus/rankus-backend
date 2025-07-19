package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.vote.VoteParticipation;

import java.util.List;
import java.util.Optional;

public interface VoteParticipationRepositoryPort {

    VoteParticipation save(VoteParticipation voteParticipation);

    Optional<VoteParticipation> findById(Long id);

    Optional<VoteParticipation> findByVoteIdAndUserId(Long voteId, Long userId);

    List<VoteParticipation> findByVoteId(Long voteId);

    List<VoteParticipation> findByUserId(Long userId);

    List<VoteParticipation> findBySelectedOptionId(Long selectedOptionId);

    void deleteById(Long id);

    void deleteByVoteId(Long voteId);

    boolean existsById(Long id);

    boolean existsByVoteIdAndUserId(Long voteId, Long userId);

    long countByVoteId(Long voteId);

    long countByUserId(Long userId);

    long countBySelectedOptionId(Long selectedOptionId);
}
package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.vote.VoteOption;

import java.util.List;
import java.util.Optional;

public interface VoteOptionRepositoryPort {

    VoteOption save(VoteOption voteOption);

    Optional<VoteOption> findById(Long id);

    List<VoteOption> findByVoteId(Long voteId);

    List<VoteOption> findByVoteIdOrderByOptionOrder(Long voteId);

    void deleteById(Long id);

    void deleteByVoteId(Long voteId);

    boolean existsById(Long id);

    long countByVoteId(Long voteId);
}
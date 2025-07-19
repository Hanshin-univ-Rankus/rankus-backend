package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.vote.VoteParticipation;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataVoteParticipationRepository extends JpaRepository<VoteParticipation, Long> {

    Optional<VoteParticipation> findByVoteIdAndUserId(Long voteId, Long userId);

    List<VoteParticipation> findByVoteId(Long voteId);

    List<VoteParticipation> findByUserId(Long userId);

    List<VoteParticipation> findBySelectedOptionId(Long selectedOptionId);

    void deleteByVoteId(Long voteId);

    boolean existsByVoteIdAndUserId(Long voteId, Long userId);

    long countByVoteId(Long voteId);

    long countByUserId(Long userId);

    long countBySelectedOptionId(Long selectedOptionId);
}
package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.vote.VoteOption;

import java.util.List;

@Repository
public interface SpringDataVoteOptionRepository extends JpaRepository<VoteOption, Long> {

    List<VoteOption> findByVoteId(Long voteId);

    @Query("SELECT vo FROM VoteOption vo WHERE vo.vote.id = :voteId ORDER BY vo.optionOrder ASC")
    List<VoteOption> findByVoteIdOrderByOptionOrder(@Param("voteId") Long voteId);

    void deleteByVoteId(Long voteId);

    long countByVoteId(Long voteId);
}
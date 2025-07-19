package org.univ.rankus.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.util.List;
import java.util.Optional;

public interface VoteRepositoryPort {

    Vote save(Vote vote);

    Optional<Vote> findById(Long id);

    List<Vote> findByLabId(Long labId);

    List<Vote> findByLabIdOrderByCreatedAtDesc(Long labId);

    List<Vote> findByLabIdAndStatus(Long labId, VoteStatus status);

    Page<Vote> findByLabId(Long labId, Pageable pageable);

    Page<Vote> findByLabIdOrderByCreatedAtDesc(Long labId, Pageable pageable);

    Page<Vote> findByLabIdAndStatus(Long labId, VoteStatus status, Pageable pageable);

    List<Vote> findByCreatorId(Long creatorId);

    List<Vote> findActiveVotesByLabId(Long labId);

    void deleteById(Long id);

    boolean existsById(Long id);

    long countByLabId(Long labId);

    long countByLabIdAndStatus(Long labId, VoteStatus status);

    long countByCreatorId(Long creatorId);
}
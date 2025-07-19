package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.util.List;

@Repository
public interface SpringDataVoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByLabId(Long labId);

    @Query("SELECT v FROM Vote v WHERE v.lab.id = :labId ORDER BY v.createdAt DESC")
    List<Vote> findByLabIdOrderByCreatedAtDesc(@Param("labId") Long labId);

    @Query("SELECT v FROM Vote v WHERE v.lab.id = :labId ORDER BY v.createdAt DESC")
    Page<Vote> findByLabIdOrderByCreatedAtDesc(@Param("labId") Long labId, Pageable pageable);

    List<Vote> findByLabIdAndStatus(Long labId, VoteStatus status);

    @Query("SELECT v FROM Vote v WHERE v.lab.id = :labId AND v.status = :status ORDER BY v.createdAt DESC")
    Page<Vote> findByLabIdAndStatusOrderByCreatedAtDesc(@Param("labId") Long labId, @Param("status") VoteStatus status, Pageable pageable);

    List<Vote> findByCreatorId(Long creatorId);

    @Query("SELECT v FROM Vote v WHERE v.lab.id = :labId AND v.status = 'ACTIVE' ORDER BY v.createdAt DESC")
    List<Vote> findActiveVotesByLabId(@Param("labId") Long labId);

    long countByLabId(Long labId);

    long countByLabIdAndStatus(Long labId, VoteStatus status);

    long countByCreatorId(Long creatorId);
}
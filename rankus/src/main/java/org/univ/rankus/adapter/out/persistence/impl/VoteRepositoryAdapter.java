package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataVoteRepository;
import org.univ.rankus.application.port.out.VoteRepositoryPort;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VoteRepositoryAdapter implements VoteRepositoryPort {

    private final SpringDataVoteRepository springDataVoteRepository;

    @Override
    public Vote save(Vote vote) {
        return springDataVoteRepository.save(vote);
    }

    @Override
    public Optional<Vote> findById(Long id) {
        return springDataVoteRepository.findById(id);
    }

    @Override
    public List<Vote> findByLabId(Long labId) {
        return springDataVoteRepository.findByLabId(labId);
    }

    @Override
    public List<Vote> findByLabIdOrderByCreatedAtDesc(Long labId) {
        return springDataVoteRepository.findByLabIdOrderByCreatedAtDesc(labId);
    }

    @Override
    public List<Vote> findByLabIdAndStatus(Long labId, VoteStatus status) {
        return springDataVoteRepository.findByLabIdAndStatus(labId, status);
    }

    @Override
    public Page<Vote> findByLabId(Long labId, Pageable pageable) {
        return springDataVoteRepository.findByLabIdOrderByCreatedAtDesc(labId, pageable);
    }

    @Override
    public Page<Vote> findByLabIdOrderByCreatedAtDesc(Long labId, Pageable pageable) {
        return springDataVoteRepository.findByLabIdOrderByCreatedAtDesc(labId, pageable);
    }

    @Override
    public Page<Vote> findByLabIdAndStatus(Long labId, VoteStatus status, Pageable pageable) {
        return springDataVoteRepository.findByLabIdAndStatusOrderByCreatedAtDesc(labId, status, pageable);
    }

    @Override
    public List<Vote> findByCreatorId(Long creatorId) {
        return springDataVoteRepository.findByCreatorId(creatorId);
    }

    @Override
    public List<Vote> findActiveVotesByLabId(Long labId) {
        return springDataVoteRepository.findActiveVotesByLabId(labId);
    }

    @Override
    public void deleteById(Long id) {
        springDataVoteRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataVoteRepository.existsById(id);
    }

    @Override
    public long countByLabId(Long labId) {
        return springDataVoteRepository.countByLabId(labId);
    }

    @Override
    public long countByLabIdAndStatus(Long labId, VoteStatus status) {
        return springDataVoteRepository.countByLabIdAndStatus(labId, status);
    }

    @Override
    public long countByCreatorId(Long creatorId) {
        return springDataVoteRepository.countByCreatorId(creatorId);
    }
}
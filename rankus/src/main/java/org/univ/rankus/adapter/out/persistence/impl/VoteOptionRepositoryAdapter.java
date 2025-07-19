package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataVoteOptionRepository;
import org.univ.rankus.application.port.out.VoteOptionRepositoryPort;
import org.univ.rankus.domain.model.vote.VoteOption;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VoteOptionRepositoryAdapter implements VoteOptionRepositoryPort {

    private final SpringDataVoteOptionRepository springDataVoteOptionRepository;

    @Override
    public VoteOption save(VoteOption voteOption) {
        return springDataVoteOptionRepository.save(voteOption);
    }

    @Override
    public Optional<VoteOption> findById(Long id) {
        return springDataVoteOptionRepository.findById(id);
    }

    @Override
    public List<VoteOption> findByVoteId(Long voteId) {
        return springDataVoteOptionRepository.findByVoteId(voteId);
    }

    @Override
    public List<VoteOption> findByVoteIdOrderByOptionOrder(Long voteId) {
        return springDataVoteOptionRepository.findByVoteIdOrderByOptionOrder(voteId);
    }

    @Override
    public void deleteById(Long id) {
        springDataVoteOptionRepository.deleteById(id);
    }

    @Override
    public void deleteByVoteId(Long voteId) {
        springDataVoteOptionRepository.deleteByVoteId(voteId);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataVoteOptionRepository.existsById(id);
    }

    @Override
    public long countByVoteId(Long voteId) {
        return springDataVoteOptionRepository.countByVoteId(voteId);
    }
}
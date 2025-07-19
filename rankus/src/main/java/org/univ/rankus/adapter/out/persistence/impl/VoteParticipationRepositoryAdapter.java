package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataVoteParticipationRepository;
import org.univ.rankus.application.port.out.VoteParticipationRepositoryPort;
import org.univ.rankus.domain.model.vote.VoteParticipation;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VoteParticipationRepositoryAdapter implements VoteParticipationRepositoryPort {

    private final SpringDataVoteParticipationRepository springDataVoteParticipationRepository;

    @Override
    public VoteParticipation save(VoteParticipation voteParticipation) {
        return springDataVoteParticipationRepository.save(voteParticipation);
    }

    @Override
    public Optional<VoteParticipation> findById(Long id) {
        return springDataVoteParticipationRepository.findById(id);
    }

    @Override
    public Optional<VoteParticipation> findByVoteIdAndUserId(Long voteId, Long userId) {
        return springDataVoteParticipationRepository.findByVoteIdAndUserId(voteId, userId);
    }

    @Override
    public List<VoteParticipation> findByVoteId(Long voteId) {
        return springDataVoteParticipationRepository.findByVoteId(voteId);
    }

    @Override
    public List<VoteParticipation> findByUserId(Long userId) {
        return springDataVoteParticipationRepository.findByUserId(userId);
    }

    @Override
    public List<VoteParticipation> findBySelectedOptionId(Long selectedOptionId) {
        return springDataVoteParticipationRepository.findBySelectedOptionId(selectedOptionId);
    }

    @Override
    public void deleteById(Long id) {
        springDataVoteParticipationRepository.deleteById(id);
    }

    @Override
    public void deleteByVoteId(Long voteId) {
        springDataVoteParticipationRepository.deleteByVoteId(voteId);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataVoteParticipationRepository.existsById(id);
    }

    @Override
    public boolean existsByVoteIdAndUserId(Long voteId, Long userId) {
        return springDataVoteParticipationRepository.existsByVoteIdAndUserId(voteId, userId);
    }

    @Override
    public long countByVoteId(Long voteId) {
        return springDataVoteParticipationRepository.countByVoteId(voteId);
    }

    @Override
    public long countByUserId(Long userId) {
        return springDataVoteParticipationRepository.countByUserId(userId);
    }

    @Override
    public long countBySelectedOptionId(Long selectedOptionId) {
        return springDataVoteParticipationRepository.countBySelectedOptionId(selectedOptionId);
    }
}
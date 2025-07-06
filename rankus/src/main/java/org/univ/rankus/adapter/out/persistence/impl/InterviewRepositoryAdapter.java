package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataInterviewRepository;
import org.univ.rankus.application.port.out.InterviewRepositoryPort;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewStatus;

import java.util.List;
import java.util.Optional;

/**
 * InterviewRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataInterviewRepository를 호출하여 실제 DB 저장·조회·삭제 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class InterviewRepositoryAdapter implements InterviewRepositoryPort {

    private final SpringDataInterviewRepository springDataInterviewRepository;

    @Override
    public Interview save(Interview interview) {
        return springDataInterviewRepository.save(interview);
    }

    @Override
    public Optional<Interview> findById(Long id) {
        return springDataInterviewRepository.findById(id);
    }

    @Override
    public List<Interview> findByLabId(Long labId) {
        return springDataInterviewRepository.findByLabId(labId);
    }

    @Override
    public List<Interview> findByLabIdAndStatus(Long labId, InterviewStatus status) {
        return springDataInterviewRepository.findByLabIdAndStatus(labId, status);
    }

    @Override
    public boolean existsByLabIdAndStatus(Long labId, InterviewStatus status) {
        return springDataInterviewRepository.existsByLabIdAndStatus(labId, status);
    }

    @Override
    public void delete(Interview interview) {
        springDataInterviewRepository.delete(interview);
    }

    @Override
    public void deleteById(Long id) {
        springDataInterviewRepository.deleteById(id);
    }

    @Override
    public List<Interview> findAll() {
        return springDataInterviewRepository.findAll();
    }

    @Override
    public List<Interview> findByStatus(InterviewStatus status) {
        return springDataInterviewRepository.findByStatus(status);
    }
}
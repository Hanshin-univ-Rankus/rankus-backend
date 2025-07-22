package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataInterviewSlotRepository;
import org.univ.rankus.application.port.out.InterviewSlotRepositoryPort;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.SlotStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * InterviewSlotRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataInterviewSlotRepository를 호출하여 실제 DB 저장·조회·삭제 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class InterviewSlotRepositoryAdapter implements InterviewSlotRepositoryPort {

    private final SpringDataInterviewSlotRepository springDataInterviewSlotRepository;

    @Override
    public InterviewSlot save(InterviewSlot slot) {
        return springDataInterviewSlotRepository.save(slot);
    }

    @Override
    public Optional<InterviewSlot> findById(Long id) {
        return springDataInterviewSlotRepository.findById(id);
    }

    @Override
    public List<InterviewSlot> findByInterviewId(Long interviewId) {
        return springDataInterviewSlotRepository.findByInterviewId(interviewId);
    }

    @Override
    public List<InterviewSlot> findByInterviewIdAndStatus(Long interviewId, SlotStatus status) {
        return springDataInterviewSlotRepository.findByInterviewIdAndStatus(interviewId, status);
    }

    @Override
    public List<InterviewSlot> findAvailableSlotsByInterviewId(Long interviewId) {
        return springDataInterviewSlotRepository.findAvailableSlotsByInterviewId(interviewId);
    }

    @Override
    public boolean existsByInterviewIdAndTimeRange(Long interviewId, LocalDateTime startTime, LocalDateTime endTime) {
        return springDataInterviewSlotRepository.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime);
    }

    @Override
    public void delete(InterviewSlot slot) {
        springDataInterviewSlotRepository.delete(slot);
    }

    @Override
    public void deleteById(Long id) {
        springDataInterviewSlotRepository.deleteById(id);
    }

    @Override
    public void deleteByInterviewId(Long interviewId) {
        springDataInterviewSlotRepository.deleteByInterviewId(interviewId);
    }

    @Override
    public List<InterviewSlot> findAll() {
        return springDataInterviewSlotRepository.findAll();
    }

    @Override
    public List<InterviewSlot> findByStatus(SlotStatus status) {
        return springDataInterviewSlotRepository.findByStatus(status);
    }

    @Override
    public List<InterviewSlot> findByStartTimeAfter(LocalDateTime dateTime) {
        return springDataInterviewSlotRepository.findByStartTimeAfter(dateTime);
    }

    @Override
    public List<InterviewSlot> findByStartTimeBefore(LocalDateTime dateTime) {
        return springDataInterviewSlotRepository.findByStartTimeBefore(dateTime);
    }

    @Override
    public Optional<InterviewSlot> findByIdForUpdate(Long id) {
        return Optional.ofNullable(springDataInterviewSlotRepository.findByIdForUpdate(id));
    }
}
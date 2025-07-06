package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.InterviewQueryUseCase;
import org.univ.rankus.application.port.out.InterviewRepositoryPort;
import org.univ.rankus.application.port.out.InterviewSlotRepositoryPort;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.InterviewStatus;
import org.univ.rankus.domain.model.interview.SlotStatus;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview 도메인 Query 서비스 구현체
 * - 면접 조회, 슬롯 조회 등 읽기 전용 작업 수행
 * - 트랜잭션: 읽기 메서드(@Transactional(readOnly = true))
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewQueryService implements InterviewQueryUseCase {

    private final InterviewRepositoryPort interviewRepositoryPort;
    private final InterviewSlotRepositoryPort slotRepositoryPort;

    @Override
    public Interview getInterviewById(Long id) {
        return interviewRepositoryPort.findById(id)
            .orElseThrow(() -> new InterviewNotFoundException(InterviewErrorCode.INTERVIEW_NOT_FOUND));
    }

    @Override
    public List<Interview> getInterviewsByLabId(Long labId) {
        return interviewRepositoryPort.findByLabId(labId);
    }

    @Override
    public List<Interview> getInterviewsByLabIdAndStatus(Long labId, InterviewStatus status) {
        return interviewRepositoryPort.findByLabIdAndStatus(labId, status);
    }

    @Override
    public List<Interview> getActiveInterviewsByLabId(Long labId) {
        return interviewRepositoryPort.findByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
    }

    @Override
    public Page<Interview> getAllInterviews(Pageable pageable) {
        // Page 구현을 위해 별도 처리 필요 - 현재는 기본 구현
        List<Interview> interviews = interviewRepositoryPort.findAll();
        return Page.empty(pageable); // TODO: 실제 페이징 구현 시 수정 필요
    }

    @Override
    public List<Interview> getInterviewsByStatus(InterviewStatus status) {
        return interviewRepositoryPort.findByStatus(status);
    }

    @Override
    public InterviewSlot getInterviewSlotById(Long id) {
        return slotRepositoryPort.findById(id)
            .orElseThrow(() -> new InterviewNotFoundException(InterviewErrorCode.SLOT_NOT_FOUND));
    }

    @Override
    public List<InterviewSlot> getSlotsByInterviewId(Long interviewId) {
        return slotRepositoryPort.findByInterviewId(interviewId);
    }

    @Override
    public List<InterviewSlot> getAvailableSlotsByInterviewId(Long interviewId) {
        return slotRepositoryPort.findAvailableSlotsByInterviewId(interviewId);
    }

    @Override
    public List<InterviewSlot> getSlotsByInterviewIdAndStatus(Long interviewId, SlotStatus status) {
        return slotRepositoryPort.findByInterviewIdAndStatus(interviewId, status);
    }

    @Override
    public List<InterviewSlot> getSlotsByInterviewIdOrderByTime(Long interviewId) {
        // Repository에 구현된 메서드가 있다면 사용, 없다면 기본 조회 후 정렬
        List<InterviewSlot> slots = slotRepositoryPort.findByInterviewId(interviewId);
        return slots.stream()
            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
            .toList();
    }

    @Override
    public List<InterviewSlot> getSlotsAfter(LocalDateTime dateTime) {
        return slotRepositoryPort.findByStartTimeAfter(dateTime);
    }

    @Override
    public List<InterviewSlot> getSlotsBefore(LocalDateTime dateTime) {
        return slotRepositoryPort.findByStartTimeBefore(dateTime);
    }

    @Override
    public boolean hasActiveInterview(Long labId) {
        return interviewRepositoryPort.existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
    }

    @Override
    public boolean hasReservedSlots(Long interviewId) {
        List<InterviewSlot> slots = slotRepositoryPort.findByInterviewId(interviewId);
        return slots.stream().anyMatch(slot -> slot.getCurrentApplicants() > 0);
    }

    @Override
    public boolean hasConflictingSlots(Long interviewId, LocalDateTime startTime, LocalDateTime endTime) {
        return slotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime);
    }
}
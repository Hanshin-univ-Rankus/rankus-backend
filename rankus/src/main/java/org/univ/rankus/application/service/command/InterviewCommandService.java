package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.InterviewCommandUseCase;
import org.univ.rankus.application.port.out.InterviewRepositoryPort;
import org.univ.rankus.application.port.out.InterviewSlotRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.InterviewStatus;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Interview 도메인 Command 서비스 구현체
 * - 면접 생성, 활성화, 슬롯 관리 등 상태 변경 작업 수행
 * - 트랜잭션: 쓰기 메서드(@Transactional)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InterviewCommandService implements InterviewCommandUseCase {

    private final InterviewRepositoryPort interviewRepositoryPort;
    private final InterviewSlotRepositoryPort slotRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    @Override
    public Interview createInterview(Long labId, LocalDate startDate, LocalDate endDate,
                                     Integer durationMinutes, Integer maxApplicantsPerSlot) {

        // 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 해당 랩실에 이미 활성화된 면접이 있는지 확인
        if (interviewRepositoryPort.existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE)) {
            throw new InterviewValidationException(InterviewErrorCode.DUPLICATE_INTERVIEW);
        }

        // 면접 생성
        Interview interview = new Interview(lab, startDate, endDate, durationMinutes, maxApplicantsPerSlot);

        return interviewRepositoryPort.save(interview);
    }

    @Override
    public Interview activateInterview(Long interviewId) {
        Interview interview = getInterviewById(interviewId);

        // 해당 랩실에 이미 활성화된 다른 면접이 있는지 확인
        if (interviewRepositoryPort.existsByLabIdAndStatus(interview.getLab().getId(), InterviewStatus.ACTIVE)) {
            throw new InterviewValidationException(InterviewErrorCode.DUPLICATE_INTERVIEW);
        }

        interview.activate();
        return interviewRepositoryPort.save(interview);
    }

    @Override
    public Interview deactivateInterview(Long interviewId) {
        Interview interview = getInterviewById(interviewId);
        interview.deactivate();
        return interviewRepositoryPort.save(interview);
    }

    @Override
    public Interview closeInterview(Long interviewId) {
        Interview interview = getInterviewById(interviewId);
        interview.close();
        return interviewRepositoryPort.save(interview);
    }

    @Override
    public InterviewSlot createInterviewSlot(Long interviewId, LocalDateTime startTime,
                                             LocalDateTime endTime, Integer maxApplicants) {
        Interview interview = getInterviewById(interviewId);

        // 최대 지원자 수가 null이면 면접 설정값 사용
        Integer finalMaxApplicants = maxApplicants != null ? maxApplicants : interview.getMaxApplicantsPerSlot();

        // 시간 충돌 확인
        if (slotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime)) {
            throw new InterviewValidationException(InterviewErrorCode.SLOT_TIME_CONFLICT);
        }

        // 슬롯 생성
        InterviewSlot slot = new InterviewSlot(interview, startTime, endTime, finalMaxApplicants);
        interview.addSlot(slot);

        InterviewSlot savedSlot = slotRepositoryPort.save(slot);
        interviewRepositoryPort.save(interview); // 연관관계 업데이트

        return savedSlot;
    }

    @Override
    public List<InterviewSlot> createMultipleInterviewSlots(Long interviewId,
                                                            List<SlotCreationInfo> slotInfos) {
        Interview interview = getInterviewById(interviewId);
        List<InterviewSlot> createdSlots = new ArrayList<>();

        for (SlotCreationInfo slotInfo : slotInfos) {
            // 시간 충돌 확인
            if (slotRepositoryPort.existsByInterviewIdAndTimeRange(
                    interviewId, slotInfo.startTime(), slotInfo.endTime())) {
                throw new InterviewValidationException(InterviewErrorCode.SLOT_TIME_CONFLICT);
            }

            // 최대 지원자 수 설정
            Integer maxApplicants = slotInfo.maxApplicants() != null ?
                    slotInfo.maxApplicants() : interview.getMaxApplicantsPerSlot();

            // 슬롯 생성
            InterviewSlot slot = new InterviewSlot(interview, slotInfo.startTime(),
                    slotInfo.endTime(), maxApplicants);
            interview.addSlot(slot);

            InterviewSlot savedSlot = slotRepositoryPort.save(slot);
            createdSlots.add(savedSlot);
        }

        interviewRepositoryPort.save(interview); // 연관관계 업데이트
        return createdSlots;
    }

    @Override
    public InterviewSlot cancelInterviewSlot(Long slotId) {
        InterviewSlot slot = getInterviewSlotById(slotId);
        slot.cancel();
        return slotRepositoryPort.save(slot);
    }

    @Override
    public InterviewSlot reactivateInterviewSlot(Long slotId) {
        InterviewSlot slot = getInterviewSlotById(slotId);
        slot.reactivate();
        return slotRepositoryPort.save(slot);
    }

    @Override
    public void deleteInterviewSlot(Long slotId) {
        InterviewSlot slot = getInterviewSlotById(slotId);

        // 예약이 있는 슬롯은 삭제 불가
        if (slot.getCurrentApplicants() > 0) {
            throw new InterviewValidationException(InterviewErrorCode.CANNOT_CANCEL_SLOT_WITH_APPLICANTS);
        }

        slotRepositoryPort.delete(slot);
    }

    @Override
    public void deleteInterview(Long interviewId) {
        Interview interview = getInterviewById(interviewId);

        // 활성화된 면접은 삭제 불가
        if (interview.isActive()) {
            throw new InterviewValidationException(InterviewErrorCode.CANNOT_DEACTIVATE_CLOSED);
        }

        // 예약된 슬롯이 있는지 확인
        List<InterviewSlot> slots = slotRepositoryPort.findByInterviewId(interviewId);
        boolean hasReservations = slots.stream().anyMatch(slot -> slot.getCurrentApplicants() > 0);

        if (hasReservations) {
            throw new InterviewValidationException(InterviewErrorCode.CANNOT_CANCEL_SLOT_WITH_APPLICANTS);
        }

        // 모든 슬롯 삭제 후 면접 삭제
        slotRepositoryPort.deleteByInterviewId(interviewId);
        interviewRepositoryPort.delete(interview);
    }

    @Override
    public Interview updateInterview(Long interviewId, LocalDate startDate, LocalDate endDate,
                                     Integer durationMinutes, Integer maxApplicantsPerSlot) {
        Interview interview = getInterviewById(interviewId);

        // 활성화된 면접은 수정 제한
        if (interview.isActive()) {
            throw new InterviewValidationException(InterviewErrorCode.ALREADY_ACTIVATED);
        }

        // 새로운 면접 객체로 교체 (불변 객체 패턴)
        Lab lab = interview.getLab();
        Interview updatedInterview = new Interview(lab, startDate, endDate, durationMinutes, maxApplicantsPerSlot);

        // 기존 면접 삭제 후 새 면접 저장
        interviewRepositoryPort.delete(interview);
        return interviewRepositoryPort.save(updatedInterview);
    }

    /**
     * 면접 조회 헬퍼 메서드
     */
    private Interview getInterviewById(Long interviewId) {
        return interviewRepositoryPort.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(InterviewErrorCode.INTERVIEW_NOT_FOUND));
    }

    /**
     * 슬롯 조회 헬퍼 메서드
     */
    private InterviewSlot getInterviewSlotById(Long slotId) {
        return slotRepositoryPort.findById(slotId)
                .orElseThrow(() -> new InterviewNotFoundException(InterviewErrorCode.SLOT_NOT_FOUND));
    }
}
package org.univ.rankus.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.InterviewStatus;
import org.univ.rankus.domain.model.interview.SlotStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview 도메인 Query UseCase 인터페이스
 * - 면접 조회, 슬롯 조회 등 읽기 전용 작업 정의
 */
public interface InterviewQueryUseCase {

    /**
     * ID로 면접을 조회합니다.
     *
     * @param id 조회할 면접 ID
     * @return Interview 엔티티
     * @throws org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException 면접을 찾을 수 없는 경우
     */
    Interview getInterviewById(Long id);

    /**
     * 특정 랩실의 모든 면접을 조회합니다.
     *
     * @param labId 랩실 ID
     * @return Interview 리스트
     */
    List<Interview> getInterviewsByLabId(Long labId);

    /**
     * 특정 랩실의 특정 상태 면접을 조회합니다.
     *
     * @param labId  랩실 ID
     * @param status 면접 상태
     * @return Interview 리스트
     */
    List<Interview> getInterviewsByLabIdAndStatus(Long labId, InterviewStatus status);

    /**
     * 특정 랩실의 활성화된 면접을 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 활성화된 Interview 리스트
     */
    List<Interview> getActiveInterviewsByLabId(Long labId);

    /**
     * 모든 면접을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return Interview 페이지
     */
    Page<Interview> getAllInterviews(Pageable pageable);

    /**
     * 특정 상태의 모든 면접을 조회합니다.
     *
     * @param status 면접 상태
     * @return Interview 리스트
     */
    List<Interview> getInterviewsByStatus(InterviewStatus status);

    /**
     * ID로 면접 슬롯을 조회합니다.
     *
     * @param id 조회할 슬롯 ID
     * @return InterviewSlot 엔티티
     * @throws org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException 슬롯을 찾을 수 없는 경우
     */
    InterviewSlot getInterviewSlotById(Long id);

    /**
     * 특정 면접의 모든 슬롯을 조회합니다.
     *
     * @param interviewId 면접 ID
     * @return InterviewSlot 리스트
     */
    List<InterviewSlot> getSlotsByInterviewId(Long interviewId);

    /**
     * 특정 면접의 예약 가능한 슬롯을 조회합니다.
     *
     * @param interviewId 면접 ID
     * @return 예약 가능한 InterviewSlot 리스트
     */
    List<InterviewSlot> getAvailableSlotsByInterviewId(Long interviewId);

    /**
     * 특정 면접의 특정 상태 슬롯을 조회합니다.
     *
     * @param interviewId 면접 ID
     * @param status      슬롯 상태
     * @return InterviewSlot 리스트
     */
    List<InterviewSlot> getSlotsByInterviewIdAndStatus(Long interviewId, SlotStatus status);

    /**
     * 특정 면접의 슬롯을 시간순으로 조회합니다.
     *
     * @param interviewId 면접 ID
     * @return 시간순 정렬된 InterviewSlot 리스트
     */
    List<InterviewSlot> getSlotsByInterviewIdOrderByTime(Long interviewId);

    /**
     * 특정 시간 이후의 슬롯을 조회합니다.
     *
     * @param dateTime 기준 시간
     * @return 기준 시간 이후의 InterviewSlot 리스트
     */
    List<InterviewSlot> getSlotsAfter(LocalDateTime dateTime);

    /**
     * 특정 시간 이전의 슬롯을 조회합니다.
     *
     * @param dateTime 기준 시간
     * @return 기준 시간 이전의 InterviewSlot 리스트
     */
    List<InterviewSlot> getSlotsBefore(LocalDateTime dateTime);

    /**
     * 특정 랩실에 활성화된 면접이 있는지 확인합니다.
     *
     * @param labId 랩실 ID
     * @return 활성화된 면접이 있으면 true
     */
    boolean hasActiveInterview(Long labId);

    /**
     * 특정 면접에 예약된 슬롯이 있는지 확인합니다.
     *
     * @param interviewId 면접 ID
     * @return 예약된 슬롯이 있으면 true
     */
    boolean hasReservedSlots(Long interviewId);

    /**
     * 특정 시간 범위에 겹치는 슬롯이 있는지 확인합니다.
     *
     * @param interviewId 면접 ID
     * @param startTime   시작 시간
     * @param endTime     종료 시간
     * @return 겹치는 슬롯이 있으면 true
     */
    boolean hasConflictingSlots(Long interviewId, LocalDateTime startTime, LocalDateTime endTime);
}
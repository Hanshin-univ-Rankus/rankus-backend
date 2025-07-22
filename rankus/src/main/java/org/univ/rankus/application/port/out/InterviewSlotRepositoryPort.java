package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.SlotStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * InterviewSlot 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스 계층은 이 인터페이스를 통해서만 InterviewSlot을 저장·조회·삭제합니다.
 */
public interface InterviewSlotRepositoryPort {

    /**
     * 새로운 InterviewSlot을 저장 또는 업데이트합니다.
     *
     * @param slot 저장할 InterviewSlot 엔티티
     * @return 영속화된 InterviewSlot (저장 후 ID 포함)
     */
    InterviewSlot save(InterviewSlot slot);

    /**
     * ID로 InterviewSlot을 조회합니다.
     *
     * @param id 조회할 InterviewSlot ID
     * @return Optional.of(InterviewSlot) 또는 Optional.empty()
     */
    Optional<InterviewSlot> findById(Long id);

    /**
     * 특정 면접 ID에 속한 모든 InterviewSlot을 조회합니다.
     *
     * @param interviewId 조회할 면접 ID
     * @return 해당 면접에 속한 InterviewSlot 리스트 (빈 리스트 가능)
     */
    List<InterviewSlot> findByInterviewId(Long interviewId);

    /**
     * 특정 면접의 특정 상태인 InterviewSlot을 조회합니다.
     *
     * @param interviewId 면접 ID
     * @param status      슬롯 상태
     * @return 해당 조건에 맞는 InterviewSlot 리스트
     */
    List<InterviewSlot> findByInterviewIdAndStatus(Long interviewId, SlotStatus status);

    /**
     * 특정 면접의 예약 가능한 슬롯을 조회합니다.
     *
     * @param interviewId 면접 ID
     * @return 예약 가능한 InterviewSlot 리스트
     */
    List<InterviewSlot> findAvailableSlotsByInterviewId(Long interviewId);

    /**
     * 특정 시간 범위의 슬롯이 존재하는지 확인합니다.
     *
     * @param interviewId 면접 ID
     * @param startTime   시작 시간
     * @param endTime     종료 시간
     * @return 해당 시간 범위에 슬롯이 있으면 true
     */
    boolean existsByInterviewIdAndTimeRange(Long interviewId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * InterviewSlot을 삭제합니다.
     *
     * @param slot 삭제할 InterviewSlot 엔티티
     */
    void delete(InterviewSlot slot);

    /**
     * ID로 InterviewSlot을 삭제합니다.
     *
     * @param id 삭제할 InterviewSlot ID
     */
    void deleteById(Long id);

    /**
     * 특정 면접의 모든 슬롯을 삭제합니다.
     *
     * @param interviewId 면접 ID
     */
    void deleteByInterviewId(Long interviewId);

    /**
     * 모든 InterviewSlot을 조회합니다.
     *
     * @return 모든 InterviewSlot 리스트
     */
    List<InterviewSlot> findAll();

    /**
     * 특정 상태의 모든 슬롯을 조회합니다.
     *
     * @param status 슬롯 상태
     * @return 해당 상태의 InterviewSlot 리스트
     */
    List<InterviewSlot> findByStatus(SlotStatus status);

    /**
     * 특정 시간 이후의 슬롯을 조회합니다.
     *
     * @param dateTime 기준 시간
     * @return 기준 시간 이후의 InterviewSlot 리스트
     */
    List<InterviewSlot> findByStartTimeAfter(LocalDateTime dateTime);

    /**
     * 특정 시간 이전의 슬롯을 조회합니다.
     *
     * @param dateTime 기준 시간
     * @return 기준 시간 이전의 InterviewSlot 리스트
     */
    List<InterviewSlot> findByStartTimeBefore(LocalDateTime dateTime);

    /**
     * 슬롯 예약을 위한 비관적 잠금으로 슬롯을 조회합니다.
     * 동시 예약을 방지하기 위해 SELECT FOR UPDATE를 사용합니다.
     *
     * @param id 조회할 InterviewSlot ID
     * @return Optional.of(InterviewSlot) 또는 Optional.empty()
     */
    Optional<InterviewSlot> findByIdForUpdate(Long id);
}
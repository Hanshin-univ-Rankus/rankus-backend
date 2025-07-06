package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview 도메인 Command UseCase 인터페이스
 * - 면접 생성, 활성화, 슬롯 관리 등 상태 변경 작업 정의
 */
public interface InterviewCommandUseCase {

    /**
     * 새로운 면접을 생성합니다.
     *
     * @param labId                면접을 진행할 랩실 ID
     * @param startDate            면접 시작일
     * @param endDate              면접 종료일
     * @param durationMinutes      면접 소요 시간 (분)
     * @param maxApplicantsPerSlot 슬롯당 최대 지원자 수
     * @return 생성된 Interview 엔티티
     */
    Interview createInterview(Long labId, LocalDate startDate, LocalDate endDate,
                              Integer durationMinutes, Integer maxApplicantsPerSlot);

    /**
     * 면접을 활성화합니다.
     *
     * @param interviewId 활성화할 면접 ID
     * @return 활성화된 Interview 엔티티
     */
    Interview activateInterview(Long interviewId);

    /**
     * 면접을 비활성화합니다.
     *
     * @param interviewId 비활성화할 면접 ID
     * @return 비활성화된 Interview 엔티티
     */
    Interview deactivateInterview(Long interviewId);

    /**
     * 면접을 종료합니다.
     *
     * @param interviewId 종료할 면접 ID
     * @return 종료된 Interview 엔티티
     */
    Interview closeInterview(Long interviewId);

    /**
     * 면접 슬롯을 생성합니다.
     *
     * @param interviewId   슬롯을 추가할 면접 ID
     * @param startTime     슬롯 시작 시간
     * @param endTime       슬롯 종료 시간
     * @param maxApplicants 최대 지원자 수 (null일 경우 면접 설정값 사용)
     * @return 생성된 InterviewSlot 엔티티
     */
    InterviewSlot createInterviewSlot(Long interviewId, LocalDateTime startTime,
                                      LocalDateTime endTime, Integer maxApplicants);

    /**
     * 여러 면접 슬롯을 일괄 생성합니다.
     *
     * @param interviewId 슬롯을 추가할 면접 ID
     * @param slotInfos   슬롯 정보 리스트 (시작시간, 종료시간, 최대지원자수)
     * @return 생성된 InterviewSlot 리스트
     */
    List<InterviewSlot> createMultipleInterviewSlots(Long interviewId,
                                                     List<SlotCreationInfo> slotInfos);

    /**
     * 면접 슬롯을 취소합니다.
     *
     * @param slotId 취소할 슬롯 ID
     * @return 취소된 InterviewSlot 엔티티
     */
    InterviewSlot cancelInterviewSlot(Long slotId);

    /**
     * 면접 슬롯을 재활성화합니다.
     *
     * @param slotId 재활성화할 슬롯 ID
     * @return 재활성화된 InterviewSlot 엔티티
     */
    InterviewSlot reactivateInterviewSlot(Long slotId);

    /**
     * 면접 슬롯을 삭제합니다.
     *
     * @param slotId 삭제할 슬롯 ID
     */
    void deleteInterviewSlot(Long slotId);

    /**
     * 면접을 삭제합니다.
     *
     * @param interviewId 삭제할 면접 ID
     */
    void deleteInterview(Long interviewId);

    /**
     * 면접 설정을 수정합니다.
     *
     * @param interviewId          수정할 면접 ID
     * @param startDate            면접 시작일
     * @param endDate              면접 종료일
     * @param durationMinutes      면접 소요 시간 (분)
     * @param maxApplicantsPerSlot 슬롯당 최대 지원자 수
     * @return 수정된 Interview 엔티티
     */
    Interview updateInterview(Long interviewId, LocalDate startDate, LocalDate endDate,
                              Integer durationMinutes, Integer maxApplicantsPerSlot);

    /**
     * 슬롯 생성 정보를 담는 내부 클래스
     */
    record SlotCreationInfo(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer maxApplicants
    ) {
    }
}
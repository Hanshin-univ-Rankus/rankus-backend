package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CalendarEvent 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스는 이 인터페이스만 통해서 CalendarEvent를 저장·조회합니다.
 */
public interface CalendarEventRepositoryPort {

    /**
     * 새로운 CalendarEvent를 저장하거나 수정합니다.
     *
     * @param event 저장할 CalendarEvent 엔티티
     * @return 저장된 CalendarEvent (영속화 후 ID 포함)
     */
    CalendarEvent save(CalendarEvent event);

    /**
     * ID로 CalendarEvent를 조회합니다.
     *
     * @param id 조회할 CalendarEvent ID
     * @return Optional.of(CalendarEvent) or Optional.empty()
     */
    Optional<CalendarEvent> findById(Long id);

    /**
     * 랩별로 모든 CalendarEvent를 조회합니다.
     *
     * @param labId 조회할 랩 ID
     * @return 해당 랩의 모든 CalendarEvent 리스트
     */
    List<CalendarEvent> findByLabId(Long labId);

    /**
     * 랩별 특정 기간의 CalendarEvent를 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 CalendarEvent 리스트
     */
    List<CalendarEvent> findByLabIdAndEventDateBetween(Long labId, LocalDate startDate, LocalDate endDate);

    /**
     * 랩별 특정 타입의 기간별 CalendarEvent를 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param type      조회할 이벤트 타입
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 타입별 기간별 CalendarEvent 리스트
     */
    List<CalendarEvent> findByLabIdAndTypeAndEventDateBetween(Long labId, EventType type, LocalDate startDate, LocalDate endDate);

    /**
     * 면접 ID로 CalendarEvent를 조회합니다.
     *
     * @param interviewId 조회할 면접 ID
     * @return Optional.of(CalendarEvent) or Optional.empty()
     */
    Optional<CalendarEvent> findByInterviewId(Long interviewId);

    /**
     * 면접 ID로 연결된 모든 CalendarEvent를 조회합니다.
     *
     * @param interviewId 조회할 면접 ID
     * @return 해당 면접과 연결된 모든 CalendarEvent 리스트
     */
    List<CalendarEvent> findAllByInterviewId(Long interviewId);

    /**
     * 특정 CalendarEvent를 삭제합니다.
     *
     * @param event 삭제할 CalendarEvent 엔티티
     */
    void delete(CalendarEvent event);

    /**
     * 면접 ID로 연결된 모든 CalendarEvent를 삭제합니다.
     *
     * @param interviewId 삭제할 면접 ID
     */
    void deleteByInterviewId(Long interviewId);
}
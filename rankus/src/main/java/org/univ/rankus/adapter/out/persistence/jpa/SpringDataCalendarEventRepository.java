package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 순수 JPA 기반 CalendarEvent 저장소
 * - JpaRepository<CalendarEvent, Long>을 상속하면 기본 CRUD 메서드가 모두 제공됩니다.
 * - 캘린더 이벤트 조회에 필요한 커스텀 메서드들을 정의합니다.
 */
@Repository
public interface SpringDataCalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

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
     * 면접 ID로 연결된 모든 CalendarEvent를 삭제합니다.
     *
     * @param interviewId 삭제할 면접 ID
     */
    @Modifying
    @Query("DELETE FROM CalendarEvent c WHERE c.interviewId = :interviewId")
    void deleteByInterviewId(@Param("interviewId") Long interviewId);
}
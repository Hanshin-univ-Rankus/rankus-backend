package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * CalendarEvent 조회 유스케이스
 * - 캘린더 이벤트 조회 관련 비즈니스 로직을 정의합니다.
 */
public interface CalendarEventQueryUseCase {

    /**
     * ID로 특정 캘린더 이벤트의 상세 정보를 조회합니다.
     *
     * @param id 조회할 캘린더 이벤트 ID
     * @return 캘린더 이벤트
     * @throws CalendarEventNotFoundException 이벤트를 찾을 수 없는 경우
     */
    CalendarEvent getEventById(Long id);

    /**
     * 랩별로 모든 캘린더 이벤트를 조회합니다.
     *
     * @param labId 조회할 랩 ID
     * @return 해당 랩의 모든 캘린더 이벤트 리스트
     */
    List<CalendarEvent> getEventsByLabId(Long labId);

    /**
     * 랩별 특정 기간의 캘린더 이벤트를 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 캘린더 이벤트 리스트
     */
    List<CalendarEvent> getEventsByLabIdAndDateRange(Long labId, LocalDate startDate, LocalDate endDate);

    /**
     * 랩별 특정 기간의 일반 일정(SCHEDULE)을 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 일반 일정 리스트
     */
    List<CalendarEvent> getSchedulesByLabIdAndDateRange(Long labId, LocalDate startDate, LocalDate endDate);

    /**
     * 랩별 특정 기간의 면접 일정(INTERVIEW)을 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 면접 일정 리스트
     */
    List<CalendarEvent> getInterviewsByLabIdAndDateRange(Long labId, LocalDate startDate, LocalDate endDate);

    /**
     * 면접 ID로 연결된 캘린더 이벤트를 조회합니다.
     *
     * @param interviewId 조회할 면접 ID
     * @return 면접과 연결된 캘린더 이벤트 (옵셔널)
     * @throws CalendarEventNotFoundException 이벤트를 찾을 수 없는 경우
     */
    CalendarEvent getEventByInterviewId(Long interviewId);
}
package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.application.port.out.CalendarEventRepositoryPort;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.EventType;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * CalendarEventQueryService 구현체 (포트–어댑터 패턴 적용 버전)
 * - CalendarEventRepositoryPort만 바라보고, 내부에서 JPA 구현체는 Adapter가 처리
 * - 조회 메서드에 @Transactional(readOnly = true) 적용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 최적화
public class CalendarEventQueryService implements CalendarEventQueryUseCase {

    private final CalendarEventRepositoryPort calendarEventRepositoryPort;

    /**
     * ID로 특정 캘린더 이벤트의 상세 정보를 조회합니다.
     *
     * @param id 조회할 캘린더 이벤트 ID
     * @return 캘린더 이벤트
     * @throws CalendarEventNotFoundException 이벤트를 찾을 수 없는 경우
     */
    @Override
    public CalendarEvent getEventById(Long id) {
        return calendarEventRepositoryPort.findById(id)
                .orElseThrow(() ->
                        new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND)
                );
    }

    /**
     * 랩별로 모든 캘린더 이벤트를 조회합니다.
     *
     * @param labId 조회할 랩 ID
     * @return 해당 랩의 모든 캘린더 이벤트 리스트
     */
    @Override
    public List<CalendarEvent> getEventsByLabId(Long labId) {
        return calendarEventRepositoryPort.findByLabId(labId);
    }

    /**
     * 랩별 특정 기간의 캘린더 이벤트를 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 캘린더 이벤트 리스트
     */
    @Override
    public List<CalendarEvent> getEventsByLabIdAndDateRange(Long labId, LocalDate startDate, LocalDate endDate) {
        return calendarEventRepositoryPort.findByLabIdAndEventDateBetween(labId, startDate, endDate);
    }

    /**
     * 랩별 특정 기간의 일반 일정(SCHEDULE)을 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 일반 일정 리스트
     */
    @Override
    public List<CalendarEvent> getSchedulesByLabIdAndDateRange(Long labId, LocalDate startDate, LocalDate endDate) {
        return calendarEventRepositoryPort.findByLabIdAndTypeAndEventDateBetween(labId, EventType.SCHEDULE, startDate, endDate);
    }

    /**
     * 랩별 특정 기간의 면접 일정(INTERVIEW)을 조회합니다.
     *
     * @param labId     조회할 랩 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate   조회 종료 날짜 (포함)
     * @return 해당 랩의 기간별 면접 일정 리스트
     */
    @Override
    public List<CalendarEvent> getInterviewsByLabIdAndDateRange(Long labId, LocalDate startDate, LocalDate endDate) {
        return calendarEventRepositoryPort.findByLabIdAndTypeAndEventDateBetween(labId, EventType.INTERVIEW, startDate, endDate);
    }

    /**
     * 면접 ID로 연결된 캘린더 이벤트를 조회합니다.
     *
     * @param interviewId 조회할 면접 ID
     * @return 면접과 연결된 캘린더 이벤트
     * @throws CalendarEventNotFoundException 이벤트를 찾을 수 없는 경우
     */
    @Override
    public CalendarEvent getEventByInterviewId(Long interviewId) {
        return calendarEventRepositoryPort.findByInterviewId(interviewId)
                .orElseThrow(() ->
                        new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND)
                );
    }
}
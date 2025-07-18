package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.calendar.CalendarEvent;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * CalendarEvent 도메인 Command UseCase 인터페이스
 * - 캘린더 이벤트 생성, 수정, 삭제 등 상태 변경 작업 정의
 */
public interface CalendarEventCommandUseCase {

    /**
     * 새로운 일반 일정(SCHEDULE)을 생성합니다.
     *
     * @param labId       일정을 생성할 랩실 ID
     * @param title       일정 제목
     * @param description 일정 설명 (optional)
     * @param eventDate   일정 날짜
     * @return 생성된 CalendarEvent 엔티티
     */
    CalendarEvent createSchedule(Long labId, String title, String description, LocalDate eventDate);

    /**
     * 새로운 면접 일정(INTERVIEW)을 생성합니다.
     * Interview 시스템과 연동하여 자동으로 생성됩니다.
     *
     * @param labId       면접을 진행할 랩실 ID
     * @param title       면접 제목
     * @param description 면접 설명 (optional)
     * @param eventDate   면접 날짜
     * @param startTime   면접 시작 시간
     * @param endTime     면접 종료 시간
     * @param interviewId 연결된 면접 ID
     * @return 생성된 CalendarEvent 엔티티
     */
    CalendarEvent createInterviewEvent(Long labId, String title, String description,
                                       LocalDate eventDate, LocalTime startTime,
                                       LocalTime endTime, Long interviewId);

    /**
     * 일반 일정(SCHEDULE)을 수정합니다.
     *
     * @param eventId     수정할 이벤트 ID
     * @param title       수정할 제목
     * @param description 수정할 설명 (optional)
     * @param eventDate   수정할 날짜
     * @return 수정된 CalendarEvent 엔티티
     */
    CalendarEvent updateSchedule(Long eventId, String title, String description, LocalDate eventDate);

    /**
     * 면접 일정(INTERVIEW)을 수정합니다.
     * Interview 시스템과 연동하여 자동으로 수정됩니다.
     *
     * @param eventId     수정할 이벤트 ID
     * @param title       수정할 제목
     * @param description 수정할 설명 (optional)
     * @param eventDate   수정할 날짜
     * @param startTime   수정할 시작 시간
     * @param endTime     수정할 종료 시간
     * @return 수정된 CalendarEvent 엔티티
     */
    CalendarEvent updateInterviewEvent(Long eventId, String title, String description,
                                       LocalDate eventDate, LocalTime startTime, LocalTime endTime);

    /**
     * 캘린더 이벤트를 삭제합니다.
     *
     * @param eventId 삭제할 이벤트 ID
     */
    void deleteEvent(Long eventId);

    /**
     * 면접 ID로 연결된 모든 캘린더 이벤트를 삭제합니다.
     * Interview 시스템과 연동하여 자동으로 삭제됩니다.
     *
     * @param interviewId 삭제할 면접 ID
     */
    void deleteEventsByInterviewId(Long interviewId);

    /**
     * 면접 ID로 연결된 캘린더 이벤트를 업데이트합니다.
     * Interview 시스템과 연동하여 자동으로 업데이트됩니다.
     *
     * @param interviewId 업데이트할 면접 ID
     * @param title       수정할 제목
     * @param description 수정할 설명 (optional)
     * @param eventDate   수정할 날짜
     * @param startTime   수정할 시작 시간
     * @param endTime     수정할 종료 시간
     * @return 수정된 CalendarEvent 엔티티
     */
    CalendarEvent updateEventByInterviewId(Long interviewId, String title, String description,
                                           LocalDate eventDate, LocalTime startTime, LocalTime endTime);
}
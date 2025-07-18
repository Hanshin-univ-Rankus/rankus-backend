package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.CalendarEventCommandUseCase;
import org.univ.rankus.application.port.out.CalendarEventRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventNotFoundException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * CalendarEvent 도메인 Command 서비스 구현체
 * - 캘린더 이벤트 생성, 수정, 삭제 등 상태 변경 작업 수행
 * - 트랜잭션: 쓰기 메서드(@Transactional)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CalendarEventCommandService implements CalendarEventCommandUseCase {

    private final CalendarEventRepositoryPort calendarEventRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    /**
     * 새로운 일반 일정(SCHEDULE)을 생성합니다.
     *
     * @param labId       일정을 생성할 랩실 ID
     * @param title       일정 제목
     * @param description 일정 설명 (optional)
     * @param eventDate   일정 날짜
     * @return 생성된 CalendarEvent 엔티티
     */
    @Override
    public CalendarEvent createSchedule(Long labId, String title, String description, LocalDate eventDate) {
        // 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 일반 일정 생성
        CalendarEvent event = new CalendarEvent(lab, title, description, eventDate);

        return calendarEventRepositoryPort.save(event);
    }

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
    @Override
    public CalendarEvent createInterviewEvent(Long labId, String title, String description,
                                              LocalDate eventDate, LocalTime startTime,
                                              LocalTime endTime, Long interviewId) {
        // 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 면접 일정 생성
        CalendarEvent event = new CalendarEvent(lab, title, description, eventDate, startTime, endTime, interviewId);

        return calendarEventRepositoryPort.save(event);
    }

    /**
     * 일반 일정(SCHEDULE)을 수정합니다.
     *
     * @param eventId     수정할 이벤트 ID
     * @param title       수정할 제목
     * @param description 수정할 설명 (optional)
     * @param eventDate   수정할 날짜
     * @return 수정된 CalendarEvent 엔티티
     */
    @Override
    public CalendarEvent updateSchedule(Long eventId, String title, String description, LocalDate eventDate) {
        // 이벤트 존재 확인
        CalendarEvent event = calendarEventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

        // 일반 일정 수정
        event.updateSchedule(title, description, eventDate);

        return calendarEventRepositoryPort.save(event);
    }

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
    @Override
    public CalendarEvent updateInterviewEvent(Long eventId, String title, String description,
                                              LocalDate eventDate, LocalTime startTime, LocalTime endTime) {
        // 이벤트 존재 확인
        CalendarEvent event = calendarEventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

        // 면접 일정 수정
        event.updateInterview(title, description, eventDate, startTime, endTime);

        return calendarEventRepositoryPort.save(event);
    }

    /**
     * 캘린더 이벤트를 삭제합니다.
     *
     * @param eventId 삭제할 이벤트 ID
     */
    @Override
    public void deleteEvent(Long eventId) {
        // 이벤트 존재 확인
        CalendarEvent event = calendarEventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

        calendarEventRepositoryPort.delete(event);
    }

    /**
     * 면접 ID로 연결된 모든 캘린더 이벤트를 삭제합니다.
     * Interview 시스템과 연동하여 자동으로 삭제됩니다.
     *
     * @param interviewId 삭제할 면접 ID
     */
    @Override
    public void deleteEventsByInterviewId(Long interviewId) {
        calendarEventRepositoryPort.deleteByInterviewId(interviewId);
    }

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
    @Override
    public CalendarEvent updateEventByInterviewId(Long interviewId, String title, String description,
                                                  LocalDate eventDate, LocalTime startTime, LocalTime endTime) {
        // 면접 ID로 이벤트 조회
        CalendarEvent event = calendarEventRepositoryPort.findByInterviewId(interviewId)
                .orElseThrow(() -> new CalendarEventNotFoundException(CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND));

        // 면접 일정 수정
        event.updateInterview(title, description, eventDate, startTime, endTime);

        return calendarEventRepositoryPort.save(event);
    }
}
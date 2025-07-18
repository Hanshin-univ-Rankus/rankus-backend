package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataCalendarEventRepository;
import org.univ.rankus.application.port.out.CalendarEventRepositoryPort;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.calendar.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CalendarEventRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataCalendarEventRepository를 호출하여 실제 DB 저장·조회 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class CalendarEventRepositoryAdapter implements CalendarEventRepositoryPort {

    private final SpringDataCalendarEventRepository springDataCalendarEventRepository;

    @Override
    public CalendarEvent save(CalendarEvent event) {
        return springDataCalendarEventRepository.save(event);
    }

    @Override
    public Optional<CalendarEvent> findById(Long id) {
        return springDataCalendarEventRepository.findById(id);
    }

    @Override
    public List<CalendarEvent> findByLabId(Long labId) {
        return springDataCalendarEventRepository.findByLabId(labId);
    }

    @Override
    public List<CalendarEvent> findByLabIdAndEventDateBetween(Long labId, LocalDate startDate, LocalDate endDate) {
        return springDataCalendarEventRepository.findByLabIdAndEventDateBetween(labId, startDate, endDate);
    }

    @Override
    public List<CalendarEvent> findByLabIdAndTypeAndEventDateBetween(Long labId, EventType type, LocalDate startDate, LocalDate endDate) {
        return springDataCalendarEventRepository.findByLabIdAndTypeAndEventDateBetween(labId, type, startDate, endDate);
    }

    @Override
    public Optional<CalendarEvent> findByInterviewId(Long interviewId) {
        return springDataCalendarEventRepository.findByInterviewId(interviewId);
    }

    @Override
    public List<CalendarEvent> findAllByInterviewId(Long interviewId) {
        return springDataCalendarEventRepository.findAllByInterviewId(interviewId);
    }

    @Override
    public void delete(CalendarEvent event) {
        springDataCalendarEventRepository.delete(event);
    }

    @Override
    public void deleteByInterviewId(Long interviewId) {
        springDataCalendarEventRepository.deleteByInterviewId(interviewId);
    }
}
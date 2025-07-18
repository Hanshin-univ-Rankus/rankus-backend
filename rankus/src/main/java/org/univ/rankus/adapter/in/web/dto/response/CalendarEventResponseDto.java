package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.calendar.CalendarEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CalendarEventResponseDto {
    private final Long id;
    private final Long labId;
    private final String labName;
    private final String type;
    private final String title;
    private final String description;
    private final LocalDate eventDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Long interviewId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static CalendarEventResponseDto from(CalendarEvent event) {
        return CalendarEventResponseDto.builder()
                .id(event.getId())
                .labId(event.getLab().getId())
                .labName(event.getLab().getName())
                .type(event.getType().name())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .interviewId(event.getInterviewId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    public static List<CalendarEventResponseDto> fromList(List<CalendarEvent> events) {
        return events.stream()
                .map(CalendarEventResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 면접 일정 여부 확인
     */
    public boolean isInterviewEvent() {
        return "INTERVIEW".equals(type);
    }

    /**
     * 일반 일정 여부 확인
     */
    public boolean isScheduleEvent() {
        return "SCHEDULE".equals(type);
    }

    /**
     * 시간 정보 포함 여부 확인
     */
    public boolean hasTime() {
        return startTime != null && endTime != null;
    }
}
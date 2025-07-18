package org.univ.rankus.domain.model.calendar;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventErrorCode;
import org.univ.rankus.domain.model.calendar.exception.CalendarEventValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 캘린더 이벤트 엔티티
 * - 일반 일정(SCHEDULE): 날짜만 포함, 시간 없음
 * - 면접 일정(INTERVIEW): 날짜와 시간 포함, Interview 시스템과 연동
 */
@Getter
@Entity
@Table(name = "calendar_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private EventType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "interview_id")
    private Long interviewId;

    /**
     * 일반 일정 생성자 (SCHEDULE 타입)
     */
    public CalendarEvent(Lab lab, String title, String description, LocalDate eventDate) {
        this.lab = validateLab(lab);
        this.type = EventType.SCHEDULE;
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.eventDate = validateEventDate(eventDate);
        this.startTime = null;
        this.endTime = null;
        this.interviewId = null;
    }

    /**
     * 면접 일정 생성자 (INTERVIEW 타입)
     */
    public CalendarEvent(Lab lab, String title, String description, LocalDate eventDate,
                         LocalTime startTime, LocalTime endTime, Long interviewId) {
        this.lab = validateLab(lab);
        this.type = EventType.INTERVIEW;
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.eventDate = validateEventDate(eventDate);
        this.startTime = validateStartTime(startTime);
        this.endTime = validateEndTime(endTime);
        validateTimeRange(this.startTime, this.endTime);
        this.interviewId = interviewId;
    }

    /**
     * 일정 정보 수정 (일반 일정)
     */
    public void updateSchedule(String title, String description, LocalDate eventDate) {
        if (this.type != EventType.SCHEDULE) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.EVENT_TYPE_REQUIRED);
        }
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.eventDate = validateEventDate(eventDate);
    }

    /**
     * 면접 일정 정보 수정 (면접 일정)
     */
    public void updateInterview(String title, String description, LocalDate eventDate,
                                LocalTime startTime, LocalTime endTime) {
        if (this.type != EventType.INTERVIEW) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.EVENT_TYPE_REQUIRED);
        }
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.eventDate = validateEventDate(eventDate);
        this.startTime = validateStartTime(startTime);
        this.endTime = validateEndTime(endTime);
        validateTimeRange(this.startTime, this.endTime);
    }

    /**
     * 면접 일정 여부 확인
     */
    public boolean isInterviewEvent() {
        return this.type == EventType.INTERVIEW;
    }

    /**
     * 일반 일정 여부 확인
     */
    public boolean isScheduleEvent() {
        return this.type == EventType.SCHEDULE;
    }

    /**
     * 시간 포함 여부 확인
     */
    public boolean hasTime() {
        return this.startTime != null && this.endTime != null;
    }

    // ========== 검증 메서드들 ==========

    private Lab validateLab(Lab lab) {
        if (lab == null) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.LAB_REQUIRED);
        }
        return lab;
    }

    private String validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.TITLE_REQUIRED);
        }
        if (title.length() > 100) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.TITLE_TOO_LONG);
        }
        return title;
    }

    private String validateDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.DESCRIPTION_TOO_LONG);
        }
        return description;
    }

    private LocalDate validateEventDate(LocalDate eventDate) {
        if (eventDate == null) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.EVENT_DATE_REQUIRED);
        }
        if (eventDate.isBefore(LocalDate.now())) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.EVENT_DATE_PAST);
        }
        return eventDate;
    }

    private LocalTime validateStartTime(LocalTime startTime) {
        if (startTime == null) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.START_TIME_REQUIRED);
        }
        return startTime;
    }

    private LocalTime validateEndTime(LocalTime endTime) {
        if (endTime == null) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.END_TIME_REQUIRED);
        }
        return endTime;
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new CalendarEventValidationException(CalendarEventErrorCode.INVALID_TIME_RANGE);
        }
    }
}
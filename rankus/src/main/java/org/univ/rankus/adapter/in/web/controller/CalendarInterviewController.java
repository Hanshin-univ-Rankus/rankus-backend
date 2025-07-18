package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.CalendarEventResponseDto;
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.calendar.CalendarEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * 캘린더 면접 일정 조회 API Controller
 * - 면접 일정(INTERVIEW) 조회 전용
 * - 면접 일정은 Interview 시스템에서 자동으로 생성/수정/삭제
 * - 읽기 전용 API만 제공
 */
@Tag(name = "Calendar Interview", description = "캘린더 면접 일정 조회 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/calendar/interviews")
@SecurityRequirement(name = "bearerAuth")
public class CalendarInterviewController {

    private final CalendarEventQueryUseCase queryUseCase;

    @Operation(summary = "면접 일정 목록 조회", description = "랩실의 면접 일정을 기간별로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "면접 일정 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 조회 권한이 없음"
            )
    })
    @GetMapping
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_CALENDAR')")
    public ResponseEntity<ApiResponse<List<CalendarEventResponseDto>>> getInterviewSchedules(
            @PathVariable @Positive Long labId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<CalendarEvent> events = queryUseCase.getInterviewsByLabIdAndDateRange(labId, startDate, endDate);
        List<CalendarEventResponseDto> response = CalendarEventResponseDto.fromList(events);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 일정 상세 조회", description = "특정 면접 일정의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "면접 일정 조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 면접 일정을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 조회 권한이 없음"
            )
    })
    @GetMapping("/{eventId}")
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_CALENDAR')")
    public ResponseEntity<ApiResponse<CalendarEventResponseDto>> getInterviewSchedule(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CalendarEvent event = queryUseCase.getEventById(eventId);
        CalendarEventResponseDto response = CalendarEventResponseDto.from(event);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 ID로 면접 일정 조회", description = "Interview ID로 연결된 면접 일정을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "면접 일정 조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 면접 일정을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 조회 권한이 없음"
            )
    })
    @GetMapping("/by-interview/{interviewId}")
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_CALENDAR')")
    public ResponseEntity<ApiResponse<CalendarEventResponseDto>> getInterviewScheduleByInterviewId(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long interviewId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CalendarEvent event = queryUseCase.getEventByInterviewId(interviewId);
        CalendarEventResponseDto response = CalendarEventResponseDto.from(event);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
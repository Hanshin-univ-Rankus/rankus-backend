package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.CalendarEventCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.CalendarEventUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.CalendarEventResponseDto;
import org.univ.rankus.application.port.in.command.CalendarEventCommandUseCase;
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.calendar.CalendarEvent;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * 캘린더 일반 일정 관리 API Controller
 * - 일반 일정(SCHEDULE) 생성, 조회, 수정, 삭제
 * - 면접 일정(INTERVIEW)은 CalendarInterviewController에서 처리
 */
@Tag(name = "Calendar Schedule", description = "캘린더 일반 일정 관리 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/calendar/schedules")
@SecurityRequirement(name = "bearerAuth")
public class CalendarScheduleController {

    private final CalendarEventCommandUseCase commandUseCase;
    private final CalendarEventQueryUseCase queryUseCase;

    @Operation(summary = "일반 일정 목록 조회", description = "랩실의 일반 일정을 기간별로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "일반 일정 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 조회 권한이 없음"
            )
    })
    @GetMapping
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_CALENDAR')")
    public ResponseEntity<ApiResponse<List<CalendarEventResponseDto>>> getSchedules(
            @PathVariable @Positive Long labId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<CalendarEvent> events = queryUseCase.getSchedulesByLabIdAndDateRange(labId, startDate, endDate);
        List<CalendarEventResponseDto> response = CalendarEventResponseDto.fromList(events);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "일반 일정 상세 조회", description = "특정 일반 일정의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "일반 일정 조회 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 일정을 찾을 수 없음"
            )
    })
    @GetMapping("/{eventId}")
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_CALENDAR')")
    public ResponseEntity<ApiResponse<CalendarEventResponseDto>> getSchedule(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CalendarEvent event = queryUseCase.getEventById(eventId);
        CalendarEventResponseDto response = CalendarEventResponseDto.from(event);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "일반 일정 생성", description = "새로운 일반 일정을 생성합니다. (랩장/매니저 권한 필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "일반 일정 생성 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 관리 권한이 없음"
            )
    })
    @PostMapping
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_CALENDAR')")
    public ResponseEntity<ApiResponse<CalendarEventResponseDto>> createSchedule(
            @PathVariable @Positive Long labId,
            @Valid @RequestBody CalendarEventCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CalendarEvent event = commandUseCase.createSchedule(
                labId, request.getTitle(), request.getDescription(), request.getEventDate()
        );
        CalendarEventResponseDto response = CalendarEventResponseDto.from(event);

        URI location = URI.create("/api/labs/" + labId + "/calendar/schedules/" + event.getId());
        return ResponseEntity.created(location).body(ApiResponse.created(response));
    }

    @Operation(summary = "일반 일정 수정", description = "기존 일반 일정을 수정합니다. (랩장/매니저 권한 필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "일반 일정 수정 성공",
                    content = @Content(schema = @Schema(implementation = CalendarEventResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 일정을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 관리 권한이 없음"
            )
    })
    @PutMapping("/{eventId}")
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_CALENDAR')")
    public ResponseEntity<ApiResponse<CalendarEventResponseDto>> updateSchedule(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody CalendarEventUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        CalendarEvent event = commandUseCase.updateSchedule(
                eventId, request.getTitle(), request.getDescription(), request.getEventDate()
        );
        CalendarEventResponseDto response = CalendarEventResponseDto.from(event);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "일반 일정 삭제", description = "기존 일반 일정을 삭제합니다. (랩장/매니저 권한 필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "일반 일정 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 일정을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "해당 랩의 캘린더 관리 권한이 없음"
            )
    })
    @DeleteMapping("/{eventId}")
    @PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_CALENDAR')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable @Positive Long labId,
            @PathVariable @Positive Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        commandUseCase.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
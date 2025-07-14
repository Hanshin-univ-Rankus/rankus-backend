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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.AttendanceCheckRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.AttendanceSessionUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.*;
import org.univ.rankus.application.port.in.AttendanceSessionCommandUseCase;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/attendance/sessions")
@Tag(name = "AttendanceSession", description = "출석 세션 관리 API")
public class AttendanceSessionController {

    private final AttendanceSessionCommandUseCase attendanceSessionCommandUseCase;
    private final AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 생성", description = "랩실의 출석 세션을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "출석 세션 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "출석 관리 권한 없음", content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_ATTENDANCE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> createSession(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @Valid @RequestBody AttendanceSessionCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.createSession(
                labId,
                request.getTitle(),
                request.getQrValidityMinutes(),
                userDetails.getUserId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(AttendanceSessionResponseDto.from(session), "출석 세션이 생성되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 종료", description = "활성 상태의 출석 세션을 종료합니다.")
    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> endSession(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.endSession(sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "출석 세션이 종료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 취소", description = "출석 세션을 취소합니다.")
    @PostMapping("/{sessionId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> cancelSession(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.cancelSession(sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "출석 세션이 취소되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 제목 수정", description = "출석 세션의 제목을 수정합니다.")
    @PutMapping("/{sessionId}/title")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> updateSessionTitle(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @Valid @RequestBody AttendanceSessionUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.updateSessionTitle(
                sessionId,
                request.getTitle(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "세션 제목이 수정되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "QR 유효시간 수정", description = "출석 세션의 QR 유효시간을 수정합니다.")
    @PutMapping("/{sessionId}/qr-validity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> updateQRValidityMinutes(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @Valid @RequestBody AttendanceSessionUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.updateQRValidityMinutes(
                sessionId,
                request.getQrValidityMinutes(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "QR 유효시간이 수정되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "QR 코드 생성", description = "출석 체크용 QR 코드를 생성합니다.")
    @PostMapping("/{sessionId}/qr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<QRTokenResponseDto>> generateQRCode(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        QRToken qrToken = attendanceSessionCommandUseCase.generateQRCode(sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(QRTokenResponseDto.from(qrToken), "QR 코드가 생성되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 체크", description = "QR 코드를 스캔하여 출석을 체크합니다.")
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> checkAttendance(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @Valid @RequestBody AttendanceCheckRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceSessionCommandUseCase.checkAttendance(
                request.getQrToken(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "출석 체크가 완료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 조회", description = "특정 출석 세션을 조회합니다.")
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_ATTENDANCE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> getSession(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionQueryUseCase.findSessionById(sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "출석 세션 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "활성 출석 세션 목록 조회", description = "랩실의 활성 상태 출석 세션 목록을 조회합니다.")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_ATTENDANCE')")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponseDto>>> getActiveSessions(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceSession> sessions = attendanceSessionQueryUseCase.findActiveSessionsByLabId(labId, userDetails.getUserId());
        List<AttendanceSessionResponseDto> responseList = sessions.stream()
                .map(AttendanceSessionResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "활성 출석 세션 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 목록 조회", description = "랩실의 모든 출석 세션 목록을 페이징하여 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_ATTENDANCE')")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponseDto>>> getSessions(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceSession> sessions = attendanceSessionQueryUseCase.findSessionsByLabId(labId, userDetails.getUserId(), page, size);
        List<AttendanceSessionResponseDto> responseList = sessions.stream()
                .map(AttendanceSessionResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "출석 세션 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 통계 조회", description = "특정 출석 세션의 출석 통계를 조회합니다.")
    @GetMapping("/{sessionId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_ATTENDANCE')")
    public ResponseEntity<ApiResponse<AttendanceStatisticsResponseDto>> getSessionStatistics(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession.AttendanceStatistics statistics = attendanceSessionQueryUseCase.getSessionStatistics(
                sessionId,
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(
                AttendanceStatisticsResponseDto.from(statistics),
                "출석 통계 조회 성공"
        ));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내가 참여한 출석 세션 목록 조회", description = "현재 사용자가 참여한 출석 세션 목록을 조회합니다.")
    @GetMapping("/my-sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponseDto>>> getMySessions(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceSession> sessions = attendanceSessionQueryUseCase.findSessionsByUserId(
                userDetails.getUserId(),
                page,
                size
        );
        List<AttendanceSessionResponseDto> responseList = sessions.stream()
                .map(AttendanceSessionResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "내 참여 세션 목록 조회 성공"));
    }
}
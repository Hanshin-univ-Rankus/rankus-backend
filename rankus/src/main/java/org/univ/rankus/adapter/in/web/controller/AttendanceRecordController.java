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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.AttendanceStatusUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.AttendanceRecordResponseDto;
import org.univ.rankus.application.port.in.AttendanceRecordCommandUseCase;
import org.univ.rankus.application.port.in.AttendanceRecordQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance/records")
@Tag(name = "AttendanceRecord", description = "출석 기록 관리 API")
public class AttendanceRecordController {

    private final AttendanceRecordCommandUseCase attendanceRecordCommandUseCase;
    private final AttendanceRecordQueryUseCase attendanceRecordQueryUseCase;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 기록 조회", description = "특정 출석 기록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "출석 기록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "조회 권한 없음", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "출석 기록을 찾을 수 없음", content = @Content
            )
    })
    @GetMapping("/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> getRecord(
            @PathVariable @Positive(message = "기록 ID는 양수여야 합니다") Long recordId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceRecordQueryUseCase.findRecordById(recordId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "출석 기록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "세션 출석 기록 목록 조회", description = "특정 세션의 모든 출석 기록을 조회합니다.")
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceRecordPermissionHandler.hasPermissionForSession(authentication, #sessionId, 'VIEW')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponseDto>>> getRecordsBySession(
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceRecord> records = attendanceRecordQueryUseCase.findRecordsBySessionId(sessionId, userDetails.getUserId());
        List<AttendanceRecordResponseDto> responseList = records.stream()
                .map(AttendanceRecordResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "세션 출석 기록 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "특정 사용자의 세션 출석 기록 조회", description = "특정 세션에서 특정 사용자의 출석 기록을 조회합니다.")
    @GetMapping("/sessions/{sessionId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@unifiedPermissionEvaluator.hasPermission(authentication, #sessionId, 'AttendanceSession', 'VIEW') or " +
            "#userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> getRecordBySessionAndUser(
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다") Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceRecordQueryUseCase.findRecordBySessionIdAndUserId(
                sessionId, userId, userDetails.getUserId()
        );

        if (record == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "출석 기록이 없습니다"));
        }

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "출석 기록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "사용자 출석 기록 목록 조회", description = "특정 사용자의 모든 출석 기록을 페이징하여 조회합니다.")
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponseDto>>> getRecordsByUser(
            @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceRecord> records = attendanceRecordQueryUseCase.findRecordsByUserId(
                userId, userDetails.getUserId(), page, size
        );
        List<AttendanceRecordResponseDto> responseList = records.stream()
                .map(AttendanceRecordResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "사용자 출석 기록 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 출석 기록 목록 조회", description = "특정 랩실의 모든 출석 기록을 페이징하여 조회합니다.")
    @GetMapping("/labs/{labId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponseDto>>> getRecordsByLab(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceRecord> records = attendanceRecordQueryUseCase.findRecordsByLabId(
                labId, userDetails.getUserId(), page, size
        );
        List<AttendanceRecordResponseDto> responseList = records.stream()
                .map(AttendanceRecordResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "랩실 출석 기록 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 출석 기록 목록 조회", description = "현재 사용자의 출석 기록을 페이징하여 조회합니다.")
    @GetMapping("/my-records")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponseDto>>> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttendanceRecord> records = attendanceRecordQueryUseCase.findRecordsByUserId(
                userDetails.getUserId(), userDetails.getUserId(), page, size
        );
        List<AttendanceRecordResponseDto> responseList = records.stream()
                .map(AttendanceRecordResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseList, "내 출석 기록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 상태를 결석으로 변경", description = "출석 기록의 상태를 결석으로 변경합니다.")
    @PutMapping("/{recordId}/absent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceRecordPermissionHandler.hasPermission(authentication, #recordId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> markAsAbsent(
            @PathVariable @Positive(message = "기록 ID는 양수여야 합니다") Long recordId,
            @Valid @RequestBody AttendanceStatusUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceRecordCommandUseCase.markAsAbsent(
                recordId, userDetails.getUserId(), request.getReason()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "결석 처리가 완료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 상태를 지각으로 변경", description = "출석 기록의 상태를 지각으로 변경합니다.")
    @PutMapping("/{recordId}/late")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceRecordPermissionHandler.hasPermission(authentication, #recordId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> markAsLate(
            @PathVariable @Positive(message = "기록 ID는 양수여야 합니다") Long recordId,
            @Valid @RequestBody AttendanceStatusUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceRecordCommandUseCase.markAsLate(
                recordId, userDetails.getUserId(), request.getReason()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "지각 처리가 완료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 상태를 출석으로 변경", description = "출석 기록의 상태를 출석으로 변경합니다.")
    @PutMapping("/{recordId}/present")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceRecordPermissionHandler.hasPermission(authentication, #recordId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> markAsPresent(
            @PathVariable @Positive(message = "기록 ID는 양수여야 합니다") Long recordId,
            @Valid @RequestBody AttendanceStatusUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceRecordCommandUseCase.markAsPresent(
                recordId, userDetails.getUserId(), request.getReason()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "출석 처리가 완료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 상태 직접 변경", description = "출석 기록의 상태를 직접 변경합니다.")
    @PutMapping("/{recordId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceRecordPermissionHandler.hasPermission(authentication, #recordId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> updateAttendanceStatus(
            @PathVariable @Positive(message = "기록 ID는 양수여야 합니다") Long recordId,
            @Valid @RequestBody AttendanceStatusUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceRecordCommandUseCase.updateAttendanceStatus(
                recordId, request.getStatus(), userDetails.getUserId(), request.getReason()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "출석 상태가 변경되었습니다"));
    }
}
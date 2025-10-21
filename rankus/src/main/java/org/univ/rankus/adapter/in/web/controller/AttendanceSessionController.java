package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.univ.rankus.domain.model.attendance.SecureQRToken;

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
    @Operation(
            summary = "출석 세션 생성",
            description = """
                    랩실의 새로운 출석 세션을 생성합니다.
                                        
                    ## 기능 설명
                    - QR 코드 기반 출석 체크 시스템
                    - 세션별로 독립적인 출석 관리
                    - 실시간 출석 현황 모니터링
                    - 자동 QR 코드 유효시간 설정
                                        
                    ## 권한 요구사항
                    - 랩장, 매니저, 교수, 관리자만 세션 생성 가능
                    - 해당 랩실의 출석 관리 권한 필요
                                        
                    ## 사용 시나리오
                    1. 세션 생성 (제목, QR 유효시간 설정)
                    2. QR 코드 생성 및 표시
                    3. 학생들의 QR 스캔을 통한 출석 체크
                    4. 세션 종료 및 출석 결과 확인
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "출석 세션 생성 정보",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttendanceSessionCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "세션 생성 예시",
                            summary = "일반적인 출석 세션 생성",
                            value = """
                                    {
                                      "title": "2024-02-15 정기 미팅",
                                      "qrValidityMinutes": 10
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "출석 세션 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "출석 세션이 생성되었습니다",
                                              "data": {
                                                "sessionId": 1,
                                                "labId": 1,
                                                "title": "2024-02-15 정기 미팅",
                                                "status": "ACTIVE",
                                                "qrValidityMinutes": 10,
                                                "createdBy": 1,
                                                "createdAt": "2024-02-15T14:00:00",
                                                "updatedAt": "2024-02-15T14:00:00",
                                                "endTime": null
                                              },
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "검증 실패",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "입력값 검증 실패",
                                              "data": null,
                                              "errors": [
                                                "세션 제목은 필수입니다",
                                                "QR 유효시간은 1분 이상이어야 합니다"
                                              ],
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "출석 관리 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "출석 관리 권한이 없습니다",
                                              "data": null,
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'MANAGE_ATTENDANCE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> createSession(
            @Parameter(description = "출석 세션을 생성할 랩실의 ID", required = true, example = "1")
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
    @Operation(summary = "출석 세션 종료", description = """
            활성 상태의 출석 세션을 종료합니다.
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "출석 세션 종료 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음 또는 경로 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "경로 불일치",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "세션을 찾을 수 없습니다",
                                              "data": null,
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    @Deprecated // Phase 3: Use PATCH /{sessionId}/status instead
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> endSession(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.endSession(labId, sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "출석 세션이 종료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 취소", description = """
            출석 세션을 취소합니다.
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @PostMapping("/{sessionId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    @Deprecated // Phase 3: Use PATCH /{sessionId}/status 대신
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> cancelSession(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.cancelSession(labId, sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "출석 세션이 취소되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 제목 수정", description = """
            출석 세션의 제목을 수정합니다.
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @PutMapping("/{sessionId}/title")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> updateSessionTitle(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @Valid @RequestBody AttendanceSessionUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.updateSessionTitle(
                labId,
                sessionId,
                request.getTitle(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "세션 제목이 수정되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "QR 유효시간 수정", description = """
            출석 세션의 QR 유효시간을 수정합니다.
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @PutMapping("/{sessionId}/qr-validity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> updateQRValidityMinutes(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @Valid @RequestBody AttendanceSessionUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceSession session = attendanceSessionCommandUseCase.updateQRValidityMinutes(
                labId,
                sessionId,
                request.getQrValidityMinutes(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), "QR 유효시간이 수정되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "QR 코드 생성", description = """
            출석 체크용 QR 코드를 생성합니다.
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "QR 코드 생성 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음 또는 경로 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "경로 불일치",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "세션을 찾을 수 없습니다",
                                              "data": null,
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{sessionId}/qr")
    @Deprecated // Secure QR 사용 권장: /{sessionId}/qr/secure
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<QRTokenResponseDto>> generateQRCode(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        QRToken qrToken = attendanceSessionCommandUseCase.generateQRCode(labId, sessionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(QRTokenResponseDto.from(qrToken), "QR 코드가 생성되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "보안 QR 코드 생성", description = "출석 체크용 Secure QR 토큰을 생성합니다. 이 토큰은 암호화되어 있으며 프론트 URL과 함께 반환됩니다.")
    @PostMapping("/{sessionId}/qr/secure")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<SecureQRTokenResponseDto>> generateSecureQRCode(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SecureQRToken token = attendanceSessionCommandUseCase.generateSecureQRCode(labId, sessionId, userDetails.getUserId());
        // 프론트 기본 URL (추후 properties 로 이동 가능)
        String baseUrl = "https://rankus.vercel.app/attend?qt=";
        return ResponseEntity.ok(ApiResponse.success(SecureQRTokenResponseDto.from(token, baseUrl + token.getEncryptedToken()), "보안 QR 코드가 생성되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 체크", description = """
            QR 코드를 스캔하여 출석을 체크합니다.
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @PostMapping("/check")
    @Deprecated // 글로벌 엔드포인트 POST /api/attendance/check 사용 권장
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceRecordResponseDto>> checkAttendance(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @Valid @RequestBody AttendanceCheckRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AttendanceRecord record = attendanceSessionCommandUseCase.checkAttendance(
                labId,
                request.getQrToken(),
                userDetails.getUserId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordResponseDto.from(record), "출석 체크가 완료되었습니다"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 조회", description = "특정 출석 세션을 조회합니다.")
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
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
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
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
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
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
            "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "출석 세션 상태 변경 (RESTful)", description = """
            출석 세션의 상태를 RESTful하게 변경합니다. Phase 3 개선사항.
                        
            ## 지원 가능한 상태
            - `COMPLETED`: 세션 종료
            - `CANCELLED`: 세션 취소
                        
            ## 보안 검증
            - 경로의 labId와 세션이 속한 랩이 일치하지 않으면 404 에러 반환
            - 다른 랩의 세션에 접근할 수 없음
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "세션 상태 변경 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 - 지원하지 않는 상태 또는 필수값 누락",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "세션을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PatchMapping("/{sessionId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
            "@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> changeSessionStatus(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "세션 ID는 양수여야 합니다") Long sessionId,
            @Valid @RequestBody AttendanceSessionUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 상태 필드가 제공되지 않으면 예외 발생
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("변경할 상태는 필수입니다");
        }

        AttendanceSession session;
        String message;

        switch (request.getStatus().toUpperCase()) {
            case "COMPLETED" -> {
                session = attendanceSessionCommandUseCase.endSession(labId, sessionId, userDetails.getUserId());
                message = "출석 세션이 종료되었습니다";
            }
            case "CANCELLED" -> {
                session = attendanceSessionCommandUseCase.cancelSession(labId, sessionId, userDetails.getUserId());
                message = "출석 세션이 취소되었습니다";
            }
            default -> throw new IllegalArgumentException("지원하지 않는 상태입니다: " + request.getStatus());
        }

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponseDto.from(session), message));
    }
}

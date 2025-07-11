package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.adapter.in.web.dto.request.ScoreSubmissionApprovalRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.ScoreSubmissionCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.*;
import org.univ.rankus.application.port.in.command.FileUploadUseCase;
import org.univ.rankus.application.port.in.command.ScoreSubmissionCommandUseCase;
import org.univ.rankus.application.port.in.query.ScoreSubmissionQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.policy.DuplicateCheckPolicy;

import java.time.LocalDate;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/score-submissions")
@Tag(name = "ScoreSubmission", description = "점수 신청 API")
public class ScoreSubmissionController {

    private final ScoreSubmissionCommandUseCase scoreSubmissionCommandUseCase;
    private final ScoreSubmissionQueryUseCase scoreSubmissionQueryUseCase;
    private final FileUploadUseCase fileUploadUseCase;

    @Operation(summary = "점수 신청", description = "새로운 점수 신청을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "점수 신청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ScoreSubmissionResponseDto>> createScoreSubmission(
            @Valid @RequestBody ScoreSubmissionCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ScoreSubmission submission = scoreSubmissionCommandUseCase.submitScore(
                userDetails.getUserId(),
                request.getLabId(),
                request.getCategory(),
                request.getAchievementDescription(),
                request.getAchievementDate(),
                request.getProofFileUrl(),
                request.getApplicationReason(),
                request.getRelatedLink(),
                request.getVisibility()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ScoreSubmissionResponseDto.from(submission)));
    }

    @Operation(summary = "점수 신청 상세 조회", description = "특정 점수 신청의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "점수 신청 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "점수 신청을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ScoreSubmissionResponseDto>> getScoreSubmission(
            @PathVariable @Positive Long submissionId) {

        ScoreSubmission submission = scoreSubmissionQueryUseCase.findSubmissionById(submissionId);

        return ResponseEntity.ok(ApiResponse.success(ScoreSubmissionResponseDto.from(submission)));
    }

    @Operation(summary = "내 점수 신청 목록 조회", description = "현재 사용자의 점수 신청 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "점수 신청 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ScoreSubmissionResponseDto>>> getMyScoreSubmissions(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<ScoreSubmission> submissions = scoreSubmissionQueryUseCase
                .findSubmissionsByUserId(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(submissions, ScoreSubmissionResponseDto::from)));
    }

    @Operation(summary = "랩실 점수 신청 목록 조회", description = "특정 랩실의 점수 신청 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "랩실 점수 신청 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/lab/{labId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ScoreSubmissionResponseDto>>> getLabScoreSubmissions(
            @PathVariable @Positive Long labId,
            @RequestParam(required = false) SubmissionStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ScoreSubmission> submissions = (status != null)
                ? scoreSubmissionQueryUseCase.findSubmissionsByLabIdAndStatus(labId, status, pageable)
                : scoreSubmissionQueryUseCase.findSubmissionsByLabId(labId, pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(submissions, ScoreSubmissionResponseDto::from)));
    }

    @Operation(summary = "승인 대기 점수 신청 목록 조회", description = "승인자가 처리할 수 있는 PENDING 상태의 점수 신청 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "승인 대기 점수 신청 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('LAB_LEADER') or hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ScoreSubmissionResponseDto>>> getPendingScoreSubmissions(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<ScoreSubmission> submissions = scoreSubmissionQueryUseCase
                .findPendingSubmissionsForApprover(userDetails.getUserId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(submissions, ScoreSubmissionResponseDto::from)));
    }

    @Operation(summary = "점수 신청 승인", description = "특정 점수 신청을 승인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "점수 신청 승인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "승인 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "점수 신청을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/{submissionId}/approve")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('LAB_LEADER') or hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveScoreSubmission(
            @PathVariable @Positive Long submissionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        scoreSubmissionCommandUseCase.approveSubmission(submissionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "점수 신청 거부", description = "특정 점수 신청을 거부합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "점수 신청 거부 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "거부 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "점수 신청을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/{submissionId}/reject")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('LAB_LEADER') or hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectScoreSubmission(
            @PathVariable @Positive Long submissionId,
            @Valid @RequestBody ScoreSubmissionApprovalRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        scoreSubmissionCommandUseCase.rejectSubmission(
                submissionId, userDetails.getUserId(), request.getRejectionReason());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "점수 신청 상태 정정", description = "점수 신청의 상태를 정정합니다 (승인 ↔ 거부).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "점수 신청 정정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "정정 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "정정 불가능 상태",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/{submissionId}/correct")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('LAB_LEADER') or hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> correctScoreSubmission(
            @PathVariable @Positive Long submissionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        scoreSubmissionCommandUseCase.correctSubmissionStatus(submissionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "점수 신청 삭제", description = "본인이 신청한 점수 신청을 삭제합니다. (PENDING 상태만 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "점수 신청 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "삭제 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "삭제 불가능 상태",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @DeleteMapping("/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteScoreSubmission(
            @PathVariable @Positive Long submissionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        scoreSubmissionCommandUseCase.deleteSubmission(submissionId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "중복 검사", description = "점수 신청 시 중복 여부를 검사합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "중복 검사 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/check-duplicates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DuplicateCheckResponseDto>> checkDuplicates(
            @RequestParam LocalDate achievementDate,
            @RequestParam ScoreCategory category,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DuplicateCheckPolicy.DuplicateCheckResult result = scoreSubmissionQueryUseCase
                .checkDuplicates(userDetails.getUserId(), achievementDate, category);

        return ResponseEntity.ok(ApiResponse.success(DuplicateCheckResponseDto.from(result)));
    }

    @Operation(summary = "사용자 총 점수 조회", description = "현재 사용자의 총 승인된 점수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "사용자 총 점수 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/my-total-score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> getMyTotalScore(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int totalScore = scoreSubmissionQueryUseCase.calculateUserTotalScore(userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(totalScore));
    }

    @Operation(summary = "랩실 내 사용자 점수 조회", description = "특정 랩실에서 현재 사용자의 승인된 점수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "랩실 내 사용자 점수 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/my-lab-score/{labId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> getMyLabScore(
            @PathVariable @Positive Long labId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int labScore = scoreSubmissionQueryUseCase.calculateUserScoreInLab(userDetails.getUserId(), labId);

        return ResponseEntity.ok(ApiResponse.success(labScore));
    }

    @Operation(summary = "증빙서류 파일 업로드", description = "점수 신청용 증빙서류 파일을 업로드합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "파일 업로드 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 파일 형식 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "파일 업로드 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping(value = "/upload-proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadProofFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            String fileUrl = fileUploadUseCase.uploadProofFile(file, userDetails.getUserId());

            FileUploadResponseDto responseDto = FileUploadResponseDto.success(
                    fileUrl, file.getOriginalFilename(), null,
                    file.getSize(), file.getContentType(), "proof-file", userDetails.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(responseDto, "증빙서류 파일이 성공적으로 업로드되었습니다."));

        } catch (Exception e) {
            FileUploadResponseDto responseDto = FileUploadResponseDto.failure(
                    file.getOriginalFilename(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(422, "파일 업로드 실패: " + e.getMessage(), responseDto));
        }
    }

    @Operation(summary = "증빙서류 파일과 점수 신청 동시 처리", description = "파일을 업로드하고 점수 신청을 동시에 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "파일 업로드 및 점수 신청 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "파일 업로드 또는 점수 신청 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping(value = "/with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ScoreSubmissionResponseDto>> createScoreSubmissionWithFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("labId") @Positive Long labId,
            @RequestParam("category") ScoreCategory category,
            @RequestParam("achievementDescription") String achievementDescription,
            @RequestParam("achievementDate") LocalDate achievementDate,
            @RequestParam(value = "applicationReason", required = false) String applicationReason,
            @RequestParam(value = "relatedLink", required = false) String relatedLink,
            @RequestParam("visibility") String visibility,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // 1. 파일 업로드
            String fileUrl = fileUploadUseCase.uploadProofFile(file, userDetails.getUserId());

            // 2. 점수 신청 생성
            ScoreSubmission submission = scoreSubmissionCommandUseCase.submitScore(
                    userDetails.getUserId(),
                    labId,
                    category,
                    achievementDescription,
                    achievementDate,
                    fileUrl,
                    applicationReason,
                    relatedLink,
                    org.univ.rankus.domain.model.ranking.VisibilityLevel.valueOf(visibility.toUpperCase())
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(ScoreSubmissionResponseDto.from(submission),
                            "파일 업로드 및 점수 신청이 성공적으로 완료되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(422, "파일 업로드 또는 점수 신청 실패: " + e.getMessage(), null));
        }
    }
}
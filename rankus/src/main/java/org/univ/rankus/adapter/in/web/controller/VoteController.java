package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.VoteCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.VoteParticipateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.PageResponse;
import org.univ.rankus.adapter.in.web.dto.response.VoteParticipationResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.VoteResponseDto;
import org.univ.rankus.application.port.in.command.VoteCommandUseCase;
import org.univ.rankus.application.port.in.query.VoteQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteParticipation;
import org.univ.rankus.domain.model.vote.VoteStatus;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/votes")
@Tag(name = "Vote", description = "투표 시스템 API")
public class VoteController {

    private final VoteQueryUseCase voteQueryUseCase;
    private final VoteCommandUseCase voteCommandUseCase;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "랩실 투표 목록 조회",
            description = "특정 랩실의 투표를 페이징하여 조회합니다. 최신순으로 정렬됩니다.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "0부터 시작하는 페이지 인덱스", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "페이지 크기(1 이상)", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "정렬: '필드,방향' 형식. 예: createdAt,desc",
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", example = "createdAt,desc")))
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "투표 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "조회 권한 없음", content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_VOTES')")
    public ResponseEntity<ApiResponse<PageResponse<VoteResponseDto>>> getLabVotes(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable
    ) {
        Page<Vote> votePage = voteQueryUseCase.findVotesByLabId(labId, pageable);
        PageResponse<VoteResponseDto> pageResponse = PageResponse.of(votePage, VoteResponseDto::from);

        return ResponseEntity.ok(ApiResponse.success(pageResponse, "투표 목록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "랩실 투표 전체 목록 조회", description = "특정 랩실의 모든 투표를 조회합니다.")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_VOTES')")
    public ResponseEntity<ApiResponse<List<VoteResponseDto>>> getAllLabVotes(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId
    ) {
        List<Vote> votes = voteQueryUseCase.findVotesByLabId(labId);
        List<VoteResponseDto> responseList = VoteResponseDto.fromList(votes);

        return ResponseEntity.ok(ApiResponse.success(responseList, "전체 투표 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "활성 투표 목록 조회", description = "특정 랩실의 활성 상태 투표만 조회합니다.")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_VOTES')")
    public ResponseEntity<ApiResponse<List<VoteResponseDto>>> getActiveVotes(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId
    ) {
        List<Vote> activeVotes = voteQueryUseCase.findActiveVotesByLabId(labId);
        List<VoteResponseDto> responseList = VoteResponseDto.fromList(activeVotes);

        return ResponseEntity.ok(ApiResponse.success(responseList, "활성 투표 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "상태별 투표 조회", description = "특정 랩실의 특정 상태 투표를 조회합니다.")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_VOTES')")
    public ResponseEntity<ApiResponse<List<VoteResponseDto>>> getVotesByStatus(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable VoteStatus status
    ) {
        List<Vote> votes = voteQueryUseCase.findVotesByLabIdAndStatus(labId, status);
        List<VoteResponseDto> responseList = VoteResponseDto.fromList(votes);

        return ResponseEntity.ok(ApiResponse.success(responseList, "상태별 투표 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "투표 상세 조회", description = "투표 ID로 투표 상세 정보를 조회합니다.")
    @GetMapping("/{voteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'VIEW')")
    public ResponseEntity<ApiResponse<VoteResponseDto>> getVote(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId
    ) {
        Vote vote = voteQueryUseCase.findVoteById(voteId);
        VoteResponseDto responseDto = VoteResponseDto.from(vote);

        return ResponseEntity.ok(ApiResponse.success(responseDto, "투표 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "투표 생성",
            description = """
                    랩실에 새로운 투표를 생성합니다.
                                        
                    ## 기능 설명
                    - 랩실 내 의사결정을 위한 투표 생성
                    - 2개 이상 5개 이하의 선택지 제공
                    - 투표 마감일 설정 가능
                    - 투표 생성 후 자동으로 활성 상태로 변경
                                        
                    ## 권한 요구사항
                    - 랩장, 매니저, 교수, 관리자만 투표 생성 가능
                    - 일반 멤버는 투표 참여만 가능
                                        
                    ## 투표 규칙
                    - 각 사용자는 투표당 1번만 참여 가능
                    - 마감일 이후 자동으로 투표 종료
                    - 생성자는 언제든 투표 종료/취소 가능
                    """
    )
    @RequestBody(
            description = "투표 생성 정보",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VoteCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "투표 생성 예시",
                            summary = "랩실 회식 장소 투표",
                            value = """
                                    {
                                      "title": "다음 주 랩실 회식 장소 투표",
                                      "description": "2024년 2월 랩실 회식 장소를 결정하기 위한 투표입니다. 많은 참여 부탁드립니다!",
                                      "deadline": "2024-02-10T23:59:59",
                                      "optionTexts": [
                                        "한식당 (삼겹살)",
                                        "중식당 (짜장면)",
                                        "일식당 (초밥)",
                                        "치킨집 (후라이드)"
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "투표 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "투표 생성 성공",
                                              "data": {
                                                "id": 1,
                                                "title": "다음 주 랩실 회식 장소 투표",
                                                "description": "2024년 2월 랩실 회식 장소를 결정하기 위한 투표입니다.",
                                                "creatorId": 1,
                                                "labId": 1,
                                                "deadline": "2024-02-10T23:59:59",
                                                "status": "ACTIVE",
                                                "options": [
                                                  {"id": 1, "text": "한식당 (삼겹살)", "voteCount": 0},
                                                  {"id": 2, "text": "중식당 (짜장면)", "voteCount": 0},
                                                  {"id": 3, "text": "일식당 (초밥)", "voteCount": 0},
                                                  {"id": 4, "text": "치킨집 (후라이드)", "voteCount": 0}
                                                ],
                                                "totalVotes": 0,
                                                "createdAt": "2024-01-15T10:30:00"
                                              },
                                              "timestamp": "2024-01-15T10:30:00"
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
                                                "투표 제목은 필수입니다",
                                                "투표 선택지는 2개 이상 5개 이하여야 합니다",
                                                "투표 마감일은 현재 시간 이후여야 합니다"
                                              ],
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "투표 생성 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "투표 생성 권한이 없습니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'CREATE_VOTE')")
    public ResponseEntity<ApiResponse<VoteResponseDto>> createVote(
            @Parameter(description = "투표를 생성할 랩실의 ID", required = true, example = "1")
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @Valid @RequestBody VoteCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Vote createdVote = voteCommandUseCase.createVote(
                requestDto.getTitle(),
                requestDto.getDescription(),
                userDetails.getUserId(),
                labId,
                requestDto.getDeadline(),
                requestDto.getOptionTexts()
        );

        VoteResponseDto responseDto = VoteResponseDto.from(createdVote);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(responseDto, "투표 생성 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "투표 참여", description = "투표에 참여합니다.")
    @PostMapping("/{voteId}/participate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'PARTICIPATE')")
    public ResponseEntity<ApiResponse<VoteParticipationResponseDto>> participateInVote(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId,
            @Valid @RequestBody VoteParticipateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        VoteParticipation participation = voteCommandUseCase.participateInVote(
                voteId,
                userDetails.getUserId(),
                requestDto.getSelectedOptionId()
        );

        VoteParticipationResponseDto responseDto = VoteParticipationResponseDto.from(participation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(responseDto, "투표 참여 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "투표 종료", description = "투표를 종료합니다.")
    @PatchMapping("/{voteId}/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'MANAGE')")
    public ResponseEntity<ApiResponse<VoteResponseDto>> closeVote(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId
    ) {
        Vote closedVote = voteCommandUseCase.closeVote(voteId);
        VoteResponseDto responseDto = VoteResponseDto.from(closedVote);

        return ResponseEntity.ok(ApiResponse.success(responseDto, "투표 종료 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "투표 취소", description = "투표를 취소합니다.")
    @PatchMapping("/{voteId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'MANAGE')")
    public ResponseEntity<ApiResponse<VoteResponseDto>> cancelVote(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId
    ) {
        Vote canceledVote = voteCommandUseCase.cancelVote(voteId);
        VoteResponseDto responseDto = VoteResponseDto.from(canceledVote);

        return ResponseEntity.ok(ApiResponse.success(responseDto, "투표 취소 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "투표 삭제", description = "투표를 삭제합니다. (참여자가 없을 때만 가능)")
    @DeleteMapping("/{voteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteVote(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId
    ) {
        voteCommandUseCase.deleteVote(voteId);
        return ResponseEntity.ok(ApiResponse.success(null, "투표 삭제 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "투표 참여 기록 조회", description = "특정 투표의 참여 기록을 조회합니다.")
    @GetMapping("/{voteId}/participations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'VIEW_RESULTS')")
    public ResponseEntity<ApiResponse<List<VoteParticipationResponseDto>>> getVoteParticipations(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId
    ) {
        List<VoteParticipation> participations = voteQueryUseCase.findVoteParticipationsByVoteId(voteId);
        List<VoteParticipationResponseDto> responseList = VoteParticipationResponseDto.fromList(participations);

        return ResponseEntity.ok(ApiResponse.success(responseList, "투표 참여 기록 조회 성공"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 투표 참여 여부 확인", description = "현재 사용자가 특정 투표에 참여했는지 확인합니다.")
    @GetMapping("/{voteId}/my-participation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @votePermissionHandler.hasPermissionForVote(authentication.principal, #voteId, 'VIEW')")
    public ResponseEntity<ApiResponse<Boolean>> checkMyParticipation(
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId,
            @PathVariable @Positive(message = "투표 ID는 양수여야 합니다") Long voteId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean hasParticipated = voteQueryUseCase.hasUserParticipated(voteId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(hasParticipated, "참여 여부 확인 성공"));
    }
}
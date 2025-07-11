package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.PageResponse;
import org.univ.rankus.adapter.in.web.dto.response.RankingResponseDto;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
@Tag(name = "Ranking", description = "랭킹 조회 API")
public class RankingController {

    private final RankingQueryUseCase rankingQueryUseCase;

    @Operation(summary = "전체 랩실 랭킹 조회", description = "모든 랩실의 랭킹을 페이징하여 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "랭킹 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/labs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<RankingResponseDto>>> getLabRankings(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RankingQueryUseCase.LabRankingResult> rankings = rankingQueryUseCase.getLabRankings(pageable);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(rankings, RankingResponseDto::from)));
    }

    @Operation(summary = "특정 랩실 랭킹 조회", description = "특정 랩실의 상세 랭킹 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "랭킹 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "랩실을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/labs/{labId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RankingResponseDto>> getLabRanking(
            @PathVariable @Positive Long labId) {

        RankingQueryUseCase.LabRankingResult ranking = rankingQueryUseCase.getLabRanking(labId);

        return ResponseEntity.ok(ApiResponse.success(RankingResponseDto.from(ranking)));
    }

    @Operation(summary = "랩실 상위 기여자 조회", description = "특정 랩실의 상위 기여자 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "기여자 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "랩실을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/labs/{labId}/contributors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RankingResponseDto.UserContributionResponseDto>>> getTopContributors(
            @PathVariable @Positive Long labId,
            @RequestParam(defaultValue = "5") int limit) {

        List<RankingQueryUseCase.UserContribution> contributors = rankingQueryUseCase.getTopContributors(labId, limit);

        List<RankingResponseDto.UserContributionResponseDto> responseDtos = contributors.stream()
                .map(RankingResponseDto.UserContributionResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }

    @Operation(summary = "내 랩실 랭킹 조회", description = "현재 사용자가 속한 랩실들의 랭킹 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "내 랩실 랭킹 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/my-labs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RankingResponseDto>>> getMyLabRankings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<RankingQueryUseCase.LabRankingResult> rankings = rankingQueryUseCase.getUserLabRankings(userDetails.getUserId());

        List<RankingResponseDto> responseDtos = rankings.stream()
                .map(RankingResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }

    @Operation(summary = "랩실 총 점수 조회", description = "특정 랩실의 총 점수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "랩실 총 점수 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "랩실을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/labs/{labId}/total-score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> getLabTotalScore(
            @PathVariable @Positive Long labId) {

        int totalScore = rankingQueryUseCase.calculateLabTotalScore(labId);

        return ResponseEntity.ok(ApiResponse.success(totalScore));
    }

    @Operation(summary = "랩실 내 사용자 기여도 조회", description = "특정 랩실에서 사용자의 기여도를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "사용자 기여도 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "랩실을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/labs/{labId}/my-contribution")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> getMyContributionInLab(
            @PathVariable @Positive Long labId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int contribution = rankingQueryUseCase.calculateUserContributionInLab(userDetails.getUserId(), labId);

        return ResponseEntity.ok(ApiResponse.success(contribution));
    }

    @Operation(summary = "점수 범위별 랩실 수 조회", description = "특정 점수 범위에 속하는 랩실의 수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "랩실 수 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/labs/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getLabsCountInScoreRange(
            @RequestParam(defaultValue = "0") int minScore,
            @RequestParam(defaultValue = "1000") int maxScore) {

        long count = rankingQueryUseCase.countLabsInScoreRange(minScore, maxScore);

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
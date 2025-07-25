package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabDashboardResponseDto;
import org.univ.rankus.application.port.in.query.LabDashboardQueryUseCase;

/**
 * 랩실 대시보드 REST API 컨트롤러
 * 랩실 멤버 전용 대시보드 정보를 제공합니다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs/{labId}/dashboard")
@Tag(name = "LabDashboard", description = "랩실 대시보드 API")
public class LabDashboardController {

    private final LabDashboardQueryUseCase labDashboardQueryUseCase;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "랩실 대시보드 조회",
            description = """
                    랩실의 종합 대시보드 정보를 조회합니다.
                                        
                    ## 접근 권한
                    - 해당 랩실 멤버(LAB_MEMBER 이상)만 접근 가능
                    - ADMIN, PROFESSOR는 모든 랩실 접근 가능
                                        
                    ## 포함 데이터
                    - **랩실 기본 정보**: 이름, 교수, 설명, 개설일, 대표사진
                    - **최신 공지사항**: 최대 3개, 제목과 작성일
                    - **현재 투표**: 최대 3개, 제목과 상태
                    - **최근 자료**: 최대 3개, 공개 자료만 표시
                    - **일정**: 최대 3개, 다가오는 일정순
                    - **랩실 멤버**: 최대 3개, 최근 가입순
                                        
                    ## 활용 용도
                    - 랩실 멤버용 메인 대시보드
                    - 각 영역별 미리보기 제공
                    - 상세 내용은 개별 페이지에서 확인
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "대시보드 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "랩실 대시보드 조회 성공",
                                              "data": {
                                                "labInfo": {
                                                  "id": 1,
                                                  "name": "AI 연구실",
                                                  "professorName": "김교수",
                                                  "description": "인공지능과 머신러닝을 연구하는 랩실입니다",
                                                  "establishedDate": "2024-01-01",
                                                  "representativeImage": null
                                                },
                                                "notices": [
                                                  {
                                                    "id": 1,
                                                    "title": "정기 미팅 안내",
                                                    "createdAt": "2024-07-25T10:30:00",
                                                    "type": "NOTICE"
                                                  }
                                                ],
                                                "votes": [
                                                  {
                                                    "id": 1,
                                                    "title": "미팅 시간 투표",
                                                    "createdAt": "2024-07-25T09:00:00",
                                                    "type": "VOTE",
                                                    "additionalInfo": "ACTIVE"
                                                  }
                                                ],
                                                "resources": [
                                                  {
                                                    "id": 1,
                                                    "title": "알고리즘 강의자료",
                                                    "createdAt": "2024-07-24T14:20:00",
                                                    "type": "RESOURCE"
                                                  }
                                                ],
                                                "schedules": [
                                                  {
                                                    "id": 1,
                                                    "title": "랩 미팅",
                                                    "createdAt": "2024-07-25T08:00:00",
                                                    "type": "SCHEDULE"
                                                  }
                                                ],
                                                "members": [
                                                  {
                                                    "id": 1,
                                                    "title": "홍길동",
                                                    "createdAt": "2024-07-20T16:30:00",
                                                    "type": "MEMBER",
                                                    "additionalInfo": "LAB_MEMBER"
                                                  }
                                                ]
                                              },
                                              "timestamp": "2024-07-25T15:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 랩실 대시보드에 접근할 권한이 없습니다",
                                              "data": null,
                                              "timestamp": "2024-07-25T15:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "랩실을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "랩실 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "해당 랩실을 찾을 수 없습니다",
                                              "data": null,
                                              "timestamp": "2024-07-25T15:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labDashboardPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW')")
    public ResponseEntity<ApiResponse<LabDashboardResponseDto>> getDashboard(
            @Parameter(description = "조회할 랩실의 ID", required = true, example = "1")
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다") Long labId
    ) {
        LabDashboardResponseDto dashboard = labDashboardQueryUseCase.getDashboardByLabId(labId);

        ApiResponse<LabDashboardResponseDto> response = ApiResponse.success(
                dashboard,
                "랩실 대시보드 조회 성공"
        );

        return ResponseEntity.ok(response);
    }
}
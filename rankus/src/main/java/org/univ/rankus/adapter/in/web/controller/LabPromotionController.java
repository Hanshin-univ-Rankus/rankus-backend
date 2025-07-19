package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.LabResponseDto;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.util.List;

/**
 * Lab 관련 REST API 컨트롤러
 * <p>
 * - GET /api/v1/labs         : 인증된 사용자 모두 접근 가능
 * - GET /api/v1/labs/{labId} : 인증된 사용자 모두 접근 가능
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labs")
@Tag(name = "LabPromotion", description = "랩실 홍보 API")
public class LabPromotionController {

    // 읽기 전용 Query 인터페이스 주입
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;

    /**
     * GET /api/labs
     * - 모든 랩실을 랭킹 내림차순으로 조회합니다.
     */
    @Operation(
            summary = "랩실 목록 조회", 
            description = """
                    모든 공개된 랩실을 랭킹 순으로 조회합니다.
                    
                    ## 기능 설명
                    - 인증 없이 접근 가능한 공개 API
                    - 랩실 홍보 및 정보 제공 목적
                    - 랭킹 순으로 정렬된 목록 반환
                    - 랩실 기본 정보와 통계 포함
                    
                    ## 포함 정보
                    - 랩실 기본 정보 (이름, 설명, 카테고리)
                    - 현재 멤버 수 및 모집 현황
                    - 랩실 점수 및 순위
                    - 최근 활동 정보
                    - 랩실 대표 이미지
                    
                    ## 활용 용도
                    - 랩실 탐색 및 비교
                    - 지원할 랩실 선택
                    - 랩실 홍보 효과 확인
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "랩실 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "랩실 목록 조회 성공",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "name": "AI 연구실",
                                                  "description": "인공지능과 머신러닝을 연구하는 랩실입니다",
                                                  "category": "COMPUTER_SCIENCE",
                                                  "memberCount": 12,
                                                  "maxMembers": 15,
                                                  "isRecruiting": true,
                                                  "totalScore": 850,
                                                  "rank": 1,
                                                  "professorName": "김교수",
                                                  "createdAt": "2024-01-01T09:00:00"
                                                },
                                                {
                                                  "id": 2,
                                                  "name": "소프트웨어 연구실",
                                                  "description": "웹 및 모바일 소프트웨어 개발 연구",
                                                  "category": "COMPUTER_SCIENCE",
                                                  "memberCount": 8,
                                                  "maxMembers": 10,
                                                  "isRecruiting": false,
                                                  "totalScore": 720,
                                                  "rank": 2,
                                                  "professorName": "이교수",
                                                  "createdAt": "2024-01-05T14:30:00"
                                                }
                                              ],
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<LabResponseDto>>> listLabs() {
        // 1) 도메인에서 데이터 조회
        List<Lab> labs = labPromotionQueryUseCase.listLabs();

        // 2) DTO 변환
        List<LabResponseDto> dtoList = LabResponseDto.fromList(labs);

        // 3) ApiResponse 빌드
        ApiResponse<List<LabResponseDto>> body = ApiResponse.success(dtoList, "랩실 목록 조회 성공");

        // 4) 200 OK + ApiResponse 바디 반환
        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/labs/{labId}
     * - ID로 특정 랩실을 조회합니다.
     */
    @Operation(
            summary = "랩실 상세 조회", 
            description = """
                    특정 랩실의 상세 정보를 조회합니다.
                    
                    ## 기능 설명
                    - 인증 없이 접근 가능한 공개 API
                    - 랩실의 모든 공개 정보 제공
                    - 지원 전 랩실 정보 확인 용도
                    - 랩실 홍보 페이지 데이터 소스
                    
                    ## 포함 정보
                    - 랩실 기본 정보 (이름, 설명, 연구분야)
                    - 교수 및 멤버 정보
                    - 랩실 통계 (점수, 순위, 활동 현황)
                    - 모집 정보 (모집 여부, 면접 정보)
                    - 랩실 이미지 및 연락처
                    - 최근 공지사항 및 성과
                    
                    ## 활용 용도
                    - 랩실 지원 전 정보 확인
                    - 랩실 비교 및 분석
                    - 랩실 홍보 페이지 구성
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "랩실 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "랩실 정보 조회 성공",
                                              "data": {
                                                "id": 1,
                                                "name": "AI 연구실",
                                                "description": "인공지능과 머신러닝을 연구하는 랩실입니다. 최신 AI 기술을 활용한 다양한 프로젝트를 진행하고 있습니다.",
                                                "category": "COMPUTER_SCIENCE",
                                                "researchField": "Artificial Intelligence, Machine Learning, Deep Learning",
                                                "professorName": "김교수",
                                                "professorEmail": "kim@university.edu",
                                                "memberCount": 12,
                                                "maxMembers": 15,
                                                "isRecruiting": true,
                                                "totalScore": 850,
                                                "rank": 1,
                                                "location": "공학관 301호",
                                                "contactEmail": "ai-lab@university.edu",
                                                "website": "https://ai-lab.university.edu",
                                                "establishedDate": "2023-03-01",
                                                "createdAt": "2024-01-01T09:00:00"
                                              },
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "랩실 정보 없음",
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
                                              "timestamp": "2024-02-15T14:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{labId}")
    public ResponseEntity<ApiResponse<LabResponseDto>> getLab(
            @Parameter(description = "조회할 랩실의 ID", required = true, example = "1")
            @PathVariable @Positive(message = "랩실 ID는 양수여야 합니다.") Long labId
    ) {
        // 1) 도메인에서 단일 객체 조회
        Lab lab = labPromotionQueryUseCase.getLabById(labId);

        // 2) DTO 변환
        LabResponseDto dto = LabResponseDto.from(lab);

        // 3) ApiResponse 빌드
        ApiResponse<LabResponseDto> body = ApiResponse.success(dto, "랩실 정보 조회 성공");

        // 4) 200 OK + ApiResponse 바디 반환
        return ResponseEntity.ok(body);
    }
}
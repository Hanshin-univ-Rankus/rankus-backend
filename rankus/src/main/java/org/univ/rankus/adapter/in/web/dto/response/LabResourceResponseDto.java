package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 랩실 자료 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "랩실 자료 응답")
public class LabResourceResponseDto {

    @Schema(description = "자료 ID", example = "1")
    private Long id;

    @Schema(description = "자료 제목", example = "알고리즘 강의자료")
    private String title;

    @Schema(description = "자료 설명", example = "정렬 알고리즘에 대한 상세한 설명 자료입니다")
    private String description;

    @Schema(description = "원본 파일명", example = "algorithm_lecture.pdf")
    private String fileName;

    @Schema(description = "파일 크기 (bytes)", example = "2048576")
    private Long fileSize;

    @Schema(description = "파일 크기 (MB)", example = "2.0")
    private Double fileSizeInMB;

    @Schema(description = "파일 확장자", example = "pdf")
    private String fileExtension;

    @Schema(description = "자료 카테고리", example = "LECTURE_NOTE")
    private ResourceCategory category;

    @Schema(description = "카테고리 표시명", example = "강의자료")
    private String categoryDisplayName;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "다운로드 횟수", example = "15")
    private Integer downloadCount;

    @Schema(description = "랩실 ID", example = "1")
    private Long labId;

    @Schema(description = "랩실 이름", example = "AI 연구실")
    private String labName;

    @Schema(description = "업로더 ID", example = "1")
    private Long uploaderId;

    @Schema(description = "업로더 이름", example = "김철수")
    private String uploaderName;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-16T14:20:00")
    private LocalDateTime updatedAt;

    /**
     * LabResource 엔티티로부터 DTO 생성
     */
    public static LabResourceResponseDto from(LabResource labResource) {
        return LabResourceResponseDto.builder()
                .id(labResource.getId())
                .title(labResource.getTitle())
                .description(labResource.getDescription())
                .fileName(labResource.getFileName())
                .fileSize(labResource.getFileSize())
                .fileSizeInMB(labResource.getFileSizeInMB())
                .fileExtension(labResource.getFileExtension())
                .category(labResource.getCategory())
                .categoryDisplayName(labResource.getCategory().getDisplayName())
                .isPublic(labResource.getIsPublic())
                .downloadCount(labResource.getDownloadCount())
                .labId(labResource.getLab().getId())
                .labName(labResource.getLab().getName())
                .uploaderId(labResource.getUploader().getId())
                .uploaderName(labResource.getUploader().getName())
                .createdAt(labResource.getCreatedAt())
                .updatedAt(labResource.getUpdatedAt())
                .build();
    }

    /**
     * LabResource 리스트로부터 DTO 리스트 생성
     */
    public static List<LabResourceResponseDto> fromList(List<LabResource> labResources) {
        return labResources.stream()
                .map(LabResourceResponseDto::from)
                .toList();
    }
}
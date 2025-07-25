package org.univ.rankus.application.port.in.query;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.util.List;

/**
 * 랩실 자료 Query UseCase
 */
public interface LabResourceQueryUseCase {

    /**
     * 랩실 자료 목록을 조회합니다 (페이징).
     *
     * @param labId         랩실 ID
     * @param category      자료 카테고리 (선택적)
     * @param search        검색어 (선택적)
     * @param pageable      페이징 정보
     * @param currentUserId 현재 사용자 ID
     * @return 자료 목록 페이지
     * @throws org.univ.rankus.domain.model.lab.exception.LabNotFoundException                    랩실을 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 조회 권한이 없는 경우
     */
    Page<LabResourceResponseDto> getLabResources(Long labId, ResourceCategory category, String search, Pageable pageable, Long currentUserId);

    /**
     * 랩실 자료 전체 목록을 조회합니다.
     *
     * @param labId         랩실 ID
     * @param currentUserId 현재 사용자 ID
     * @return 자료 목록
     * @throws org.univ.rankus.domain.model.lab.exception.LabNotFoundException                    랩실을 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 조회 권한이 없는 경우
     */
    List<LabResourceResponseDto> getAllLabResources(Long labId, Long currentUserId);

    /**
     * 특정 랩실 자료를 조회합니다.
     *
     * @param resourceId    자료 ID
     * @param currentUserId 현재 사용자 ID
     * @return 자료 정보
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException   자료를 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 조회 권한이 없는 경우
     */
    LabResourceResponseDto getLabResource(Long resourceId, Long currentUserId);

    /**
     * 자료 파일을 다운로드합니다.
     *
     * @param resourceId    자료 ID
     * @param currentUserId 현재 사용자 ID
     * @return 파일 리소스
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException   자료를 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 다운로드 권한이 없는 경우
     * @throws org.univ.rankus.domain.model.file.exception.FileUploadException                    파일을 찾을 수 없는 경우
     */
    Resource downloadLabResource(Long resourceId, Long currentUserId);

    /**
     * 사용자가 업로드한 자료 목록을 조회합니다.
     *
     * @param uploaderId 업로더 ID
     * @param pageable   페이징 정보
     * @return 자료 목록 페이지
     */
    Page<LabResourceResponseDto> getResourcesByUploader(Long uploaderId, Pageable pageable);

    /**
     * 자료 카테고리 목록을 조회합니다.
     *
     * @return 카테고리 목록
     */
    List<ResourceCategory> getResourceCategories();

    /**
     * 랩실별 자료 통계를 조회합니다.
     *
     * @param labId         랩실 ID
     * @param currentUserId 현재 사용자 ID
     * @return 자료 통계 정보
     * @throws org.univ.rankus.domain.model.lab.exception.LabNotFoundException                    랩실을 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 조회 권한이 없는 경우
     */
    LabResourceStatsDto getLabResourceStats(Long labId, Long currentUserId);

    /**
     * 랩실 자료 통계 DTO
     */
    record LabResourceStatsDto(
            long totalCount,
            long publicCount,
            long privateCount,
            java.util.Map<ResourceCategory, Long> countByCategory
    ) {
    }
}
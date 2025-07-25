package org.univ.rankus.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.util.List;
import java.util.Optional;

/**
 * 랩실 자료 Repository Port
 */
public interface LabResourceRepositoryPort {

    /**
     * 랩실 자료 저장
     */
    LabResource save(LabResource labResource);

    /**
     * ID로 랩실 자료 조회
     */
    Optional<LabResource> findById(Long id);

    /**
     * 랩실별 자료 목록 조회 (페이징)
     */
    Page<LabResource> findByLabId(Long labId, Pageable pageable);

    /**
     * 랩실별 자료 목록 조회 (전체)
     */
    List<LabResource> findByLabId(Long labId);

    /**
     * 랩실별 카테고리로 자료 목록 조회 (페이징)
     */
    Page<LabResource> findByLabIdAndCategory(Long labId, ResourceCategory category, Pageable pageable);

    /**
     * 랩실별 제목으로 자료 검색 (페이징)
     */
    Page<LabResource> findByLabIdAndTitleContaining(Long labId, String title, Pageable pageable);

    /**
     * 랩실별 카테고리와 제목으로 자료 검색 (페이징)
     */
    Page<LabResource> findByLabIdAndCategoryAndTitleContaining(Long labId, ResourceCategory category, String title, Pageable pageable);

    /**
     * 업로더별 자료 목록 조회 (페이징)
     */
    Page<LabResource> findByUploaderId(Long uploaderId, Pageable pageable);

    /**
     * 랩실별 공개 자료만 조회 (페이징)
     */
    Page<LabResource> findByLabIdAndIsPublicTrue(Long labId, Pageable pageable);

    /**
     * 랩실 자료 삭제
     */
    void delete(LabResource labResource);

    /**
     * ID로 랩실 자료 삭제
     */
    void deleteById(Long id);

    /**
     * 랩실별 자료 개수 조회
     */
    long countByLabId(Long labId);

    /**
     * 랩실별 카테고리 자료 개수 조회
     */
    long countByLabIdAndCategory(Long labId, ResourceCategory category);

    /**
     * 업로더별 자료 개수 조회
     */
    long countByUploaderId(Long uploaderId);

    /**
     * 파일 URL로 자료 존재 여부 확인
     */
    boolean existsByFileUrl(String fileUrl);
}
package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.util.List;

/**
 * LabResource용 Spring Data JPA Repository
 */
public interface SpringDataLabResourceRepository extends JpaRepository<LabResource, Long> {

    /**
     * 랩실별 자료 목록 조회 (페이징, 생성일시 역순)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.lab.id = :labId ORDER BY lr.createdAt DESC")
    Page<LabResource> findByLabId(@Param("labId") Long labId, Pageable pageable);

    /**
     * 랩실별 자료 목록 조회 (전체, 생성일시 역순)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.lab.id = :labId ORDER BY lr.createdAt DESC")
    List<LabResource> findByLabId(@Param("labId") Long labId);

    /**
     * 랩실별 카테고리로 자료 목록 조회 (페이징)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.lab.id = :labId AND lr.category = :category ORDER BY lr.createdAt DESC")
    Page<LabResource> findByLabIdAndCategory(@Param("labId") Long labId, @Param("category") ResourceCategory category, Pageable pageable);

    /**
     * 랩실별 제목으로 자료 검색 (페이징)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.lab.id = :labId AND lr.title LIKE %:title% ORDER BY lr.createdAt DESC")
    Page<LabResource> findByLabIdAndTitleContaining(@Param("labId") Long labId, @Param("title") String title, Pageable pageable);

    /**
     * 랩실별 카테고리와 제목으로 자료 검색 (페이징)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.lab.id = :labId AND lr.category = :category AND lr.title LIKE %:title% ORDER BY lr.createdAt DESC")
    Page<LabResource> findByLabIdAndCategoryAndTitleContaining(@Param("labId") Long labId, @Param("category") ResourceCategory category, @Param("title") String title, Pageable pageable);

    /**
     * 업로더별 자료 목록 조회 (페이징)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.uploader.id = :uploaderId ORDER BY lr.createdAt DESC")
    Page<LabResource> findByUploaderId(@Param("uploaderId") Long uploaderId, Pageable pageable);

    /**
     * 랩실별 공개 자료만 조회 (페이징)
     */
    @Query("SELECT lr FROM LabResource lr WHERE lr.lab.id = :labId AND lr.isPublic = true ORDER BY lr.createdAt DESC")
    Page<LabResource> findByLabIdAndIsPublicTrue(@Param("labId") Long labId, Pageable pageable);

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
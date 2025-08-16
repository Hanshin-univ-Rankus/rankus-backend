package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataLabNoticeRepository extends JpaRepository<LabNotice, Long> {

    // ...existing code...
    // 기존 단순 메서드는 사용하지 않고 fetch join 변형을 사용합니다.
    @Query("SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE l.id = :labId ORDER BY n.pinned DESC, n.createdAt DESC")
    List<LabNotice> findByLabIdOrderByIsPinnedDescCreatedAtDesc(@Param("labId") Long labId);

    @Query(
            value = "SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE l.id = :labId ORDER BY n.pinned DESC, n.createdAt DESC",
            countQuery = "SELECT COUNT(n) FROM LabNotice n WHERE n.lab.id = :labId"
    )
    Page<LabNotice> findByLabIdOrderByPinnedDescCreatedAtDesc(@Param("labId") Long labId, Pageable pageable);

    // 타입별 조회 - fetch join 변형
    @Query("SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE l.id = :labId AND n.type = :type ORDER BY n.pinned DESC, n.createdAt DESC")
    List<LabNotice> findByLabIdAndTypeWithJoins(@Param("labId") Long labId, @Param("type") NoticeType type);

    @Query(
            value = "SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE l.id = :labId AND n.type = :type ORDER BY n.pinned DESC, n.createdAt DESC",
            countQuery = "SELECT COUNT(n) FROM LabNotice n WHERE n.lab.id = :labId AND n.type = :type"
    )
    Page<LabNotice> findByLabIdAndTypeOrderByPinnedDescCreatedAtDesc(@Param("labId") Long labId, @Param("type") NoticeType type, Pageable pageable);

    @Query("SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE l.id = :labId AND n.pinned = :pinned ORDER BY n.createdAt DESC")
    List<LabNotice> findByLabIdAndPinnedOrderByCreatedAtDesc(@Param("labId") Long labId, @Param("pinned") boolean pinned);

    @Query("SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE l.id = :labId AND n.pinned = true ORDER BY n.createdAt DESC")
    List<LabNotice> findPinnedNoticesByLabId(@Param("labId") Long labId);

    // 상세 조회에도 fetch join 적용
    @Query("SELECT n FROM LabNotice n JOIN FETCH n.author a JOIN FETCH n.lab l WHERE n.id = :id")
    Optional<LabNotice> findByIdWithJoins(@Param("id") Long id);

    // 카운트 쿼리들은 기본 메서드 사용
    long countByLabId(Long labId);

    long countByLabIdAndType(Long labId, NoticeType type);
}
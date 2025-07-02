package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.notice.LabNotice;
import org.univ.rankus.domain.model.notice.NoticeType;

import java.util.List;

@Repository
public interface SpringDataLabNoticeRepository extends JpaRepository<LabNotice, Long> {

    List<LabNotice> findByLabId(Long labId);

    @Query("SELECT n FROM LabNotice n WHERE n.lab.id = :labId ORDER BY n.pinned DESC, n.createdAt DESC")
    List<LabNotice> findByLabIdOrderByIsPinnedDescCreatedAtDesc(@Param("labId") Long labId);

    @Query("SELECT n FROM LabNotice n WHERE n.lab.id = :labId ORDER BY n.pinned DESC, n.createdAt DESC")
    Page<LabNotice> findByLabIdOrderByPinnedDescCreatedAtDesc(@Param("labId") Long labId, Pageable pageable);

    List<LabNotice> findByLabIdAndType(Long labId, NoticeType type);

    @Query("SELECT n FROM LabNotice n WHERE n.lab.id = :labId AND n.type = :type ORDER BY n.pinned DESC, n.createdAt DESC")
    Page<LabNotice> findByLabIdAndTypeOrderByPinnedDescCreatedAtDesc(@Param("labId") Long labId, @Param("type") NoticeType type, Pageable pageable);

    @Query("SELECT n FROM LabNotice n WHERE n.lab.id = :labId AND n.pinned = :pinned ORDER BY n.createdAt DESC")
    List<LabNotice> findByLabIdAndPinnedOrderByCreatedAtDesc(@Param("labId") Long labId, @Param("pinned") boolean pinned);

    @Query("SELECT n FROM LabNotice n WHERE n.lab.id = :labId AND n.pinned = true ORDER BY n.createdAt DESC")
    List<LabNotice> findPinnedNoticesByLabId(@Param("labId") Long labId);

    long countByLabId(Long labId);

    long countByLabIdAndType(Long labId, NoticeType type);
}
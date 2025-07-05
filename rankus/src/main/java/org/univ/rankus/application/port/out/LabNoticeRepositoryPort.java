package org.univ.rankus.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;

import java.util.List;
import java.util.Optional;

public interface LabNoticeRepositoryPort {

    LabNotice save(LabNotice labNotice);

    Optional<LabNotice> findById(Long id);

    List<LabNotice> findByLabId(Long labId);

    List<LabNotice> findByLabIdOrderByIsPinnedDescCreatedAtDesc(Long labId);

    List<LabNotice> findByLabIdAndType(Long labId, NoticeType type);

    Page<LabNotice> findByLabId(Long labId, Pageable pageable);

    Page<LabNotice> findByLabIdOrderByIsPinnedDescCreatedAtDesc(Long labId, Pageable pageable);

    List<LabNotice> findByLabIdAndPinned(Long labId, boolean pinned);

    Page<LabNotice> findByLabIdAndType(Long labId, NoticeType type, Pageable pageable);

    List<LabNotice> findPinnedNoticesByLabId(Long labId);

    void deleteById(Long id);

    boolean existsById(Long id);

    long countByLabId(Long labId);

    long countByLabIdAndType(Long labId, NoticeType type);
}
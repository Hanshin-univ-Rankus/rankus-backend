package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataLabNoticeRepository;
import org.univ.rankus.application.port.out.LabNoticeRepositoryPort;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LabNoticeRepositoryAdapter implements LabNoticeRepositoryPort {

    private final SpringDataLabNoticeRepository springDataLabNoticeRepository;

    @Override
    public LabNotice save(LabNotice labNotice) {
        return springDataLabNoticeRepository.save(labNotice);
    }

    @Override
    public Optional<LabNotice> findById(Long id) {
        return springDataLabNoticeRepository.findByIdWithJoins(id);
    }

    @Override
    public List<LabNotice> findByLabId(Long labId) {
        // 정렬 일관성을 위해 pinned desc, createdAt desc로 조회합니다 (fetch join 포함)
        return springDataLabNoticeRepository.findByLabIdOrderByIsPinnedDescCreatedAtDesc(labId);
    }

    @Override
    public List<LabNotice> findByLabIdOrderByIsPinnedDescCreatedAtDesc(Long labId) {
        return springDataLabNoticeRepository.findByLabIdOrderByIsPinnedDescCreatedAtDesc(labId);
    }

    @Override
    public List<LabNotice> findByLabIdAndType(Long labId, NoticeType type) {
        return springDataLabNoticeRepository.findByLabIdAndTypeWithJoins(labId, type);
    }

    @Override
    public Page<LabNotice> findByLabId(Long labId, Pageable pageable) {
        return springDataLabNoticeRepository.findByLabIdOrderByPinnedDescCreatedAtDesc(labId, pageable);
    }

    @Override
    public Page<LabNotice> findByLabIdOrderByIsPinnedDescCreatedAtDesc(Long labId, Pageable pageable) {
        return springDataLabNoticeRepository.findByLabIdOrderByPinnedDescCreatedAtDesc(labId, pageable);
    }

    @Override
    public List<LabNotice> findByLabIdAndPinned(Long labId, boolean pinned) {
        return springDataLabNoticeRepository.findByLabIdAndPinnedOrderByCreatedAtDesc(labId, pinned);
    }

    @Override
    public Page<LabNotice> findByLabIdAndType(Long labId, NoticeType type, Pageable pageable) {
        return springDataLabNoticeRepository.findByLabIdAndTypeOrderByPinnedDescCreatedAtDesc(labId, type, pageable);
    }

    @Override
    public List<LabNotice> findPinnedNoticesByLabId(Long labId) {
        return springDataLabNoticeRepository.findPinnedNoticesByLabId(labId);
    }

    @Override
    public void deleteById(Long id) {
        springDataLabNoticeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataLabNoticeRepository.existsById(id);
    }

    @Override
    public long countByLabId(Long labId) {
        return springDataLabNoticeRepository.countByLabId(labId);
    }

    @Override
    public long countByLabIdAndType(Long labId, NoticeType type) {
        return springDataLabNoticeRepository.countByLabIdAndType(labId, type);
    }
}
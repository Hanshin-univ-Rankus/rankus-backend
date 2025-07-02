package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.LabNoticeQueryUseCase;
import org.univ.rankus.application.port.out.LabNoticeRepositoryPort;
import org.univ.rankus.domain.model.notice.LabNotice;
import org.univ.rankus.domain.model.notice.NoticeType;
import org.univ.rankus.domain.model.notice.exception.NoticeNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabNoticeQueryService implements LabNoticeQueryUseCase {

    private final LabNoticeRepositoryPort labNoticeRepositoryPort;

    @Override
    public LabNotice getNoticeById(Long noticeId) {
        return labNoticeRepositoryPort.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
    }

    @Override
    public List<LabNotice> getNoticesByLabId(Long labId) {
        return labNoticeRepositoryPort.findByLabIdOrderByIsPinnedDescCreatedAtDesc(labId);
    }

    @Override
    public Page<LabNotice> getNoticesByLabId(Long labId, Pageable pageable) {
        return labNoticeRepositoryPort.findByLabIdOrderByIsPinnedDescCreatedAtDesc(labId, pageable);
    }

    @Override
    public List<LabNotice> getNoticesByLabIdAndType(Long labId, NoticeType type) {
        return labNoticeRepositoryPort.findByLabIdAndType(labId, type);
    }

    @Override
    public List<LabNotice> getPinnedNoticesByLabId(Long labId) {
        return labNoticeRepositoryPort.findPinnedNoticesByLabId(labId);
    }

    @Override
    public long countNoticesByLabId(Long labId) {
        return labNoticeRepositoryPort.countByLabId(labId);
    }

    @Override
    public long countNoticesByLabIdAndType(Long labId, NoticeType type) {
        return labNoticeRepositoryPort.countByLabIdAndType(labId, type);
    }
}
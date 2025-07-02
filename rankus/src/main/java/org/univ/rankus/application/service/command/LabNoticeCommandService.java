package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.LabNoticeCommandUseCase;
import org.univ.rankus.application.port.out.LabNoticeRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.notice.LabNotice;
import org.univ.rankus.domain.model.notice.NoticeType;
import org.univ.rankus.domain.model.notice.exception.NoticeNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class LabNoticeCommandService implements LabNoticeCommandUseCase {

    private final LabNoticeRepositoryPort labNoticeRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public LabNotice createNotice(String title, String content, Long authorId, Long labId, NoticeType type, boolean isPinned) {
        // 1) 작성자 검증
        User author = userRepositoryPort.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2) 랩실 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3) 공지사항 생성 및 저장
        LabNotice notice = new LabNotice(title, content, type, isPinned, author, lab);
        return labNoticeRepositoryPort.save(notice);
    }

    @Override
    public LabNotice createNotice(String title, String content, Long authorId, Long labId) {
        return createNotice(title, content, authorId, labId, NoticeType.NORMAL, false);
    }

    @Override
    public LabNotice updateNotice(Long noticeId, String title, String content, NoticeType type, boolean isPinned) {
        // 1) 기존 공지사항 조회
        LabNotice notice = labNoticeRepositoryPort.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        // 2) 공지사항 수정
        notice.updateTitle(title);
        notice.updateContent(content);
        notice.updateType(type);

        if (isPinned) {
            notice.pin();
        } else {
            notice.unpin();
        }

        // 3) 저장 후 반환
        return labNoticeRepositoryPort.save(notice);
    }

    @Override
    public void deleteNotice(Long noticeId) {
        // 1) 존재 여부 확인
        if (!labNoticeRepositoryPort.existsById(noticeId)) {
            throw new NoticeNotFoundException(noticeId);
        }

        // 2) 삭제
        labNoticeRepositoryPort.deleteById(noticeId);
    }

    @Override
    public LabNotice togglePinNotice(Long noticeId) {
        // 1) 기존 공지사항 조회
        LabNotice notice = labNoticeRepositoryPort.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

        // 2) 고정 상태 토글
        notice.togglePin();

        // 3) 저장 후 반환
        return labNoticeRepositoryPort.save(notice);
    }
}
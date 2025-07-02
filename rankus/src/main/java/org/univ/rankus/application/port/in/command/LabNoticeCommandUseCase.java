package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.notice.LabNotice;
import org.univ.rankus.domain.model.notice.NoticeType;

public interface LabNoticeCommandUseCase {

    /**
     * 새로운 공지사항을 생성합니다.
     *
     * @param title    제목
     * @param content  내용
     * @param authorId 작성자 ID
     * @param labId    랩실 ID
     * @param type     공지사항 타입
     * @param isPinned 고정 여부
     * @return 생성된 공지사항
     */
    LabNotice createNotice(String title, String content, Long authorId, Long labId, NoticeType type, boolean isPinned);

    /**
     * 기본 타입으로 공지사항을 생성합니다. (NORMAL, 고정 안함)
     *
     * @param title    제목
     * @param content  내용
     * @param authorId 작성자 ID
     * @param labId    랩실 ID
     * @return 생성된 공지사항
     */
    LabNotice createNotice(String title, String content, Long authorId, Long labId);

    /**
     * 공지사항을 수정합니다.
     *
     * @param noticeId 공지사항 ID
     * @param title    새 제목
     * @param content  새 내용
     * @param type     새 타입
     * @param isPinned 새 고정 여부
     * @return 수정된 공지사항
     */
    LabNotice updateNotice(Long noticeId, String title, String content, NoticeType type, boolean isPinned);

    /**
     * 공지사항을 삭제합니다.
     *
     * @param noticeId 공지사항 ID
     */
    void deleteNotice(Long noticeId);

    /**
     * 공지사항의 고정 상태를 토글합니다.
     *
     * @param noticeId 공지사항 ID
     * @return 수정된 공지사항
     */
    LabNotice togglePinNotice(Long noticeId);
}
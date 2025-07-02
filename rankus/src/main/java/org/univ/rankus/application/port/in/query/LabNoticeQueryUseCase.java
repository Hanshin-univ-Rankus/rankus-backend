package org.univ.rankus.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.notice.LabNotice;
import org.univ.rankus.domain.model.notice.NoticeType;

import java.util.List;

public interface LabNoticeQueryUseCase {

    /**
     * ID로 특정 공지사항을 조회합니다.
     *
     * @param noticeId 조회할 공지사항 ID
     * @return 공지사항 엔티티
     * @throws org.univ.rankus.domain.model.notice.exception.NoticeNotFoundException
     */
    LabNotice getNoticeById(Long noticeId);

    /**
     * 특정 랩실의 모든 공지사항을 조회합니다.
     * 고정 공지 → 최신순으로 정렬
     *
     * @param labId 랩실 ID
     * @return 공지사항 리스트
     */
    List<LabNotice> getNoticesByLabId(Long labId);

    /**
     * 특정 랩실의 공지사항을 페이징하여 조회합니다.
     * 고정 공지 → 최신순으로 정렬
     *
     * @param labId    랩실 ID
     * @param pageable 페이징 정보
     * @return 페이징된 공지사항
     */
    Page<LabNotice> getNoticesByLabId(Long labId, Pageable pageable);

    /**
     * 특정 랩실의 특정 타입 공지사항을 조회합니다.
     *
     * @param labId 랩실 ID
     * @param type  공지사항 타입
     * @return 공지사항 리스트
     */
    List<LabNotice> getNoticesByLabIdAndType(Long labId, NoticeType type);

    /**
     * 특정 랩실의 고정된 공지사항만 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 고정 공지사항 리스트
     */
    List<LabNotice> getPinnedNoticesByLabId(Long labId);

    /**
     * 특정 랩실의 공지사항 개수를 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 공지사항 개수
     */
    long countNoticesByLabId(Long labId);

    /**
     * 특정 랩실의 특정 타입 공지사항 개수를 조회합니다.
     *
     * @param labId 랩실 ID
     * @param type  공지사항 타입
     * @return 공지사항 개수
     */
    long countNoticesByLabIdAndType(Long labId, NoticeType type);
}
package org.univ.rankus.application.port.in.query;

import org.univ.rankus.adapter.in.web.dto.response.LabDashboardResponseDto;

/**
 * 랩실 대시보드 조회를 위한 Use Case 인터페이스
 */
public interface LabDashboardQueryUseCase {

    /**
     * 특정 랩실의 대시보드 정보를 조회합니다.
     * 랩실 기본 정보와 5개 위젯(공지사항, 투표, 자료, 일정, 멤버)의 미리보기 데이터를 포함합니다.
     * 각 위젯은 최신 3개 항목을 조회합니다.
     *
     * @param labId 랩실 ID
     * @return 대시보드 응답 DTO
     */
    LabDashboardResponseDto getDashboardByLabId(Long labId);
}
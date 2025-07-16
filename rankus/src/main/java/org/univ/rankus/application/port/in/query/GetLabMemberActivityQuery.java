package org.univ.rankus.application.port.in.query;

import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;

/**
 * 랩원 활동 통계 조회 쿼리 인터페이스
 */
public interface GetLabMemberActivityQuery {

    /**
     * 특정 랩원의 활동 통계를 조회합니다.
     *
     * @param labId    랩실 ID
     * @param memberId 멤버 ID
     * @return 활동 통계 정보
     */
    LabMemberDetailResponse.LabActivityStats getLabMemberActivityStats(Long labId, Long memberId);
}
package org.univ.rankus.application.port.in.lab.statistics;

import org.univ.rankus.adapter.in.web.lab.statistics.dto.LabComprehensiveStatsResponse;

/**
 * 랩실 종합 통계 조회 쿼리 인터페이스
 */
public interface GetLabComprehensiveStatsQuery {

    /**
     * 전체 랩실 종합 통계를 조회합니다.
     * - ADMIN, PROFESSOR만 접근 가능
     *
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 랩실 종합 통계
     */
    LabComprehensiveStatsResponse getLabComprehensiveStats(Long requesterId);

    /**
     * 특정 랩실의 상세 통계를 조회합니다.
     *
     * @param labId       랩실 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 랩실 상세 통계
     */
    LabComprehensiveStatsResponse.LabStats getLabDetailedStats(Long labId, Long requesterId);
}
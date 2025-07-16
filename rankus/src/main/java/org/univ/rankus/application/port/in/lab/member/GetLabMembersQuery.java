package org.univ.rankus.application.port.in.lab.member;

import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;
import org.univ.rankus.domain.model.user.User;

import java.util.List;

/**
 * 랩실 멤버 조회 쿼리 인터페이스
 */
public interface GetLabMembersQuery {

    /**
     * 특정 랩실의 모든 멤버를 조회합니다.
     *
     * @param labId       조회할 랩실 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 해당 랩실에 속한 모든 사용자 목록
     */
    List<User> getLabMembers(Long labId, Long requesterId);

    /**
     * 특정 랩실의 멤버를 ID로 조회합니다.
     *
     * @param labId       조회할 랩실 ID
     * @param memberId    조회할 멤버 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 해당 멤버 정보
     */
    User getLabMember(Long labId, Long memberId, Long requesterId);

    /**
     * 특정 랩실의 멤버 상세 정보를 조회합니다.
     *
     * @param labId       조회할 랩실 ID
     * @param memberId    조회할 멤버 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 해당 멤버의 상세 정보 및 활동 통계
     */
    LabMemberDetailResponse getLabMemberDetail(Long labId, Long memberId, Long requesterId);
}
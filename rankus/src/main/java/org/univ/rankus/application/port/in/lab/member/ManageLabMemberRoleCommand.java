package org.univ.rankus.application.port.in.lab.member;

/**
 * 랩실 멤버 역할 관리 명령 인터페이스
 */
public interface ManageLabMemberRoleCommand {

    /**
     * 랩원을 랩매니저로 승급시킵니다.
     *
     * @param labId     랩실 ID
     * @param memberId  승급할 멤버 ID
     * @param managerId 승급을 수행하는 관리자 ID
     */
    void promoteToManager(Long labId, Long memberId, Long managerId);

    /**
     * 랩매니저를 랩원으로 강등시킵니다.
     *
     * @param labId     랩실 ID
     * @param memberId  강등할 멤버 ID
     * @param managerId 강등을 수행하는 관리자 ID
     */
    void demoteToMember(Long labId, Long memberId, Long managerId);
}
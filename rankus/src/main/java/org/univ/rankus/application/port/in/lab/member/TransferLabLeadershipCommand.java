package org.univ.rankus.application.port.in.lab.member;

/**
 * 랩장 위임 명령 인터페이스
 */
public interface TransferLabLeadershipCommand {

    /**
     * 랩장 권한을 다른 멤버에게 위임합니다.
     *
     * @param labId           랩실 ID
     * @param newLeaderId     새로운 랩장 ID
     * @param currentLeaderId 현재 랩장 ID
     */
    void transferLeadership(Long labId, Long newLeaderId, Long currentLeaderId);
}
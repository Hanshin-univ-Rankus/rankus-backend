package org.univ.rankus.application.service.lab.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.lab.member.ManageLabMemberRoleCommand;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

/**
 * 랩실 멤버 역할 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ManageLabMemberRoleService implements ManageLabMemberRoleCommand {

    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    @Override
    public void promoteToManager(Long labId, Long memberId, Long managerId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        // 2. 관리자 권한 확인
        User manager = userRepositoryPort.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));

        if (!canManageLabMembers(manager, lab)) {
            throw new IllegalStateException("You don't have permission to manage lab members");
        }

        // 3. 승급할 멤버 확인
        User member = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        // 4. 멤버가 해당 랩실에 속하는지 확인
        if (member.getLab() == null || !member.getLab().equals(lab)) {
            throw new IllegalArgumentException("Member does not belong to the specified lab");
        }

        // 5. 현재 역할 확인 (LAB_MEMBER만 승급 가능)
        if (member.getRole() != Role.LAB_MEMBER) {
            throw new IllegalStateException("Only LAB_MEMBER can be promoted to LAB_MANAGER");
        }

        // 6. 랩매니저로 승급
        member.changeRole(Role.LAB_MANAGER);
        userRepositoryPort.save(member);
    }

    @Override
    public void demoteToMember(Long labId, Long memberId, Long managerId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        // 2. 관리자 권한 확인
        User manager = userRepositoryPort.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));

        if (!canManageLabMembers(manager, lab)) {
            throw new IllegalStateException("You don't have permission to manage lab members");
        }

        // 3. 강등할 멤버 확인
        User member = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        // 4. 멤버가 해당 랩실에 속하는지 확인
        if (member.getLab() == null || !member.getLab().equals(lab)) {
            throw new IllegalArgumentException("Member does not belong to the specified lab");
        }

        // 5. 현재 역할 확인 (LAB_MANAGER만 강등 가능)
        if (member.getRole() != Role.LAB_MANAGER) {
            throw new IllegalStateException("Only LAB_MANAGER can be demoted to LAB_MEMBER");
        }

        // 6. 랩원으로 강등
        member.changeRole(Role.LAB_MEMBER);
        userRepositoryPort.save(member);
    }

    /**
     * 랩실 멤버 관리 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실 관리 가능
     * - LAB_LEADER: 소속 랩실만 관리 가능
     */
    private boolean canManageLabMembers(User user, Lab lab) {
        // ADMIN, PROFESSOR는 모든 랩실 관리 가능
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR) {
            return true;
        }

        // LAB_LEADER는 소속 랩실만 관리 가능
        return user.getRole() == Role.LAB_LEADER
                && user.getLab() != null
                && user.getLab().equals(lab);
    }
}
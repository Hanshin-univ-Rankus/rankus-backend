package org.univ.rankus.application.service.lab.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.lab.member.TransferLabLeadershipCommand;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

/**
 * 랩장 위임 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransferLabLeadershipService implements TransferLabLeadershipCommand {

    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    @Override
    public void transferLeadership(Long labId, Long newLeaderId, Long currentLeaderId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        // 2. 현재 랩장 확인
        User currentLeader = userRepositoryPort.findById(currentLeaderId)
                .orElseThrow(() -> new IllegalArgumentException("Current leader not found with id: " + currentLeaderId));

        // 3. 랩장 위임 권한 확인
        if (!canTransferLeadership(currentLeader, lab)) {
            throw new IllegalStateException("You don't have permission to transfer leadership");
        }

        // 4. 새로운 랩장 확인
        User newLeader = userRepositoryPort.findById(newLeaderId)
                .orElseThrow(() -> new IllegalArgumentException("New leader not found with id: " + newLeaderId));

        // 5. 새로운 랩장이 해당 랩실에 속하는지 확인
        if (newLeader.getLab() == null || !newLeader.getLab().equals(lab)) {
            throw new IllegalArgumentException("New leader does not belong to the specified lab");
        }

        // 6. 새로운 랩장이 랩원 이상인지 확인
        if (newLeader.getRole() == Role.STUDENT) {
            throw new IllegalStateException("New leader must be at least LAB_MEMBER");
        }

        // 7. 자기 자신에게 위임하는 것은 불가
        if (currentLeaderId.equals(newLeaderId)) {
            throw new IllegalArgumentException("Cannot transfer leadership to yourself");
        }

        // 8. 현재 랩장을 랩매니저로 강등
        currentLeader.changeRole(Role.LAB_MANAGER);
        userRepositoryPort.save(currentLeader);

        // 9. 새로운 랩장으로 승급
        newLeader.changeRole(Role.LAB_LEADER);
        userRepositoryPort.save(newLeader);
    }

    /**
     * 랩장 위임 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실에 대해 가능
     * - LAB_LEADER: 소속 랩실에 대해서만 가능
     */
    private boolean canTransferLeadership(User user, Lab lab) {
        // ADMIN, PROFESSOR는 모든 랩실에 대해 가능
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR) {
            return true;
        }

        // LAB_LEADER는 소속 랩실에 대해서만 가능
        return user.getRole() == Role.LAB_LEADER
                && user.getLab() != null
                && user.getLab().equals(lab);
    }
}
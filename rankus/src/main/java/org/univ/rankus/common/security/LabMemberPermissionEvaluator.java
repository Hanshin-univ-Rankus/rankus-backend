package org.univ.rankus.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

/**
 * 랩실 멤버 관련 권한 검증 컴포넌트
 */
@Component
@RequiredArgsConstructor
public class LabMemberPermissionEvaluator {

    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    /**
     * 랩실 멤버 조회 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실 조회 가능
     * - LAB_MEMBER 이상: 소속 랩실만 조회 가능
     */
    public boolean canViewLabMembers(Long labId, Long userId) {
        try {
            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Lab lab = labRepositoryPort.findById(labId)
                    .orElseThrow(() -> new IllegalArgumentException("Lab not found"));

            // 기존 출석 조회 권한과 동일한 로직 사용
            return user.canViewLabAttendance(lab);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 랩실 멤버 관리 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실 관리 가능
     * - LAB_MANAGER, LAB_LEADER: 소속 랩실만 관리 가능
     */
    public boolean canManageLabMembers(Long labId, Long userId) {
        try {
            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Lab lab = labRepositoryPort.findById(labId)
                    .orElseThrow(() -> new IllegalArgumentException("Lab not found"));

            // 기존 출석 관리 권한과 동일한 로직 사용
            return user.canManageLabAttendance(lab);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 랩장 위임 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실에 대해 가능
     * - LAB_LEADER: 소속 랩실에 대해서만 가능
     */
    public boolean canTransferLabLeadership(Long labId, Long userId) {
        try {
            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Lab lab = labRepositoryPort.findById(labId)
                    .orElseThrow(() -> new IllegalArgumentException("Lab not found"));

            // ADMIN, PROFESSOR는 모든 랩실에 대해 가능
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR) {
                return true;
            }

            // LAB_LEADER는 소속 랩실에 대해서만 가능
            return user.getRole() == Role.LAB_LEADER
                    && user.getLab() != null
                    && user.getLab().equals(lab);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 일반적인 권한 확인 메서드
     */
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        try {
            Long userId = getUserIdFromAuthentication(authentication);

            if (targetDomainObject instanceof Lab) {
                Lab lab = (Lab) targetDomainObject;
                String permissionStr = permission.toString();

                switch (permissionStr) {
                    case "VIEW_MEMBERS":
                        return canViewLabMembers(lab.getId(), userId);
                    case "MANAGE_MEMBERS":
                        return canManageLabMembers(lab.getId(), userId);
                    case "TRANSFER_LEADERSHIP":
                        return canTransferLabLeadership(lab.getId(), userId);
                    default:
                        return false;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof org.univ.rankus.common.security.customUser.CustomUserDetails) {
            return ((org.univ.rankus.common.security.customUser.CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        throw new IllegalArgumentException("Invalid authentication principal");
    }
}
package org.univ.rankus.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
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
     *
     * 참고: 멤버 조회 권한은 출석 조회 권한과 동일한 레벨을 사용합니다.
     * (LAB_MEMBER 이상이면 해당 랩실의 멤버 목록을 볼 수 있어야 함)
     */
    public boolean canViewLabMembers(Long labId, Authentication auth) {
        if (auth == null || labId == null) {
            return false;
        }

        // 1) 토큰 권한 기반 ADMIN/PROFESSOR 우선 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }

        // 2) DB 기반 권한 체크
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();

        try {
            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Lab lab = labRepositoryPort.findById(labId)
                    .orElseThrow(() -> new IllegalArgumentException("Lab not found"));

            // 멤버 조회 권한 = 출석 조회 권한과 동일 (LAB_MEMBER 이상)
            return user.canViewLabAttendance(lab);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 랩실 멤버 관리 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실 관리 가능
     * - LAB_MANAGER, LAB_LEADER: 소속 랩실만 관리 가능
     *
     * 참고: 멤버 관리 권한은 출석 관리 권한과 동일한 레벨을 사용합니다.
     * (LAB_MANAGER 이상이면 해당 랩실의 멤버를 관리할 수 있어야 함)
     */
    public boolean canManageLabMembers(Long labId, Authentication auth) {
        if (auth == null || labId == null) {
            return false;
        }

        // 1) 토큰 권한 기반 ADMIN/PROFESSOR 우선 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }

        // 2) DB 기반 권한 체크
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();

        try {
            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Lab lab = labRepositoryPort.findById(labId)
                    .orElseThrow(() -> new IllegalArgumentException("Lab not found"));

            // 멤버 관리 권한 = 출석 관리 권한과 동일 (LAB_MANAGER 이상)
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
    public boolean canTransferLabLeadership(Long labId, Authentication auth) {
        if (auth == null || labId == null) {
            return false;
        }

        // 1) 토큰 권한 기반 ADMIN/PROFESSOR 우선 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }

        // 2) DB 기반 권한 체크
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();

        try {
            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Lab lab = labRepositoryPort.findById(labId)
                    .orElseThrow(() -> new IllegalArgumentException("Lab not found"));

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
                        return canViewLabMembers(lab.getId(), authentication);
                    case "MANAGE_MEMBERS":
                        return canManageLabMembers(lab.getId(), authentication);
                    case "TRANSFER_LEADERSHIP":
                        return canTransferLabLeadership(lab.getId(), authentication);
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

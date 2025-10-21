package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabDashboardPermissionHandler implements DomainPermissionEvaluator {

    private final LabPromotionQueryUseCase labPromotionQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "LabDashboard";
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        if (auth == null) {
            return false;
        }
        // ADMIN/PROFESSOR는 토큰 권한으로 바로 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }
        return hasPermissionForLab(auth, targetId, permission);
    }

    /**
     * Lab ID를 기반으로 랩실 대시보드 접근 권한을 체크합니다.
     * 랩실 멤버(LAB_MEMBER 이상) + ADMIN, PROFESSOR만 접근 가능
     *
     * @param auth         인증 객체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW)
     * @return 권한 여부
     */
    public boolean hasPermissionForLab(Authentication auth, Serializable labId, String permission) {
        if (auth == null || !(labId instanceof Long) || permission == null) {
            return false;
        }

        // 1) 관리자/교수는 즉시 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }

        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById((Long) labId);
        String perm = permission.toUpperCase();

        if (PermissionConstants.VIEW.equals(perm)) {
            return user.canViewLabNotices(lab);
        }
        return false;
    }
}

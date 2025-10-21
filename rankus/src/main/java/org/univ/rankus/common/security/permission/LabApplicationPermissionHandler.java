package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabApplicationPermissionHandler implements DomainPermissionEvaluator {

    private final LabApplicationQueryUseCase queryUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;

    @Override
    public String targetType() {
        return "LabApplication";
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        if (auth == null || permission == null || !(targetId instanceof Long id)) {
            return false;
        }
        // ADMIN/PROFESSOR 토큰 권한 기반 우선 허용
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

        String perm = permission.toUpperCase();
        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);

        try {
            switch (perm) {
                case PermissionConstants.DELETE:
                case PermissionConstants.CANCEL: {
                    LabApplication app = queryUseCase.getApplicationById(id);
                    return user.isAdmin() || app.isOwnedBy(userId);
                }
                case PermissionConstants.APPROVE:
                case PermissionConstants.REJECT:
                case PermissionConstants.VIEW: {
                    LabApplication app = null;
                    try {
                        app = queryUseCase.getApplicationById(id);
                    } catch (RuntimeException notFoundOrInvalid) {
                        // appId가 아닐 수 있음 → labId로 간주
                    }

                    if (app != null) {
                        return user.canManageLabApplications(app.getLab());
                    }

                    Lab lab = labPromotionQueryUseCase.getLabById(id);
                    return user.canManageLabApplications(lab);
                }
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lab ID를 기반으로 가입 신청 목록/조회 권한을 체크합니다. (컨트롤러에서 직접 호출 가능)
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

        String perm = permission.toUpperCase();
        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById((Long) labId);

        if (PermissionConstants.VIEW.equals(perm)) {
            return user.canManageLabApplications(lab);
        }
        return false;
    }
}

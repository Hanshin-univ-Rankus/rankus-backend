package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
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
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        return hasPermissionForLab(principalObj, targetId, permission);
    }

    /**
     * Lab ID를 기반으로 랩실 대시보드 접근 권한을 체크합니다.
     * 랩실 멤버(LAB_MEMBER 이상) + ADMIN, PROFESSOR만 접근 가능
     *
     * @param principalObj 인증 주체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW)
     * @return 권한 여부
     */
    public boolean hasPermissionForLab(Object principalObj, Serializable labId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(labId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById((Long) labId);

        return switch (permission) {
            case "VIEW", "view" -> user.canViewLabNotices(lab); // 동일한 권한 로직 사용
            default -> false;
        };
    }
}
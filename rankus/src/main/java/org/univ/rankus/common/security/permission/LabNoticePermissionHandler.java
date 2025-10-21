package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabNoticeQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabNoticePermissionHandler implements DomainPermissionEvaluator {

    private final LabNoticeQueryUseCase noticeQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "LabNotice";
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        if (auth == null || !(targetId instanceof Long) || permission == null) {
            return false;
        }
        // ADMIN/PROFESSOR는 토큰 권한으로 바로 허용
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
        Long noticeId = (Long) targetId;

        LabNotice notice = noticeQueryUseCase.getNoticeById(noticeId);
        String perm = permission.toUpperCase();

        return switch (perm) {
            case PermissionConstants.VIEW -> user.canViewLabNotices(notice.getLab());
            case PermissionConstants.CREATE, PermissionConstants.UPDATE, PermissionConstants.DELETE, PermissionConstants.MANAGE ->
                    user.canManageLabNotices(notice.getLab());
            default -> false;
        };
    }

    /**
     * Lab ID를 기반으로 랩실 공지사항 권한을 체크합니다.
     *
     * @param auth         인증 객체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW_NOTICES, MANAGE_NOTICES)
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

        return switch (permission) {
            case PermissionConstants.VIEW_NOTICES -> user.canViewLabNotices(lab);
            case PermissionConstants.MANAGE_NOTICES -> user.canManageLabNotices(lab);
            default -> false;
        };
    }
}

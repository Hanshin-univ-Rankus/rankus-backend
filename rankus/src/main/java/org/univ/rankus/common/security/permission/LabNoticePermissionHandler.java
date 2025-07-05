package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
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
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long noticeId = (Long) targetId;

        LabNotice notice = noticeQueryUseCase.getNoticeById(noticeId);

        return switch (permission) {
            case "view", "VIEW" -> user.canViewLabNotices(notice.getLab());
            case "create", "CREATE", "update", "UPDATE", "delete", "DELETE" ->
                    user.canManageLabNotices(notice.getLab());
            case "manage", "MANAGE" -> user.canManageLabNotices(notice.getLab());
            default -> false;
        };
    }

    /**
     * Lab ID를 기반으로 랩실 공지사항 권한을 체크합니다.
     *
     * @param principalObj 인증 주체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW_NOTICES, MANAGE_NOTICES)
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
            case "VIEW_NOTICES" -> user.canViewLabNotices(lab);
            case "MANAGE_NOTICES" -> user.canManageLabNotices(lab);
            default -> false;
        };
    }
}
package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabApplicationPermissionHandler implements DomainPermissionEvaluator {

    private final LabApplicationQueryUseCase queryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "LabApplication";
    }

    @Override
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long id = (Long) targetId;

        // 모든 권한에 대해 applicationId로 처리
        LabApplication app = queryUseCase.getApplicationById(id);

        return switch (permission) {
            case "cancel", "DELETE" -> user.isAdmin() || app.isOwnedBy(userId);
            case "approve", "reject", "view" -> user.canManageLabApplications(app.getLab());
            default -> false;
        };
    }
}
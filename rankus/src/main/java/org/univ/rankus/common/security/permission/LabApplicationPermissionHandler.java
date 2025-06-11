package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabApplicationPermissionHandler implements DomainPermissionEvaluator {

    private final LabApplicationQueryUseCase queryUseCase;
    private final UserQueryUseCase           userQueryUseCase;

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
        LabApplication app = queryUseCase.getApplicationById((Long) targetId);
        User user = userQueryUseCase.getUserById(userId);

        switch (permission) {
            case "cancel":
                return app.isOwnedBy(userId);
            case "approve":
            case "reject":
            case "view":
                return user.isLabLeaderOrLabManagerInLab(app.getLab());
            default:
                return false;
        }
    }
}
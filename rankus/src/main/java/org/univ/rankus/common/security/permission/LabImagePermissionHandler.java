package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabImagePermissionHandler implements DomainPermissionEvaluator {

    private final LabImageQueryUseCase queryUseCase;
    private final UserQueryUseCase     userQueryUseCase;

    @Override
    public String targetType() {
        return "LabImage";
    }

    @Override
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }
        Long userId = ((CustomUserDetails) principalObj).getUserId();
        LabImage img = queryUseCase.getImageById((Long) targetId);
        User user = userQueryUseCase.getUserById(userId);

        switch (permission) {
            case "delete":
                return user.isLabLeaderOrLabManagerInLab(img.getLab());
            case "view":
                return true; // 인증된 사용자는 모두 조회 가능
            default:
                return false;
        }
    }
}
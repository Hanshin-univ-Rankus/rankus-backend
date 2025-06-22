package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabImagePermissionHandler implements DomainPermissionEvaluator {

    private final LabImageQueryUseCase queryUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final LabPromotionQueryUseCase labQueryUseCase;

    @Override
    public String targetType() {
        return "LabImage";
    }

    @Override
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);

        // targetId가 Lab ID인 경우 (이미지 생성 권한)
        if (permission.equals("create") && targetId instanceof Long) {
            Lab lab = labQueryUseCase.getLabById((Long) targetId);
            return user.isLabLeaderOrLabManagerInLab(lab);
        }

        // targetId가 LabImage ID인 경우 (이미지 삭제/조회 권한)
        if (targetId instanceof Long) {
            LabImage img = queryUseCase.getImageById((Long) targetId);

            switch (permission) {
                case "delete":
                    return user.isLabLeaderOrLabManagerInLab(img.getLab());
                case "view":
                    return true; // 인증된 사용자는 모두 조회 가능
                default:
                    return false;
            }
        }

        return false;
    }
}
package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabImage;
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
        if (!(principalObj instanceof CustomUserDetails) || permission == null) {
            return false;
        }
        if (!(targetId instanceof Long id)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        String perm = permission.toUpperCase();

        try {
            return switch (perm) {
                case PermissionConstants.CREATE -> { // targetId = labId
                    Lab lab = labQueryUseCase.getLabById(id);
                    yield user.canManageLabNotices(lab);
                }
                case PermissionConstants.DELETE -> { // targetId = imageId
                    LabImage img = queryUseCase.getImageById(id);
                    yield user.canManageLabNotices(img.getLab());
                }
                case PermissionConstants.VIEW -> true; // 공개 조회 허용
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }
}
package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabCreationRequestQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class LabCreationRequestPermissionHandler implements DomainPermissionEvaluator {

    private final LabCreationRequestQueryUseCase queryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "LabCreationRequest";
    }

    @Override
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long) || permission == null) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long requestId = (Long) targetId;
        String perm = permission.toUpperCase();

        LabCreationRequest request = queryUseCase.getLabCreationRequestById(requestId);

        return switch (perm) {
            case PermissionConstants.DELETE -> request.isOwnedBy(userId) || user.getRole() == Role.ADMIN;
            case PermissionConstants.VIEW -> request.isOwnedBy(userId) || isAdminOrProfessor(user);
            case PermissionConstants.APPROVE, PermissionConstants.REJECT -> isAdminOrProfessor(user);
            default -> false;
        };
    }

    private boolean isAdminOrProfessor(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR;
    }
}
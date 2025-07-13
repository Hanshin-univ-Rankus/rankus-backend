package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class AttendanceSessionPermissionHandler implements DomainPermissionEvaluator {

    private final AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "AttendanceSession";
    }

    @Override
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long sessionId = (Long) targetId;

        AttendanceSession session = attendanceSessionQueryUseCase.findSessionById(sessionId, userId);

        return switch (permission) {
            case "view", "VIEW" -> user.canViewLabAttendance(getLabFromSession(session));
            case "create", "CREATE", "update", "UPDATE", "delete", "DELETE" ->
                    user.canManageLabAttendance(getLabFromSession(session));
            case "manage", "MANAGE" -> user.canManageLabAttendance(getLabFromSession(session));
            default -> false;
        };
    }

    /**
     * Lab ID를 기반으로 출석 세션 권한을 체크합니다.
     *
     * @param principalObj 인증 주체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW_ATTENDANCE, MANAGE_ATTENDANCE)
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
            case "VIEW_ATTENDANCE" -> user.canViewLabAttendance(lab);
            case "MANAGE_ATTENDANCE" -> user.canManageLabAttendance(lab);
            default -> false;
        };
    }

    private Lab getLabFromSession(AttendanceSession session) {
        return labPromotionQueryUseCase.getLabById(session.getLabId());
    }
}
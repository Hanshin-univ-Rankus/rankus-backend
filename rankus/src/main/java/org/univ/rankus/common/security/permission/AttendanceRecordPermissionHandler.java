package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.AttendanceRecordQueryUseCase;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class AttendanceRecordPermissionHandler implements DomainPermissionEvaluator {

    private final AttendanceRecordQueryUseCase attendanceRecordQueryUseCase;
    private final AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "AttendanceRecord";
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        if (auth == null || permission == null || !(targetId instanceof Long recordId)) {
            return false;
        }
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
        String perm = permission.toUpperCase();

        AttendanceRecord record = attendanceRecordQueryUseCase.findRecordById(recordId, userId);
        AttendanceSession session = attendanceSessionQueryUseCase.findSessionById(record.getSessionId(), userId);
        Lab lab = labPromotionQueryUseCase.getLabById(session.getLabId());

        return switch (perm) {
            case PermissionConstants.VIEW -> record.isOwnedBy(userId) || user.canViewLabAttendance(lab);
            case PermissionConstants.UPDATE, PermissionConstants.DELETE, PermissionConstants.MANAGE -> user.canManageLabAttendance(lab);
            default -> false;
        };
    }

    /**
     * 세션 ID를 기반으로 출석 기록 권한을 체크합니다.
     */
    public boolean hasPermissionForSession(Authentication auth, Serializable sessionId, String permission) {
        if (auth == null || !(sessionId instanceof Long) || permission == null) {
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

        try {
            Long userId = ((CustomUserDetails) principalObj).getUserId();
            User user = userQueryUseCase.getUserById(userId);
            AttendanceSession session = attendanceSessionQueryUseCase.findSessionById((Long) sessionId, userId);
            Lab lab = labPromotionQueryUseCase.getLabById(session.getLabId());
            String perm = permission.toUpperCase();

            return switch (perm) {
                case PermissionConstants.VIEW -> user.canViewLabAttendance(lab);
                case PermissionConstants.MANAGE -> user.canManageLabAttendance(lab);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }
}

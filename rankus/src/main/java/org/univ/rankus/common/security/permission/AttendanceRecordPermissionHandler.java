package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
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
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long recordId = (Long) targetId;

        AttendanceRecord record = attendanceRecordQueryUseCase.findRecordById(recordId, userId);
        AttendanceSession session = attendanceSessionQueryUseCase.findSessionById(record.getSessionId(), userId);
        Lab lab = labPromotionQueryUseCase.getLabById(session.getLabId());

        return switch (permission) {
            case "view", "VIEW" -> {
                // 본인의 기록이거나 랩실 조회 권한이 있는 경우
                yield record.isOwnedBy(userId) || user.canViewLabAttendance(lab);
            }
            case "update", "UPDATE", "delete", "DELETE", "manage", "MANAGE" -> {
                // 랩실 출석 관리 권한이 있는 경우만
                yield user.canManageLabAttendance(lab);
            }
            default -> false;
        };
    }

    /**
     * 세션 ID를 기반으로 출석 기록 권한을 체크합니다.
     *
     * @param principalObj 인증 주체
     * @param sessionId    세션 ID
     * @param permission   권한 타입 (VIEW, MANAGE)
     * @return 권한 여부
     */
    public boolean hasPermissionForSession(Object principalObj, Serializable sessionId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(sessionId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        AttendanceSession session = attendanceSessionQueryUseCase.findSessionById((Long) sessionId, userId);
        Lab lab = labPromotionQueryUseCase.getLabById(session.getLabId());

        return switch (permission) {
            case "VIEW" -> user.canViewLabAttendance(lab);
            case "MANAGE" -> user.canManageLabAttendance(lab);
            default -> false;
        };
    }
}
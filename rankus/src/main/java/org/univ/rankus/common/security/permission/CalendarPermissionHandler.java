package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

/**
 * Calendar 도메인 권한 처리 핸들러
 * - 캘린더 이벤트 관리 권한 검증
 * - 랩실 기반 권한 체크
 */
@Component
@RequiredArgsConstructor
public class CalendarPermissionHandler {

    private final CalendarEventQueryUseCase calendarEventQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;

    /**
     * 랩실에 대한 캘린더 권한이 있는지 확인
     */
    public boolean hasPermissionForLab(CustomUserDetails userDetails, Long labId, String permission) {
        if (userDetails == null || labId == null) {
            return false;
        }

        Long userId = userDetails.getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById(labId);

        return switch (permission) {
            case "MANAGE_CALENDAR" -> user.canManageLabNotices(lab); // 기존 랩실 관리 권한 재사용
            case "VIEW_CALENDAR" -> true; // 모든 인증된 사용자가 조회 가능
            case "VIEW_APPLICANTS" -> user.canManageLabNotices(lab); // 랩장/매니저, 교수, 관리자만 지원자 정보 조회 가능
            default -> false;
        };
    }

    /**
     * 특정 캘린더 이벤트에 대한 권한이 있는지 확인
     */
    public boolean hasPermissionForEvent(CustomUserDetails userDetails, Long eventId, String permission) {
        if (userDetails == null || eventId == null) {
            return false;
        }

        Long userId = userDetails.getUserId();
        User user = userQueryUseCase.getUserById(userId);
        CalendarEvent event = calendarEventQueryUseCase.getEventById(eventId);

        return switch (permission) {
            case "MANAGE_CALENDAR" -> user.canManageLabNotices(event.getLab());
            case "VIEW_CALENDAR" -> true;
            case "VIEW_APPLICANTS" -> user.canManageLabNotices(event.getLab());
            default -> false;
        };
    }

    /**
     * 사용자가 특정 랩의 멤버인지 확인
     */
    public boolean isLabMember(CustomUserDetails userDetails, Long labId) {
        if (userDetails == null || labId == null) {
            return false;
        }

        Long userId = userDetails.getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById(labId);

        // 사용자가 해당 랩의 멤버인지 확인
        return user.getLab() != null && user.getLab().getId().equals(labId);
    }

    /**
     * 사용자가 특정 랩을 관리할 수 있는지 확인
     */
    public boolean canManageLab(CustomUserDetails userDetails, Long labId) {
        if (userDetails == null || labId == null) {
            return false;
        }

        Long userId = userDetails.getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById(labId);

        return user.canManageLabNotices(lab);
    }
}
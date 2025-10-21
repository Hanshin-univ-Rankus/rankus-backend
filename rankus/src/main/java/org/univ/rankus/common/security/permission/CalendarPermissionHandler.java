package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public boolean hasPermissionForLab(Authentication auth, Long labId, String permission) {
        if (auth == null || labId == null || permission == null) {
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
        if (!(principalObj instanceof CustomUserDetails userDetails)) {
            return false;
        }

        try {
            Long userId = userDetails.getUserId();
            User user = userQueryUseCase.getUserById(userId);
            Lab lab = labPromotionQueryUseCase.getLabById(labId);
            String perm = permission.toUpperCase();

            return switch (perm) {
                case PermissionConstants.MANAGE_CALENDAR -> user.canManageLabNotices(lab);
                case PermissionConstants.VIEW_CALENDAR -> user.canViewLabNotices(lab);  // ✅ 수정: 랩실 멤버만 조회 가능
                case PermissionConstants.VIEW_APPLICANTS -> user.canManageLabNotices(lab);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 특정 캘린더 이벤트에 대한 권한이 있는지 확인
     */
    public boolean hasPermissionForEvent(Authentication auth, Long eventId, String permission) {
        if (auth == null || eventId == null || permission == null) {
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
        if (!(principalObj instanceof CustomUserDetails userDetails)) {
            return false;
        }

        try {
            Long userId = userDetails.getUserId();
            User user = userQueryUseCase.getUserById(userId);
            CalendarEvent event = calendarEventQueryUseCase.getEventById(eventId);
            String perm = permission.toUpperCase();

            return switch (perm) {
                case PermissionConstants.MANAGE_CALENDAR -> user.canManageLabNotices(event.getLab());
                case PermissionConstants.VIEW_CALENDAR -> user.canViewLabNotices(event.getLab());  // ✅ 수정
                case PermissionConstants.VIEW_APPLICANTS -> user.canManageLabNotices(event.getLab());
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
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
}
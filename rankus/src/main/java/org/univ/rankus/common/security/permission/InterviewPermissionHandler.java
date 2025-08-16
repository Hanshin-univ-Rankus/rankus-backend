package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.InterviewQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

/**
 * Interview 도메인 권한 처리 핸들러
 * - 면접 관리 권한 검증
 * - 랩실 기반 권한 체크
 */
@Component
@RequiredArgsConstructor
public class InterviewPermissionHandler {

    private final InterviewQueryUseCase interviewQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;

    /**
     * 랩실에 대한 면접 관리 권한이 있는지 확인
     */
    public boolean hasPermissionForLab(CustomUserDetails userDetails, Long labId, String permission) {
        if (userDetails == null || labId == null || permission == null) {
            return false;
        }

        Long userId = userDetails.getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById(labId);
        String perm = permission.toUpperCase();

        return switch (perm) {
            case PermissionConstants.MANAGE_INTERVIEWS -> user.canManageLabNotices(lab); // 기존 랩실 관리 권한 재사용
            case PermissionConstants.VIEW_INTERVIEWS -> true; // 모든 인증된 사용자가 조회 가능
            default -> false;
        };
    }

    /**
     * 특정 면접에 대한 권한이 있는지 확인
     */
    public boolean hasPermissionForInterview(CustomUserDetails userDetails, Long interviewId, String permission) {
        if (userDetails == null || interviewId == null || permission == null) {
            return false;
        }

        Long userId = userDetails.getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Interview interview = interviewQueryUseCase.getInterviewById(interviewId);
        String perm = permission.toUpperCase();

        return switch (perm) {
            case PermissionConstants.MANAGE_INTERVIEWS -> user.canManageLabNotices(interview.getLab());
            case PermissionConstants.VIEW_INTERVIEWS -> true;
            default -> false;
        };
    }
}
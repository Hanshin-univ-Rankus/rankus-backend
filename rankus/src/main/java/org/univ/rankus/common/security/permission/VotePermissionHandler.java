package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.application.port.in.query.VoteQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class VotePermissionHandler implements DomainPermissionEvaluator {

    private final VoteQueryUseCase voteQueryUseCase;
    private final LabPromotionQueryUseCase labPromotionQueryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "Vote";
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        if (auth == null || permission == null || !(targetId instanceof Long voteId)) {
            return false;
        }

        // 1) 토큰 권한 기반 ADMIN/PROFESSOR 우선 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }

        // 2) 사용자/도메인 기반 판정
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            return false;
        }
        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        String perm = permission.toUpperCase();

        Vote vote = voteQueryUseCase.findVoteById(voteId);

        return switch (perm) {
            case PermissionConstants.VIEW -> user.canViewVotes(vote.getLab());
            case PermissionConstants.PARTICIPATE -> vote.canUserParticipate(user);
            case PermissionConstants.MANAGE -> vote.canUserManage(user);
            case PermissionConstants.DELETE -> vote.canUserManage(user) && vote.canBeDeleted();
            case PermissionConstants.VIEW_RESULTS -> user.canManageVotes(vote.getLab());
            default -> false;
        };
    }

    /**
     * Vote ID를 기반으로 투표 권한을 체크합니다.
     *
     * @param principalObj 인증 주체
     * @param voteId       투표 ID
     * @param permission   권한 타입 (VIEW, PARTICIPATE, MANAGE, DELETE, VIEW_RESULTS)
     * @return 권한 여부
     */
    public boolean hasPermissionForVote(Object principalObj, Serializable voteId, String permission) {
        return hasPermission(principalObj, voteId, permission);
    }

    /**
     * Lab ID를 기반으로 투표 권한을 체크합니다.
     *
     * @param principalObj 인증 주체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW_VOTES, CREATE_VOTE)
     * @return 권한 여부
     */
    public boolean hasPermissionForLab(Object principalObj, Serializable labId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(labId instanceof Long) || permission == null) {
            return false;
        }

        CustomUserDetails principal = (CustomUserDetails) principalObj;
        // 1) 관리자/교수는 즉시 허용 (DB 조회 없이 단축)
        for (GrantedAuthority auth : principal.getAuthorities()) {
            String a = auth.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }

        Long userId = principal.getUserId();
        String perm = permission.toUpperCase();

        try {
            User user = userQueryUseCase.getUserById(userId);
            Lab lab = labPromotionQueryUseCase.getLabById((Long) labId);

            return switch (perm) {
                case PermissionConstants.VIEW_VOTES -> user.canViewVotes(lab);
                case PermissionConstants.CREATE_VOTE -> user.canCreateVotes(lab);
                default -> false;
            };
        } catch (Exception e) {
            // 조회 실패 등 예외 발생 시 안전하게 거부
            return false;
        }
    }
}

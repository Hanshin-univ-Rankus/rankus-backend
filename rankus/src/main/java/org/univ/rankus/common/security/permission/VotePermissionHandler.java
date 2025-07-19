package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
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
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long voteId = (Long) targetId;

        Vote vote = voteQueryUseCase.findVoteById(voteId);

        return switch (permission) {
            case "view", "VIEW" -> user.canViewVotes(vote.getLab());
            case "participate", "PARTICIPATE" -> vote.canUserParticipate(user);
            case "manage", "MANAGE" -> vote.canUserManage(user);
            case "delete", "DELETE" -> vote.canUserManage(user) && vote.canBeDeleted();
            case "view_results", "VIEW_RESULTS" -> user.canManageVotes(vote.getLab());
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
        if (!(principalObj instanceof CustomUserDetails) || !(labId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Lab lab = labPromotionQueryUseCase.getLabById((Long) labId);

        return switch (permission) {
            case "VIEW_VOTES" -> user.canViewVotes(lab);
            case "CREATE_VOTE" -> user.canCreateVotes(lab);
            default -> false;
        };
    }
}
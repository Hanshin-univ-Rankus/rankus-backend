package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
            log.warn("VotePermissionHandler - Invalid parameters: auth={}, targetId={}, permission={}",
                    auth != null, targetId, permission);
            return false;
        }

        // 1) 토큰 권한 기반 ADMIN/PROFESSOR 우선 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                log.debug("VotePermissionHandler - ADMIN/PROFESSOR access granted for vote {}", voteId);
                return true;
            }
        }

        // 2) 사용자/도메인 기반 판정
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            log.warn("VotePermissionHandler - Invalid principal type for vote {}", voteId);
            return false;
        }

        try {
            Long userId = ((CustomUserDetails) principalObj).getUserId();
            User user = userQueryUseCase.getUserById(userId);
            String perm = permission.toUpperCase();

            Vote vote = voteQueryUseCase.findVoteById(voteId);

            boolean hasPermission = switch (perm) {
                case PermissionConstants.VIEW -> user.canViewVotes(vote.getLab());
                case PermissionConstants.PARTICIPATE -> vote.canUserParticipate(user);
                case PermissionConstants.MANAGE -> vote.canUserManage(user);
                case PermissionConstants.DELETE -> vote.canUserManage(user) && vote.canBeDeleted();
                case PermissionConstants.VIEW_RESULTS -> user.canManageVotes(vote.getLab());
                default -> false;
            };

            if (!hasPermission) {
                log.warn("VotePermissionHandler - Permission denied: userId={}, voteId={}, permission={}, userRole={}, userLab={}",
                        userId, voteId, perm, user.getRole(), user.getLab() != null ? user.getLab().getId() : null);
            } else {
                log.debug("VotePermissionHandler - Permission granted: userId={}, voteId={}, permission={}",
                        userId, voteId, perm);
            }

            return hasPermission;
        } catch (Exception e) {
            log.error("VotePermissionHandler - Exception during permission check for vote {}: {}",
                    voteId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Vote ID를 기반으로 투표 권한을 체크합니다.
     *
     * @param auth         인증 객체
     * @param voteId       투표 ID
     * @param permission   권한 타입 (VIEW, PARTICIPATE, MANAGE, DELETE, VIEW_RESULTS)
     * @return 권한 여부
     */
    public boolean hasPermissionForVote(Authentication auth, Serializable voteId, String permission) {
        return hasPermission(auth, voteId, permission);
    }

    /**
     * Lab ID를 기반으로 투표 권한을 체크합니다.
     *
     * @param auth         인증 객체
     * @param labId        랩실 ID
     * @param permission   권한 타입 (VIEW_VOTES, CREATE_VOTE)
     * @return 권한 여부
     */
    public boolean hasPermissionForLab(Authentication auth, Serializable labId, String permission) {
        if (auth == null || !(labId instanceof Long) || permission == null) {
            log.warn("VotePermissionHandler - Invalid lab parameters: auth={}, labId={}, permission={}",
                    auth != null, labId, permission);
            return false;
        }

        // 1) 관리자/교수는 즉시 허용 (DB 조회 없이 단축)
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                log.debug("VotePermissionHandler - ADMIN/PROFESSOR access granted for lab {}", labId);
                return true;
            }
        }

        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomUserDetails)) {
            log.warn("VotePermissionHandler - Invalid principal type for lab {}", labId);
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        String perm = permission.toUpperCase();

        try {
            User user = userQueryUseCase.getUserById(userId);
            Lab lab = labPromotionQueryUseCase.getLabById((Long) labId);

            boolean hasPermission = switch (perm) {
                case PermissionConstants.VIEW_VOTES -> user.canViewVotes(lab);
                case PermissionConstants.CREATE_VOTE -> user.canCreateVotes(lab);
                default -> false;
            };

            if (!hasPermission) {
                log.warn("VotePermissionHandler - Lab permission denied: userId={}, labId={}, permission={}, userRole={}, userLab={}",
                        userId, labId, perm, user.getRole(), user.getLab() != null ? user.getLab().getId() : null);
            } else {
                log.debug("VotePermissionHandler - Lab permission granted: userId={}, labId={}, permission={}",
                        userId, labId, perm);
            }

            return hasPermission;
        } catch (Exception e) {
            log.error("VotePermissionHandler - Exception during lab permission check for lab {}: {}",
                    labId, e.getMessage(), e);
            return false;
        }
    }
}

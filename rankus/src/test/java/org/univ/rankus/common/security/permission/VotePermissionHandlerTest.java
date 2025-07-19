package org.univ.rankus.common.security.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.application.port.in.query.VoteQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("VotePermissionHandler 단위 테스트")
class VotePermissionHandlerTest {

    @Mock
    private VoteQueryUseCase voteQueryUseCase;

    @Mock
    private LabPromotionQueryUseCase labPromotionQueryUseCase;

    @Mock
    private UserQueryUseCase userQueryUseCase;

    @InjectMocks
    private VotePermissionHandler handler;

    private CustomUserDetails principal;
    private static final Long USER_ID = 10L;
    private static final Long VOTE_ID = 100L;
    private static final Long LAB_ID = 200L;

    @BeforeEach
    void setUp() {
        principal = mock(CustomUserDetails.class);
    }

    @Nested
    @DisplayName("targetType 검증")
    class TargetTypeTests {

        @Test
        @DisplayName("Vote 타입을 반환")
        void targetType_returnsVote() {
            // when & then
            assertThat(handler.targetType()).isEqualTo("Vote");
        }
    }

    @Nested
    @DisplayName("투표별 권한 검증 (hasPermission)")
    class VotePermissionTests {

        @Test
        @DisplayName("view 권한 - 조회 권한이 있는 경우 허용")
        void view_permission_allowed() {
            // given
            User user = DomainUserFactory.buildAdminUser();
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean result = handler.hasPermission(principal, VOTE_ID, "view");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("participate 권한 - 참여 가능한 경우 허용")
        void participate_permission_allowed() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User user = DomainUserFactory.buildLabMemberWithLab(lab);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean result = handler.hasPermission(principal, VOTE_ID, "participate");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("manage 권한 - 관리 권한이 있는 경우 허용")
        void manage_permission_allowed() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User creator = DomainUserFactory.buildLabManagerWithLab(lab);
            Vote vote = DomainVoteFactory.buildVoteWithCreatorAndLab(creator, lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(creator);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean result = handler.hasPermission(principal, VOTE_ID, "manage");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("delete 권한 - 관리 권한이 있고 삭제 가능한 경우 허용")
        void delete_permission_allowed() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User creator = DomainUserFactory.buildLabManagerWithLab(lab);
            Vote vote = DomainVoteFactory.buildVoteWithCreatorAndLab(creator, lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(creator);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean result = handler.hasPermission(principal, VOTE_ID, "delete");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("view_results 권한 - 투표 관리 권한이 있는 경우 허용")
        void viewResults_permission_allowed() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User user = DomainUserFactory.buildLabLeaderWithLab(lab);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean result = handler.hasPermission(principal, VOTE_ID, "view_results");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("unknown 권한 - 알 수 없는 권한의 경우 거부")
        void unknown_permission_denied() {
            // given
            User user = DomainUserFactory.buildAdminUser();
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean result = handler.hasPermission(principal, VOTE_ID, "unknown");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("잘못된 principal 타입인 경우 거부")
        void invalid_principal_type_denied() {
            // given
            String invalidPrincipal = "invalid";

            // when
            boolean result = handler.hasPermission(invalidPrincipal, VOTE_ID, "view");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("잘못된 targetId 타입인 경우 거부")
        void invalid_targetId_type_denied() {
            // when
            boolean result = handler.hasPermission(principal, "invalid", "view");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("랩실별 권한 검증 (hasPermissionForLab)")
    class LabPermissionTests {

        @Test
        @DisplayName("VIEW_VOTES 권한 - 조회 권한이 있는 경우 허용")
        void viewVotes_permission_allowed() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User user = DomainUserFactory.buildLabMemberWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // when
            boolean result = handler.hasPermissionForLab(principal, LAB_ID, "VIEW_VOTES");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("CREATE_VOTE 권한 - 생성 권한이 있는 경우 허용")
        void createVote_permission_allowed() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User user = DomainUserFactory.buildLabManagerWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // when
            boolean result = handler.hasPermissionForLab(principal, LAB_ID, "CREATE_VOTE");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("CREATE_VOTE 권한 - LAB_MEMBER는 생성 권한이 없음")
        void createVote_permission_denied_for_member() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User user = DomainUserFactory.buildLabMemberWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // when
            boolean result = handler.hasPermissionForLab(principal, LAB_ID, "CREATE_VOTE");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("unknown 권한 - 알 수 없는 권한의 경우 거부")
        void unknown_lab_permission_denied() {
            // given
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User user = DomainUserFactory.buildAdminUser();

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // when
            boolean result = handler.hasPermissionForLab(principal, LAB_ID, "UNKNOWN");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("잘못된 principal 타입인 경우 거부")
        void invalid_principal_type_for_lab_denied() {
            // given
            String invalidPrincipal = "invalid";

            // when
            boolean result = handler.hasPermissionForLab(invalidPrincipal, LAB_ID, "VIEW_VOTES");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("잘못된 labId 타입인 경우 거부")
        void invalid_labId_type_denied() {
            // when
            boolean result = handler.hasPermissionForLab(principal, "invalid", "VIEW_VOTES");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermissionForVote 메서드 검증")
    class HasPermissionForVoteTests {

        @Test
        @DisplayName("hasPermissionForVote는 hasPermission과 동일하게 동작")
        void hasPermissionForVote_delegatesToHasPermission() {
            // given
            User user = DomainUserFactory.buildAdminUser();
            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);

            given(principal.getUserId()).willReturn(USER_ID);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(user);
            given(voteQueryUseCase.findVoteById(VOTE_ID)).willReturn(vote);

            // when
            boolean directResult = handler.hasPermission(principal, VOTE_ID, "view");
            boolean delegateResult = handler.hasPermissionForVote(principal, VOTE_ID, "view");

            // then
            assertThat(directResult).isEqualTo(delegateResult);
            assertThat(delegateResult).isTrue();
        }
    }
}
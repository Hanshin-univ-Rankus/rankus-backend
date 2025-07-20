package org.univ.rankus.domain.model.vote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoteParticipation 도메인 단위 테스트")
class VoteParticipationTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("투표가 null일 때 VOTE_NOT_FOUND 예외 발생")
        void constructor_nullVote_throwsVoteNotFound() {
            // given
            User user = DomainUserFactory.buildValidUserWithId(1L);
            VoteOption option = DomainVoteFactory.buildValidVoteOption();

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteParticipation(null, user, option)
            );

            assertEquals(VoteErrorCode.VOTE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("사용자가 null일 때 VOTE_PARTICIPATION_PERMISSION_DENIED 예외 발생")
        void constructor_nullUser_throwsParticipationPermissionDenied() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            VoteOption option = DomainVoteFactory.buildVoteOptionWithVote(vote);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteParticipation(vote, null, option)
            );

            assertEquals(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED, ex.getErrorCode());
        }

        @Test
        @DisplayName("선택 옵션이 null일 때 VOTE_OPTION_NOT_FOUND 예외 발생")
        void constructor_nullSelectedOption_throwsVoteOptionNotFound() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            User user = DomainUserFactory.buildValidUserWithId(1L);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteParticipation(vote, user, null)
            );

            assertEquals(VoteErrorCode.VOTE_OPTION_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효한 입력 시 정상 생성 및 투표 수 증가")
        void constructor_validInputs_success() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            User user = DomainUserFactory.buildValidUserWithId(1L);
            VoteOption option = DomainVoteFactory.buildVoteOptionWithVote(vote);
            int initialVoteCount = option.getVoteCount();

            // when
            VoteParticipation participation = new VoteParticipation(vote, user, option);

            // then
            assertEquals(vote, participation.getVote());
            assertEquals(user, participation.getUser());
            assertEquals(option, participation.getSelectedOption());
            assertEquals(initialVoteCount + 1, option.getVoteCount());
        }
    }

    @Nested
    @DisplayName("참여자 확인")
    class ParticipantVerificationTests {

        @Test
        @DisplayName("참여자 ID가 일치하면 true 반환")
        void isParticipatedBy_matchingUserId_returnsTrue() {
            // given
            User user = DomainUserFactory.buildValidUserWithId(123L);
            VoteParticipation participation = DomainVoteFactory.buildVoteParticipationWithUser(user);

            // when & then
            assertTrue(participation.isParticipatedBy(123L));
        }

        @Test
        @DisplayName("참여자 ID가 일치하지 않으면 false 반환")
        void isParticipatedBy_nonMatchingUserId_returnsFalse() {
            // given
            User user = DomainUserFactory.buildValidUserWithId(123L);
            VoteParticipation participation = DomainVoteFactory.buildVoteParticipationWithUser(user);

            // when & then
            assertFalse(participation.isParticipatedBy(456L));
        }
    }

    @Nested
    @DisplayName("투표 소속 확인")
    class VoteBelongingTests {

        @Test
        @DisplayName("투표 ID가 일치하면 true 반환")
        void belongsToVote_matchingVoteId_returnsTrue() {
            // given
            Vote vote = DomainVoteFactory.buildValidVoteWithId(789L);
            VoteParticipation participation = DomainVoteFactory.buildVoteParticipationWithVote(vote);

            // when & then
            assertTrue(participation.belongsToVote(789L));
        }

        @Test
        @DisplayName("투표 ID가 일치하지 않으면 false 반환")
        void belongsToVote_nonMatchingVoteId_returnsFalse() {
            // given
            Vote vote = DomainVoteFactory.buildValidVoteWithId(789L);
            VoteParticipation participation = DomainVoteFactory.buildVoteParticipationWithVote(vote);

            // when & then
            assertFalse(participation.belongsToVote(999L));
        }
    }

    @Nested
    @DisplayName("비즈니스 로직 검증")
    class BusinessLogicTests {

        @Test
        @DisplayName("투표 참여 시 선택한 옵션의 투표 수가 증가")
        void constructor_incrementsSelectedOptionVoteCount() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            User user = DomainUserFactory.buildValidUserWithId(1L);
            VoteOption option1 = DomainVoteFactory.buildVoteOptionWithVote(vote);
            VoteOption option2 = DomainVoteFactory.buildVoteOptionWithVote(vote);

            assertEquals(0, option1.getVoteCount());
            assertEquals(0, option2.getVoteCount());

            // when
            new VoteParticipation(vote, user, option1);

            // then
            assertEquals(1, option1.getVoteCount());
            assertEquals(0, option2.getVoteCount()); // 다른 옵션은 증가하지 않음
        }

        @Test
        @DisplayName("동일한 옵션에 여러 명이 투표하면 투표 수 누적")
        void multipleParticipations_incrementsVoteCount() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            VoteOption option = DomainVoteFactory.buildVoteOptionWithVote(vote);
            User user1 = DomainUserFactory.buildValidUserWithId(1L);
            User user2 = DomainUserFactory.buildValidUserWithId(2L);
            User user3 = DomainUserFactory.buildValidUserWithId(3L);

            assertEquals(0, option.getVoteCount());

            // when
            new VoteParticipation(vote, user1, option);
            new VoteParticipation(vote, user2, option);
            new VoteParticipation(vote, user3, option);

            // then
            assertEquals(3, option.getVoteCount());
        }
    }
}
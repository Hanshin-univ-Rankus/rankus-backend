package org.univ.rankus.domain.model.vote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoteOption 도메인 단위 테스트")
class VoteOptionTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("투표가 null일 때 VOTE_NOT_FOUND 예외 발생")
        void constructor_nullVote_throwsVoteNotFound() {
            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteOption(null, "선택지 내용", 1)
            );

            assertEquals(VoteErrorCode.VOTE_NOT_FOUND, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] optionText=''{0}'' → VOTE_OPTION_TEXT_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("선택지 내용이 null 또는 blank일 때 VOTE_OPTION_TEXT_REQUIRED 예외 발생")
        void constructor_nullOrBlankOptionText_throwsOptionTextRequired(String optionText) {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteOption(vote, optionText, 1)
            );
            assertEquals(VoteErrorCode.VOTE_OPTION_TEXT_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("선택지 내용 길이 100 초과 시 VOTE_OPTION_TEXT_TOO_LONG 예외 발생")
        void constructor_optionTextTooLong_throwsOptionTextTooLong() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            String longOptionText = "a".repeat(101);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteOption(vote, longOptionText, 1)
            );
            assertEquals(VoteErrorCode.VOTE_OPTION_TEXT_TOO_LONG, ex.getErrorCode());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("순서가 1 미만일 때 VOTE_OPTIONS_REQUIRED 예외 발생")
        void constructor_invalidOrder_throwsOptionsRequired(int order) {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteOption(vote, "선택지 내용", order)
            );
            assertEquals(VoteErrorCode.VOTE_OPTIONS_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("순서가 null일 때 VOTE_OPTIONS_REQUIRED 예외 발생")
        void constructor_nullOrder_throwsOptionsRequired() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new VoteOption(vote, "선택지 내용", null)
            );
            assertEquals(VoteErrorCode.VOTE_OPTIONS_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효한 입력 시 정상 생성")
        void constructor_validInputs_success() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            String optionText = "선택지 내용";
            Integer order = 1;

            // when
            VoteOption option = new VoteOption(vote, optionText, order);

            // then
            assertEquals(vote, option.getVote());
            assertEquals(optionText, option.getOptionText());
            assertEquals(order, option.getOptionOrder());
            assertEquals(0, option.getVoteCount());
        }

        @Test
        @DisplayName("선택지 내용 앞뒤 공백 제거")
        void constructor_trimOptionText_success() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            String optionText = "  선택지 내용  ";

            // when
            VoteOption option = new VoteOption(vote, optionText, 1);

            // then
            assertEquals("선택지 내용", option.getOptionText());
        }
    }

    @Nested
    @DisplayName("투표 수 관리")
    class VoteCountManagementTests {

        @Test
        @DisplayName("투표 수 증가")
        void incrementVoteCount_success() {
            // given
            VoteOption option = DomainVoteFactory.buildValidVoteOption();
            int initialCount = option.getVoteCount();

            // when
            option.incrementVoteCount();

            // then
            assertEquals(initialCount + 1, option.getVoteCount());
        }

        @Test
        @DisplayName("투표 수 감소")
        void decrementVoteCount_success() {
            // given
            VoteOption option = DomainVoteFactory.buildValidVoteOption();
            option.incrementVoteCount(); // 1로 만듦
            option.incrementVoteCount(); // 2로 만듦
            int initialCount = option.getVoteCount();

            // when
            option.decrementVoteCount();

            // then
            assertEquals(initialCount - 1, option.getVoteCount());
        }

        @Test
        @DisplayName("투표 수가 0일 때 감소해도 음수가 되지 않음")
        void decrementVoteCount_zeroCount_staysZero() {
            // given
            VoteOption option = DomainVoteFactory.buildValidVoteOption();
            assertEquals(0, option.getVoteCount());

            // when
            option.decrementVoteCount();

            // then
            assertEquals(0, option.getVoteCount());
        }

        @Test
        @DisplayName("여러 번 증가/감소 테스트")
        void voteCount_multipleOperations_success() {
            // given
            VoteOption option = DomainVoteFactory.buildValidVoteOption();

            // when & then
            option.incrementVoteCount(); // 1
            assertEquals(1, option.getVoteCount());

            option.incrementVoteCount(); // 2
            assertEquals(2, option.getVoteCount());

            option.incrementVoteCount(); // 3
            assertEquals(3, option.getVoteCount());

            option.decrementVoteCount(); // 2
            assertEquals(2, option.getVoteCount());

            option.decrementVoteCount(); // 1
            assertEquals(1, option.getVoteCount());

            option.decrementVoteCount(); // 0
            assertEquals(0, option.getVoteCount());

            option.decrementVoteCount(); // 0 (음수가 되지 않음)
            assertEquals(0, option.getVoteCount());
        }
    }
}
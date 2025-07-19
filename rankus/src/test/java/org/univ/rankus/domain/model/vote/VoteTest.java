package org.univ.rankus.domain.model.vote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vote 도메인 단위 테스트")
class VoteTest {

    private User createTestUser() {
        return DomainUserFactory.buildValidUserWithId(1L);
    }

    private Lab createTestLab() {
        return new Lab(
                "테스트랩",
                LabCategory.AI,
                "테스트 설명",
                "테스트 교수"
        );
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("생성자 null 입력 시 예외 발생")
        void constructor_nullCreator_throwsException() {
            // given
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(null, lab, "제목", "설명", deadline)
            );

            assertEquals(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED, ex.getErrorCode());
        }

        @Test
        @DisplayName("랩실 null 입력 시 예외 발생")
        void constructor_nullLab_throwsException() {
            // given
            User creator = createTestUser();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(creator, null, "제목", "설명", deadline)
            );

            assertEquals(VoteErrorCode.VOTE_NOT_FOUND, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] title=''{0}'' → VOTE_TITLE_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("제목이 null 또는 blank일 때 VOTE_TITLE_REQUIRED 예외 발생")
        void constructor_nullOrBlankTitle_throwsTitleRequired(String title) {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(creator, lab, title, "설명", deadline)
            );
            assertEquals(VoteErrorCode.VOTE_TITLE_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("제목 길이 200 초과 시 VOTE_TITLE_TOO_LONG 예외 발생")
        void constructor_titleTooLong_throwsTitleTooLong() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            String longTitle = "a".repeat(201);
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(creator, lab, longTitle, "설명", deadline)
            );
            assertEquals(VoteErrorCode.VOTE_TITLE_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("설명 길이 1000 초과 시 VOTE_DESCRIPTION_TOO_LONG 예외 발생")
        void constructor_descriptionTooLong_throwsDescriptionTooLong() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            String longDescription = "a".repeat(1001);
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(creator, lab, "제목", longDescription, deadline)
            );
            assertEquals(VoteErrorCode.VOTE_DESCRIPTION_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("마감일이 null일 때 VOTE_DEADLINE_REQUIRED 예외 발생")
        void constructor_nullDeadline_throwsDeadlineRequired() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(creator, lab, "제목", "설명", null)
            );
            assertEquals(VoteErrorCode.VOTE_DEADLINE_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("마감일이 과거일 때 VOTE_DEADLINE_PAST 예외 발생")
        void constructor_pastDeadline_throwsDeadlinePast() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime pastDeadline = LocalDateTime.now().minusDays(1);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> new Vote(creator, lab, "제목", "설명", pastDeadline)
            );
            assertEquals(VoteErrorCode.VOTE_DEADLINE_PAST, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효한 입력 시 정상 생성")
        void constructor_validInputs_success() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            // when
            Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);

            // then
            assertEquals("투표 제목", vote.getTitle());
            assertEquals("투표 설명", vote.getDescription());
            assertEquals(deadline, vote.getDeadline());
            assertEquals(VoteStatus.ACTIVE, vote.getStatus());
            assertEquals(0, vote.getTotalVotes());
            assertEquals(creator, vote.getCreator());
            assertEquals(lab, vote.getLab());
        }
    }

    @Nested
    @DisplayName("투표 선택지 관리")
    class OptionManagementTests {

        @Test
        @DisplayName("선택지 2개 미만 시 VOTE_OPTIONS_REQUIRED 예외 발생")
        void setOptions_lessThanTwo_throwsOptionsRequired() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> vote.setOptions(Collections.singletonList("옵션1"))
            );
            assertEquals(VoteErrorCode.VOTE_OPTIONS_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("선택지 5개 초과 시 VOTE_OPTIONS_TOO_MANY 예외 발생")
        void setOptions_moreThanFive_throwsOptionsTooMany() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            // when & then
            VoteValidationException ex = assertThrows(
                    VoteValidationException.class,
                    () -> vote.setOptions(Arrays.asList("옵션1", "옵션2", "옵션3", "옵션4", "옵션5", "옵션6"))
            );
            assertEquals(VoteErrorCode.VOTE_OPTIONS_TOO_MANY, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효한 선택지 설정 시 정상 동작")
        void setOptions_validOptions_success() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            // when
            vote.setOptions(Arrays.asList("옵션1", "옵션2", "옵션3"));

            // then
            assertEquals(3, vote.getOptions().size());
        }
    }

    @Nested
    @DisplayName("투표 상태 관리")
    class StatusManagementTests {

        @Test
        @DisplayName("활성 투표 종료 시 정상 동작")
        void close_activeVote_success() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            // when
            vote.close();

            // then
            assertEquals(VoteStatus.CLOSED, vote.getStatus());
        }

        @Test
        @DisplayName("활성 투표 취소 시 정상 동작")
        void cancel_activeVote_success() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            // when
            vote.cancel();

            // then
            assertEquals(VoteStatus.CANCELED, vote.getStatus());
        }
    }

    @Nested
    @DisplayName("권한 검증")
    class PermissionTests {

        @Test
        @DisplayName("투표 생성자는 관리 권한을 가짐")
        void canUserManage_creator_returnsTrue() {
            // given
            User creator = createTestUser();
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            // when & then
            assertTrue(vote.canUserManage(creator));
        }

        @Test
        @DisplayName("ADMIN은 모든 투표 관리 권한을 가짐")
        void canUserManage_admin_returnsTrue() {
            // given
            User creator = DomainUserFactory.buildValidUserWithId(1L);
            Lab lab = createTestLab();
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab, "제목", deadline);

            User admin = DomainUserFactory.buildValidUserWithId(2L);
            admin.changeRole(Role.ADMIN);

            // when & then
            assertTrue(vote.canUserManage(admin));
        }

        @Test
        @DisplayName("랩실이 다른 사용자는 관리 권한이 없음")
        void canUserManage_differentLab_returnsFalse() {
            // given
            User creator = DomainUserFactory.buildValidUserWithId(1L);
            Lab lab1 = createTestLab();
            creator.assignLab(lab1);
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);
            Vote vote = new Vote(creator, lab1, "제목", deadline);

            User otherUser = DomainUserFactory.buildValidUserWithId(2L);
            Lab lab2 = createTestLab();
            otherUser.assignLab(lab2);
            otherUser.changeRole(Role.LAB_LEADER);

            // when & then
            assertFalse(vote.canUserManage(otherUser));
        }
    }
}
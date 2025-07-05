package org.univ.rankus.domain.model.notice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeErrorCode;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeValidationException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LabNotice 도메인 단위 테스트")
class LabNoticeTest {

    private final String validTitle = "테스트 공지사항";
    private final String validContent = "공지사항 내용입니다.";
    private final NoticeType validType = NoticeType.NORMAL;
    private final boolean validPinned = false;
    private User validAuthor;
    private Lab validLab;

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 값으로 공지사항 생성 성공 - 기본 생성자")
        void validNotice_basic_constructor_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();

            // when
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // then
            assertThat(notice.getTitle()).isEqualTo(validTitle);
            assertThat(notice.getContent()).isEqualTo(validContent);
            assertThat(notice.getAuthor()).isEqualTo(validAuthor);
            assertThat(notice.getLab()).isEqualTo(validLab);
            assertThat(notice.getType()).isEqualTo(NoticeType.NORMAL);  // 기본값
            assertThat(notice.isPinned()).isFalse();  // 기본값
        }

        @Test
        @DisplayName("유효한 값으로 공지사항 생성 성공 - 전체 생성자")
        void validNotice_full_constructor_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();

            // when
            LabNotice notice = new LabNotice(validTitle, validContent, NoticeType.URGENT, true, validAuthor, validLab);

            // then
            assertThat(notice.getTitle()).isEqualTo(validTitle);
            assertThat(notice.getContent()).isEqualTo(validContent);
            assertThat(notice.getType()).isEqualTo(NoticeType.URGENT);
            assertThat(notice.isPinned()).isTrue();
            assertThat(notice.getAuthor()).isEqualTo(validAuthor);
            assertThat(notice.getLab()).isEqualTo(validLab);
        }

        @ParameterizedTest(name = "[{index}] title=''{0}'' → TITLE_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("title이 null 또는 blank일 경우 TITLE_REQUIRED 예외 발생")
        void titleNullOrBlank_throwsTitleRequired(String title) {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(title, validContent, validAuthor, validLab)
            );
            assertEquals(NoticeErrorCode.TITLE_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("title이 100자를 초과할 경우 TITLE_TOO_LONG 예외 발생")
        void titleTooLong_throwsTitleTooLong() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            String longTitle = "a".repeat(101);

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(longTitle, validContent, validAuthor, validLab)
            );
            assertEquals(NoticeErrorCode.TITLE_TOO_LONG, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] content=''{0}'' → CONTENT_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("content가 null 또는 blank일 경우 CONTENT_REQUIRED 예외 발생")
        void contentNullOrBlank_throwsContentRequired(String content) {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(validTitle, content, validAuthor, validLab)
            );
            assertEquals(NoticeErrorCode.CONTENT_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("content가 2000자를 초과할 경우 CONTENT_TOO_LONG 예외 발생")
        void contentTooLong_throwsContentTooLong() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            String longContent = "a".repeat(2001);

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(validTitle, longContent, validAuthor, validLab)
            );
            assertEquals(NoticeErrorCode.CONTENT_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("author가 null일 경우 AUTHOR_REQUIRED 예외 발생")
        void authorNull_throwsAuthorRequired() {
            // given
            validLab = DomainLabFactory.buildAiLab();

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(validTitle, validContent, null, validLab)
            );
            assertEquals(NoticeErrorCode.AUTHOR_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("lab이 null일 경우 LAB_REQUIRED 예외 발생")
        void labNull_throwsLabRequired() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(validTitle, validContent, validAuthor, null)
            );
            assertEquals(NoticeErrorCode.LAB_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("type이 null일 경우 TYPE_REQUIRED 예외 발생")
        void typeNull_throwsTypeRequired() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();

            // when & then
            NoticeValidationException ex = assertThrows(
                    NoticeValidationException.class,
                    () -> new LabNotice(validTitle, validContent, null, false, validAuthor, validLab)
            );
            assertEquals(NoticeErrorCode.TYPE_REQUIRED, ex.getErrorCode());
        }
    }


    @Nested
    @DisplayName("비즈니스 로직 검증")
    class BusinessLogicTests {

        @Test
        @DisplayName("공지사항 고정 설정")
        void pin_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when
            notice.pin();

            // then
            assertThat(notice.isPinned()).isTrue();
        }

        @Test
        @DisplayName("공지사항 고정 해제")
        void unpin_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, NoticeType.NORMAL, true, validAuthor, validLab);

            // when
            notice.unpin();

            // then
            assertThat(notice.isPinned()).isFalse();
        }

        @Test
        @DisplayName("공지사항 고정 상태 토글")
        void togglePin_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);
            boolean initialPinnedState = notice.isPinned();

            // when
            notice.togglePin();

            // then
            assertThat(notice.isPinned()).isEqualTo(!initialPinnedState);

            // when - 다시 토글
            notice.togglePin();

            // then
            assertThat(notice.isPinned()).isEqualTo(initialPinnedState);
        }

        @Test
        @DisplayName("공지사항 작성자 확인 - 동일한 작성자")
        void isAuthor_sameAuthor_returnsTrue() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when
            boolean result = notice.isAuthor(validAuthor);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("공지사항 작성자 확인 - 다른 작성자")
        void isAuthor_differentAuthor_returnsFalse() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            User otherAuthor = DomainUserFactory.buildLabMemberUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when
            boolean result = notice.isAuthor(otherAuthor);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("공지사항이 특정 랩실에 속하는지 확인 - 동일한 랩실")
        void belongsToLab_sameLab_returnsTrue() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when
            boolean result = notice.belongsToLab(validLab);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("공지사항이 특정 랩실에 속하는지 확인 - 다른 랩실")
        void belongsToLab_differentLab_returnsFalse() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            Lab otherLab = new Lab("다른랩", LabCategory.DB, "데이터베이스 연구실", "다른교수");
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when
            boolean result = notice.belongsToLab(otherLab);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("긴급 공지사항 확인 - URGENT 타입")
        void isUrgent_urgentType_returnsTrue() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, NoticeType.URGENT, false, validAuthor, validLab);

            // when
            boolean result = notice.isUrgent();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("긴급 공지사항 확인 - NORMAL 타입")
        void isUrgent_normalType_returnsFalse() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, NoticeType.NORMAL, false, validAuthor, validLab);

            // when
            boolean result = notice.isUrgent();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("수정 메서드 검증")
    class UpdateMethodTests {

        @Test
        @DisplayName("제목 수정 성공")
        void updateTitle_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);
            String newTitle = "수정된 제목";

            // when
            notice.updateTitle(newTitle);

            // then
            assertThat(notice.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("내용 수정 성공")
        void updateContent_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);
            String newContent = "수정된 내용입니다.";

            // when
            notice.updateContent(newContent);

            // then
            assertThat(notice.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("타입 수정 성공")
        void updateType_success() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);
            NoticeType newType = NoticeType.URGENT;

            // when
            notice.updateType(newType);

            // then
            assertThat(notice.getType()).isEqualTo(newType);
        }

        @ParameterizedTest(name = "[{index}] title=''{0}'' → TITLE_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("잘못된 제목으로 수정시 예외 발생")
        void updateTitle_invalidTitle_throwsException(String invalidTitle) {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when & then
            assertThatThrownBy(() -> notice.updateTitle(invalidTitle))
                    .isInstanceOf(NoticeValidationException.class);
        }

        @ParameterizedTest(name = "[{index}] content=''{0}'' → CONTENT_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("잘못된 내용으로 수정시 예외 발생")
        void updateContent_invalidContent_throwsException(String invalidContent) {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when & then
            assertThatThrownBy(() -> notice.updateContent(invalidContent))
                    .isInstanceOf(NoticeValidationException.class);
        }

        @Test
        @DisplayName("null 타입으로 수정시 예외 발생")
        void updateType_nullType_throwsException() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            LabNotice notice = new LabNotice(validTitle, validContent, validAuthor, validLab);

            // when & then
            assertThatThrownBy(() -> notice.updateType(null))
                    .isInstanceOf(NoticeValidationException.class)
                    .satisfies(exception -> {
                        NoticeValidationException noticeException = (NoticeValidationException) exception;
                        assertThat(noticeException.getErrorCode()).isEqualTo(NoticeErrorCode.TYPE_REQUIRED);
                    });
        }
    }

    @Nested
    @DisplayName("문자열 검증 및 Trim 처리")
    class StringValidationTests {

        @Test
        @DisplayName("제목과 내용 앞뒤 공백 제거")
        void titleAndContent_trimWhitespace() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            String titleWithSpaces = "  제목  ";
            String contentWithSpaces = "  내용  ";

            // when
            LabNotice notice = new LabNotice(titleWithSpaces, contentWithSpaces, validAuthor, validLab);

            // then
            assertThat(notice.getTitle()).isEqualTo("제목");
            assertThat(notice.getContent()).isEqualTo("내용");
        }

        @Test
        @DisplayName("제목 최대 길이 경계값 테스트")
        void title_boundaryLength_test() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            String exactLengthTitle = "a".repeat(100);  // 정확히 100자

            // when
            LabNotice notice = new LabNotice(exactLengthTitle, validContent, validAuthor, validLab);

            // then
            assertThat(notice.getTitle()).isEqualTo(exactLengthTitle);
            assertThat(notice.getTitle().length()).isEqualTo(100);
        }

        @Test
        @DisplayName("내용 최대 길이 경계값 테스트")
        void content_boundaryLength_test() {
            // given
            validAuthor = DomainUserFactory.buildStudentUser();
            validLab = DomainLabFactory.buildAiLab();
            String exactLengthContent = "a".repeat(2000);  // 정확히 2000자

            // when
            LabNotice notice = new LabNotice(validTitle, exactLengthContent, validAuthor, validLab);

            // then
            assertThat(notice.getContent()).isEqualTo(exactLengthContent);
            assertThat(notice.getContent().length()).isEqualTo(2000);
        }
    }
}
package org.univ.rankus.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 도메인 단위 테스트")
class UserTest {

    private final Lab dummyLab = new Lab("DummyLab", "설명", "학과", LabCategory.AI);

    @Nested
    @DisplayName("생성 동작 테스트")
    class CreationTests {

        @Test
        @DisplayName("올바른 정보로 생성하면 모든 필드가 정상 설정된다")
        void createUser_success() {
            // given
            String name = "홍길동";
            String email = "test@univ.ac.kr";
            String rawPassword = "password123";

            // when
            User user = new User(name, email, rawPassword, dummyLab);

            // then
            assertAll("User 필드 검증",
                    () -> assertNull(user.getId(), "영속화 전이므로 id는 null이어야 한다"),
                    () -> assertEquals(name, user.getName(), "이름이 설정되어야 한다"),
                    () -> assertEquals(email, user.getEmail(), "이메일이 설정되어야 한다"),
                    () -> assertNotNull(user.getPassword().getHash(), "비밀번호 해시가 생성되어야 한다"),
                    () -> assertTrue(user.matchesPassword(rawPassword), "matchesPassword로 원문 검증 가능해야 한다"),
                    () -> assertEquals(dummyLab, user.getLab(), "소속 랩실이 설정되어야 한다")
            );
        }

        @Test
        @DisplayName("소속 랩실이 null이어도 생성에 성공한다")
        void createUser_nullLab_success() {
            // given
            String name = "김철수";
            String email = "kim@univ.ac.kr";
            String rawPassword = "securePass1";

            // when
            User user = new User(name, email, rawPassword, null);

            // then
            assertAll("null lab 허용 검증",
                    () -> assertNull(user.getLab(), "lab이 null이어야 한다"),
                    () -> assertEquals(name, user.getName()),
                    () -> assertEquals(email, user.getEmail()),
                    () -> assertTrue(user.matchesPassword(rawPassword))
            );
        }

        @ParameterizedTest(name = "이름이 빈 문자열 \"{0}\" 일 때 예외 발생")
        @ValueSource(strings = { "", "   " })
        @DisplayName("이름이 비어 있으면 IllegalArgumentException이 발생한다")
        void createUser_invalidName_throws(String invalidName) {
            assertThrows(IllegalArgumentException.class, () ->
                    new User(invalidName, "test@univ.ac.kr", "password123", dummyLab)
            );
        }

        @Test
        @DisplayName("이름이 null이면 IllegalArgumentException이 발생한다")
        void createUser_nullName_throws() {
            assertThrows(IllegalArgumentException.class, () ->
                    new User(null, "test@univ.ac.kr", "password123", dummyLab)
            );
        }

        @ParameterizedTest(name = "이메일 포맷이 잘못된 \"{0}\" 일 때 예외 발생")
        @ValueSource(strings = { "", "invalid-email", "user@.com", "user.com" })
        @DisplayName("유효하지 않은 이메일이면 IllegalArgumentException이 발생한다")
        void createUser_invalidEmail_throws(String invalidEmail) {
            assertThrows(IllegalArgumentException.class, () ->
                    new User("홍길동", invalidEmail, "password123", dummyLab)
            );
        }

        @Test
        @DisplayName("이메일이 null이면 IllegalArgumentException이 발생한다")
        void createUser_nullEmail_throws() {
            assertThrows(IllegalArgumentException.class, () ->
                    new User("홍길동", null, "password123", dummyLab)
            );
        }

        @ParameterizedTest(name = "비밀번호 길이가 {0}자리일 때 예외 발생")
        @ValueSource(strings = { "", "short", "1234567" })
        @DisplayName("비밀번호가 8자 미만이면 IllegalArgumentException이 발생한다")
        void createUser_shortPassword_throws(String shortPwd) {
            assertThrows(IllegalArgumentException.class, () ->
                    new User("홍길동", "test@univ.ac.kr", shortPwd, dummyLab)
            );
        }

        @Test
        @DisplayName("비밀번호가 null이면 IllegalArgumentException이 발생한다")
        void createUser_nullPassword_throws() {
            assertThrows(IllegalArgumentException.class, () ->
                    new User("홍길동", "test@univ.ac.kr", null, dummyLab)
            );
        }
    }
}

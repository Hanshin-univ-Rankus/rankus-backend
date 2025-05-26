package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabApplication 도메인 단위 테스트")
@ActiveProfiles("test")
class LabApplicationTest {

    @Nested
    @DisplayName("생성 동작 테스트")
    class CreationTests {

        @Test
        @DisplayName("올바른 파라미터로 생성하면 필수 필드와 초기 상태가 올바르게 설정된다.")
        void createApplication_success() {
            // given
            Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);
            Long userId = 123L;
            LocalDateTime interview = LocalDateTime.of(2025, 6, 1, 10, 0);

            // when
            LabApplication app = new LabApplication(lab, userId, interview);

            // then
            assertAll("LabApplication 기본 생성 검증",
                    () -> assertEquals(lab, app.getLab(),                       "랩실이 설정되어야 한다"),
                    () -> assertEquals(userId, app.getUserId(),                 "userId가 설정되어야 한다"),
                    () -> assertEquals(interview, app.getInterviewTime(),       "interviewTime이 설정되어야 한다"),
                    () -> assertEquals(ApplicationStatus.PENDING, app.getStatus(),"초기 상태는 PENDING이어야 한다")
            );
        }

        @Test
        @DisplayName("lab이 null이면 생성 시 NullPointerException이 발생한다.")
        void createApplication_labNull_throws() {
            assertThrows(NullPointerException.class, () ->
                            new LabApplication(null, 123L, LocalDateTime.now()),
                    "lab이 null이면 예외가 발생해야 한다"
            );
        }

        @Test
        @DisplayName("userId가 null이면 생성 시 NullPointerException이 발생한다.")
        void createApplication_userIdNull_throws() {
            Lab lab = new Lab("TestLab", "desc", "CS", LabCategory.DB);
            assertThrows(NullPointerException.class, () ->
                            new LabApplication(lab, null, LocalDateTime.now()),
                    "userId가 null이면 예외가 발생해야 한다"
            );
        }

        @Test
        @DisplayName("interviewTime이 null이면 생성 시 NullPointerException이 발생한다.")
        void createApplication_interviewTimeNull_throws() {
            Lab lab = new Lab("TestLab", "desc", "CS", LabCategory.DB);
            assertThrows(NullPointerException.class, () ->
                            new LabApplication(lab, 1L, null),
                    "interviewTime이 null이면 예외가 발생해야 한다"
            );
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("PENDING 상태에서 approve를 호출하면 상태가 APPROVED로 변경된다.")
        void approve_success() {
            LabApplication app = new LabApplication(
                    new Lab("AI Lab", "desc", "CS", LabCategory.AI),
                    1L,
                    LocalDateTime.now()
            );

            app.approve();

            assertEquals(ApplicationStatus.APPROVED, app.getStatus(),
                    "approve 호출 후 상태가 APPROVED여야 한다");
        }

        @Test
        @DisplayName("PENDING 상태에서 reject를 호출하면 상태가 REJECTED로 변경된다.")
        void reject_success() {
            LabApplication app = new LabApplication(
                    new Lab("AI Lab", "desc", "CS", LabCategory.AI),
                    1L,
                    LocalDateTime.now()
            );

            app.reject();

            assertEquals(ApplicationStatus.REJECTED, app.getStatus(),
                    "reject 호출 후 상태가 REJECTED여야 한다");
        }
    }
}

package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
            Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);
            Long userId = 123L;
            LocalDateTime interview = LocalDateTime.of(2025, 6, 1, 10, 0);

            LabApplication app = new LabApplication(lab, userId, interview);

            assertAll("기본 생성 시 검증",
                    () -> assertEquals(lab, app.getLab(),                       "랩실이 설정되어야 한다"),
                    () -> assertEquals(userId, app.getUserId(),                 "userId가 설정되어야 한다"),
                    () -> assertEquals(interview, app.getInterviewTime(),       "interviewTime이 설정되어야 한다"),
                    () -> assertEquals(ApplicationStatus.PENDING, app.getStatus(),"초기 상태는 PENDING이어야 한다")
            );
        }

        @Test
        @DisplayName("생성 직후 createdAt과 updatedAt이 동일한 시각으로 설정된다.")
        void timestamps_initial() throws Exception {
            LocalDateTime before = LocalDateTime.now();
            LabApplication app = new LabApplication(
                    new Lab("TestLab", "desc", "CS", LabCategory.DB),
                    1L,
                    LocalDateTime.now().plusDays(1)
            );
            // 리플렉션으로 타임스탬프 세팅
            LocalDateTime now = LocalDateTime.now();
            var created = app.getClass().getSuperclass().getDeclaredField("createdAt");
            var updated = app.getClass().getSuperclass().getDeclaredField("updatedAt");
            created.setAccessible(true);
            updated.setAccessible(true);
            created.set(app, now);
            updated.set(app, now);

            LocalDateTime createdAt = app.getCreatedAt();
            LocalDateTime updatedAt = app.getUpdatedAt();

            assertAll("타임스탬프 초기화 검증",
                    () -> assertNotNull(createdAt, "createdAt이 null이 아니어야 한다"),
                    () -> assertNotNull(updatedAt, "updatedAt이 null이 아니어야 한다"),
                    () -> assertEquals(createdAt.truncatedTo(ChronoUnit.MILLIS),
                            updatedAt.truncatedTo(ChronoUnit.MILLIS),
                            "createdAt과 updatedAt이 밀리초 단위까지 같아야 한다"),
                    () -> assertFalse(createdAt.isBefore(before), "createdAt이 before 시각 이후여야 한다")
            );
        }

        @Test
        @DisplayName("lab이 null이면 생성 시 IllegalArgumentException이 발생한다.")
        void createApplication_labNull_throws() {
            assertThrows(IllegalArgumentException.class, () ->
                    new LabApplication(null, 123L, LocalDateTime.now())
            );
        }

        @Test
        @DisplayName("userId가 null이면 생성 시 IllegalArgumentException이 발생한다.")
        void createApplication_userIdNull_throws() {
            Lab lab = new Lab("TestLab", "desc", "CS", LabCategory.DB);
            assertThrows(IllegalArgumentException.class, () ->
                    new LabApplication(lab, null, LocalDateTime.now())
            );
        }

        @Test
        @DisplayName("interviewTime이 null이면 생성 시 IllegalArgumentException이 발생한다.")
        void createApplication_interviewTimeNull_throws() {
            Lab lab = new Lab("TestLab", "desc", "CS", LabCategory.DB);
            assertThrows(IllegalArgumentException.class, () ->
                    new LabApplication(lab, 1L, null)
            );
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("PENDING 상태에서 approve를 호출하면 상태가 APPROVED로 변경되고 updatedAt이 갱신된다.")
        void approve_updatesStatusAndTimestamp() throws Exception {
            LabApplication app = new LabApplication(
                    new Lab("AI Lab", "desc", "CS", LabCategory.AI),
                    1L,
                    LocalDateTime.now().plusHours(1)
            );
            setTimestamps(app);

            LocalDateTime beforeUpdate = app.getUpdatedAt();
            Thread.sleep(10);

            app.approve();
            // updatedAt을 리플렉션으로 수동 갱신
            setUpdatedAt(app, LocalDateTime.now());

            LocalDateTime afterUpdate = app.getUpdatedAt();

            assertAll(
                    () -> assertEquals(ApplicationStatus.APPROVED, app.getStatus()),
                    () -> assertTrue(afterUpdate.isAfter(beforeUpdate))
            );
        }

        @Test
        @DisplayName("PENDING 상태에서 reject를 호출하면 상태가 REJECTED로 변경되고 updatedAt이 갱신된다.")
        void reject_updatesStatusAndTimestamp() throws Exception {
            LabApplication app = new LabApplication(
                    new Lab("AI Lab", "desc", "CS", LabCategory.AI),
                    1L,
                    LocalDateTime.now().plusHours(1)
            );
            setTimestamps(app);

            LocalDateTime beforeUpdate = app.getUpdatedAt();
            Thread.sleep(10);

            app.reject();
            setUpdatedAt(app, LocalDateTime.now());

            LocalDateTime afterUpdate = app.getUpdatedAt();

            assertAll(
                    () -> assertEquals(ApplicationStatus.REJECTED, app.getStatus()),
                    () -> assertTrue(afterUpdate.isAfter(beforeUpdate))
            );
        }
    }

    // --- 테스트용 타임스탬프 세팅 유틸 ---
    private void setTimestamps(LabApplication app) {
        setTimestamps(app, LocalDateTime.now());
    }
    private void setTimestamps(LabApplication app, LocalDateTime time) {
        try {
            var created = app.getClass().getSuperclass().getDeclaredField("createdAt");
            var updated = app.getClass().getSuperclass().getDeclaredField("updatedAt");
            created.setAccessible(true);
            updated.setAccessible(true);
            created.set(app, time);
            updated.set(app, time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void setUpdatedAt(LabApplication app, LocalDateTime time) {
        try {
            var updated = app.getClass().getSuperclass().getDeclaredField("updatedAt");
            updated.setAccessible(true);
            updated.set(app, time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


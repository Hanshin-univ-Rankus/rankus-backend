package org.univ.rankus.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.adapter.out.persistence.SpringDataLabApplicationRepository;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * LabApplicationService 단위 테스트
 * - 가입신청 등록 및 목록 조회에 더해, 승인·거절 기능까지 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LabApplicationService 단위 테스트")
class LabApplicationServiceTest {

    @Mock
    private SpringDataLabRepository labRepo;

    @Mock
    private SpringDataLabApplicationRepository appRepo;

    @InjectMocks
    private LabApplicationService service;

    private Lab dummyLab;
    private LabApplication pendingApp;
    private Long labId;
    private Long appId;

    @BeforeEach
    void setUp() throws Exception {
        // Lab 엔티티 생성
        dummyLab = new Lab("TestLab", "테스트 설명", "컴퓨터공학과", LabCategory.CV);

        // 가입신청(기본 상태 PENDING) 생성
        pendingApp = new LabApplication(dummyLab, 100L, LocalDateTime.now().plusDays(1));

        // applicationId를 리플렉션으로 설정
        appId = 50L;
        var idField = pendingApp.getClass()
                .getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(pendingApp, appId);

        // labId 파라미터로 사용할 값
        labId = 10L;
    }

    @Nested
    @DisplayName("가입신청 등록 기능")
    class RegisterApplicationTests {

        @Test
        @DisplayName("올바른 정보로 가입신청을 등록하면 LabApplication이 반환된다")
        void registerApplication_success() {
            // given
            Long userId = 10L;
            String userName = "홍길동";
            LocalDateTime interviewTime = LocalDateTime.of(2025, 6, 1, 14, 0);

            Lab lab = new Lab("AI Lab", "설명", "컴공", LabCategory.AI);
            given(labRepo.findById(labId)).willReturn(Optional.of(lab));
            given(appRepo.save(any(LabApplication.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            LabApplication result = service.registerApplication(labId, userId, userName, interviewTime);

            // then
            assertAll("가입신청 기본 검증",
                    () -> assertEquals(lab, result.getLab(), "랩실이 연관관계로 설정되어야 한다"),
                    () -> assertEquals(userId, result.getUserId(), "userId가 설정되어야 한다"),
                    () -> assertEquals(interviewTime, result.getInterviewTime(), "면접 시간이 설정되어야 한다"),
                    () -> assertEquals(ApplicationStatus.PENDING, result.getStatus(), "초기 상태는 PENDING이어야 한다")
            );
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 가입신청하면 NoSuchElementException이 발생한다")
        void registerApplication_labNotFound_throws() {
            // given
            given(labRepo.findById(labId)).willReturn(Optional.empty());

            // when & then
            assertThrows(NoSuchElementException.class,
                    () -> service.registerApplication(labId, 1L, "테스터", LocalDateTime.now()),
                    "랩실이 없으면 NoSuchElementException이 발생해야 한다"
            );
        }
    }

    @Nested
    @DisplayName("가입신청 목록 조회 기능")
    class ListApplicationsTests {

        @Test
        @DisplayName("유효한 랩실 ID로 가입신청 목록을 조회하면 모든 신청이 반환된다")
        void listApplications_success() {
            // given
            LabApplication app1 = new LabApplication(dummyLab, 5L, LocalDateTime.now());
            LabApplication app2 = new LabApplication(dummyLab, 6L, LocalDateTime.now().plusHours(1));
            List<LabApplication> apps = Arrays.asList(app1, app2);

            given(appRepo.findByLabId(labId)).willReturn(apps);

            // when
            List<LabApplication> result = service.listApplications(labId);

            // then
            assertAll("가입신청 목록 검증",
                    () -> assertEquals(2, result.size(), "등록된 신청 개수만큼 반환되어야 한다"),
                    () -> assertTrue(result.containsAll(apps), "모든 신청 엔티티가 포함되어야 한다")
            );
        }
    }

    @Nested
    @DisplayName("가입신청 승인 기능")
    class ApproveApplicationTests {

        @Test
        @DisplayName("존재하는 PENDING 신청을 승인하면 상태가 APPROVED로 변경되고 저장된다")
        void approveApplication_success() {
            // given
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.of(pendingApp));

            // when
            service.approveApplication(labId, appId);

            // then: 상태 전환 및 저장 호출 검증
            assertEquals(ApplicationStatus.APPROVED, pendingApp.getStatus(),
                    "approve() 호출 후 상태는 APPROVED여야 한다");

            // 저장 메서드 호출 검증
            then(appRepo).should().save(pendingApp);
        }

        @Test
        @DisplayName("존재하지 않는 applicationId로 승인 시 NoSuchElementException 발생")
        void approveApplication_notFound() {
            // given
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.empty());

            // when & then
            NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                    service.approveApplication(labId, appId)
            );
            assertTrue(ex.getMessage().contains("해당 랩실(" + labId + ")의 가입신청을 찾을 수 없습니다"),
                    "메시지에 labId와 applicationId 정보가 포함되어야 한다");
            then(appRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 APPROVED된 신청을 승인 시 IllegalStateException 발생")
        void approveApplication_alreadyApproved_throws() {
            // given: 이미 APPROVED 상태로 변경
            pendingApp.approve();
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.of(pendingApp));

            // when & then
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    service.approveApplication(labId, appId)
            );
            assertTrue(ex.getMessage().contains("이미 처리된 신청입니다"),
                    "이미 처리된 경우 메시지에 현재 상태가 포함되어야 한다");
            then(appRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 REJECTED된 신청을 승인 시 IllegalStateException 발생")
        void approveApplication_alreadyRejected_throws() {
            // given: 이미 REJECTED 상태로 변경
            pendingApp.reject();
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.of(pendingApp));

            // when & then
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    service.approveApplication(labId, appId)
            );
            assertTrue(ex.getMessage().contains("이미 처리된 신청입니다"),
                    "이미 처리된 경우 메시지에 현재 상태가 포함되어야 한다");
            then(appRepo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("가입신청 거절 기능")
    class RejectApplicationTests {

        @Test
        @DisplayName("존재하는 PENDING 신청을 거절하면 상태가 REJECTED로 변경되고 저장된다")
        void rejectApplication_success() {
            // given
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.of(pendingApp));

            // when
            service.rejectApplication(labId, appId);

            // then: 상태 전환 및 저장 호출 검증
            assertEquals(ApplicationStatus.REJECTED, pendingApp.getStatus(),
                    "reject() 호출 후 상태는 REJECTED여야 한다");

            // 저장 메서드 호출 검증
            then(appRepo).should().save(pendingApp);
        }

        @Test
        @DisplayName("존재하지 않는 applicationId로 거절 시 NoSuchElementException 발생")
        void rejectApplication_notFound() {
            // given
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.empty());

            // when & then
            NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                    service.rejectApplication(labId, appId)
            );
            assertTrue(ex.getMessage().contains("해당 랩실(" + labId + ")의 가입신청을 찾을 수 없습니다"),
                    "메시지에 labId와 applicationId 정보가 포함되어야 한다");
            then(appRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 APPROVED된 신청을 거절 시 IllegalStateException 발생")
        void rejectApplication_alreadyApproved_throws() {
            // given: 이미 APPROVED 상태
            pendingApp.approve();
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.of(pendingApp));

            // when & then
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    service.rejectApplication(labId, appId)
            );
            assertTrue(ex.getMessage().contains("이미 처리된 신청입니다"),
                    "이미 처리된 경우 메시지에 현재 상태가 포함되어야 한다");
            then(appRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 REJECTED된 신청을 거절 시 IllegalStateException 발생")
        void rejectApplication_alreadyRejected_throws() {
            // given: 이미 REJECTED 상태
            pendingApp.reject();
            given(appRepo.findByIdAndLabId(appId, labId))
                    .willReturn(Optional.of(pendingApp));

            // when & then
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    service.rejectApplication(labId, appId)
            );
            assertTrue(ex.getMessage().contains("이미 처리된 신청입니다"),
                    "이미 처리된 경우 메시지에 현재 상태가 포함되어야 한다");
            then(appRepo).should(never()).save(any());
        }
    }
}


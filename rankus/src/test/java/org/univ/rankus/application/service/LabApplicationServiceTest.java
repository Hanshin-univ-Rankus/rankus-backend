package org.univ.rankus.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("LabApplicationService 단위 테스트")
class LabApplicationServiceTest {

    @Mock
    private SpringDataLabRepository labRepo;

    @Mock
    private SpringDataLabApplicationRepository appRepo;

    @InjectMocks
    private LabApplicationService service;

    @Nested
    @DisplayName("가입신청 등록 기능")
    class RegisterApplicationTests {

        @Test
        @DisplayName("올바른 정보로 가입신청을 등록하면 LabApplication이 반환된다")
        void registerApplication_success() {
            // given
            Long labId = 1L;
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
            Long labId = 99L;
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
            Long labId = 2L;
            Lab lab = new Lab("DB Lab", "설명", "컴공", LabCategory.DB);
            LabApplication app1 = new LabApplication(lab, 5L, LocalDateTime.now());
            LabApplication app2 = new LabApplication(lab, 6L, LocalDateTime.now().plusHours(1));
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
}

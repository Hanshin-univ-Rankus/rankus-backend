package org.univ.rankus.adapter.out.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.univ.rankus.adapter.out.persistence.impl.LabApplicationRepositoryAdapter;
import org.univ.rankus.adapter.out.persistence.impl.LabRepositoryAdapter;
import org.univ.rankus.adapter.out.persistence.impl.UserRepositoryAdapter;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.integration.IntegrationLabApplicationFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationLabFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationUserFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({LabApplicationRepositoryAdapter.class, LabRepositoryAdapter.class, UserRepositoryAdapter.class})
@DisplayName("Spring Data JPA LabApplicationRepository 통합 테스트")
class SpringDataLabApplicationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private LabApplicationRepositoryPort appRepo;

    @Autowired
    private LabRepositoryPort labRepo;

    @Autowired
    private UserRepositoryPort userRepo;

    @Test
    @DisplayName("save: LabApplication 저장 후 ID 자동 생성")
    void save_generatesId() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User user = IntegrationUserFactory.persistValidUser(userRepo);
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        // when
        LabApplication saved = IntegrationLabApplicationFactory.persistValidPendingApplication(appRepo, lab, user, time);
        // then
        assertNotNull(saved.getId(), "저장된 LabApplication은 ID가 자동 생성되어야 한다");
        assertTrue(saved.getId() > 0, "생성된 ID는 양수여야 한다");
    }

    @Test
    @DisplayName("findByLabId: 특정 Lab ID로 조회 시 해당 애플리케이션만 반환")
    void findByLabId_returnsOnlyThatLabApplications() {
        Lab lab1 = IntegrationLabFactory.persistValidLab(labRepo);
        Lab lab2 = IntegrationLabFactory.persistCustomLab(labRepo, "OtherLab", lab1.getCategory(), "desc", "ProfX");
        User user1 = IntegrationUserFactory.persistValidUser(userRepo);
        User user2 = IntegrationUserFactory.persistCustomUser(userRepo, "User2", "u2@example.com", "Password!23");
        LocalDateTime t1 = LocalDateTime.now().plusDays(1);
        LocalDateTime t2 = LocalDateTime.now().plusDays(2);

        IntegrationLabApplicationFactory.persistValidPendingApplication(appRepo, lab1, user1, t1);
        IntegrationLabApplicationFactory.persistValidPendingApplication(appRepo, lab1, user2, t2);
        IntegrationLabApplicationFactory.persistValidPendingApplication(appRepo, lab2, user1, t1);

        // when
        List<LabApplication> apps1 = appRepo.findByLabId(lab1.getId());
        List<LabApplication> apps2 = appRepo.findByLabId(lab2.getId());
        // then
        assertEquals(2, apps1.size(), "lab1에 대해 2개의 애플리케이션을 반환해야 한다");
        assertEquals(1, apps2.size(), "lab2에 대해 1개의 애플리케이션을 반환해야 한다");
    }

    @Test
    @DisplayName("existsByLabIdAndUser: 저장된 LabApplication에 대해 true 반환")
    void existsByLabIdAndUser_existing_returnsTrue() {
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User user = IntegrationUserFactory.persistValidUser(userRepo);
        IntegrationLabApplicationFactory.persistValidPendingApplication(appRepo, lab, user, LocalDateTime.now().plusDays(1));

        boolean exists = appRepo.existsByLabIdAndUser(lab.getId(), user);
        assertTrue(exists, "existing labId & user 조합에 대해 true 반환");
    }

    @Test
    @DisplayName("existsByLabIdAndUser: 비저장 LabApplication에 대해 false 반환")
    void existsByLabIdAndUser_nonExisting_returnsFalse() {
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User user = IntegrationUserFactory.persistValidUser(userRepo);
        boolean exists = appRepo.existsByLabIdAndUser(lab.getId(), user);
        assertFalse(exists, "비저장 labId & user 조합에 대해 false 반환");
    }

    @Test
    @DisplayName("delete: 삭제 후 existsByLabIdAndUserId false 반환")
    void delete_persistsRemoval() {
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User user = IntegrationUserFactory.persistValidUser(userRepo);
        LabApplication saved = IntegrationLabApplicationFactory.persistValidPendingApplication(appRepo, lab, user, LocalDateTime.now().plusDays(1));

        // when
        appRepo.delete(saved);
        // then
        assertFalse(appRepo.existsByLabIdAndUserId(lab.getId(), user.getId()), "삭제 후 existsByLabIdAndUserId false 반환");
    }
}
package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * DomainLabApplicationFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 LabApplication 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainLabApplicationFactory {
    private DomainLabApplicationFactory() {
    }

    public static LabApplication buildValidPendingApplication(Lab lab, User user, LocalDateTime time) {
        return new LabApplication(lab, user, time);
    }

    public static LabApplication buildValidPendingWithId(Long id, Lab lab, User user, LocalDateTime time) {
        LabApplication app = buildValidPendingApplication(lab, user, time);
        ReflectionTestUtils.setField(app, "id", id);
        return app;
    }

    public static LabApplication buildInvalidApp_NullLab(User user, LocalDateTime time) {
        return new LabApplication(null, user, time);
    }

    public static LabApplication buildInvalidApp_NullUser(Lab lab, LocalDateTime time) {
        return new LabApplication(lab, null, time);
    }

    public static LabApplication buildInvalidApp_NullTime(Lab lab, User user) {
        return new LabApplication(lab, user, null);
    }

    public static LabApplication buildInvalidApp_PastTime(Lab lab, User user) {
        return new LabApplication(lab, user, LocalDateTime.now().minusDays(1));
    }

    public static LabApplication buildWithStatus(Lab lab, User user, ApplicationStatus status) {
        LabApplication app = new LabApplication(lab, user, LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(app, "status", status);
        return app;
    }

    public static LabApplication buildApprovedApplication(Lab lab, User user) {
        return buildWithStatus(lab, user, ApplicationStatus.APPROVED);
    }

    public static LabApplication buildRejectedApplication(Lab lab, User user) {
        return buildWithStatus(lab, user, ApplicationStatus.REJECTED);
    }

    public static LabApplication buildDefaultPendingApplication() {
        Lab lab = DomainLabFactory.buildValidLab();
        User user = DomainUserFactory.buildValidUser();
        return buildValidPendingApplication(lab, user, LocalDateTime.now().plusDays(1));
    }
}

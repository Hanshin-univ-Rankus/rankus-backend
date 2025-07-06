package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.interview.InterviewSlot;
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

    public static LabApplication buildValidPendingApplication(Lab lab, User user, InterviewSlot slot) {
        // ID 설정 헬퍼 활용
        ensureEntityIds(lab, user, slot);
        return new LabApplication(lab, user, slot);
    }
    
    private static void ensureEntityIds(Lab lab, User user, InterviewSlot slot) {
        if (lab.getId() == null) {
            ReflectionTestUtils.setField(lab, "id", 1L);
        }
        if (user.getId() == null) {
            ReflectionTestUtils.setField(user, "id", 1L);
        }
        if (slot.getId() == null) {
            ReflectionTestUtils.setField(slot, "id", 1L);
        }
        
        // Interview 연관 관계 ID 설정
        if (slot.getInterview() != null) {
            if (slot.getInterview().getId() == null) {
                ReflectionTestUtils.setField(slot.getInterview(), "id", 1L);
            }
            if (slot.getInterview().getLab() != null) {
                ReflectionTestUtils.setField(slot.getInterview().getLab(), "id", lab.getId());
            }
        }
    }

    public static LabApplication buildValidPendingWithId(Long id, Lab lab, User user, InterviewSlot slot) {
        LabApplication app = buildValidPendingApplication(lab, user, slot);
        ReflectionTestUtils.setField(app, "id", id);
        return app;
    }

    public static LabApplication buildInvalidApp_NullLab(User user, InterviewSlot slot) {
        return new LabApplication(null, user, slot);
    }

    public static LabApplication buildInvalidApp_NullUser(Lab lab, InterviewSlot slot) {
        return new LabApplication(lab, null, slot);
    }

    public static LabApplication buildInvalidApp_NullSlot(Lab lab, User user) {
        return new LabApplication(lab, user, null);
    }


    public static LabApplication buildWithStatus(Lab lab, User user, ApplicationStatus status) {
        InterviewSlot slot = DomainInterviewSlotFactory.buildValidSlot();
        LabApplication app = buildValidPendingApplication(lab, user, slot);
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
        InterviewSlot slot = DomainInterviewSlotFactory.buildValidSlot();
        return buildValidPendingApplication(lab, user, slot);
    }

    // 간소화된 헬퍼 메서드들
    public static LabApplication buildValidPendingApplication(Lab lab, User user, LocalDateTime time) {
        Lab commonLab = (lab != null) ? lab : DomainLabFactory.buildValidLab();
        InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithTimeAndLab(time, commonLab);
        return new LabApplication(commonLab, user, slot);
    }

    public static LabApplication buildInvalidApp_PastTime(Lab lab, User user) {
        Lab commonLab = (lab != null) ? lab : DomainLabFactory.buildValidLab();
        InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithTimeAndLab(LocalDateTime.now().minusDays(1), commonLab);
        return new LabApplication(commonLab, user, slot);
    }
}

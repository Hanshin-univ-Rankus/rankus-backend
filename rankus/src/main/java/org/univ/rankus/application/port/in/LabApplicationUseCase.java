// src/main/java/org/univ/rankus/application/port/in/LabApplicationUseCase.java
package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.lab.LabApplication;

import java.time.LocalDateTime;
import java.util.List;

public interface LabApplicationUseCase {

    /**
     * 주어진 랩실 ID, 사용자 정보로 가입신청을 등록하고 저장된 LabApplication을 반환한다.
     */
    LabApplication registerApplication(
            Long labId,
            Long userId,
            String userName,
            LocalDateTime interviewTime
    );

    /**
     * 주어진 랩실 ID의 모든 가입신청 목록을 반환한다.
     */
    List<LabApplication> listApplications(Long labId);

    void approveApplication(Long labId, Long applicationId);

    void rejectApplication (Long labId, Long applicationId);
}

// src/main/java/org/univ/rankus/application/service/LabApplicationService.java
package org.univ.rankus.application.service;

import org.springframework.stereotype.Service;
import org.univ.rankus.adapter.out.persistence.SpringDataLabApplicationRepository;
import org.univ.rankus.application.port.in.LabApplicationUseCase;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LabApplicationService implements LabApplicationUseCase {

    private final SpringDataLabRepository labRepo;
    private final SpringDataLabApplicationRepository appRepo;

    public LabApplicationService(
            SpringDataLabRepository labRepo,
            SpringDataLabApplicationRepository appRepo
    ) {
        this.labRepo = labRepo;
        this.appRepo = appRepo;
    }

    @Override
    public LabApplication registerApplication(
            Long labId,
            Long userId,
            String userName,
            LocalDateTime interviewTime
    ) {
        // 1) 랩실 조회, 없으면 예외
        Lab lab = labRepo.findById(labId)
                .orElseThrow(() ->
                        new NoSuchElementException("해당 ID의 랩실을 찾을 수 없습니다: " + labId)
                );

        // 2) 자동 교수 배정 (요구사항)
        lab.autoAssignProfessorIfMatches(userName);

        // 3) 가입신청 엔티티 생성 및 저장
        LabApplication application = new LabApplication(lab, userId, interviewTime);
        return appRepo.save(application);
    }

    @Override
    public List<LabApplication> listApplications(Long labId) {
        // 단순 조회
        return appRepo.findByLabId(labId);
    }
}

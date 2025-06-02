// src/main/java/org/univ/rankus/application/service/LabApplicationService.java
package org.univ.rankus.application.service;

import org.springframework.stereotype.Service;
import org.univ.rankus.adapter.out.persistence.SpringDataLabApplicationRepository;
import org.univ.rankus.application.port.in.LabApplicationUseCase;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.ApplicationStatus;
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

    @Override
    public void approveApplication(Long labId, Long applicationId) {
        // 1) 해당 labId 내에서 application 조회
        LabApplication app = appRepo.findByIdAndLabId(applicationId, labId)
                .orElseThrow(() ->
                        new NoSuchElementException("해당 랩실(" + labId + ")의 가입신청을 찾을 수 없습니다: " + applicationId)
                );

        // 2) 이미 승인/거절된 상태인지 확인
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다. 현재 상태: " + app.getStatus());
        }

        // 3) 승인 로직 (도메인 메서드 호출)
        app.approve();  // 내부에서 status와 updatedAt 갱신

        // 4) 저장
        appRepo.save(app);
    }

    @Override
    public void rejectApplication(Long labId, Long applicationId) {
        LabApplication app = appRepo.findByIdAndLabId(applicationId, labId)
                .orElseThrow(() ->
                        new NoSuchElementException("해당 랩실(" + labId + ")의 가입신청을 찾을 수 없습니다: " + applicationId)
                );

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다. 현재 상태: " + app.getStatus());
        }

        app.reject();  // status = REJECTED, updatedAt 갱신
        appRepo.save(app);
    }
}

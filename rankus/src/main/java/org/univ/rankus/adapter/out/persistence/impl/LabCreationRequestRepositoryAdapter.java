package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataLabCreationRequestRepository;
import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * LabCreationRequestRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataLabCreationRequestRepository를 호출하여 실제 DB 저장·조회·삭제 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class LabCreationRequestRepositoryAdapter implements LabCreationRequestRepositoryPort {

    private final SpringDataLabCreationRequestRepository springDataLabCreationRequestRepository;

    @Override
    public LabCreationRequest save(LabCreationRequest request) {
        return springDataLabCreationRequestRepository.save(request);
    }

    @Override
    public Optional<LabCreationRequest> findById(Long id) {
        return springDataLabCreationRequestRepository.findById(id);
    }

    @Override
    public List<LabCreationRequest> findAllByCreatedAtDesc() {
        return springDataLabCreationRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<LabCreationRequest> findByStatusOrderByCreatedAtDesc(LabCreationStatus status) {
        return springDataLabCreationRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<LabCreationRequest> findByRequesterOrderByCreatedAtDesc(User requester) {
        return springDataLabCreationRequestRepository.findByRequesterOrderByCreatedAtDesc(requester);
    }

    @Override
    public boolean existsByRequesterAndRequestedLabNameAndStatus(User requester, String requestedLabName, LabCreationStatus status) {
        return springDataLabCreationRequestRepository.existsByRequesterAndRequestedLabNameAndStatus(requester, requestedLabName, status);
    }

    @Override
    public boolean existsByRequestedLabNameAndStatus(String requestedLabName, LabCreationStatus status) {
        return springDataLabCreationRequestRepository.existsByRequestedLabNameAndStatus(requestedLabName, status);
    }

    @Override
    public void delete(LabCreationRequest request) {
        springDataLabCreationRequestRepository.delete(request);
    }

    @Override
    public void deleteById(Long id) {
        springDataLabCreationRequestRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataLabCreationRequestRepository.existsById(id);
    }
}
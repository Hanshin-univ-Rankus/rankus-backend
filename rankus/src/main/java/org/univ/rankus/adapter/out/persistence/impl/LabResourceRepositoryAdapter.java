package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataLabResourceRepository;
import org.univ.rankus.application.port.out.LabResourceRepositoryPort;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.util.List;
import java.util.Optional;

/**
 * LabResource Repository Adapter 구현체
 */
@Repository
@RequiredArgsConstructor
public class LabResourceRepositoryAdapter implements LabResourceRepositoryPort {

    private final SpringDataLabResourceRepository springDataLabResourceRepository;

    @Override
    public LabResource save(LabResource labResource) {
        return springDataLabResourceRepository.save(labResource);
    }

    @Override
    public Optional<LabResource> findById(Long id) {
        return springDataLabResourceRepository.findById(id);
    }

    @Override
    public Page<LabResource> findByLabId(Long labId, Pageable pageable) {
        return springDataLabResourceRepository.findByLabId(labId, pageable);
    }

    @Override
    public List<LabResource> findByLabId(Long labId) {
        return springDataLabResourceRepository.findByLabId(labId);
    }

    @Override
    public Page<LabResource> findByLabIdAndCategory(Long labId, ResourceCategory category, Pageable pageable) {
        return springDataLabResourceRepository.findByLabIdAndCategory(labId, category, pageable);
    }

    @Override
    public Page<LabResource> findByLabIdAndTitleContaining(Long labId, String title, Pageable pageable) {
        return springDataLabResourceRepository.findByLabIdAndTitleContaining(labId, title, pageable);
    }

    @Override
    public Page<LabResource> findByLabIdAndCategoryAndTitleContaining(Long labId, ResourceCategory category, String title, Pageable pageable) {
        return springDataLabResourceRepository.findByLabIdAndCategoryAndTitleContaining(labId, category, title, pageable);
    }

    @Override
    public Page<LabResource> findByUploaderId(Long uploaderId, Pageable pageable) {
        return springDataLabResourceRepository.findByUploaderId(uploaderId, pageable);
    }

    @Override
    public Page<LabResource> findByLabIdAndIsPublicTrue(Long labId, Pageable pageable) {
        return springDataLabResourceRepository.findByLabIdAndIsPublicTrue(labId, pageable);
    }

    @Override
    public void delete(LabResource labResource) {
        springDataLabResourceRepository.delete(labResource);
    }

    @Override
    public void deleteById(Long id) {
        springDataLabResourceRepository.deleteById(id);
    }

    @Override
    public long countByLabId(Long labId) {
        return springDataLabResourceRepository.countByLabId(labId);
    }

    @Override
    public long countByLabIdAndCategory(Long labId, ResourceCategory category) {
        return springDataLabResourceRepository.countByLabIdAndCategory(labId, category);
    }

    @Override
    public long countByUploaderId(Long uploaderId) {
        return springDataLabResourceRepository.countByUploaderId(uploaderId);
    }

    @Override
    public boolean existsByFileUrl(String fileUrl) {
        return springDataLabResourceRepository.existsByFileUrl(fileUrl);
    }
}
package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;
import org.univ.rankus.application.port.in.query.LabResourceQueryUseCase;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.LabResourceRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.file.FileUploadPort;
import org.univ.rankus.domain.model.file.exception.FileUploadException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceErrorCode;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 랩실 자료 Query Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabResourceQueryService implements LabResourceQueryUseCase {

    private final LabResourceRepositoryPort labResourceRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final FileUploadPort fileUploadPort;

    @Override
    public Page<LabResourceResponseDto> getLabResources(Long labId, ResourceCategory category, String search,
                                                        Pageable pageable, Long currentUserId) {
        log.info("Getting lab resources for lab: {}, category: {}, search: {}, user: {}", labId, category, search, currentUserId);

        // 랩실 조회 및 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canViewLabResources(lab)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 조건에 따른 자료 조회
        Page<LabResource> resourcePage;

        if (category != null && StringUtils.hasText(search)) {
            // 카테고리 + 검색어
            resourcePage = labResourceRepositoryPort.findByLabIdAndCategoryAndTitleContaining(labId, category, search, pageable);
        } else if (category != null) {
            // 카테고리만
            resourcePage = labResourceRepositoryPort.findByLabIdAndCategory(labId, category, pageable);
        } else if (StringUtils.hasText(search)) {
            // 검색어만
            resourcePage = labResourceRepositoryPort.findByLabIdAndTitleContaining(labId, search, pageable);
        } else {
            // 전체 조회
            resourcePage = labResourceRepositoryPort.findByLabId(labId, pageable);
        }

        return resourcePage.map(LabResourceResponseDto::from);
    }

    @Override
    public List<LabResourceResponseDto> getAllLabResources(Long labId, Long currentUserId) {
        log.info("Getting all lab resources for lab: {}, user: {}", labId, currentUserId);

        // 랩실 조회 및 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canViewLabResources(lab)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_ACCESS_DENIED);
        }

        List<LabResource> resources = labResourceRepositoryPort.findByLabId(labId);
        return LabResourceResponseDto.fromList(resources);
    }

    @Override
    public LabResourceResponseDto getLabResource(Long resourceId, Long currentUserId) {
        log.info("Getting lab resource: {}, user: {}", resourceId, currentUserId);

        // 자료 조회 및 검증
        LabResource labResource = labResourceRepositoryPort.findById(resourceId)
                .orElseThrow(() -> new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canViewLabResources(labResource.getLab())) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_ACCESS_DENIED);
        }

        return LabResourceResponseDto.from(labResource);
    }

    @Override
    @Transactional
    public Resource downloadLabResource(Long resourceId, Long currentUserId) {
        log.info("Downloading lab resource: {}, user: {}", resourceId, currentUserId);

        // 자료 조회 및 검증
        LabResource labResource = labResourceRepositoryPort.findById(resourceId)
                .orElseThrow(() -> new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 다운로드 권한 검증
        if (!currentUser.canDownloadLabResource(labResource)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_DOWNLOAD_DENIED);
        }

        // 다운로드 횟수 증가
        labResource.incrementDownloadCount();
        labResourceRepositoryPort.save(labResource);

        // 파일 리소스 반환
        try {
            Resource resource = new UrlResource(labResource.getFileUrl());
            if (resource.exists() && resource.isReadable()) {
                log.info("File download successful: {}", labResource.getFileUrl());
                return resource;
            } else {
                throw new FileUploadException(LabResourceErrorCode.RESOURCE_FILE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            log.error("Invalid file URL: {}", labResource.getFileUrl(), e);
            throw new FileUploadException(LabResourceErrorCode.RESOURCE_FILE_NOT_FOUND);
        }
    }

    @Override
    public Page<LabResourceResponseDto> getResourcesByUploader(Long uploaderId, Pageable pageable) {
        log.info("Getting resources by uploader: {}", uploaderId);

        Page<LabResource> resourcePage = labResourceRepositoryPort.findByUploaderId(uploaderId, pageable);
        return resourcePage.map(LabResourceResponseDto::from);
    }

    @Override
    public List<ResourceCategory> getResourceCategories() {
        return Arrays.asList(ResourceCategory.values());
    }

    @Override
    public LabResourceStatsDto getLabResourceStats(Long labId, Long currentUserId) {
        log.info("Getting lab resource stats for lab: {}, user: {}", labId, currentUserId);

        // 랩실 조회 및 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canViewLabResources(lab)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 통계 정보 수집
        long totalCount = labResourceRepositoryPort.countByLabId(labId);

        List<LabResource> allResources = labResourceRepositoryPort.findByLabId(labId);
        long publicCount = allResources.stream().mapToLong(r -> r.getIsPublic() ? 1 : 0).sum();
        long privateCount = totalCount - publicCount;

        Map<ResourceCategory, Long> countByCategory = Arrays.stream(ResourceCategory.values())
                .collect(Collectors.toMap(
                        category -> category,
                        category -> labResourceRepositoryPort.countByLabIdAndCategory(labId, category)
                ));

        return new LabResourceStatsDto(totalCount, publicCount, privateCount, countByCategory);
    }
}
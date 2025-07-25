package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.adapter.in.web.dto.request.LabResourceCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabResourceUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;
import org.univ.rankus.application.port.in.command.LabResourceCommandUseCase;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.LabResourceRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.file.FileUploadPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceErrorCode;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

/**
 * 랩실 자료 Command Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LabResourceCommandService implements LabResourceCommandUseCase {

    private final LabResourceRepositoryPort labResourceRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final FileUploadPort fileUploadPort;

    @Override
    public LabResourceResponseDto createLabResource(Long labId, LabResourceCreateRequestDto request,
                                                    MultipartFile file, Long uploaderId) {
        log.info("Creating lab resource for lab: {}, uploader: {}", labId, uploaderId);

        // 랩실 조회 및 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 업로더 조회 및 검증
        User uploader = userRepositoryPort.findById(uploaderId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!uploader.canCreateLabResources(lab)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_ACCESS_DENIED);
        }

        // 파일 업로드
        String fileUrl = fileUploadPort.uploadFile(file, uploaderId, "lab-resource");
        log.info("File uploaded successfully: {}", fileUrl);

        // 랩실 자료 생성
        LabResource labResource = new LabResource(
                request.getTitle(),
                request.getDescription(),
                file.getOriginalFilename(),
                fileUrl,
                file.getSize(),
                request.getCategory(),
                request.getIsPublic(),
                lab,
                uploader
        );

        // 저장
        LabResource savedResource = labResourceRepositoryPort.save(labResource);
        log.info("Lab resource created successfully: {}", savedResource.getId());

        return LabResourceResponseDto.from(savedResource);
    }

    @Override
    public LabResourceResponseDto updateLabResource(Long resourceId, LabResourceUpdateRequestDto request,
                                                    Long currentUserId) {
        log.info("Updating lab resource: {}, user: {}", resourceId, currentUserId);

        // 자료 조회 및 검증
        LabResource labResource = labResourceRepositoryPort.findById(resourceId)
                .orElseThrow(() -> new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canManageLabResource(labResource)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_MODIFICATION_DENIED);
        }

        // 자료 정보 업데이트
        labResource.updateTitle(request.getTitle());
        labResource.updateDescription(request.getDescription());
        labResource.updateCategory(request.getCategory());

        if (request.getIsPublic() != null) {
            labResource.setPublic(request.getIsPublic());
        }

        // 저장
        LabResource updatedResource = labResourceRepositoryPort.save(labResource);
        log.info("Lab resource updated successfully: {}", updatedResource.getId());

        return LabResourceResponseDto.from(updatedResource);
    }

    @Override
    public void deleteLabResource(Long resourceId, Long currentUserId) {
        log.info("Deleting lab resource: {}, user: {}", resourceId, currentUserId);

        // 자료 조회 및 검증
        LabResource labResource = labResourceRepositoryPort.findById(resourceId)
                .orElseThrow(() -> new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canManageLabResource(labResource)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_DELETION_DENIED);
        }

        // 파일 삭제
        try {
            fileUploadPort.deleteFile(labResource.getFileUrl(), currentUserId);
            log.info("File deleted successfully: {}", labResource.getFileUrl());
        } catch (Exception e) {
            log.warn("Failed to delete file: {}, continuing with resource deletion", labResource.getFileUrl(), e);
        }

        // 자료 삭제
        labResourceRepositoryPort.delete(labResource);
        log.info("Lab resource deleted successfully: {}", resourceId);
    }

    @Override
    public LabResourceResponseDto toggleResourcePublic(Long resourceId, Long currentUserId) {
        log.info("Toggling resource public status: {}, user: {}", resourceId, currentUserId);

        // 자료 조회 및 검증
        LabResource labResource = labResourceRepositoryPort.findById(resourceId)
                .orElseThrow(() -> new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND));

        // 현재 사용자 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 권한 검증
        if (!currentUser.canManageLabResource(labResource)) {
            throw new LabResourcePermissionException(LabResourceErrorCode.RESOURCE_MODIFICATION_DENIED);
        }

        // 공개 여부 토글
        labResource.togglePublic();

        // 저장
        LabResource updatedResource = labResourceRepositoryPort.save(labResource);
        log.info("Resource public status toggled: {}, isPublic: {}", resourceId, updatedResource.getIsPublic());

        return LabResourceResponseDto.from(updatedResource);
    }
}
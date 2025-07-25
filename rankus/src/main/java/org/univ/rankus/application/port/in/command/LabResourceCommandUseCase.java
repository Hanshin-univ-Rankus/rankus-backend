package org.univ.rankus.application.port.in.command;

import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.adapter.in.web.dto.request.LabResourceCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabResourceUpdateRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;

/**
 * 랩실 자료 Command UseCase
 */
public interface LabResourceCommandUseCase {

    /**
     * 랩실 자료를 생성합니다.
     *
     * @param labId      랩실 ID
     * @param request    자료 생성 요청 데이터
     * @param file       업로드할 파일
     * @param uploaderId 업로더 ID
     * @return 생성된 자료 정보
     * @throws org.univ.rankus.domain.model.lab.exception.LabNotFoundException                    랩실을 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.user.exception.UserNotFoundException                  사용자를 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceValidationException 자료 데이터가 유효하지 않은 경우
     * @throws org.univ.rankus.domain.model.file.exception.FileUploadException                    파일 업로드에 실패한 경우
     */
    LabResourceResponseDto createLabResource(Long labId, LabResourceCreateRequestDto request, MultipartFile file, Long uploaderId);

    /**
     * 랩실 자료 정보를 수정합니다.
     *
     * @param resourceId    자료 ID
     * @param request       자료 수정 요청 데이터
     * @param currentUserId 현재 사용자 ID
     * @return 수정된 자료 정보
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException   자료를 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceValidationException 수정 데이터가 유효하지 않은 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 수정 권한이 없는 경우
     */
    LabResourceResponseDto updateLabResource(Long resourceId, LabResourceUpdateRequestDto request, Long currentUserId);

    /**
     * 랩실 자료를 삭제합니다.
     *
     * @param resourceId    삭제할 자료 ID
     * @param currentUserId 현재 사용자 ID
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException   자료를 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 삭제 권한이 없는 경우
     */
    void deleteLabResource(Long resourceId, Long currentUserId);

    /**
     * 자료의 공개 여부를 토글합니다.
     *
     * @param resourceId    자료 ID
     * @param currentUserId 현재 사용자 ID
     * @return 수정된 자료 정보
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourceNotFoundException   자료를 찾을 수 없는 경우
     * @throws org.univ.rankus.domain.model.lab.resource.exception.LabResourcePermissionException 수정 권한이 없는 경우
     */
    LabResourceResponseDto toggleResourcePublic(Long resourceId, Long currentUserId);
}
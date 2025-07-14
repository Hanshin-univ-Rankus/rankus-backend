package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.FileUploadResponseDto;
import org.univ.rankus.application.port.in.command.FileUploadUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 업로드 컨트롤러
 * <p>
 * 파일 업로드/다운로드/삭제 관련 REST API를 제공합니다.
 * 기존 ScoreSubmissionController와 동일한 패턴을 따릅니다.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "FileUpload", description = "파일 업로드 API")
public class FileUploadController {

    private final FileUploadUseCase fileUploadUseCase;

    /**
     * 증빙서류 파일 업로드
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "증빙서류 파일 업로드", description = "점수 신청용 증빙서류 파일을 업로드합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "파일 업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 파일 형식 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "파일 업로드 실패")
    })
    @PostMapping(value = "/proof-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadProofFile(
            @Parameter(description = "업로드할 증빙서류 파일 (PDF, JPG, PNG만 허용, 최대 10MB)")
            @RequestParam("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("증빙서류 파일 업로드 요청 - 사용자: {}, 파일명: {}, 크기: {} bytes",
                userDetails.getUserId(), file.getOriginalFilename(), file.getSize());

        try {
            String fileUrl = fileUploadUseCase.uploadProofFile(file, userDetails.getUserId());

            FileUploadResponseDto responseDto = FileUploadResponseDto.success(
                    fileUrl, file.getOriginalFilename(), null,
                    file.getSize(), file.getContentType(), "proof-file", userDetails.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(responseDto, "증빙서류 파일이 성공적으로 업로드되었습니다."));

        } catch (Exception e) {
            log.error("증빙서류 파일 업로드 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userDetails.getUserId(), file.getOriginalFilename(), e.getMessage());

            FileUploadResponseDto responseDto = FileUploadResponseDto.failure(
                    file.getOriginalFilename(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(422, "파일 업로드 실패: " + e.getMessage(), responseDto));
        }
    }

    /**
     * 프로필 이미지 파일 업로드
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "프로필 이미지 파일 업로드", description = "사용자 프로필 이미지 파일을 업로드합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "파일 업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 파일 형식 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "파일 업로드 실패")
    })
    @PostMapping(value = "/profile-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadProfileImage(
            @Parameter(description = "업로드할 프로필 이미지 파일 (JPG, PNG만 허용, 최대 10MB)")
            @RequestParam("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("프로필 이미지 파일 업로드 요청 - 사용자: {}, 파일명: {}, 크기: {} bytes",
                userDetails.getUserId(), file.getOriginalFilename(), file.getSize());

        try {
            String fileUrl = fileUploadUseCase.uploadProfileImage(file, userDetails.getUserId());

            FileUploadResponseDto responseDto = FileUploadResponseDto.success(
                    fileUrl, file.getOriginalFilename(), null,
                    file.getSize(), file.getContentType(), "profile-image", userDetails.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(responseDto, "프로필 이미지 파일이 성공적으로 업로드되었습니다."));

        } catch (Exception e) {
            log.error("프로필 이미지 파일 업로드 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userDetails.getUserId(), file.getOriginalFilename(), e.getMessage());

            FileUploadResponseDto responseDto = FileUploadResponseDto.failure(
                    file.getOriginalFilename(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(422, "파일 업로드 실패: " + e.getMessage(), responseDto));
        }
    }

    /**
     * 파일 다운로드
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "파일 다운로드", description = "업로드된 파일을 다운로드합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일이 존재하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "파일 접근 권한 없음")
    })
    @GetMapping("/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "다운로드할 파일명")
            @PathVariable String fileName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("파일 다운로드 요청 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);

        try {
            // 파일 경로 구성 (proof-file 또는 profile-image 디렉토리 확인)
            Path filePath = getFilePath(fileName);

            if (!Files.exists(filePath)) {
                log.warn("파일이 존재하지 않음 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);
                return ResponseEntity.notFound().build();
            }

            // 파일 권한 확인 (파일명에 userId가 포함되어 있는지 확인)
            if (!isFileOwnedByUser(fileName, userDetails.getUserId())) {
                log.warn("파일 접근 권한 없음 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일 읽기 불가 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);
                return ResponseEntity.notFound().build();
            }

            // Content-Type 설정
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("파일 다운로드 성공 - 사용자: {}, 파일명: {}, 크기: {} bytes",
                    userDetails.getUserId(), fileName, Files.size(filePath));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("잘못된 파일 URL - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("파일 다운로드 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userDetails.getUserId(), fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 삭제
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "파일 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일이 존재하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "파일 삭제 권한 없음")
    })
    @DeleteMapping("/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> deleteFile(
            @Parameter(description = "삭제할 파일명")
            @PathVariable String fileName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("파일 삭제 요청 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);

        try {
            String fileUrl = String.format("http://localhost:8080/api/files/%s", fileName);
            boolean deleted = fileUploadUseCase.deleteFile(fileUrl, userDetails.getUserId());

            if (deleted) {
                FileUploadResponseDto responseDto = FileUploadResponseDto.deleteSuccess(
                        fileUrl, userDetails.getUserId());

                return ResponseEntity.ok()
                        .body(ApiResponse.success(responseDto, "파일이 성공적으로 삭제되었습니다."));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (SecurityException e) {
            log.warn("파일 삭제 권한 없음 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, "파일 삭제 권한이 없습니다.", null));
        } catch (Exception e) {
            log.error("파일 삭제 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userDetails.getUserId(), fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "파일 삭제 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 파일 정보 조회
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "파일 정보 조회", description = "업로드된 파일의 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "파일 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일이 존재하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "파일 접근 권한 없음")
    })
    @GetMapping("/{fileName:.+}/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> getFileInfo(
            @Parameter(description = "조회할 파일명")
            @PathVariable String fileName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("파일 정보 조회 요청 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);

        try {
            String fileUrl = String.format("http://localhost:8080/api/files/%s", fileName);

            // 파일 권한 확인
            if (!isFileOwnedByUser(fileName, userDetails.getUserId())) {
                log.warn("파일 접근 권한 없음 - 사용자: {}, 파일명: {}", userDetails.getUserId(), fileName);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(403, "파일 접근 권한이 없습니다.", null));
            }

            boolean exists = fileUploadUseCase.isFileExists(fileUrl);
            long fileSize = exists ? fileUploadUseCase.getFileSize(fileUrl) : 0;

            FileUploadResponseDto responseDto = FileUploadResponseDto.fileInfo(
                    fileUrl, fileSize, exists);

            return ResponseEntity.ok()
                    .body(ApiResponse.success(responseDto, "파일 정보 조회가 완료되었습니다."));

        } catch (Exception e) {
            log.error("파일 정보 조회 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userDetails.getUserId(), fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "파일 정보 조회 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 파일 경로를 구합니다.
     */
    private Path getFilePath(String fileName) {
        String category = fileName.contains("profile") ? "profile-image" : "proof-file";
        return Paths.get("./uploads", category, fileName);
    }

    /**
     * 파일이 해당 사용자의 소유인지 확인합니다.
     */
    private boolean isFileOwnedByUser(String fileName, Long userId) {
        return fileName.startsWith(userId + "_");
    }
}
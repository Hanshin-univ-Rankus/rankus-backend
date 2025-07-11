package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.domain.model.file.FileUploadPort;
import org.univ.rankus.domain.model.file.FileValidationPolicy;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FileUploadService 테스트 클래스
 * <p>
 * 파일 업로드 서비스의 비즈니스 로직 테스트를 수행합니다.
 * 기존 ScoreSubmissionCommandServiceTest와 동일한 패턴을 따릅니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadService 테스트")
class FileUploadServiceTest {

    @Mock
    private FileUploadPort fileUploadPort;

    @Mock
    private FileValidationPolicy fileValidationPolicy;

    @InjectMocks
    private FileUploadService fileUploadService;

    private MultipartFile testFile;
    private Long testUserId;
    private String testFileUrl;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testFileUrl = "http://localhost:8080/api/files/1_1641024000000_test.pdf";
        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
    }

    @Test
    @DisplayName("증빙서류 파일 업로드 성공")
    void uploadProofFile_Success() {
        // Given
        doNothing().when(fileValidationPolicy).validateFile(any(MultipartFile.class));
        when(fileUploadPort.uploadFile(any(MultipartFile.class), anyLong(), eq("proof-file")))
                .thenReturn(testFileUrl);

        // When
        String result = fileUploadService.uploadProofFile(testFile, testUserId);

        // Then
        assertThat(result).isEqualTo(testFileUrl);
        verify(fileValidationPolicy).validateFile(testFile);
        verify(fileUploadPort).uploadFile(testFile, testUserId, "proof-file");
    }

    @Test
    @DisplayName("증빙서류 파일 업로드 실패 - 유효성 검증 실패")
    void uploadProofFile_ValidationFailed() {
        // Given
        doThrow(new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE))
                .when(fileValidationPolicy).validateFile(any(MultipartFile.class));

        // When & Then
        assertThatThrownBy(() -> fileUploadService.uploadProofFile(testFile, testUserId))
                .isInstanceOf(RankingValidationException.class)
                .hasMessage(RankingErrorCode.FILE_UPLOAD_FAILED.getMessage());

        verify(fileValidationPolicy).validateFile(testFile);
    }

    @Test
    @DisplayName("증빙서류 파일 업로드 실패 - 파일 업로드 실패")
    void uploadProofFile_UploadFailed() {
        // Given
        doNothing().when(fileValidationPolicy).validateFile(any(MultipartFile.class));
        doThrow(new RuntimeException("파일 업로드 실패"))
                .when(fileUploadPort).uploadFile(any(MultipartFile.class), anyLong(), eq("proof-file"));

        // When & Then
        assertThatThrownBy(() -> fileUploadService.uploadProofFile(testFile, testUserId))
                .isInstanceOf(RankingValidationException.class)
                .hasMessage(RankingErrorCode.FILE_UPLOAD_FAILED.getMessage());

        verify(fileValidationPolicy).validateFile(testFile);
        verify(fileUploadPort).uploadFile(testFile, testUserId, "proof-file");
    }

    @Test
    @DisplayName("프로필 이미지 파일 업로드 성공")
    void uploadProfileImage_Success() {
        // Given
        MultipartFile imageFile = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "image content".getBytes()
        );
        String imageUrl = "http://localhost:8080/api/files/1_1641024000000_profile.jpg";

        doNothing().when(fileValidationPolicy).validateFile(any(MultipartFile.class));
        when(fileUploadPort.uploadFile(any(MultipartFile.class), anyLong(), eq("profile-image")))
                .thenReturn(imageUrl);

        // When
        String result = fileUploadService.uploadProfileImage(imageFile, testUserId);

        // Then
        assertThat(result).isEqualTo(imageUrl);
        verify(fileValidationPolicy).validateFile(imageFile);
        verify(fileUploadPort).uploadFile(imageFile, testUserId, "profile-image");
    }

    @Test
    @DisplayName("프로필 이미지 파일 업로드 실패 - 이미지 파일이 아님")
    void uploadProfileImage_NotImageFile() {
        // Given
        MultipartFile nonImageFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        doNothing().when(fileValidationPolicy).validateFile(any(MultipartFile.class));

        // When & Then
        assertThatThrownBy(() -> fileUploadService.uploadProfileImage(nonImageFile, testUserId))
                .isInstanceOf(RankingValidationException.class)
                .hasMessage(RankingErrorCode.FILE_UPLOAD_FAILED.getMessage());

        verify(fileValidationPolicy).validateFile(nonImageFile);
    }

    @Test
    @DisplayName("프로필 이미지 파일 업로드 실패 - Content-Type이 null")
    void uploadProfileImage_NullContentType() {
        // Given
        MultipartFile nullContentTypeFile = new MockMultipartFile(
                "file",
                "image.jpg",
                null,
                "image content".getBytes()
        );

        doNothing().when(fileValidationPolicy).validateFile(any(MultipartFile.class));

        // When & Then
        assertThatThrownBy(() -> fileUploadService.uploadProfileImage(nullContentTypeFile, testUserId))
                .isInstanceOf(RankingValidationException.class)
                .hasMessage(RankingErrorCode.FILE_UPLOAD_FAILED.getMessage());

        verify(fileValidationPolicy).validateFile(nullContentTypeFile);
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenReturn(true);
        when(fileUploadPort.deleteFile(testFileUrl, testUserId)).thenReturn(true);

        // When
        boolean result = fileUploadService.deleteFile(testFileUrl, testUserId);

        // Then
        assertThat(result).isTrue();
        verify(fileUploadPort).fileExists(testFileUrl);
        verify(fileUploadPort).deleteFile(testFileUrl, testUserId);
    }

    @Test
    @DisplayName("파일 삭제 실패 - 파일이 존재하지 않음")
    void deleteFile_FileNotExists() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenReturn(false);

        // When
        boolean result = fileUploadService.deleteFile(testFileUrl, testUserId);

        // Then
        assertThat(result).isFalse();
        verify(fileUploadPort).fileExists(testFileUrl);
    }

    @Test
    @DisplayName("파일 삭제 실패 - 삭제 실패")
    void deleteFile_DeleteFailed() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenReturn(true);
        when(fileUploadPort.deleteFile(testFileUrl, testUserId)).thenReturn(false);

        // When
        boolean result = fileUploadService.deleteFile(testFileUrl, testUserId);

        // Then
        assertThat(result).isFalse();
        verify(fileUploadPort).fileExists(testFileUrl);
        verify(fileUploadPort).deleteFile(testFileUrl, testUserId);
    }

    @Test
    @DisplayName("파일 삭제 실패 - 권한 에러")
    void deleteFile_SecurityException() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenReturn(true);
        when(fileUploadPort.deleteFile(testFileUrl, testUserId))
                .thenThrow(new SecurityException("파일 삭제 권한이 없습니다."));

        // When & Then
        assertThatThrownBy(() -> fileUploadService.deleteFile(testFileUrl, testUserId))
                .isInstanceOf(RankingValidationException.class)
                .hasMessage(RankingErrorCode.FILE_UPLOAD_FAILED.getMessage());

        verify(fileUploadPort).fileExists(testFileUrl);
        verify(fileUploadPort).deleteFile(testFileUrl, testUserId);
    }

    @Test
    @DisplayName("파일 존재 여부 확인 성공")
    void isFileExists_Success() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenReturn(true);

        // When
        boolean result = fileUploadService.isFileExists(testFileUrl);

        // Then
        assertThat(result).isTrue();
        verify(fileUploadPort).fileExists(testFileUrl);
    }

    @Test
    @DisplayName("파일 존재 여부 확인 실패 - 파일이 존재하지 않음")
    void isFileExists_FileNotExists() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenReturn(false);

        // When
        boolean result = fileUploadService.isFileExists(testFileUrl);

        // Then
        assertThat(result).isFalse();
        verify(fileUploadPort).fileExists(testFileUrl);
    }

    @Test
    @DisplayName("파일 존재 여부 확인 실패 - 에러 발생")
    void isFileExists_Exception() {
        // Given
        when(fileUploadPort.fileExists(testFileUrl)).thenThrow(new RuntimeException("파일 시스템 오류"));

        // When
        boolean result = fileUploadService.isFileExists(testFileUrl);

        // Then
        assertThat(result).isFalse();
        verify(fileUploadPort).fileExists(testFileUrl);
    }

    @Test
    @DisplayName("파일 크기 조회 성공")
    void getFileSize_Success() {
        // Given
        long expectedSize = 1024L;
        when(fileUploadPort.getFileSize(testFileUrl)).thenReturn(expectedSize);

        // When
        long result = fileUploadService.getFileSize(testFileUrl);

        // Then
        assertThat(result).isEqualTo(expectedSize);
        verify(fileUploadPort).getFileSize(testFileUrl);
    }

    @Test
    @DisplayName("파일 크기 조회 실패 - 파일이 존재하지 않음")
    void getFileSize_FileNotExists() {
        // Given
        when(fileUploadPort.getFileSize(testFileUrl)).thenReturn(-1L);

        // When
        long result = fileUploadService.getFileSize(testFileUrl);

        // Then
        assertThat(result).isEqualTo(-1L);
        verify(fileUploadPort).getFileSize(testFileUrl);
    }

    @Test
    @DisplayName("파일 크기 조회 실패 - 에러 발생")
    void getFileSize_Exception() {
        // Given
        when(fileUploadPort.getFileSize(testFileUrl)).thenThrow(new RuntimeException("파일 시스템 오류"));

        // When
        long result = fileUploadService.getFileSize(testFileUrl);

        // Then
        assertThat(result).isEqualTo(-1L);
        verify(fileUploadPort).getFileSize(testFileUrl);
    }
}
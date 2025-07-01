package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.exception.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabImageCommandServiceTest {

    @Mock
    private LabRepositoryPort labRepo;

    @Mock
    private LabImageRepositoryPort labImageRepo;

    @InjectMocks
    private LabImageCommandService service;

    /**
     * 공통 헬퍼: 주어진 labId로 조회되는 Lab mock을 준비
     */
    private Lab givenExistingLab(Long labId) {
        Lab mockLab = mock(Lab.class);
        when(labRepo.findById(labId)).thenReturn(Optional.of(mockLab));
        return mockLab;
    }

    /**
     * 공통 헬퍼: 주어진 imageId로 조회되는 LabImage mock을 준비
     */
    private LabImage givenExistingImage(Long imageId) {
        LabImage mockImage = mock(LabImage.class);
        when(labImageRepo.findById(imageId)).thenReturn(Optional.of(mockImage));
        return mockImage;
    }

    @Nested
    @DisplayName("addImage 메서드는")
    class AddImageTests {

        @Test
        @DisplayName("정상 입력 시 LabImage를 저장하고 반환한다")
        void addImageSuccess() {
            // given
            Long labId = 10L;
            String url = "http://example.com/img.png";
            ImageType type = ImageType.REPRESENTATIVE;
            Lab mockLab = givenExistingLab(labId);

            // save 호출 시 전달된 엔티티 그대로 반환
            when(labImageRepo.save(any(LabImage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            LabImage result = service.addImage(labId, url, type);

            // then
            assertThat(result).isNotNull();
            // LabImage 생성자에 lab, url, type이 전달됐는지 확인
            assertThat(result.getLab()).isSameAs(mockLab);
            assertThat(result.getImageUrl()).isEqualTo(url);
            assertThat(result.getType()).isEqualTo(type);

            verify(labRepo).findById(labId);
            verify(labImageRepo).save(any(LabImage.class));
        }

        @Test
        @DisplayName("존재하지 않는 labId 입력 시 LabNotFoundException을 던진다")
        void addImageLabNotFound() {
            // given
            Long labId = 99L;
            when(labRepo.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.addImage(labId, "url", ImageType.ADDITIONAL))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepo).findById(labId);
            verify(labImageRepo, never()).save(any());
        }

        @Test
        @DisplayName("도메인 검증 실패 시 LabImageValidationException을 던진다")
        void addImageDomainValidationFails() {
            // given
            Long labId = 5L;
            givenExistingLab(labId);

            // when & then
            assertThatThrownBy(() -> service.addImage(
                    labId,
                    "invalid-url",                // 잘못된 URL 포맷
                    ImageType.REPRESENTATIVE))
                    .isInstanceOf(LabImageValidationException.class)
                    .satisfies(ex -> {
                        LabImageValidationException e =
                                (LabImageValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabImageErrorCode.IMAGE_URL_INVALID);
                    });

            // save() 호출 이전에 예외가 발생하므로, save()는 절대 호출되지 않아야 한다
            verify(labRepo).findById(labId);
            verify(labImageRepo, never()).save(any());
        }

        @Nested
        @DisplayName("deleteImage 메서드는")
        class DeleteImageTests {

            @Test
            @DisplayName("정상 삭제 시 LabImageRepositoryPort.delete를 호출한다")
            void deleteImageSuccess() {
                // given
                Long imageId = 20L;
                LabImage mockImage = givenExistingImage(imageId);

                // when
                service.deleteImage(imageId, /*userId*/ 123L);

                // then
                verify(labImageRepo).findById(imageId);
                verify(labImageRepo).delete(mockImage);
            }

            @Test
            @DisplayName("존재하지 않는 imageId 입력 시 LabImageNotFoundException을 던진다")
            void deleteImageNotFound() {
                // given
                Long imageId = 99L;
                when(labImageRepo.findById(imageId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> service.deleteImage(imageId, 1L))
                        .isInstanceOf(LabImageNotFoundException.class)
                        .satisfies(ex -> {
                            LabImageNotFoundException e = (LabImageNotFoundException) ex;
                            assertThat(e.getErrorCode())
                                    .isEqualTo(LabImageErrorCode.IMAGE_NOT_FOUND);
                        });

                verify(labImageRepo).findById(imageId);
                verify(labImageRepo, never()).delete(any());
            }

            @Test
            @DisplayName("삭제 실패 시 LabImageValidationException을 던진다")
            void deleteImageFailure() {
                // given
                Long imageId = 30L;
                LabImage mockImage = givenExistingImage(imageId);

                // delete 호출 시 예외 발생
                doThrow(new RuntimeException("DB error"))
                        .when(labImageRepo).delete(mockImage);

                // when & then
                assertThatThrownBy(() -> service.deleteImage(imageId, 1L))
                        .isInstanceOf(LabImageValidationException.class)
                        .satisfies(ex -> {
                            LabImageValidationException e = (LabImageValidationException) ex;
                            assertThat(e.getErrorCode())
                                    .isEqualTo(LabImageErrorCode.IMAGE_DELETE_FAILED);
                        });

                verify(labImageRepo).findById(imageId);
                verify(labImageRepo).delete(mockImage);
            }
        }
    }
}
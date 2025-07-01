package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabImageErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabImageNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabImageQueryServiceTest {

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private LabImageRepositoryPort labImageRepositoryPort;

    @InjectMocks
    private LabImageQueryService service;

    @Nested
    @DisplayName("listImagesByLab 메서드는")
    class ListImagesByLabTests {

        @Test
        @DisplayName("존재하는 labId 입력 시 LabImage 리스트를 반환한다")
        void listImagesSuccess() {
            // given
            Long labId = 10L;
            Lab mockLab = mock(Lab.class);
            when(labRepositoryPort.findById(labId))
                    .thenReturn(Optional.of(mockLab));

            LabImage img1 = mock(LabImage.class);
            LabImage img2 = mock(LabImage.class);
            List<LabImage> images = List.of(img1, img2);
            when(labImageRepositoryPort.findByLabId(labId))
                    .thenReturn(images);

            // when
            List<LabImage> result = service.listImagesByLab(labId);

            // then
            assertThat(result).isSameAs(images);
            verify(labRepositoryPort).findById(labId);
            verify(labImageRepositoryPort).findByLabId(labId);
        }

        @Test
        @DisplayName("존재하지 않는 labId 입력 시 LabNotFoundException을 던진다")
        void listImagesLabNotFound() {
            // given
            Long labId = 20L;
            when(labRepositoryPort.findById(labId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.listImagesByLab(labId))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(labImageRepositoryPort, never()).findByLabId(any());
        }
    }

    @Nested
    @DisplayName("getImageById 메서드는")
    class GetImageByIdTests {

        @Test
        @DisplayName("존재하는 imageId 입력 시 LabImage를 반환한다")
        void getImageByIdSuccess() {
            // given
            Long imageId = 30L;
            LabImage mockImage = mock(LabImage.class);
            when(labImageRepositoryPort.findById(imageId))
                    .thenReturn(Optional.of(mockImage));

            // when
            LabImage result = service.getImageById(imageId);

            // then
            assertThat(result).isSameAs(mockImage);
            verify(labImageRepositoryPort).findById(imageId);
        }

        @Test
        @DisplayName("존재하지 않는 imageId 입력 시 LabImageNotFoundException을 던진다")
        void getImageByIdNotFound() {
            // given
            Long imageId = 40L;
            when(labImageRepositoryPort.findById(imageId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getImageById(imageId))
                    .isInstanceOf(LabImageNotFoundException.class)
                    .satisfies(ex -> {
                        LabImageNotFoundException e = (LabImageNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabImageErrorCode.IMAGE_NOT_FOUND);
                    });

            verify(labImageRepositoryPort).findById(imageId);
        }
    }
}
package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabPromotionQueryServiceTest {

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private LabPromotionQueryService service;

    @Nested
    @DisplayName("listLabs 메서드는")
    class ListLabsTests {

        @Test
        @DisplayName("저장된 랩실이 있으면 findAllByRankingDesc 결과를 그대로 반환한다")
        void listLabsSuccess() {
            // given
            Lab lab1 = mock(Lab.class);
            Lab lab2 = mock(Lab.class);
            List<Lab> expected = List.of(lab1, lab2);
            when(labRepositoryPort.findAllByRankingDesc()).thenReturn(expected);

            // when
            List<Lab> actual = service.listLabs();

            // then
            assertThat(actual).isSameAs(expected);
            verify(labRepositoryPort).findAllByRankingDesc();
        }
    }

    @Nested
    @DisplayName("getLabById 메서드는")
    class GetLabByIdTests {

        @Test
        @DisplayName("존재하는 ID인 경우 Lab을 반환한다")
        void getLabByIdSuccess() {
            // given
            Long labId = 42L;
            Lab mockLab = mock(Lab.class);
            when(labRepositoryPort.findById(labId))
                    .thenReturn(Optional.of(mockLab));

            // when
            Lab actual = service.getLabById(labId);

            // then
            assertThat(actual).isSameAs(mockLab);
            verify(labRepositoryPort).findById(labId);
        }

        @Test
        @DisplayName("존재하지 않는 ID인 경우 LabNotFoundException을 던진다")
        void getLabByIdNotFound() {
            // given
            Long labId = 99L;
            when(labRepositoryPort.findById(labId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getLabById(labId))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
        }
    }
}
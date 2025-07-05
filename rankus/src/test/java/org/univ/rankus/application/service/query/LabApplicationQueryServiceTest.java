package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabApplicationErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabApplicationNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LabApplicationQueryServiceTest {

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private LabApplicationRepositoryPort labApplicationRepositoryPort;

    @InjectMocks
    private LabApplicationQueryService service;

    @Nested
    @DisplayName("listApplicationsByLab 메서드는")
    class ListApplicationsByLabTests {

        @Test
        @DisplayName("존재하는 랩 ID인 경우, 해당 랩 신청 목록을 반환한다")
        void whenLabExists_thenReturnApplications() {
            // given
            Long labId = 42L;
            LabApplication app1 = org.mockito.Mockito.mock(LabApplication.class);
            LabApplication app2 = org.mockito.Mockito.mock(LabApplication.class);
            Lab mockLab = org.mockito.Mockito.mock(Lab.class);
            given(labRepositoryPort.findById(labId)).willReturn(Optional.of(mockLab));
            given(labApplicationRepositoryPort.findByLabId(labId)).willReturn(List.of(app1, app2));

            // when
            List<LabApplication> result = service.listApplicationsByLab(labId);

            // then
            assertThat(result).containsExactly(app1, app2);
            verify(labRepositoryPort).findById(labId);
            verify(labApplicationRepositoryPort).findByLabId(labId);
        }

        @Test
        @DisplayName("존재하지 않는 랩 ID인 경우, LabNotFoundException을 던진다")
        void whenLabNotFound_thenThrowException() {
            // given
            Long labId = 99L;
            given(labRepositoryPort.findById(labId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.listApplicationsByLab(labId))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });
            verify(labRepositoryPort).findById(labId);
        }
    }

    @Nested
    @DisplayName("getApplicationById 메서드는")
    class GetApplicationByIdTests {

        @Test
        @DisplayName("존재하는 신청 ID인 경우, 해당 신청을 반환한다")
        void whenApplicationExists_thenReturnApplication() {
            // given
            Long appId = 7L;
            LabApplication mockApp = org.mockito.Mockito.mock(LabApplication.class);
            given(labApplicationRepositoryPort.findById(appId)).willReturn(Optional.of(mockApp));

            // when
            LabApplication result = service.getApplicationById(appId);

            // then
            assertThat(result).isSameAs(mockApp);
            verify(labApplicationRepositoryPort).findById(appId);
        }

        @Test
        @DisplayName("존재하지 않는 신청 ID인 경우, LabApplicationNotFoundException을 던진다")
        void whenApplicationNotFound_thenThrowException() {
            // given
            Long appId = 8L;
            given(labApplicationRepositoryPort.findById(appId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getApplicationById(appId))
                    .isInstanceOf(LabApplicationNotFoundException.class)
                    .satisfies(ex -> {
                        LabApplicationNotFoundException e = (LabApplicationNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabApplicationErrorCode.APPLICATION_NOT_FOUND);
                    });
            verify(labApplicationRepositoryPort).findById(appId);
        }
    }
}
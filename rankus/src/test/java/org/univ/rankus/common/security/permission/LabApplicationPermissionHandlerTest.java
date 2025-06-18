package org.univ.rankus.common.security.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LabApplicationPermissionHandlerTest {

    @Mock
    private LabApplicationQueryUseCase queryUseCase;

    @Mock
    private UserQueryUseCase userQueryUseCase;

    @InjectMocks
    private LabApplicationPermissionHandler handler;

    private CustomUserDetails principal;
    private static final Long USER_ID = 10L;
    private static final Long APP_ID = 100L;

    @BeforeEach
    void setUp() {
        principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(USER_ID);
    }

    @Nested
    @DisplayName("cancel 권한 검증")
    class CancelPermission {

        @Test
        @DisplayName("소유자일 경우 허용")
        void ownerCanCancel() {
            LabApplication mockApp = mock(LabApplication.class);
            given(queryUseCase.getApplicationById(APP_ID)).willReturn(mockApp);
            given(mockApp.isOwnedBy(USER_ID)).willReturn(true);

            boolean allowed = handler.hasPermission(principal, APP_ID, "cancel");
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("비소유자일 경우 거부")
        void nonOwnerCannotCancel() {
            LabApplication mockApp = mock(LabApplication.class);
            given(queryUseCase.getApplicationById(APP_ID)).willReturn(mockApp);
            given(mockApp.isOwnedBy(USER_ID)).willReturn(false);

            boolean allowed = handler.hasPermission(principal, APP_ID, "cancel");
            assertThat(allowed).isFalse();
        }
    }

    @Nested
    @DisplayName("approve/reject/view 권한 검증")
    class ApproveRejectViewPermission {

        private LabApplication mockApp;
        private Lab mockLab;
        private User mockUser;

        @BeforeEach
        void init() {
            mockApp = mock(LabApplication.class);
            mockLab = mock(Lab.class);
            mockUser = mock(User.class);

            // 공통 stub
            given(queryUseCase.getApplicationById(APP_ID)).willReturn(mockApp);
            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(mockApp.getLab()).willReturn(mockLab);
        }

        @Test
        @DisplayName("랩장 권한 보유 시 approve 가능")
        void leaderCanApprove() {
            given(mockUser.isLabLeaderOrLabManagerInLab(mockLab)).willReturn(true);
            boolean allowed = handler.hasPermission(principal, APP_ID, "approve");
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("랩장 권한 없으면 approve 불가")
        void nonLeaderCannotApprove() {
            given(mockUser.isLabLeaderOrLabManagerInLab(mockLab)).willReturn(false);
            boolean allowed = handler.hasPermission(principal, APP_ID, "approve");
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("reject도 같은 로직 적용")
        void leaderCanReject() {
            given(mockUser.isLabLeaderOrLabManagerInLab(mockLab)).willReturn(true);
            boolean allowed = handler.hasPermission(principal, APP_ID, "reject");
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("view도 같은 로직 적용")
        void leaderCanView() {
            given(mockUser.isLabLeaderOrLabManagerInLab(mockLab)).willReturn(true);
            boolean allowed = handler.hasPermission(principal, APP_ID, "view");
            assertThat(allowed).isTrue();
        }
    }

    @Nested
    @DisplayName("정의되지 않은 권한 검증")
    class UnknownPermission {

        @Test
        @DisplayName("정의되지 않은 permission 입력 시 거부")
        void unknownPermissionReturnsFalse() {
            boolean allowed = handler.hasPermission(principal, APP_ID, "unknown");
            assertThat(allowed).isFalse();
        }
    }
}

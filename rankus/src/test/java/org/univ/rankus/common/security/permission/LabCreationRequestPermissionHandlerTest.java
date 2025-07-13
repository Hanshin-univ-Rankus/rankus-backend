package org.univ.rankus.common.security.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.application.port.in.query.LabCreationRequestQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabCreationRequestFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LabCreationRequestPermissionHandler 단위 테스트")
class LabCreationRequestPermissionHandlerTest {

    @Mock
    private LabCreationRequestQueryUseCase queryUseCase;

    @Mock
    private UserQueryUseCase userQueryUseCase;

    private LabCreationRequestPermissionHandler permissionHandler;

    @BeforeEach
    void setUp() {
        permissionHandler = new LabCreationRequestPermissionHandler(queryUseCase, userQueryUseCase);
    }

    @Test
    @DisplayName("targetType이 LabCreationRequest를 반환한다")
    void targetType_returnsLabCreationRequest() {
        // when
        String targetType = permissionHandler.targetType();

        // then
        assertThat(targetType).isEqualTo("LabCreationRequest");
    }

    @Test
    @DisplayName("DELETE 권한: 신청자 본인만 가능하다")
    void DELETE_permission_allowsOwnerOnly() {
        // given
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(requesterId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(requesterId)).willReturn(requester);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "DELETE");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("DELETE 권한: 다른 사용자는 불가능하다")
    void DELETE_permission_deniesOtherUsers() {
        // given
        Long requesterId = 1L;
        Long otherUserId = 2L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(otherUserId);
        User otherUser = DomainUserFactory.buildValidUserWithId(otherUserId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(otherUserId)).willReturn(otherUser);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "DELETE");

        // then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("DELETE 권한: 관리자는 모든 요청을 삭제할 수 있다")
    void DELETE_permission_allowsAdmin() {
        // given
        Long adminId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(adminId);
        User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", adminId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(adminId)).willReturn(admin);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "DELETE");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("VIEW 권한: 신청자 본인은 가능하다")
    void VIEW_permission_allowsOwner() {
        // given
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(requesterId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(requesterId)).willReturn(requester);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "VIEW");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("VIEW 권한: 관리자는 가능하다")
    void VIEW_permission_allowsAdmin() {
        // given
        Long adminId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(adminId);
        User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", adminId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(adminId)).willReturn(admin);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "VIEW");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("VIEW 권한: 교수는 가능하다")
    void VIEW_permission_allowsProfessor() {
        // given
        Long professorId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(professorId);
        User professor = DomainUserFactory.buildValidUserWithRole(Role.PROFESSOR);
        ReflectionTestUtils.setField(professor, "id", professorId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(professorId)).willReturn(professor);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "VIEW");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("VIEW 권한: 일반 사용자는 불가능하다")
    void VIEW_permission_deniesOtherUsers() {
        // given
        Long otherUserId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(otherUserId);
        User otherUser = DomainUserFactory.buildValidUserWithId(otherUserId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
        ReflectionTestUtils.setField(request, "id", requestId);

        given(userQueryUseCase.getUserById(otherUserId)).willReturn(otherUser);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "VIEW");

        // then
        assertThat(hasPermission).isFalse();
    }

    private CustomUserDetails createCustomUserDetails(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        return principal;
    }
}
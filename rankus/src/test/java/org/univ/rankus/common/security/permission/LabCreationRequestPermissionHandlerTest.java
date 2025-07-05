package org.univ.rankus.common.security.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

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
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

        given(userQueryUseCase.getUserById(otherUserId)).willReturn(otherUser);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "VIEW");

        // then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("APPROVE 권한: 관리자는 가능하다")
    void APPROVE_permission_allowsAdmin() {
        // given
        Long adminId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(adminId);
        User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", adminId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

        given(userQueryUseCase.getUserById(adminId)).willReturn(admin);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "APPROVE");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("APPROVE 권한: 교수는 가능하다")
    void APPROVE_permission_allowsProfessor() {
        // given
        Long professorId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(professorId);
        User professor = DomainUserFactory.buildValidUserWithRole(Role.PROFESSOR);
        ReflectionTestUtils.setField(professor, "id", professorId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

        given(userQueryUseCase.getUserById(professorId)).willReturn(professor);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "APPROVE");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("APPROVE 권한: 일반 사용자는 불가능하다")
    void APPROVE_permission_deniesOtherUsers() {
        // given
        Long studentId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(studentId);
        User student = DomainUserFactory.buildValidUserWithId(studentId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

        given(userQueryUseCase.getUserById(studentId)).willReturn(student);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "APPROVE");

        // then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("REJECT 권한: 관리자는 가능하다")
    void REJECT_permission_allowsAdmin() {
        // given
        Long adminId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(adminId);
        User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", adminId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

        given(userQueryUseCase.getUserById(adminId)).willReturn(admin);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "REJECT");

        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("REJECT 권한: 일반 사용자는 불가능하다")
    void REJECT_permission_deniesOtherUsers() {
        // given
        Long studentId = 2L;
        Long requesterId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(studentId);
        User student = DomainUserFactory.buildValidUserWithId(studentId);
        User requester = DomainUserFactory.buildValidUserWithId(requesterId);
        LabCreationRequest request = createRequestWithRequester(requestId, requester);

        given(userQueryUseCase.getUserById(studentId)).willReturn(student);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "REJECT");

        // then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("알 수 없는 권한은 거부한다")
    void unknownPermission_isDenied() {
        // given
        Long userId = 1L;
        Long requestId = 100L;

        CustomUserDetails principal = createCustomUserDetails(userId);
        User user = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
        ReflectionTestUtils.setField(user, "id", userId);
        LabCreationRequest request = createRequestWithRequester(requestId, user);

        given(userQueryUseCase.getUserById(userId)).willReturn(user);
        given(queryUseCase.getLabCreationRequestById(requestId)).willReturn(request);

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, requestId, "UNKNOWN");

        // then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("잘못된 principal 타입은 거부한다")
    void invalidPrincipalType_isDenied() {
        // given
        Object invalidPrincipal = "invalid";
        Long requestId = 100L;

        // when
        boolean hasPermission = permissionHandler.hasPermission(invalidPrincipal, requestId, "DELETE");

        // then
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("잘못된 targetId 타입은 거부한다")
    void invalidTargetIdType_isDenied() {
        // given
        CustomUserDetails principal = createCustomUserDetails(1L);
        String invalidTargetId = "invalid";

        // when
        boolean hasPermission = permissionHandler.hasPermission(principal, invalidTargetId, "DELETE");

        // then
        assertThat(hasPermission).isFalse();
    }

    private CustomUserDetails createCustomUserDetails(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        lenient().when(principal.getUserId()).thenReturn(userId);
        return principal;
    }

    private LabCreationRequest createRequestWithRequester(Long requestId, User requester) {
        LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithId(requestId);
        ReflectionTestUtils.setField(request, "requester", requester);
        return request;
    }
}
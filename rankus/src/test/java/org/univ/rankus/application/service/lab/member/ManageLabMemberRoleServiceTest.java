package org.univ.rankus.application.service.lab.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManageLabMemberRoleService 테스트")
class ManageLabMemberRoleServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private ManageLabMemberRoleService manageLabMemberRoleService;

    private static final Long LAB_ID = 1L;
    private static final Long MANAGER_ID = 2L;
    private static final Long MEMBER_ID = 3L;

    @Test
    @DisplayName("promoteToManager > 관리자 권한으로 랩매니저 승급 성공")
    void promoteToManager_AdminRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID);

        // then
        assertThat(member.getRole()).isEqualTo(Role.LAB_MANAGER);
        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(MANAGER_ID);
        verify(userRepositoryPort).findById(MEMBER_ID);
        verify(userRepositoryPort).save(member);
    }

    @Test
    @DisplayName("promoteToManager > 교수 권한으로 랩매니저 승급 성공")
    void promoteToManager_ProfessorRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.PROFESSOR);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID);

        // then
        assertThat(member.getRole()).isEqualTo(Role.LAB_MANAGER);
        verify(userRepositoryPort).save(member);
    }

    @Test
    @DisplayName("promoteToManager > 랩 리더 권한으로 소속 랩실 멤버 승급 성공")
    void promoteToManager_LabLeaderRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildLabLeaderWithLab(lab);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID);

        // then
        assertThat(member.getRole()).isEqualTo(Role.LAB_MANAGER);
        verify(userRepositoryPort).save(member);
    }

    @Test
    @DisplayName("promoteToManager > 존재하지 않는 랩실 ID로 승급 시 예외 발생")
    void promoteToManager_LabNotFound_ThrowsException() {
        // given
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lab not found with id: " + LAB_ID);
    }

    @Test
    @DisplayName("promoteToManager > 존재하지 않는 관리자 ID로 승급 시 예외 발생")
    void promoteToManager_ManagerNotFound_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Manager not found with id: " + MANAGER_ID);
    }

    @Test
    @DisplayName("promoteToManager > 권한 없는 사용자가 승급 시도 시 예외 발생")
    void promoteToManager_UnauthorizedUser_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildValidUserWithId(MANAGER_ID);
        manager.changeRole(Role.STUDENT);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You don't have permission to manage lab members");
    }

    @Test
    @DisplayName("promoteToManager > 존재하지 않는 멤버 ID로 승급 시 예외 발생")
    void promoteToManager_MemberNotFound_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member not found with id: " + MEMBER_ID);
    }

    @Test
    @DisplayName("promoteToManager > 다른 랩실 멤버 승급 시 예외 발생")
    void promoteToManager_MemberNotInLab_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        Lab otherLab = DomainLabFactory.buildValidLabWithId(999L);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabMemberWithLab(otherLab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member does not belong to the specified lab");
    }

    @Test
    @DisplayName("promoteToManager > 이미 매니저인 멤버 승급 시 예외 발생")
    void promoteToManager_AlreadyManager_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabManagerWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.promoteToManager(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only LAB_MEMBER can be promoted to LAB_MANAGER");
    }

    @Test
    @DisplayName("demoteToMember > 관리자 권한으로 랩매니저 강등 성공")
    void demoteToMember_AdminRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabManagerWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        manageLabMemberRoleService.demoteToMember(LAB_ID, MEMBER_ID, MANAGER_ID);

        // then
        assertThat(member.getRole()).isEqualTo(Role.LAB_MEMBER);
        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(MANAGER_ID);
        verify(userRepositoryPort).findById(MEMBER_ID);
        verify(userRepositoryPort).save(member);
    }

    @Test
    @DisplayName("demoteToMember > 교수 권한으로 랩매니저 강등 성공")
    void demoteToMember_ProfessorRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.PROFESSOR);
        User member = DomainUserFactory.buildLabManagerWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        manageLabMemberRoleService.demoteToMember(LAB_ID, MEMBER_ID, MANAGER_ID);

        // then
        assertThat(member.getRole()).isEqualTo(Role.LAB_MEMBER);
        verify(userRepositoryPort).save(member);
    }

    @Test
    @DisplayName("demoteToMember > 랩 리더 권한으로 소속 랩실 멤버 강등 성공")
    void demoteToMember_LabLeaderRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildLabLeaderWithLab(lab);
        User member = DomainUserFactory.buildLabManagerWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        manageLabMemberRoleService.demoteToMember(LAB_ID, MEMBER_ID, MANAGER_ID);

        // then
        assertThat(member.getRole()).isEqualTo(Role.LAB_MEMBER);
        verify(userRepositoryPort).save(member);
    }

    @Test
    @DisplayName("demoteToMember > 일반 멤버 강등 시 예외 발생")
    void demoteToMember_AlreadyMember_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.demoteToMember(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only LAB_MANAGER can be demoted to LAB_MEMBER");
    }

    @Test
    @DisplayName("demoteToMember > 권한 없는 사용자가 강등 시도 시 예외 발생")
    void demoteToMember_UnauthorizedUser_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildValidUserWithId(MANAGER_ID);
        manager.changeRole(Role.LAB_MEMBER);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.demoteToMember(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You don't have permission to manage lab members");
    }

    @Test
    @DisplayName("demoteToMember > 다른 랩실 멤버 강등 시 예외 발생")
    void demoteToMember_MemberNotInLab_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        Lab otherLab = DomainLabFactory.buildValidLabWithId(999L);
        User manager = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabManagerWithLab(otherLab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> manageLabMemberRoleService.demoteToMember(LAB_ID, MEMBER_ID, MANAGER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member does not belong to the specified lab");
    }
}
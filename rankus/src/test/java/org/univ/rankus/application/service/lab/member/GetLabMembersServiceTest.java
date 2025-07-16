package org.univ.rankus.application.service.lab.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;
import org.univ.rankus.application.port.in.query.GetLabMemberActivityQuery;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLabMembersService 테스트")
class GetLabMembersServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private GetLabMemberActivityQuery getLabMemberActivityQuery;

    @InjectMocks
    private GetLabMembersService getLabMembersService;

    private static final Long LAB_ID = 1L;
    private static final Long REQUESTER_ID = 2L;
    private static final Long MEMBER_ID = 3L;

    @Test
    @DisplayName("getLabMembers > 관리자 권한으로 랩실 멤버 조회 성공")
    void getLabMembers_AdminRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member1 = DomainUserFactory.buildLabMemberWithLab(lab);
        User member2 = DomainUserFactory.buildLabMemberWithLab(lab);
        List<User> members = Arrays.asList(member1, member2);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findByLab(lab)).willReturn(members);

        // when
        List<User> result = getLabMembersService.getLabMembers(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(member1, member2);
        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
        verify(userRepositoryPort).findByLab(lab);
    }

    @Test
    @DisplayName("getLabMembers > 교수 권한으로 랩실 멤버 조회 성공")
    void getLabMembers_ProfessorRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildUserWithLabAndRole(lab, Role.PROFESSOR);
        User member1 = DomainUserFactory.buildLabMemberWithLab(lab);
        List<User> members = Arrays.asList(member1);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findByLab(lab)).willReturn(members);

        // when
        List<User> result = getLabMembersService.getLabMembers(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(member1);
    }

    @Test
    @DisplayName("getLabMembers > 랩 리더 권한으로 소속 랩실 멤버 조회 성공")
    void getLabMembers_LabLeaderRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildLabLeaderWithLab(lab);
        User member1 = DomainUserFactory.buildLabMemberWithLab(lab);
        List<User> members = Arrays.asList(member1);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findByLab(lab)).willReturn(members);

        // when
        List<User> result = getLabMembersService.getLabMembers(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(member1);
    }

    @Test
    @DisplayName("getLabMembers > 존재하지 않는 랩실 ID로 조회 시 예외 발생")
    void getLabMembers_LabNotFound_ThrowsException() {
        // given
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getLabMembersService.getLabMembers(LAB_ID, REQUESTER_ID))
                .isInstanceOf(LabNotFoundException.class);
    }

    @Test
    @DisplayName("getLabMembers > 존재하지 않는 요청자 ID로 조회 시 예외 발생")
    void getLabMembers_RequesterNotFound_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getLabMembersService.getLabMembers(LAB_ID, REQUESTER_ID))
                .isInstanceOf(LabNotFoundException.class);
    }

    @Test
    @DisplayName("getLabMembers > 권한 없는 사용자 조회 시 예외 발생")
    void getLabMembers_UnauthorizedUser_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildValidUserWithId(REQUESTER_ID);
        requester.changeRole(Role.STUDENT);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));

        // when & then
        assertThatThrownBy(() -> getLabMembersService.getLabMembers(LAB_ID, REQUESTER_ID))
                .isInstanceOf(LabPermissionException.class);
    }

    @Test
    @DisplayName("getLabMember > 특정 랩 멤버 조회 성공")
    void getLabMember_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        User result = getLabMembersService.getLabMember(LAB_ID, MEMBER_ID, REQUESTER_ID);

        // then
        assertThat(result).isEqualTo(member);
        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
        verify(userRepositoryPort).findById(MEMBER_ID);
    }

    @Test
    @DisplayName("getLabMember > 다른 랩실 멤버 조회 시 예외 발생")
    void getLabMember_MemberNotInLab_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        Lab otherLab = DomainLabFactory.buildValidLabWithId(999L);
        User requester = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabMemberWithLab(otherLab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> getLabMembersService.getLabMember(LAB_ID, MEMBER_ID, REQUESTER_ID))
                .isInstanceOf(LabValidationException.class);
    }

    @Test
    @DisplayName("getLabMemberDetail > 관리자 권한으로 상세 정보 조회 성공")
    void getLabMemberDetail_AdminRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        LabMemberDetailResponse.LabActivityStats stats = new LabMemberDetailResponse.LabActivityStats(
                10, 8, 1, 1, 80.0, 5, 450, 10
        );

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(getLabMemberActivityQuery.getLabMemberActivityStats(LAB_ID, MEMBER_ID)).willReturn(stats);

        // when
        LabMemberDetailResponse result = getLabMembersService.getLabMemberDetail(LAB_ID, MEMBER_ID, REQUESTER_ID);

        // then
        assertThat(result.getId()).isEqualTo(member.getId());
        assertThat(result.getName()).isEqualTo(member.getName());
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
        assertThat(result.getActivityStats()).isNotNull();
        assertThat(result.getActivityStats().getTotalAttendanceSessions()).isEqualTo(10);
        assertThat(result.getActivityStats().getAttendanceRate()).isEqualTo(80.0);
        verify(getLabMemberActivityQuery).getLabMemberActivityStats(LAB_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("getLabMemberDetail > 일반 랩원 권한으로 기본 정보만 조회 성공")
    void getLabMemberDetail_LabMemberRole_BasicInfoOnly() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildLabMemberWithLab(lab);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        LabMemberDetailResponse result = getLabMembersService.getLabMemberDetail(LAB_ID, MEMBER_ID, REQUESTER_ID);

        // then
        assertThat(result.getId()).isEqualTo(member.getId());
        assertThat(result.getName()).isEqualTo(member.getName());
        assertThat(result.getEmail()).isNull(); // Basic info only
        assertThat(result.getActivityStats()).isNull(); // Basic info only
    }

    @Test
    @DisplayName("getLabMemberDetail > 랩 리더 권한으로 동일 랩실 멤버 상세 정보 조회 성공")
    void getLabMemberDetail_LabLeaderRole_SameLabMember_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildLabLeaderWithLab(lab);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);

        LabMemberDetailResponse.LabActivityStats stats = new LabMemberDetailResponse.LabActivityStats(
                12, 10, 1, 1, 83.3, 7, 520, 12
        );

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(getLabMemberActivityQuery.getLabMemberActivityStats(LAB_ID, MEMBER_ID)).willReturn(stats);

        // when
        LabMemberDetailResponse result = getLabMembersService.getLabMemberDetail(LAB_ID, MEMBER_ID, REQUESTER_ID);

        // then
        assertThat(result.getId()).isEqualTo(member.getId());
        assertThat(result.getName()).isEqualTo(member.getName());
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
        assertThat(result.getActivityStats()).isNotNull();
        assertThat(result.getActivityStats().getTotalAttendanceSessions()).isEqualTo(12);
        assertThat(result.getActivityStats().getAttendanceRate()).isEqualTo(83.3);
    }

    @Test
    @DisplayName("getLabMemberDetail > 존재하지 않는 멤버 ID로 조회 시 예외 발생")
    void getLabMemberDetail_MemberNotFound_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildUserWithLabAndRole(lab, Role.ADMIN);

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findById(MEMBER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getLabMembersService.getLabMemberDetail(LAB_ID, MEMBER_ID, REQUESTER_ID))
                .isInstanceOf(LabNotFoundException.class);
    }
}
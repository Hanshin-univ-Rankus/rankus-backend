package org.univ.rankus.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabMemberPermissionEvaluatorTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LabMemberPermissionEvaluator labMemberPermissionEvaluator;

    private Lab testLab;
    private User adminUser;
    private User professorUser;
    private User labLeaderUser;
    private User labManagerUser;
    private User labMemberUser;
    private User studentUser;
    private User outsideUser;

    @BeforeEach
    void setUp() {
        testLab = DomainLabFactory.buildValidLabWithId(1L);

        adminUser = DomainUserFactory.buildAdminUser();
        professorUser = DomainUserFactory.buildProfessorUser();
        labLeaderUser = DomainUserFactory.buildLabLeaderWithLab(testLab);
        labManagerUser = DomainUserFactory.buildLabManagerWithLab(testLab);
        labMemberUser = DomainUserFactory.buildLabMemberWithLab(testLab);
        studentUser = DomainUserFactory.buildStudentUser();
        outsideUser = DomainUserFactory.buildLabMemberUser(); // 다른 랩 소속
    }

    @Test
    void canViewLabMembers_AdminUser_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), adminUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canViewLabMembers_ProfessorUser_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(professorUser.getId())).thenReturn(Optional.of(professorUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), professorUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canViewLabMembers_LabLeaderOfSameLab_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(labLeaderUser.getId())).thenReturn(Optional.of(labLeaderUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), labLeaderUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canViewLabMembers_LabManagerOfSameLab_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(labManagerUser.getId())).thenReturn(Optional.of(labManagerUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), labManagerUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canViewLabMembers_LabMemberOfSameLab_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(labMemberUser.getId())).thenReturn(Optional.of(labMemberUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), labMemberUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canViewLabMembers_StudentUser_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), studentUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canViewLabMembers_OutsideLabMember_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(outsideUser.getId())).thenReturn(Optional.of(outsideUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), outsideUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canViewLabMembers_UserNotFound_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), 999L);

        // then
        assertFalse(result);
    }

    @Test
    void canViewLabMembers_LabNotFound_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(999L, adminUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canManageLabMembers_AdminUser_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), adminUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canManageLabMembers_ProfessorUser_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(professorUser.getId())).thenReturn(Optional.of(professorUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), professorUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canManageLabMembers_LabLeaderOfSameLab_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(labLeaderUser.getId())).thenReturn(Optional.of(labLeaderUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), labLeaderUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canManageLabMembers_LabManagerOfSameLab_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(labManagerUser.getId())).thenReturn(Optional.of(labManagerUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), labManagerUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canManageLabMembers_LabMemberOfSameLab_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(labMemberUser.getId())).thenReturn(Optional.of(labMemberUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), labMemberUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canManageLabMembers_StudentUser_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), studentUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canTransferLabLeadership_AdminUser_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canTransferLabLeadership(testLab.getId(), adminUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canTransferLabLeadership_ProfessorUser_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(professorUser.getId())).thenReturn(Optional.of(professorUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canTransferLabLeadership(testLab.getId(), professorUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canTransferLabLeadership_LabLeaderOfSameLab_ShouldReturnTrue() {
        // given
        when(userRepositoryPort.findById(labLeaderUser.getId())).thenReturn(Optional.of(labLeaderUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canTransferLabLeadership(testLab.getId(), labLeaderUser.getId());

        // then
        assertTrue(result);
    }

    @Test
    void canTransferLabLeadership_LabManagerOfSameLab_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(labManagerUser.getId())).thenReturn(Optional.of(labManagerUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canTransferLabLeadership(testLab.getId(), labManagerUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canTransferLabLeadership_LabMemberOfSameLab_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(labMemberUser.getId())).thenReturn(Optional.of(labMemberUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.canTransferLabLeadership(testLab.getId(), labMemberUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_ViewMembersPermission_ShouldReturnCorrectResult() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, "VIEW_MEMBERS");

        // then
        assertTrue(result);
    }

    @Test
    void hasPermission_ManageMembersPermission_ShouldReturnCorrectResult() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, "MANAGE_MEMBERS");

        // then
        assertTrue(result);
    }

    @Test
    void hasPermission_TransferLeadershipPermission_ShouldReturnCorrectResult() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepositoryPort.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(labRepositoryPort.findById(testLab.getId())).thenReturn(Optional.of(testLab));

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, "TRANSFER_LEADERSHIP");

        // then
        assertTrue(result);
    }

    @Test
    void hasPermission_UnknownPermission_ShouldReturnFalse() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, "UNKNOWN_PERMISSION");

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_NullAuthentication_ShouldReturnFalse() {
        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(null, testLab, "VIEW_MEMBERS");

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_NullTargetDomainObject_ShouldReturnFalse() {
        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, null, "VIEW_MEMBERS");

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_NullPermission_ShouldReturnFalse() {
        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, null);

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_NonLabTargetDomainObject_ShouldReturnFalse() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, "NotALab", "VIEW_MEMBERS");

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_InvalidPrincipal_ShouldReturnFalse() {
        // given
        when(authentication.getPrincipal()).thenReturn("InvalidPrincipal");

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, "VIEW_MEMBERS");

        // then
        assertFalse(result);
    }

    @Test
    void canViewLabMembers_ExceptionThrown_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = labMemberPermissionEvaluator.canViewLabMembers(testLab.getId(), adminUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canManageLabMembers_ExceptionThrown_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = labMemberPermissionEvaluator.canManageLabMembers(testLab.getId(), adminUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void canTransferLabLeadership_ExceptionThrown_ShouldReturnFalse() {
        // given
        when(userRepositoryPort.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = labMemberPermissionEvaluator.canTransferLabLeadership(testLab.getId(), adminUser.getId());

        // then
        assertFalse(result);
    }

    @Test
    void hasPermission_ExceptionThrown_ShouldReturnFalse() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepositoryPort.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = labMemberPermissionEvaluator.hasPermission(authentication, testLab, "VIEW_MEMBERS");

        // then
        assertFalse(result);
    }
}
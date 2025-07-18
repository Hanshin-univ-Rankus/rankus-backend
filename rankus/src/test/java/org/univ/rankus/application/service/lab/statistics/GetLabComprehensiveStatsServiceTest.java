package org.univ.rankus.application.service.lab.statistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.adapter.in.web.lab.statistics.dto.LabComprehensiveStatsResponse;
import org.univ.rankus.application.port.out.*;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLabComprehensiveStatsService 테스트")
class GetLabComprehensiveStatsServiceTest {

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;


    @Mock
    private AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;

    @Mock
    private ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;

    @Mock
    private LabApplicationRepositoryPort labApplicationRepositoryPort;

    @Mock
    private LabNoticeRepositoryPort labNoticeRepositoryPort;

    @Mock
    private InterviewRepositoryPort interviewRepositoryPort;

    @InjectMocks
    private GetLabComprehensiveStatsService getLabComprehensiveStatsService;

    private static final Long REQUESTER_ID = 1L;
    private static final Long LAB_ID = 2L;

    @Test
    @DisplayName("getLabComprehensiveStats > 관리자 권한으로 전체 랩실 종합 통계 조회 성공")
    void getLabComprehensiveStats_AdminRole_Success() {
        // given
        User requester = DomainUserFactory.buildAdminUser();
        Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
        Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
        List<Lab> labs = Arrays.asList(lab1, lab2);

        setupMockForComprehensiveStats(requester, labs);

        // when
        LabComprehensiveStatsResponse response = getLabComprehensiveStatsService.getLabComprehensiveStats(REQUESTER_ID);

        // then
        assertThat(response.getLabStats()).hasSize(2);
        assertThat(response.getOverallSummary()).isNotNull();
        assertThat(response.getOverallSummary().getTotalLabs()).isEqualTo(2);
        assertThat(response.getLabStats().get(0).getLabId()).isEqualTo(1L);
        assertThat(response.getLabStats().get(1).getLabId()).isEqualTo(2L);

        verify(userRepositoryPort).findById(REQUESTER_ID);
        verify(labRepositoryPort).findAll();
    }

    @Test
    @DisplayName("getLabComprehensiveStats > 교수 권한으로 전체 랩실 종합 통계 조회 성공")
    void getLabComprehensiveStats_ProfessorRole_Success() {
        // given
        User requester = DomainUserFactory.buildProfessorUser();
        Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
        List<Lab> labs = Arrays.asList(lab1);

        setupMockForComprehensiveStats(requester, labs);

        // when
        LabComprehensiveStatsResponse response = getLabComprehensiveStatsService.getLabComprehensiveStats(REQUESTER_ID);

        // then
        assertThat(response.getLabStats()).hasSize(1);
        assertThat(response.getOverallSummary()).isNotNull();
        assertThat(response.getOverallSummary().getTotalLabs()).isEqualTo(1);

        verify(userRepositoryPort).findById(REQUESTER_ID);
        verify(labRepositoryPort).findAll();
    }

    @Test
    @DisplayName("getLabComprehensiveStats > 권한 없는 사용자 조회 시 예외 발생")
    void getLabComprehensiveStats_UnauthorizedUser_ThrowsException() {
        // given
        User requester = DomainUserFactory.buildLabMemberWithLab(DomainLabFactory.buildValidLabWithId(LAB_ID));

        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));

        // when & then
        assertThatThrownBy(() -> getLabComprehensiveStatsService.getLabComprehensiveStats(REQUESTER_ID))
                .isInstanceOf(LabPermissionException.class);

        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabComprehensiveStats > 존재하지 않는 요청자 ID로 조회 시 예외 발생")
    void getLabComprehensiveStats_RequesterNotFound_ThrowsException() {
        // given
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getLabComprehensiveStatsService.getLabComprehensiveStats(REQUESTER_ID))
                .isInstanceOf(LabNotFoundException.class);

        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 관리자 권한으로 특정 랩실 상세 통계 조회 성공")
    void getLabDetailedStats_AdminRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildAdminUser();

        setupMockForDetailedStats(lab, requester);

        // when
        LabComprehensiveStatsResponse.LabStats result = getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLabId()).isEqualTo(LAB_ID);
        assertThat(result.getLabName()).isEqualTo(lab.getName());
        assertThat(result.getCategory()).isEqualTo(lab.getCategory());
        assertThat(result.getTotalMembers()).isEqualTo(3);
        assertThat(result.getTotalSessions()).isEqualTo(2);
        assertThat(result.getAverageAttendanceRate()).isEqualTo(66.67);

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 교수 권한으로 특정 랩실 상세 통계 조회 성공")
    void getLabDetailedStats_ProfessorRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildProfessorUser();

        setupMockForDetailedStats(lab, requester);

        // when
        LabComprehensiveStatsResponse.LabStats result = getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLabId()).isEqualTo(LAB_ID);
        assertThat(result.getLabName()).isEqualTo(lab.getName());

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 랩 리더 권한으로 소속 랩실 상세 통계 조회 성공")
    void getLabDetailedStats_LabLeaderRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildLabLeaderWithLab(lab);

        setupMockForDetailedStats(lab, requester);

        // when
        LabComprehensiveStatsResponse.LabStats result = getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLabId()).isEqualTo(LAB_ID);
        assertThat(result.getTotalMembers()).isEqualTo(3);

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 일반 랩원 권한으로 소속 랩실 상세 통계 조회 성공")
    void getLabDetailedStats_LabMemberRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildLabMemberWithLab(lab);

        setupMockForDetailedStats(lab, requester);

        // when
        LabComprehensiveStatsResponse.LabStats result = getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLabId()).isEqualTo(LAB_ID);

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 존재하지 않는 랩실 ID로 조회 시 예외 발생")
    void getLabDetailedStats_LabNotFound_ThrowsException() {
        // given
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID))
                .isInstanceOf(LabNotFoundException.class);

        verify(labRepositoryPort).findById(LAB_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 존재하지 않는 요청자 ID로 조회 시 예외 발생")
    void getLabDetailedStats_RequesterNotFound_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID))
                .isInstanceOf(LabNotFoundException.class);

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 권한 없는 사용자 조회 시 예외 발생")
    void getLabDetailedStats_UnauthorizedUser_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildStudentUser(); // 권한 없는 학생

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));

        // when & then
        assertThatThrownBy(() -> getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID))
                .isInstanceOf(LabPermissionException.class);

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
    }

    @Test
    @DisplayName("getLabDetailedStats > 빈 데이터를 가진 랩실 통계 조회 성공")
    void getLabDetailedStats_EmptyLabData_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User requester = DomainUserFactory.buildAdminUser();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(userRepositoryPort.findByLab(lab)).willReturn(Arrays.asList()); // 빈 멤버 목록
        given(attendanceSessionRepositoryPort.findByLabId(LAB_ID)).willReturn(Arrays.asList()); // 빈 세션 목록
        given(scoreSubmissionRepositoryPort.findByLabIdAndStatus(LAB_ID, SubmissionStatus.APPROVED)).willReturn(Arrays.asList());
        given(scoreSubmissionRepositoryPort.sumScoresByLabIdAndStatus(LAB_ID, SubmissionStatus.APPROVED)).willReturn(0);
        given(scoreSubmissionRepositoryPort.findByLabIdAndStatus(LAB_ID, SubmissionStatus.PENDING)).willReturn(Arrays.asList());
        given(labApplicationRepositoryPort.findByLabId(LAB_ID)).willReturn(Arrays.asList());
        given(labNoticeRepositoryPort.findByLabId(LAB_ID)).willReturn(Arrays.asList());

        // when
        LabComprehensiveStatsResponse.LabStats result = getLabComprehensiveStatsService.getLabDetailedStats(LAB_ID, REQUESTER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLabId()).isEqualTo(LAB_ID);
        assertThat(result.getTotalMembers()).isEqualTo(0);
        assertThat(result.getTotalSessions()).isEqualTo(0);
        assertThat(result.getAverageAttendanceRate()).isEqualTo(0.0);
        assertThat(result.getTotalScore()).isEqualTo(0);
        assertThat(result.getTotalScoreSubmissions()).isEqualTo(0);
        assertThat(result.getTotalApplications()).isEqualTo(0);
        assertThat(result.getTotalNotices()).isEqualTo(0);

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(REQUESTER_ID);
        verify(userRepositoryPort).findByLab(lab);
        verify(attendanceSessionRepositoryPort).findByLabId(LAB_ID);
    }

    // Helper methods
    private void setupMockForComprehensiveStats(User requester, List<Lab> labs) {
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(labRepositoryPort.findAll()).willReturn(labs);

        // 각 랩실에 대한 기본 설정
        for (Lab lab : labs) {
            setupBasicLabStats(lab);
        }
    }

    private void setupMockForDetailedStats(Lab lab, User requester) {
        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        setupBasicLabStats(lab);
    }

    private void setupBasicLabStats(Lab lab) {
        Long labId = lab.getId();

        // 멤버 설정
        User leader = DomainUserFactory.buildLabLeaderWithLab(lab);
        User manager = DomainUserFactory.buildLabManagerWithLab(lab);
        User member = DomainUserFactory.buildLabMemberWithLab(lab);
        List<User> members = Arrays.asList(leader, manager, member);

        // 출석 세션 설정
        AttendanceSession session1 = DomainAttendanceFactory.buildValidSessionWithId(1L);
        AttendanceSession session2 = DomainAttendanceFactory.buildValidSessionWithId(2L);

        // createdAt 필드 설정 (null 방지)
        ReflectionTestUtils.setField(session1, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(session2, "createdAt", LocalDateTime.now().minusDays(2));

        List<AttendanceSession> sessions = Arrays.asList(session1, session2);

        // 출석 기록 설정 - 실제 프로덕션 코드에서는 findByLabId를 사용
        AttendanceRecord record1 = DomainAttendanceFactory.buildValidRecord(); // PRESENT
        AttendanceRecord record2 = DomainAttendanceFactory.buildAbsentRecord(); // ABSENT
        AttendanceRecord record3 = DomainAttendanceFactory.buildValidRecord(); // PRESENT
        List<AttendanceRecord> allRecords = Arrays.asList(record1, record2, record3);

        given(userRepositoryPort.findByLab(lab)).willReturn(members);
        given(attendanceSessionRepositoryPort.findByLabId(labId)).willReturn(sessions);
        given(attendanceRecordRepositoryPort.findByLabId(labId, 0, Integer.MAX_VALUE)).willReturn(allRecords);
        given(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.APPROVED)).willReturn(Arrays.asList());
        given(scoreSubmissionRepositoryPort.sumScoresByLabIdAndStatus(labId, SubmissionStatus.APPROVED)).willReturn(1250);
        given(scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, SubmissionStatus.PENDING)).willReturn(Arrays.asList());
        given(labApplicationRepositoryPort.findByLabId(labId)).willReturn(Arrays.asList());
        given(labNoticeRepositoryPort.findByLabId(labId)).willReturn(Arrays.asList()); // 빈 리스트로 설정해서 stream 연산 안전하게 처리
    }
}
package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateRequest;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateResponse;
import org.univ.rankus.application.port.in.AttendanceRecordCommandUseCase;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkUpdateAttendanceService 테스트")
class BulkUpdateAttendanceServiceTest {

    @Mock
    private AttendanceRecordCommandUseCase attendanceRecordCommandUseCase;

    @Mock
    private AttendanceRepositoryPort attendanceRepositoryPort;

    @Mock
    private AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private BulkUpdateAttendanceService bulkUpdateAttendanceService;

    private static final Long LAB_ID = 1L;
    private static final Long MANAGER_ID = 2L;
    private static final Long RECORD_ID_1 = 101L;
    private static final Long RECORD_ID_2 = 102L;
    private static final String REASON = "출석 점검 후 일괄 수정";

    @Test
    @DisplayName("bulkUpdateAttendance > 관리자 권한으로 출석 일괄 수정 완전 성공")
    void bulkUpdateAttendance_AdminRole_CompleteSuccess() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildAdminUser();

        AttendanceRecord record1 = DomainAttendanceFactory.buildAbsentRecord();
        AttendanceRecord record2 = DomainAttendanceFactory.buildAbsentRecord();
        AttendanceRecord updatedRecord1 = DomainAttendanceFactory.buildValidRecord();
        AttendanceRecord updatedRecord2 = DomainAttendanceFactory.buildLateRecord();

        BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_1)).willReturn(Optional.of(record1));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_2)).willReturn(Optional.of(record2));
        given(attendanceRecordCommandUseCase.updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON)
        )).willReturn(updatedRecord1);
        given(attendanceRecordCommandUseCase.updateAttendanceStatus(
                eq(RECORD_ID_2), eq(AttendanceStatus.LATE), eq(MANAGER_ID), eq(REASON)
        )).willReturn(updatedRecord2);

        // when
        BulkAttendanceUpdateResponse response = bulkUpdateAttendanceService.bulkUpdateAttendance(
                LAB_ID, request, MANAGER_ID);

        // then
        assertThat(response.getTotalUpdates()).isEqualTo(2);
        assertThat(response.getSuccessfulUpdates()).isEqualTo(2);
        assertThat(response.getFailedUpdates()).isEqualTo(0);
        assertThat(response.isCompleteSuccess()).isTrue();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).isSuccess()).isTrue();
        assertThat(response.getResults().get(1).isSuccess()).isTrue();

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(MANAGER_ID);
        verify(attendanceRecordRepositoryPort).findById(RECORD_ID_1);
        verify(attendanceRecordRepositoryPort).findById(RECORD_ID_2);
        verify(attendanceRecordCommandUseCase).updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON));
        verify(attendanceRecordCommandUseCase).updateAttendanceStatus(
                eq(RECORD_ID_2), eq(AttendanceStatus.LATE), eq(MANAGER_ID), eq(REASON));
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 교수 권한으로 출석 일괄 수정 부분 성공")
    void bulkUpdateAttendance_ProfessorRole_PartialSuccess() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildProfessorUser();

        AttendanceRecord record1 = DomainAttendanceFactory.buildAbsentRecord();
        AttendanceRecord record2 = DomainAttendanceFactory.buildValidRecord(); // 이미 PRESENT 상태
        // record2를 PRESENT 상태로 설정하고 PRESENT로 변경을 시도하려고 함 (동일한 상태)
        AttendanceRecord updatedRecord1 = DomainAttendanceFactory.buildValidRecord();

        BulkAttendanceUpdateRequest request = createPartialSuccessRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_1)).willReturn(Optional.of(record1));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_2)).willReturn(Optional.of(record2));
        given(attendanceRecordCommandUseCase.updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON)
        )).willReturn(updatedRecord1);

        // when
        BulkAttendanceUpdateResponse response = bulkUpdateAttendanceService.bulkUpdateAttendance(
                LAB_ID, request, MANAGER_ID);

        // then
        assertThat(response.getTotalUpdates()).isEqualTo(2);
        assertThat(response.getSuccessfulUpdates()).isEqualTo(1);
        assertThat(response.getFailedUpdates()).isEqualTo(1);
        assertThat(response.isCompleteSuccess()).isFalse();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).isSuccess()).isTrue();
        assertThat(response.getResults().get(1).isSuccess()).isFalse();
        assertThat(response.getResults().get(1).getErrorMessage()).isEqualTo("Status is already PRESENT");

        verify(attendanceRecordCommandUseCase).updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON));
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 랩 매니저 권한으로 소속 랩실 출석 일괄 수정 성공")
    void bulkUpdateAttendance_LabManagerRole_Success() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildLabManagerWithLab(lab);

        AttendanceRecord record1 = DomainAttendanceFactory.buildAbsentRecord();
        AttendanceRecord updatedRecord1 = DomainAttendanceFactory.buildValidRecord();

        BulkAttendanceUpdateRequest request = createSingleUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_1)).willReturn(Optional.of(record1));
        given(attendanceRecordCommandUseCase.updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON)
        )).willReturn(updatedRecord1);

        // when
        BulkAttendanceUpdateResponse response = bulkUpdateAttendanceService.bulkUpdateAttendance(
                LAB_ID, request, MANAGER_ID);

        // then
        assertThat(response.getTotalUpdates()).isEqualTo(1);
        assertThat(response.getSuccessfulUpdates()).isEqualTo(1);
        assertThat(response.getFailedUpdates()).isEqualTo(0);
        assertThat(response.isCompleteSuccess()).isTrue();

        verify(attendanceRecordCommandUseCase).updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON));
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 출석 기록 업데이트 실패 시 예외 처리")
    void bulkUpdateAttendance_UpdateFailure_HandlesException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildAdminUser();

        AttendanceRecord record1 = DomainAttendanceFactory.buildAbsentRecord();
        AttendanceRecord record2 = DomainAttendanceFactory.buildAbsentRecord();
        AttendanceRecord updatedRecord1 = DomainAttendanceFactory.buildValidRecord();

        BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_1)).willReturn(Optional.of(record1));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_2)).willReturn(Optional.of(record2));
        given(attendanceRecordCommandUseCase.updateAttendanceStatus(
                eq(RECORD_ID_1), eq(AttendanceStatus.PRESENT), eq(MANAGER_ID), eq(REASON)
        )).willReturn(updatedRecord1);
        given(attendanceRecordCommandUseCase.updateAttendanceStatus(
                eq(RECORD_ID_2), eq(AttendanceStatus.LATE), eq(MANAGER_ID), eq(REASON)
        )).willThrow(new RuntimeException("Update failed"));

        // when
        BulkAttendanceUpdateResponse response = bulkUpdateAttendanceService.bulkUpdateAttendance(
                LAB_ID, request, MANAGER_ID);

        // then
        assertThat(response.getTotalUpdates()).isEqualTo(2);
        assertThat(response.getSuccessfulUpdates()).isEqualTo(1);
        assertThat(response.getFailedUpdates()).isEqualTo(1);
        assertThat(response.isCompleteSuccess()).isFalse();
        assertThat(response.getResults().get(0).isSuccess()).isTrue();
        assertThat(response.getResults().get(1).isSuccess()).isFalse();
        assertThat(response.getResults().get(1).getErrorMessage()).isEqualTo("Update failed");
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 존재하지 않는 랩실 ID로 요청 시 예외 발생")
    void bulkUpdateAttendance_LabNotFound_ThrowsException() {
        // given
        BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bulkUpdateAttendanceService.bulkUpdateAttendance(LAB_ID, request, MANAGER_ID))
                .isInstanceOf(LabNotFoundException.class)
                .hasMessage("해당 랩실을 찾을 수 없습니다.");

        verify(labRepositoryPort).findById(LAB_ID);
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 존재하지 않는 관리자 ID로 요청 시 예외 발생")
    void bulkUpdateAttendance_ManagerNotFound_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bulkUpdateAttendanceService.bulkUpdateAttendance(LAB_ID, request, MANAGER_ID))
                .isInstanceOf(LabNotFoundException.class)
                .hasMessage("해당 랩실을 찾을 수 없습니다.");

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(MANAGER_ID);
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 권한 없는 사용자가 요청 시 예외 발생")
    void bulkUpdateAttendance_PermissionDenied_ThrowsException() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildLabMemberWithLab(lab); // 일반 랩원 (권한 없음)
        BulkAttendanceUpdateRequest request = createBulkUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));

        // when & then
        assertThatThrownBy(() -> bulkUpdateAttendanceService.bulkUpdateAttendance(LAB_ID, request, MANAGER_ID))
                .isInstanceOf(LabPermissionException.class)
                .hasMessage("출석 일괄 수정 권한이 없습니다.");

        verify(labRepositoryPort).findById(LAB_ID);
        verify(userRepositoryPort).findById(MANAGER_ID);
    }

    @Test
    @DisplayName("bulkUpdateAttendance > 존재하지 않는 출석 기록 ID로 요청 시 실패 처리")
    void bulkUpdateAttendance_AttendanceRecordNotFound_HandleAsFailure() {
        // given
        Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
        User manager = DomainUserFactory.buildAdminUser();

        BulkAttendanceUpdateRequest request = createSingleUpdateRequest();

        given(labRepositoryPort.findById(LAB_ID)).willReturn(Optional.of(lab));
        given(userRepositoryPort.findById(MANAGER_ID)).willReturn(Optional.of(manager));
        given(attendanceRecordRepositoryPort.findById(RECORD_ID_1)).willReturn(Optional.empty());

        // when
        BulkAttendanceUpdateResponse response = bulkUpdateAttendanceService.bulkUpdateAttendance(
                LAB_ID, request, MANAGER_ID);

        // then
        assertThat(response.getTotalUpdates()).isEqualTo(1);
        assertThat(response.getSuccessfulUpdates()).isEqualTo(0);
        assertThat(response.getFailedUpdates()).isEqualTo(1);
        assertThat(response.isCompleteSuccess()).isFalse();
        assertThat(response.getResults().get(0).isSuccess()).isFalse();
        assertThat(response.getResults().get(0).getErrorMessage()).contains("Attendance record not found with id:");

        verify(attendanceRecordRepositoryPort).findById(RECORD_ID_1);
    }

    // Helper methods
    private BulkAttendanceUpdateRequest createBulkUpdateRequest() {
        BulkAttendanceUpdateRequest request = new BulkAttendanceUpdateRequest();
        request.setUpdates(Arrays.asList(
                createAttendanceUpdateItem(RECORD_ID_1, AttendanceStatus.PRESENT),
                createAttendanceUpdateItem(RECORD_ID_2, AttendanceStatus.LATE)
        ));
        request.setReason(REASON);
        return request;
    }

    private BulkAttendanceUpdateRequest createSingleUpdateRequest() {
        BulkAttendanceUpdateRequest request = new BulkAttendanceUpdateRequest();
        request.setUpdates(Arrays.asList(
                createAttendanceUpdateItem(RECORD_ID_1, AttendanceStatus.PRESENT)
        ));
        request.setReason(REASON);
        return request;
    }

    private BulkAttendanceUpdateRequest createPartialSuccessRequest() {
        BulkAttendanceUpdateRequest request = new BulkAttendanceUpdateRequest();
        request.setUpdates(Arrays.asList(
                createAttendanceUpdateItem(RECORD_ID_1, AttendanceStatus.PRESENT),
                createAttendanceUpdateItem(RECORD_ID_2, AttendanceStatus.PRESENT) // 이미 PRESENT인 상태를 다시 PRESENT로 변경
        ));
        request.setReason(REASON);
        return request;
    }

    private BulkAttendanceUpdateRequest.AttendanceUpdateItem createAttendanceUpdateItem(Long recordId, AttendanceStatus status) {
        BulkAttendanceUpdateRequest.AttendanceUpdateItem item = new BulkAttendanceUpdateRequest.AttendanceUpdateItem();
        item.setRecordId(recordId);
        item.setNewStatus(status);
        return item;
    }
}
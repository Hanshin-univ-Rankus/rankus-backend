package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestErrorCode;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestValidationException;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabCreationRequestFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabCreationRequest 도메인 단위 테스트")
class LabCreationRequestTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 입력으로 생성 시 PENDING 상태와 입력값이 올바르게 설정됨")
        void constructor_validInput_setsPendingAndCorrectValues() {
            // given
            String labName = "AI Lab";
            LabCategory category = LabCategory.AI;
            String description = "AI research lab";
            User requester = DomainUserFactory.buildStudentUser();

            // when
            LabCreationRequest request = new LabCreationRequest(labName, category, description, requester);

            // then
            assertEquals(LabCreationStatus.PENDING, request.getStatus());
            assertEquals(labName, request.getRequestedLabName());
            assertEquals(category, request.getRequestedCategory());
            assertEquals(description, request.getRequestedDescription());
            assertEquals(requester, request.getRequester());
            assertNull(request.getProcessedBy());
            assertNull(request.getRejectionReason());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("빈 랩실 이름으로 생성 시 REQUESTED_LAB_NAME_REQUIRED 예외 발생")
        void constructor_emptyLabName_throwsLabNameRequired(String emptyLabName) {
            // given
            User requester = DomainUserFactory.buildStudentUser();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> new LabCreationRequest(emptyLabName, LabCategory.AI, "Description", requester)
            );
            assertEquals(LabCreationRequestErrorCode.REQUESTED_LAB_NAME_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("11자 이상의 랩실 이름으로 생성 시 REQUESTED_LAB_NAME_TOO_LONG 예외 발생")
        void constructor_longLabName_throwsLabNameTooLong() {
            // given
            String longName = "12345678901"; // 11자
            User requester = DomainUserFactory.buildStudentUser();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> new LabCreationRequest(longName, LabCategory.AI, "Description", requester)
            );
            assertEquals(LabCreationRequestErrorCode.REQUESTED_LAB_NAME_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("null 카테고리로 생성 시 REQUESTED_CATEGORY_REQUIRED 예외 발생")
        void constructor_nullCategory_throwsCategoryRequired() {
            // given
            User requester = DomainUserFactory.buildStudentUser();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> new LabCreationRequest("TestLab", null, "Description", requester)
            );
            assertEquals(LabCreationRequestErrorCode.REQUESTED_CATEGORY_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("256자 이상의 설명으로 생성 시 REQUESTED_DESCRIPTION_TOO_LONG 예외 발생")
        void constructor_longDescription_throwsDescriptionTooLong() {
            // given
            StringBuilder longDescription = new StringBuilder();
            while (longDescription.length() <= 255) longDescription.append('a');
            User requester = DomainUserFactory.buildStudentUser();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> new LabCreationRequest("TestLab", LabCategory.AI, longDescription.toString(), requester)
            );
            assertEquals(LabCreationRequestErrorCode.REQUESTED_DESCRIPTION_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("null 신청자로 생성 시 REQUESTER_REQUIRED 예외 발생")
        void constructor_nullRequester_throwsRequesterRequired() {
            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> new LabCreationRequest("TestLab", LabCategory.AI, "Description", null)
            );
            assertEquals(LabCreationRequestErrorCode.REQUESTER_REQUIRED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("승인 기능 검증 (approve)")
    class ApproveTests {

        @Test
        @DisplayName("PENDING 상태에서 교수가 승인 시 APPROVED로 변경되고 승인자와 승인시간이 설정됨")
        void approve_pendingStatusWithProfessor_setsApprovedAndApprover() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User professor = DomainUserFactory.buildProfessorUser();

            // when
            request.approve(professor);

            // then
            assertEquals(LabCreationStatus.APPROVED, request.getStatus());
            assertEquals(professor, request.getProcessedBy());
            assertNotNull(request.getProcessedAt());
            assertNull(request.getRejectionReason());
        }

        @Test
        @DisplayName("관리자가 승인 시 APPROVED로 변경됨")
        void approve_pendingStatusWithAdmin_setsApproved() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);

            // when
            request.approve(admin);

            // then
            assertEquals(LabCreationStatus.APPROVED, request.getStatus());
            assertEquals(admin, request.getProcessedBy());
            assertNotNull(request.getProcessedAt());
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"STUDENT", "LAB_MEMBER", "LAB_MANAGER", "LAB_LEADER"})
        @DisplayName("교수나 관리자가 아닌 사용자가 승인 시도 시 INSUFFICIENT_PERMISSION_FOR_APPROVAL 예외 발생")
        void approve_nonAuthorizedUser_throwsInsufficientPermission(Role role) {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User nonAuthorizedUser = DomainUserFactory.buildValidUserWithRole(role);

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> request.approve(nonAuthorizedUser)
            );
            assertEquals(LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL, ex.getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = LabCreationStatus.class, names = {"APPROVED", "REJECTED"})
        @DisplayName("PENDING이 아닌 상태에서 승인 시도 시 CANNOT_CHANGE_STATUS_AFTER_DECISION 예외 발생")
        void approve_nonPendingStatus_throwsCannotChangeStatus(LabCreationStatus status) {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildRequestWithStatus(status);
            User professor = DomainUserFactory.buildProfessorUser();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> request.approve(professor)
            );
            assertEquals(LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION, ex.getErrorCode());
        }

        @Test
        @DisplayName("null 승인자로 승인 시도 시 INSUFFICIENT_PERMISSION_FOR_APPROVAL 예외 발생")
        void approve_nullApprover_throwsInsufficientPermission() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> request.approve(null)
            );
            assertEquals(LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("거절 기능 검증 (reject)")
    class RejectTests {

        @Test
        @DisplayName("PENDING 상태에서 교수가 거절 시 REJECTED로 변경되고 거절자와 거절시간, 거절사유가 설정됨")
        void reject_pendingStatusWithProfessor_setsRejectedAndRejector() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User professor = DomainUserFactory.buildProfessorUser();
            String rejectionReason = "연구 방향이 맞지 않음";

            // when
            request.reject(professor, rejectionReason);

            // then
            assertEquals(LabCreationStatus.REJECTED, request.getStatus());
            assertEquals(professor, request.getProcessedBy());
            assertEquals(rejectionReason, request.getRejectionReason());
            assertNotNull(request.getProcessedAt());
        }

        @Test
        @DisplayName("관리자가 거절 시 REJECTED로 변경됨")
        void reject_pendingStatusWithAdmin_setsRejected() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
            String rejectionReason = "정책 위반";

            // when
            request.reject(admin, rejectionReason);

            // then
            assertEquals(LabCreationStatus.REJECTED, request.getStatus());
            assertEquals(admin, request.getProcessedBy());
            assertEquals(rejectionReason, request.getRejectionReason());
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"STUDENT", "LAB_MEMBER", "LAB_MANAGER", "LAB_LEADER"})
        @DisplayName("교수나 관리자가 아닌 사용자가 거절 시도 시 INSUFFICIENT_PERMISSION_FOR_APPROVAL 예외 발생")
        void reject_nonAuthorizedUser_throwsInsufficientPermission(Role role) {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User nonAuthorizedUser = DomainUserFactory.buildValidUserWithRole(role);

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> request.reject(nonAuthorizedUser, "사유")
            );
            assertEquals(LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL, ex.getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = LabCreationStatus.class, names = {"APPROVED", "REJECTED"})
        @DisplayName("PENDING이 아닌 상태에서 거절 시도 시 CANNOT_CHANGE_STATUS_AFTER_DECISION 예외 발생")
        void reject_nonPendingStatus_throwsCannotChangeStatus(LabCreationStatus status) {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildRequestWithStatus(status);
            User professor = DomainUserFactory.buildProfessorUser();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> request.reject(professor, "사유")
            );
            assertEquals(LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION, ex.getErrorCode());
        }

        @Test
        @DisplayName("null 거절자로 거절 시도 시 INSUFFICIENT_PERMISSION_FOR_APPROVAL 예외 발생")
        void reject_nullRejector_throwsInsufficientPermission() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            // when & then
            LabCreationRequestValidationException ex = assertThrows(
                    LabCreationRequestValidationException.class,
                    () -> request.reject(null, "사유")
            );
            assertEquals(LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL, ex.getErrorCode());
        }

        @Test
        @DisplayName("빈 거절 사유도 허용됨 (null이나 빈 문자열 모두 가능)")
        void reject_emptyOrNullRejectionReason_allowed() {
            // given
            LabCreationRequest request1 = DomainLabCreationRequestFactory.buildValidPendingRequest();
            LabCreationRequest request2 = DomainLabCreationRequestFactory.buildValidPendingRequest();
            User professor = DomainUserFactory.buildProfessorUser();

            // when
            request1.reject(professor, null);
            request2.reject(professor, "");

            // then
            assertEquals(LabCreationStatus.REJECTED, request1.getStatus());
            assertEquals(LabCreationStatus.REJECTED, request2.getStatus());
            assertNull(request1.getRejectionReason());
            assertNull(request2.getRejectionReason());
        }
    }

    @Nested
    @DisplayName("상태 검증 메서드")
    class StatusCheckTests {

        @Test
        @DisplayName("PENDING 상태일 때 isPending()이 true 반환")
        void isPending_pendingStatus_returnsTrue() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            // when & then
            assertTrue(request.isPending());
        }

        @ParameterizedTest
        @EnumSource(value = LabCreationStatus.class, names = {"APPROVED", "REJECTED"})
        @DisplayName("PENDING이 아닌 상태일 때 isPending()이 false 반환")
        void isPending_nonPendingStatus_returnsFalse(LabCreationStatus status) {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildRequestWithStatus(status);

            // when & then
            assertFalse(request.isPending());
        }

        @Test
        @DisplayName("APPROVED 상태일 때 isApproved()가 true 반환")
        void isApproved_approvedStatus_returnsTrue() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildRequestWithStatus(LabCreationStatus.APPROVED);

            // when & then
            assertTrue(request.isApproved());
        }

        @Test
        @DisplayName("REJECTED 상태일 때 isRejected()가 true 반환")
        void isRejected_rejectedStatus_returnsTrue() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildRequestWithStatus(LabCreationStatus.REJECTED);

            // when & then
            assertTrue(request.isRejected());
        }
    }

    @Nested
    @DisplayName("소유권 검증 (isOwnedBy)")
    class OwnershipTests {

        @Test
        @DisplayName("동일한 신청자일 때 isOwnedBy()가 true 반환")
        void isOwnedBy_sameRequester_returnsTrue() {
            // given
            User requester = DomainUserFactory.buildStudentUser();
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);

            // when & then
            assertTrue(request.isOwnedBy(requester));
        }

        @Test
        @DisplayName("다른 사용자일 때 isOwnedBy()가 false 반환")
        void isOwnedBy_differentUser_returnsFalse() {
            // given
            User requester = DomainUserFactory.buildStudentUser();
            User otherUser = DomainUserFactory.buildStudentUser();
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);

            // when & then
            assertFalse(request.isOwnedBy(otherUser));
        }

        @Test
        @DisplayName("null 사용자일 때 isOwnedBy()가 false 반환")
        void isOwnedBy_nullUser_returnsFalse() {
            // given
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            // when & then
            assertFalse(request.isOwnedBy((User) null));
        }
    }

    @Nested
    @DisplayName("카테고리별 신청서 생성 테스트")
    class CategorySpecificTests {

        @ParameterizedTest
        @EnumSource(LabCategory.class)
        @DisplayName("모든 카테고리로 신청서 생성 가능")
        void createRequest_allCategories_successful(LabCategory category) {
            // given
            User requester = DomainUserFactory.buildStudentUser();

            // when
            LabCreationRequest request = new LabCreationRequest(
                    "TestLab", category, "Test description", requester
            );

            // then
            assertEquals(category, request.getRequestedCategory());
            assertEquals(LabCreationStatus.PENDING, request.getStatus());
        }

        @Test
        @DisplayName("AI 카테고리 신청서 생성")
        void createAiLabRequest_successful() {
            // given & when
            LabCreationRequest request = DomainLabCreationRequestFactory.buildAiLabRequest();

            // then
            assertEquals(LabCategory.AI, request.getRequestedCategory());
            assertTrue(request.getRequestedLabName().contains("AI"));
        }

        @Test
        @DisplayName("Database 카테고리 신청서 생성")
        void createDbLabRequest_successful() {
            // given & when
            LabCreationRequest request = DomainLabCreationRequestFactory.buildDbLabRequest();

            // then
            assertEquals(LabCategory.DB, request.getRequestedCategory());
            assertTrue(request.getRequestedLabName().contains("DB"));
        }

        @Test
        @DisplayName("Security 카테고리 신청서 생성")
        void createSecurityLabRequest_successful() {
            // given & when
            LabCreationRequest request = DomainLabCreationRequestFactory.buildSecurityLabRequest();

            // then
            assertEquals(LabCategory.SECURITY, request.getRequestedCategory());
            assertTrue(request.getRequestedLabName().contains("Sec"));
        }
    }
}
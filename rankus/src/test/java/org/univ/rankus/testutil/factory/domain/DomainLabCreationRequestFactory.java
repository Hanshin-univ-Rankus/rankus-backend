package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabCreationRequest;
import org.univ.rankus.domain.model.lab.LabCreationStatus;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * DomainLabCreationRequestFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 LabCreationRequest 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainLabCreationRequestFactory {
    private DomainLabCreationRequestFactory() {
    }

    public static LabCreationRequest buildValidPendingRequest() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest(
                "TestLab",
                LabCategory.AI,
                "Test lab",
                requester
        );
    }

    public static LabCreationRequest buildValidPendingRequestWithId(Long id) {
        LabCreationRequest request = buildValidPendingRequest();
        ReflectionTestUtils.setField(request, "id", id);
        return request;
    }

    public static LabCreationRequest buildValidPendingRequestWithRequester(User requester) {
        return new LabCreationRequest(
                "TestLab",
                LabCategory.AI,
                "Test lab",
                requester
        );
    }

    public static LabCreationRequest buildApprovedRequest(User approver) {
        LabCreationRequest request = buildValidPendingRequest();
        request.approve(approver);
        return request;
    }

    public static LabCreationRequest buildRejectedRequest(User rejector, String reason) {
        LabCreationRequest request = buildValidPendingRequest();
        request.reject(rejector, reason);
        return request;
    }

    public static LabCreationRequest buildApprovedRequestWithApprover() {
        User approver = DomainUserFactory.buildProfessorUser();
        return buildApprovedRequest(approver);
    }

    public static LabCreationRequest buildRejectedRequestWithRejector() {
        User rejector = DomainUserFactory.buildProfessorUser();
        return buildRejectedRequest(rejector, "Test reason");
    }

    public static LabCreationRequest buildRequestWithCustomData(String labName, LabCategory category, String description, User requester) {
        return new LabCreationRequest(labName, category, description, requester);
    }

    public static LabCreationRequest buildRequestWithCategory(LabCategory category) {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("TestLab", category, "Test desc", requester);
    }

    public static LabCreationRequest buildAiLabRequest() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("AI Lab", LabCategory.AI, "AI research", requester);
    }

    public static LabCreationRequest buildDbLabRequest() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("DB Lab", LabCategory.DB, "DB research", requester);
    }

    public static LabCreationRequest buildSecurityLabRequest() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("Sec Lab", LabCategory.SECURITY, "Sec research", requester);
    }

    // 검증 실패 케이스용 빌더들
    public static LabCreationRequest buildInvalidRequest_NoLabName() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("", LabCategory.AI, "Description", requester);
    }

    public static LabCreationRequest buildInvalidRequest_NullCategory() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("TestLab", null, "Description", requester);
    }

    public static LabCreationRequest buildInvalidRequest_NoDescription() {
        User requester = DomainUserFactory.buildStudentUser();
        return new LabCreationRequest("TestLab", LabCategory.AI, "", requester);
    }

    public static LabCreationRequest buildInvalidRequest_NullRequester() {
        return new LabCreationRequest("TestLab", LabCategory.AI, "Description", null);
    }

    public static LabCreationRequest buildInvalidRequest_LongLabName() {
        User requester = DomainUserFactory.buildStudentUser();
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 51) sb.append('a');
        return new LabCreationRequest(sb.toString(), LabCategory.AI, "Description", requester);
    }

    public static LabCreationRequest buildInvalidRequest_LongDescription() {
        User requester = DomainUserFactory.buildStudentUser();
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 1001) sb.append('a');
        return new LabCreationRequest("TestLab", LabCategory.AI, sb.toString(), requester);
    }

    // 상태 변경 테스트용 헬퍼 메서드들
    public static LabCreationRequest buildRequestWithStatus(LabCreationStatus status) {
        LabCreationRequest request = buildValidPendingRequest();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }

    public static LabCreationRequest buildRequestWithStatusAndApprover(LabCreationStatus status, User approver) {
        LabCreationRequest request = buildValidPendingRequestWithRequester(DomainUserFactory.buildStudentUser());
        ReflectionTestUtils.setField(request, "status", status);
        if (status == LabCreationStatus.APPROVED) {
            ReflectionTestUtils.setField(request, "approver", approver);
            ReflectionTestUtils.setField(request, "approvedAt", LocalDateTime.now());
        } else if (status == LabCreationStatus.REJECTED) {
            ReflectionTestUtils.setField(request, "rejector", approver);
            ReflectionTestUtils.setField(request, "rejectedAt", LocalDateTime.now());
            ReflectionTestUtils.setField(request, "rejectionReason", "Test reason");
        }
        return request;
    }

    // 권한 테스트용 빌더들
    public static LabCreationRequest buildRequestWithStudentRequester() {
        User requester = DomainUserFactory.buildStudentUser();
        return buildValidPendingRequestWithRequester(requester);
    }

    public static LabCreationRequest buildRequestWithProfessorRequester() {
        User requester = DomainUserFactory.buildProfessorUser();
        return buildValidPendingRequestWithRequester(requester);
    }

    public static LabCreationRequest buildRequestWithLabLeaderRequester() {
        User requester = DomainUserFactory.buildLabLeaderUser();
        return buildValidPendingRequestWithRequester(requester);
    }

    // 승인자 권한 테스트용
    public static User buildValidApprover() {
        return DomainUserFactory.buildProfessorUser();
    }

    public static User buildInvalidApprover_Student() {
        return DomainUserFactory.buildStudentUser();
    }

    public static User buildInvalidApprover_LabMember() {
        return DomainUserFactory.buildLabMemberUser();
    }
}
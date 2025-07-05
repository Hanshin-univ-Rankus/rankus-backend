package org.univ.rankus.testutil.factory.integration;

import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.user.User;

/**
 * IntegrationLabCreationRequestFactory - 통합 테스트용 LabCreationRequest 팩토리
 * Repository를 통해 실제 저장된 LabCreationRequest 엔티티를 생성합니다.
 */
public final class IntegrationLabCreationRequestFactory {
    private IntegrationLabCreationRequestFactory() {
    }

    public static LabCreationRequest persistValidPendingRequest(
            LabCreationRequestRepositoryPort repositoryPort,
            User requester) {
        LabCreationRequest request = new LabCreationRequest(
                "Test Lab",
                LabCategory.AI,
                "Test lab",
                requester
        );
        return repositoryPort.save(request);
    }

    public static LabCreationRequest persistCustomRequest(
            LabCreationRequestRepositoryPort repositoryPort,
            String labName,
            LabCategory category,
            String description,
            User requester) {
        LabCreationRequest request = new LabCreationRequest(labName, category, description, requester);
        return repositoryPort.save(request);
    }

    public static LabCreationRequest persistAiLabRequest(
            LabCreationRequestRepositoryPort repositoryPort,
            User requester) {
        return persistCustomRequest(
                repositoryPort,
                "AI Lab",
                LabCategory.AI,
                "AI research",
                requester
        );
    }

    public static LabCreationRequest persistDbLabRequest(
            LabCreationRequestRepositoryPort repositoryPort,
            User requester) {
        return persistCustomRequest(
                repositoryPort,
                "DB Lab",
                LabCategory.DB,
                "DB research",
                requester
        );
    }

    public static LabCreationRequest persistSecurityLabRequest(
            LabCreationRequestRepositoryPort repositoryPort,
            User requester) {
        return persistCustomRequest(
                repositoryPort,
                "Sec Lab",
                LabCategory.SECURITY,
                "Sec research",
                requester
        );
    }
}
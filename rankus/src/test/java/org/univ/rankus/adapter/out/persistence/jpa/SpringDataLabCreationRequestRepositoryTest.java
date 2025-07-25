package org.univ.rankus.adapter.out.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.univ.rankus.adapter.out.persistence.impl.LabCreationRequestRepositoryAdapter;
import org.univ.rankus.adapter.out.persistence.impl.UserRepositoryAdapter;
import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.integration.IntegrationLabCreationRequestFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationUserFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import({LabCreationRequestRepositoryAdapter.class, UserRepositoryAdapter.class})
@TestPropertySource(properties = "spring.jpa.show-sql=true")
@DisplayName("Spring Data JPA LabCreationRequestRepository 통합 테스트")
class SpringDataLabCreationRequestRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private LabCreationRequestRepositoryPort labCreationRequestRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Nested
    @DisplayName("기본 CRUD 연산")
    class BasicCrudTests {

        @Test
        @DisplayName("save: LabCreationRequest 저장 후 ID 자동 생성")
        void save_generatesId() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);

            // when
            LabCreationRequest saved = IntegrationLabCreationRequestFactory.persistValidPendingRequest(
                    labCreationRequestRepositoryPort, requester
            );

            // then
            assertNotNull(saved.getId(), "저장된 LabCreationRequest는 ID가 자동 생성되어야 한다");
            assertTrue(saved.getId() > 0, "생성된 ID는 양수여야 한다");
            assertEquals(LabCreationStatus.PENDING, saved.getStatus(), "기본 상태는 PENDING이어야 한다");
            assertEquals(requester.getId(), saved.getRequester().getId(), "신청자가 올바르게 설정되어야 한다");
        }

        @Test
        @DisplayName("findById: 저장된 LabCreationRequest 조회 성공")
        void findById_success() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            LabCreationRequest saved = IntegrationLabCreationRequestFactory.persistValidPendingRequest(
                    labCreationRequestRepositoryPort, requester
            );

            // when
            Optional<LabCreationRequest> found = labCreationRequestRepositoryPort.findById(saved.getId());

            // then
            assertTrue(found.isPresent(), "저장된 신청서를 찾을 수 있어야 한다");
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(saved.getRequestedLabName(), found.get().getRequestedLabName());
            assertEquals(saved.getRequester().getId(), found.get().getRequester().getId());
        }

        @Test
        @DisplayName("findById: 존재하지 않는 ID로 조회 시 Optional.empty() 반환")
        void findById_nonExistentId_returnsEmpty() {
            // given
            Long nonExistentId = 999L;

            // when
            Optional<LabCreationRequest> found = labCreationRequestRepositoryPort.findById(nonExistentId);

            // then
            assertTrue(found.isEmpty(), "존재하지 않는 ID로 조회 시 Optional.empty()를 반환해야 한다");
        }

        @Test
        @DisplayName("delete: 신청서 삭제 후 조회 불가")
        void delete_removesFromDatabase() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            LabCreationRequest saved = IntegrationLabCreationRequestFactory.persistValidPendingRequest(
                    labCreationRequestRepositoryPort, requester
            );

            // when
            labCreationRequestRepositoryPort.delete(saved);

            // then
            Optional<LabCreationRequest> found = labCreationRequestRepositoryPort.findById(saved.getId());
            assertTrue(found.isEmpty(), "삭제된 신청서는 조회되지 않아야 한다");
        }
    }

    @Nested
    @DisplayName("상태별 조회")
    class FindByStatusTests {

        @Test
        @DisplayName("findByStatusOrderByCreatedAtDesc: PENDING 상태 신청서들을 생성일 내림차순으로 조회")
        void findByStatusOrderByCreatedAtDesc_pending_success() {
            // given
            User requester1 = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            User requester2 = IntegrationUserFactory.persistCustomUser(userRepositoryPort, "User2", "user2@hs.ac.kr", "Password!23");

            LabCreationRequest request1 = IntegrationLabCreationRequestFactory.persistValidPendingRequest(
                    labCreationRequestRepositoryPort, requester1
            );
            LabCreationRequest request2 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "DB Lab", LabCategory.DB, "Database research lab", requester2
            );

            // when
            List<LabCreationRequest> pendingRequests = labCreationRequestRepositoryPort
                    .findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING);

            // then
            assertEquals(2, pendingRequests.size(), "PENDING 상태의 신청서 2개가 조회되어야 한다");

            // 모든 신청서가 PENDING 상태인지 확인
            pendingRequests.forEach(request ->
                    assertEquals(LabCreationStatus.PENDING, request.getStatus())
            );

            // 생성일 내림차순으로 정렬되어 있는지 확인 (최신 순)
            assertTrue(
                    pendingRequests.get(0).getCreatedAt().isAfter(pendingRequests.get(1).getCreatedAt()) ||
                            pendingRequests.get(0).getCreatedAt().isEqual(pendingRequests.get(1).getCreatedAt()),
                    "생성일 내림차순으로 정렬되어야 한다"
            );
        }

        @Test
        @DisplayName("findByStatusOrderByCreatedAtDesc: 해당 상태의 신청서가 없을 때 빈 리스트 반환")
        void findByStatusOrderByCreatedAtDesc_noResults_returnsEmptyList() {
            // when
            List<LabCreationRequest> approvedRequests = labCreationRequestRepositoryPort
                    .findByStatusOrderByCreatedAtDesc(LabCreationStatus.APPROVED);

            // then
            assertTrue(approvedRequests.isEmpty(), "해당 상태의 신청서가 없을 때 빈 리스트를 반환해야 한다");
        }
    }

    @Nested
    @DisplayName("신청자별 조회")
    class FindByRequesterTests {

        @Test
        @DisplayName("findByRequesterOrderByCreatedAtDesc: 특정 신청자의 모든 신청서를 생성일 내림차순으로 조회")
        void findByRequesterOrderByCreatedAtDesc_success() {
            // given
            User requester1 = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            User requester2 = IntegrationUserFactory.persistCustomUser(userRepositoryPort, "User2", "user2@hs.ac.kr", "Password!23");

            // requester1의 신청서 2개 생성
            LabCreationRequest request1 = IntegrationLabCreationRequestFactory.persistValidPendingRequest(
                    labCreationRequestRepositoryPort, requester1
            );
            LabCreationRequest request2 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "AISC", LabCategory.SECURITY, "Security research lab", requester1
            );

            // requester2의 신청서 1개 생성
            IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "DB Lab", LabCategory.DB, "Database research lab", requester2
            );

            // when
            List<LabCreationRequest> requester1Requests = labCreationRequestRepositoryPort
                    .findByRequesterOrderByCreatedAtDesc(requester1);

            // then
            assertEquals(2, requester1Requests.size(), "requester1의 신청서 2개가 조회되어야 한다");

            // 모든 신청서의 신청자가 requester1인지 확인
            requester1Requests.forEach(request ->
                    assertEquals(requester1.getId(), request.getRequester().getId())
            );

            // 생성일 내림차순으로 정렬되어 있는지 확인
            assertTrue(
                    requester1Requests.get(0).getCreatedAt().isAfter(requester1Requests.get(1).getCreatedAt()) ||
                            requester1Requests.get(0).getCreatedAt().isEqual(requester1Requests.get(1).getCreatedAt()),
                    "생성일 내림차순으로 정렬되어야 한다"
            );
        }

        @Test
        @DisplayName("findByRequesterOrderByCreatedAtDesc: 신청서가 없는 신청자 조회 시 빈 리스트 반환")
        void findByRequesterOrderByCreatedAtDesc_noRequests_returnsEmptyList() {
            // given
            User requesterWithoutRequests = IntegrationUserFactory.persistValidUser(userRepositoryPort);

            // when
            List<LabCreationRequest> requests = labCreationRequestRepositoryPort
                    .findByRequesterOrderByCreatedAtDesc(requesterWithoutRequests);

            // then
            assertTrue(requests.isEmpty(), "신청서가 없는 신청자 조회 시 빈 리스트를 반환해야 한다");
        }
    }

    @Nested
    @DisplayName("전체 조회")
    class FindAllTests {

        @Test
        @DisplayName("findAllByCreatedAtDesc: 모든 신청서를 생성일 내림차순으로 조회")
        void findAllByCreatedAtDesc_success() {
            // given
            User requester1 = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            User requester2 = IntegrationUserFactory.persistCustomUser(userRepositoryPort, "User2", "user2@hs.ac.kr", "Password!23");

            LabCreationRequest request1 = IntegrationLabCreationRequestFactory.persistValidPendingRequest(
                    labCreationRequestRepositoryPort, requester1
            );
            LabCreationRequest request2 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "DB Lab", LabCategory.DB, "Database research lab", requester2
            );
            LabCreationRequest request3 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "AISC", LabCategory.SECURITY, "Security research lab", requester1
            );

            // when
            List<LabCreationRequest> allRequests = labCreationRequestRepositoryPort.findAllByCreatedAtDesc();

            // then
            assertEquals(3, allRequests.size(), "저장된 모든 신청서 3개가 조회되어야 한다");

            // 생성일 내림차순으로 정렬되어 있는지 확인
            for (int i = 0; i < allRequests.size() - 1; i++) {
                assertTrue(
                        allRequests.get(i).getCreatedAt().isAfter(allRequests.get(i + 1).getCreatedAt()) ||
                                allRequests.get(i).getCreatedAt().isEqual(allRequests.get(i + 1).getCreatedAt()),
                        "모든 신청서가 생성일 내림차순으로 정렬되어야 한다"
                );
            }
        }

        @Test
        @DisplayName("findAllByCreatedAtDesc: 신청서가 없을 때 빈 리스트 반환")
        void findAllByCreatedAtDesc_empty_returnsEmptyList() {
            // when
            List<LabCreationRequest> allRequests = labCreationRequestRepositoryPort.findAllByCreatedAtDesc();

            // then
            assertTrue(allRequests.isEmpty(), "신청서가 없을 때 빈 리스트를 반환해야 한다");
        }
    }

    @Nested
    @DisplayName("중복 검사")
    class DuplicateCheckTests {

        @Test
        @DisplayName("existsByRequesterAndRequestedLabNameAndStatus: 동일한 신청자가 동일한 랩실 이름으로 대기 중인 신청이 있을 때 true 반환")
        void existsByRequesterAndRequestedLabNameAndStatus_duplicateExists_returnsTrue() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            String labName = "AISC";

            IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, labName, LabCategory.AI, "AI research lab", requester
            );

            // when
            boolean exists = labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                    requester, labName, LabCreationStatus.PENDING
            );

            // then
            assertTrue(exists, "동일한 신청자가 동일한 랩실 이름으로 대기 중인 신청이 있을 때 true를 반환해야 한다");
        }

        @Test
        @DisplayName("existsByRequesterAndRequestedLabNameAndStatus: 중복이 없을 때 false 반환")
        void existsByRequesterAndRequestedLabNameAndStatus_noDuplicate_returnsFalse() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            String labName = "Non-existent Lab";

            // when
            boolean exists = labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                    requester, labName, LabCreationStatus.PENDING
            );

            // then
            assertFalse(exists, "중복이 없을 때 false를 반환해야 한다");
        }

        @Test
        @DisplayName("existsByRequestedLabNameAndStatus: 동일한 랩실 이름으로 대기 중인 신청이 있을 때 true 반환")
        void existsByRequestedLabNameAndStatus_duplicateExists_returnsTrue() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            String labName = "AISC";

            IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, labName, LabCategory.AI, "Popular research lab", requester
            );

            // when
            boolean exists = labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(
                    labName, LabCreationStatus.PENDING
            );

            // then
            assertTrue(exists, "동일한 랩실 이름으로 대기 중인 신청이 있을 때 true를 반환해야 한다");
        }

        @Test
        @DisplayName("existsByRequestedLabNameAndStatus: 중복이 없을 때 false 반환")
        void existsByRequestedLabNameAndStatus_noDuplicate_returnsFalse() {
            // given
            String labName = "Unique Lab Name";

            // when
            boolean exists = labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(
                    labName, LabCreationStatus.PENDING
            );

            // then
            assertFalse(exists, "중복이 없을 때 false를 반환해야 한다");
        }

        @Test
        @DisplayName("existsByRequestedLabNameAndStatus: 동일한 이름이지만 다른 상태일 때 false 반환")
        void existsByRequestedLabNameAndStatus_sameNameDifferentStatus_returnsFalse() {
            // given
            User requester = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            String labName = "Test Lab";

            // PENDING 상태의 신청서 생성
            IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, labName, LabCategory.AI, "Test lab", requester
            );

            // when - APPROVED 상태로 검색
            boolean exists = labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(
                    labName, LabCreationStatus.APPROVED
            );

            // then
            assertFalse(exists, "동일한 이름이지만 다른 상태일 때 false를 반환해야 한다");
        }
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTests {

        @Test
        @DisplayName("여러 신청자가 다양한 상태의 신청서를 가진 경우 조회 정확성 검증")
        void complexScenario_multipleUsersAndStatuses() {
            // given
            User requester1 = IntegrationUserFactory.persistValidUser(userRepositoryPort);
            User requester2 = IntegrationUserFactory.persistCustomUser(userRepositoryPort, "User2", "user2@hs.ac.kr", "Password!23");
            User requester3 = IntegrationUserFactory.persistCustomUser(userRepositoryPort, "User3", "user3@hs.ac.kr", "Password!23");

            // 다양한 신청서 생성
            LabCreationRequest request1 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "AI Lab", LabCategory.AI, "AI research lab", requester1
            );
            LabCreationRequest request2 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "DB Lab", LabCategory.DB, "DB research lab", requester2
            );
            LabCreationRequest request3 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "DELL", LabCategory.SECURITY, "Security research lab", requester3
            );
            LabCreationRequest request4 = IntegrationLabCreationRequestFactory.persistCustomRequest(
                    labCreationRequestRepositoryPort, "AISC", LabCategory.AI, "Network research lab", requester1
            );

            // when & then
            List<LabCreationRequest> allRequests = labCreationRequestRepositoryPort.findAllByCreatedAtDesc();
            assertEquals(4, allRequests.size(), "전체 신청서 4개가 조회되어야 한다");

            List<LabCreationRequest> pendingRequests = labCreationRequestRepositoryPort
                    .findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING);
            assertEquals(4, pendingRequests.size(), "모든 신청서가 PENDING 상태여야 한다");

            List<LabCreationRequest> requester1Requests = labCreationRequestRepositoryPort
                    .findByRequesterOrderByCreatedAtDesc(requester1);
            assertEquals(2, requester1Requests.size(), "requester1의 신청서 2개가 조회되어야 한다");

            List<LabCreationRequest> requester2Requests = labCreationRequestRepositoryPort
                    .findByRequesterOrderByCreatedAtDesc(requester2);
            assertEquals(1, requester2Requests.size(), "requester2의 신청서 1개가 조회되어야 한다");

            // 중복 검사
            assertTrue(labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                    requester1, "AI Lab", LabCreationStatus.PENDING
            ), "requester1의 AI Lab 신청서가 존재해야 한다");

            assertTrue(labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(
                    "DB Lab", LabCreationStatus.PENDING
            ), "DB Lab 신청서가 존재해야 한다");
        }
    }
}
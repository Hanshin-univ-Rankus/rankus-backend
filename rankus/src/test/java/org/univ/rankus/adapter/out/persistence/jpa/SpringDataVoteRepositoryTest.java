package org.univ.rankus.adapter.out.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.adapter.out.persistence.impl.VoteRepositoryAdapter;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.application.port.out.VoteRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.integration.IntegrationLabFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationUserFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationVoteFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import({VoteRepositoryAdapter.class,
        org.univ.rankus.adapter.out.persistence.impl.LabRepositoryAdapter.class,
        org.univ.rankus.adapter.out.persistence.impl.UserRepositoryAdapter.class})
@DisplayName("Spring Data JPA VoteRepository 통합 테스트")
class SpringDataVoteRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private VoteRepositoryPort voteRepo;

    @Autowired
    private LabRepositoryPort labRepo;

    @Autowired
    private UserRepositoryPort userRepo;

    @Test
    @DisplayName("save: Vote 저장 후 ID 자동 생성")
    void save_generatesId() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);
        Vote vote = IntegrationVoteFactory.persistCustomVote(voteRepo, "테스트 투표", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);

        // then
        assertNotNull(vote.getId(), "저장된 Vote는 ID가 자동 생성되어야 한다");
        assertTrue(vote.getId() > 0, "생성된 ID는 양수여야 한다");
    }

    @Test
    @DisplayName("findById: 존재하는 투표를 조회할 수 있다")
    void findById_existingVote_returnsVote() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);
        Vote saved = IntegrationVoteFactory.persistCustomVote(voteRepo, "테스트 투표", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);

        // when
        Optional<Vote> found = voteRepo.findById(saved.getId());

        // then
        assertTrue(found.isPresent(), "저장된 투표를 찾을 수 있어야 한다");
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("findById: 존재하지 않는 투표 조회 시 Optional.empty 반환")
    void findById_nonExistingVote_returnsEmpty() {
        // when
        Optional<Vote> found = voteRepo.findById(999L);

        // then
        assertFalse(found.isPresent(), "존재하지 않는 투표는 Optional.empty를 반환해야 한다");
    }

    @Test
    @DisplayName("findByLabIdOrderByCreatedAtDesc: 랩실의 투표를 최신순으로 조회")
    void findByLabIdOrderByCreatedAtDesc_returnsVotesInOrder() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);

        Vote vote1 = IntegrationVoteFactory.persistCustomVote(voteRepo, "첫 번째 투표", "설명1",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        Vote vote2 = IntegrationVoteFactory.persistCustomVote(voteRepo, "두 번째 투표", "설명2",
                java.time.LocalDateTime.now().plusDays(14), creator, lab);

        // when
        List<Vote> votes = voteRepo.findByLabIdOrderByCreatedAtDesc(lab.getId());

        // then
        assertEquals(2, votes.size(), "랩실의 투표 2개가 조회되어야 한다");
        // 최신순 정렬 확인 (vote2가 먼저)
        assertTrue(votes.get(0).getCreatedAt().isAfter(votes.get(1).getCreatedAt()) ||
                        votes.get(0).getCreatedAt().isEqual(votes.get(1).getCreatedAt()),
                "최신순으로 정렬되어야 한다");
    }

    @Test
    @DisplayName("findByLabIdOrderByCreatedAtDesc with Pageable: 페이징된 투표 목록 조회")
    void findByLabIdOrderByCreatedAtDesc_withPageable_returnsPagedVotes() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);

        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표1", "설명1",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표2", "설명2",
                java.time.LocalDateTime.now().plusDays(14), creator, lab);
        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표3", "설명3",
                java.time.LocalDateTime.now().plusDays(21), creator, lab);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Vote> votePage = voteRepo.findByLabIdOrderByCreatedAtDesc(lab.getId(), pageable);

        // then
        assertEquals(2, votePage.getContent().size(), "페이지 크기만큼 조회되어야 한다");
        assertEquals(3, votePage.getTotalElements(), "전체 요소 수가 정확해야 한다");
        assertEquals(2, votePage.getTotalPages(), "전체 페이지 수가 정확해야 한다");
    }

    @Test
    @DisplayName("findByLabIdAndStatus: 특정 상태의 투표만 조회")
    void findByLabIdAndStatus_returnsVotesWithSpecificStatus() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);

        Vote activeVote = IntegrationVoteFactory.persistCustomVote(voteRepo, "활성 투표", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        Vote closedVote = IntegrationVoteFactory.persistCustomVote(voteRepo, "종료된 투표", "설명",
                java.time.LocalDateTime.now().plusDays(14), creator, lab);
        closedVote.close();
        voteRepo.save(closedVote);

        // when
        List<Vote> activeVotes = voteRepo.findByLabIdAndStatus(lab.getId(), VoteStatus.ACTIVE);

        // then
        assertEquals(1, activeVotes.size(), "ACTIVE 상태의 투표만 조회되어야 한다");
        assertEquals(VoteStatus.ACTIVE, activeVotes.get(0).getStatus());
    }

    @Test
    @DisplayName("findByCreatorId: 특정 사용자가 생성한 투표 조회")
    void findByCreatorId_returnsVotesCreatedByUser() {
        // given
        User creator1 = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);
        User creator2 = IntegrationUserFactory.persistUserWithRole(userRepo, Role.PROFESSOR);
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);

        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표1", "설명1",
                java.time.LocalDateTime.now().plusDays(7), creator1, lab);
        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표2", "설명2",
                java.time.LocalDateTime.now().plusDays(14), creator1, lab);
        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표3", "설명3",
                java.time.LocalDateTime.now().plusDays(21), creator2, lab);

        // when
        List<Vote> creator1Votes = voteRepo.findByCreatorId(creator1.getId());

        // then
        assertEquals(2, creator1Votes.size(), "creator1이 생성한 투표 2개가 조회되어야 한다");
        creator1Votes.forEach(vote ->
                assertEquals(creator1.getId(), vote.getCreator().getId()));
    }

    @Test
    @DisplayName("findActiveVotesByLabId: 랩실의 활성 투표만 조회")
    void findActiveVotesByLabId_returnsOnlyActiveVotes() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);

        Vote activeVote = IntegrationVoteFactory.persistCustomVote(voteRepo, "활성 투표", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        Vote closedVote = IntegrationVoteFactory.persistCustomVote(voteRepo, "종료된 투표", "설명",
                java.time.LocalDateTime.now().plusDays(14), creator, lab);
        closedVote.close();
        voteRepo.save(closedVote);

        // when
        List<Vote> activeVotes = voteRepo.findActiveVotesByLabId(lab.getId());

        // then
        assertEquals(1, activeVotes.size(), "활성 투표만 조회되어야 한다");
        assertEquals(VoteStatus.ACTIVE, activeVotes.get(0).getStatus());
    }

    @Test
    @DisplayName("countByLabId: 랩실의 총 투표 수 조회")
    void countByLabId_returnsTotalVoteCount() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);

        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표1", "설명1",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        IntegrationVoteFactory.persistCustomVote(voteRepo, "투표2", "설명2",
                java.time.LocalDateTime.now().plusDays(14), creator, lab);

        // when
        long count = voteRepo.countByLabId(lab.getId());

        // then
        assertEquals(2, count, "랩실의 총 투표 수가 정확해야 한다");
    }

    @Test
    @DisplayName("countByLabIdAndStatus: 특정 상태의 투표 수 조회")
    void countByLabIdAndStatus_returnsCountForSpecificStatus() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);

        IntegrationVoteFactory.persistCustomVote(voteRepo, "활성 투표1", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        IntegrationVoteFactory.persistCustomVote(voteRepo, "활성 투표2", "설명",
                java.time.LocalDateTime.now().plusDays(14), creator, lab);
        Vote closedVote = IntegrationVoteFactory.persistCustomVote(voteRepo, "종료된 투표", "설명",
                java.time.LocalDateTime.now().plusDays(21), creator, lab);
        closedVote.close();
        voteRepo.save(closedVote);

        // when
        long activeCount = voteRepo.countByLabIdAndStatus(lab.getId(), VoteStatus.ACTIVE);

        // then
        assertEquals(2, activeCount, "ACTIVE 상태의 투표 수가 정확해야 한다");
    }

    @Test
    @DisplayName("deleteById: 투표 삭제")
    void deleteById_removesVote() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);
        Vote saved = IntegrationVoteFactory.persistCustomVote(voteRepo, "테스트 투표", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);
        Long voteId = saved.getId();

        // when
        voteRepo.deleteById(voteId);

        // then
        Optional<Vote> found = voteRepo.findById(voteId);
        assertFalse(found.isPresent(), "삭제된 투표는 조회되지 않아야 한다");
    }

    @Test
    @DisplayName("existsById: 투표 존재 여부 확인")
    void existsById_checksVoteExistence() {
        // given
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        User creator = IntegrationUserFactory.persistUserWithRole(userRepo, Role.STUDENT);
        Vote saved = IntegrationVoteFactory.persistCustomVote(voteRepo, "테스트 투표", "설명",
                java.time.LocalDateTime.now().plusDays(7), creator, lab);

        // when & then
        assertTrue(voteRepo.existsById(saved.getId()), "저장된 투표는 존재해야 한다");
        assertFalse(voteRepo.existsById(999L), "존재하지 않는 투표는 false를 반환해야 한다");
    }
}
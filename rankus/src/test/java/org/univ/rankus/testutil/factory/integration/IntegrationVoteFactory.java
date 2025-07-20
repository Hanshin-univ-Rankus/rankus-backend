package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.VoteRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * IntegrationVoteFactory - Repository/통합 테스트 전용 Vote 테스트 팩토리
 * - DomainVoteFactory.build*() 를 호출하여 Vote 생성 후, 영속화 기능(persist)만 제공합니다.
 */
public final class IntegrationVoteFactory {
    private IntegrationVoteFactory() {
    }

    public static Vote persistValidVote(VoteRepositoryPort repo) {
        Vote vote = DomainVoteFactory.buildValidVote();
        return repo.save(vote);
    }

    public static Vote persistValidVote(TestEntityManager em) {
        Vote vote = DomainVoteFactory.buildValidVote();
        em.persist(vote);
        em.flush();
        return vote;
    }

    public static Vote persistActiveVote(VoteRepositoryPort repo) {
        Vote vote = DomainVoteFactory.buildActiveVote();
        return repo.save(vote);
    }

    public static Vote persistActiveVote(TestEntityManager em) {
        Vote vote = DomainVoteFactory.buildActiveVote();
        em.persist(vote);
        em.flush();
        return vote;
    }

    public static Vote persistClosedVote(VoteRepositoryPort repo) {
        Vote vote = DomainVoteFactory.buildClosedVote();
        return repo.save(vote);
    }

    public static Vote persistClosedVote(TestEntityManager em) {
        Vote vote = DomainVoteFactory.buildClosedVote();
        em.persist(vote);
        em.flush();
        return vote;
    }

    public static Vote persistCanceledVote(VoteRepositoryPort repo) {
        Vote vote = DomainVoteFactory.buildCanceledVote();
        return repo.save(vote);
    }

    public static Vote persistCanceledVote(TestEntityManager em) {
        Vote vote = DomainVoteFactory.buildCanceledVote();
        em.persist(vote);
        em.flush();
        return vote;
    }

    public static Vote persistCustomVote(VoteRepositoryPort repo, String title, String description,
                                         LocalDateTime deadline, User creator, Lab lab) {
        Vote vote = DomainVoteFactory.buildCustomVote(title, description, deadline, Arrays.asList("옵션1", "옵션2"), creator, lab);
        return repo.save(vote);
    }

    public static Vote persistCustomVote(TestEntityManager em, String title, String description,
                                         LocalDateTime deadline, User creator, Lab lab) {
        Vote vote = DomainVoteFactory.buildCustomVote(title, description, deadline, Arrays.asList("옵션1", "옵션2"), creator, lab);
        em.persist(vote);
        em.flush();
        return vote;
    }

    public static Vote persistVoteWithStatus(VoteRepositoryPort repo, VoteStatus status) {
        Vote vote = DomainVoteFactory.buildValidVote();
        if (status == VoteStatus.CLOSED) {
            vote.close();
        } else if (status == VoteStatus.CANCELED) {
            vote.cancel();
        }
        return repo.save(vote);
    }

    public static Vote persistVoteWithStatus(TestEntityManager em, VoteStatus status) {
        Vote vote = DomainVoteFactory.buildValidVote();
        if (status == VoteStatus.CLOSED) {
            vote.close();
        } else if (status == VoteStatus.CANCELED) {
            vote.cancel();
        }
        em.persist(vote);
        em.flush();
        return vote;
    }
}
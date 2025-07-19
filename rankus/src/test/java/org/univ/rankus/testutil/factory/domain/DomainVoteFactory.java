package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteOption;
import org.univ.rankus.domain.model.vote.VoteParticipation;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * DomainVoteFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 Vote 관련 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainVoteFactory {
    private DomainVoteFactory() {
    }

    private static long voteIdCounter = 1000L;

    public static Vote buildValidVote() {
        User creator = DomainUserFactory.buildValidUserWithId(1L);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        
        Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);
        vote.setOptions(Arrays.asList("선택지 1", "선택지 2"));
        return vote;
    }

    public static Vote buildValidVoteWithId(Long id) {
        Vote vote = buildValidVote();
        ReflectionTestUtils.setField(vote, "id", id);
        return vote;
    }

    public static Vote buildVoteWithCreator(User creator) {
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        
        Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);
        vote.setOptions(Arrays.asList("선택지 1", "선택지 2"));
        return vote;
    }

    public static Vote buildVoteWithLab(Lab lab) {
        User creator = DomainUserFactory.buildValidUserWithId(1L);
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        
        Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);
        vote.setOptions(Arrays.asList("선택지 1", "선택지 2"));
        return vote;
    }

    public static Vote buildVoteWithCreatorAndLab(User creator, Lab lab) {
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        
        Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);
        vote.setOptions(Arrays.asList("선택지 1", "선택지 2"));
        return vote;
    }

    public static Vote buildVoteWithDeadline(LocalDateTime deadline) {
        User creator = DomainUserFactory.buildValidUserWithId(1L);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        
        Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);
        vote.setOptions(Arrays.asList("선택지 1", "선택지 2"));
        return vote;
    }

    public static Vote buildVoteWithOptions(String... options) {
        User creator = DomainUserFactory.buildValidUserWithId(1L);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        
        Vote vote = new Vote(creator, lab, "투표 제목", "투표 설명", deadline);
        vote.setOptions(Arrays.asList(options));
        return vote;
    }

    public static Vote buildExpiredVote() {
        User creator = DomainUserFactory.buildValidUserWithId(1L);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(1);
        
        return buildVoteWithDeadline(pastDeadline);
    }

    public static Vote buildActiveVote() {
        return buildValidVote();
    }

    public static Vote buildClosedVote() {
        Vote vote = buildValidVote();
        vote.close();
        return vote;
    }

    public static Vote buildCanceledVote() {
        Vote vote = buildValidVote();
        vote.cancel();
        return vote;
    }

    public static VoteOption buildValidVoteOption() {
        Vote vote = buildValidVoteWithId(generateVoteId());
        return new VoteOption(vote, "선택지 내용", 1);
    }

    public static VoteOption buildVoteOptionWithVote(Vote vote) {
        return new VoteOption(vote, "선택지 내용", 1);
    }

    public static VoteOption buildVoteOptionWithText(String optionText) {
        Vote vote = buildValidVoteWithId(generateVoteId());
        return new VoteOption(vote, optionText, 1);
    }

    public static VoteOption buildVoteOptionWithOrder(Integer order) {
        Vote vote = buildValidVoteWithId(generateVoteId());
        return new VoteOption(vote, "선택지 내용", order);
    }

    public static VoteParticipation buildValidVoteParticipation() {
        Vote vote = buildValidVoteWithId(generateVoteId());
        User participant = DomainUserFactory.buildValidUserWithId(2L);
        VoteOption option = buildVoteOptionWithVote(vote);
        
        return new VoteParticipation(vote, participant, option);
    }

    public static VoteParticipation buildVoteParticipationWithVote(Vote vote) {
        User participant = DomainUserFactory.buildValidUserWithId(2L);
        VoteOption option = buildVoteOptionWithVote(vote);
        
        return new VoteParticipation(vote, participant, option);
    }

    public static VoteParticipation buildVoteParticipationWithUser(User participant) {
        Vote vote = buildValidVoteWithId(generateVoteId());
        VoteOption option = buildVoteOptionWithVote(vote);
        
        return new VoteParticipation(vote, participant, option);
    }

    public static VoteParticipation buildVoteParticipationWithOption(VoteOption option) {
        User participant = DomainUserFactory.buildValidUserWithId(2L);
        
        return new VoteParticipation(option.getVote(), participant, option);
    }

    public static Vote buildCustomVote(String title, String description, LocalDateTime deadline, 
                                     java.util.List<String> optionTexts, User creator, Lab lab) {
        Vote vote = new Vote(creator, lab, title, description, deadline);
        vote.setOptions(optionTexts);
        return vote;
    }

    private static Long generateVoteId() {
        return voteIdCounter++;
    }
}
package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.VoteCommandUseCase;
import org.univ.rankus.application.port.out.*;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteOption;
import org.univ.rankus.domain.model.vote.VoteParticipation;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteNotFoundException;
import org.univ.rankus.domain.model.vote.exception.VotePermissionException;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteCommandService implements VoteCommandUseCase {

    private final VoteRepositoryPort voteRepositoryPort;
    private final VoteOptionRepositoryPort voteOptionRepositoryPort;
    private final VoteParticipationRepositoryPort voteParticipationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    @Override
    public Vote createVote(String title, String description, Long creatorId, Long labId, LocalDateTime deadline, List<String> optionTexts) {
        // 1) 생성자 검증
        User creator = userRepositoryPort.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2) 랩실 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3) 권한 검증
        if (!canCreateVote(creator, lab)) {
            throw new VotePermissionException(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED);
        }

        // 4) 투표 생성
        Vote vote = new Vote(creator, lab, title, description, deadline);
        vote.setOptions(optionTexts);

        // 5) 투표 저장
        return voteRepositoryPort.save(vote);
    }

    @Override
    public Vote createVote(String title, Long creatorId, Long labId, LocalDateTime deadline, List<String> optionTexts) {
        return createVote(title, null, creatorId, labId, deadline, optionTexts);
    }

    @Override
    public void deleteVote(Long voteId) {
        // 1) 투표 조회
        Vote vote = voteRepositoryPort.findById(voteId)
                .orElseThrow(() -> new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND));

        // 2) 삭제 가능 여부 확인
        if (!vote.canBeDeleted()) {
            throw new VoteValidationException(VoteErrorCode.VOTE_ALREADY_PARTICIPATED);
        }

        // 3) 관련 데이터 삭제 (투표 참여 기록, 선택지)
        voteParticipationRepositoryPort.deleteByVoteId(voteId);
        voteOptionRepositoryPort.deleteByVoteId(voteId);

        // 4) 투표 삭제
        voteRepositoryPort.deleteById(voteId);
    }

    @Override
    public Vote closeVote(Long voteId) {
        // 1) 투표 조회
        Vote vote = voteRepositoryPort.findById(voteId)
                .orElseThrow(() -> new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND));

        // 2) 투표 종료
        vote.close();

        // 3) 변경사항 저장
        return voteRepositoryPort.save(vote);
    }

    @Override
    public Vote cancelVote(Long voteId) {
        // 1) 투표 조회
        Vote vote = voteRepositoryPort.findById(voteId)
                .orElseThrow(() -> new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND));

        // 2) 투표 취소
        vote.cancel();

        // 3) 변경사항 저장
        return voteRepositoryPort.save(vote);
    }

    @Override
    public VoteParticipation participateInVote(Long voteId, Long userId, Long selectedOptionId) {
        // 1) 투표 조회
        Vote vote = voteRepositoryPort.findById(voteId)
                .orElseThrow(() -> new VoteNotFoundException(VoteErrorCode.VOTE_NOT_FOUND));

        // 2) 사용자 조회
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 선택지 조회
        VoteOption selectedOption = voteOptionRepositoryPort.findById(selectedOptionId)
                .orElseThrow(() -> new VoteNotFoundException(VoteErrorCode.VOTE_OPTION_NOT_FOUND));

        // 4) 권한 검증
        if (!vote.canUserParticipate(user)) {
            throw new VotePermissionException(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED);
        }

        // 5) 중복 참여 검증
        if (voteParticipationRepositoryPort.existsByVoteIdAndUserId(voteId, userId)) {
            throw new VoteValidationException(VoteErrorCode.VOTE_ALREADY_PARTICIPATED);
        }

        // 6) 투표 참여 가능 여부 확인
        if (!vote.canParticipate()) {
            if (vote.isExpired()) {
                throw new VoteValidationException(VoteErrorCode.VOTE_DEADLINE_PAST);
            } else if (vote.getStatus().isClosed()) {
                throw new VoteValidationException(VoteErrorCode.VOTE_ALREADY_CLOSED);
            } else if (vote.getStatus().isCanceled()) {
                throw new VoteValidationException(VoteErrorCode.VOTE_ALREADY_CANCELED);
            }
        }

        // 7) 참여 기록 생성
        VoteParticipation participation = new VoteParticipation(vote, user, selectedOption);

        // 8) 투표 총 참여 수 증가
        vote.incrementTotalVotes();

        // 9) 저장
        voteRepositoryPort.save(vote);
        return voteParticipationRepositoryPort.save(participation);
    }

    /**
     * 투표 생성 권한 확인
     */
    private boolean canCreateVote(User creator, Lab lab) {
        // 해당 랩실의 LAB_LEADER, LAB_MANAGER, PROFESSOR, ADMIN만 투표 생성 가능
        return creator.getLab() != null
                && creator.getLab().equals(lab)
                && (creator.getRole().name().equals("LAB_LEADER")
                || creator.getRole().name().equals("LAB_MANAGER")
                || creator.getRole().name().equals("PROFESSOR")
                || creator.getRole().name().equals("ADMIN"));
    }
}
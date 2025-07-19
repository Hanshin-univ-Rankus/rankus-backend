package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.univ.rankus.application.port.out.*;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.vote.Vote;
import org.univ.rankus.domain.model.vote.VoteOption;
import org.univ.rankus.domain.model.vote.VoteParticipation;
import org.univ.rankus.domain.model.vote.VoteStatus;
import org.univ.rankus.domain.model.vote.exception.VoteErrorCode;
import org.univ.rankus.domain.model.vote.exception.VoteNotFoundException;
import org.univ.rankus.domain.model.vote.exception.VotePermissionException;
import org.univ.rankus.domain.model.vote.exception.VoteValidationException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.factory.domain.DomainVoteFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VoteCommandService 단위 테스트")
class VoteCommandServiceTest {

    @Mock
    private VoteRepositoryPort voteRepositoryPort;

    @Mock
    private VoteOptionRepositoryPort voteOptionRepositoryPort;

    @Mock
    private VoteParticipationRepositoryPort voteParticipationRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private VoteCommandService service;

    private static final Long VOTE_ID = 123L;
    private static final Long LAB_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Long OPTION_ID = 99L;

    @Nested
    @DisplayName("createVote 메서드는")
    class CreateVoteTests {

        @Test
        @DisplayName("유효한 정보로 투표를 생성한다")
        void createVoteSuccess() {
            // given
            User creator = DomainUserFactory.buildLabManagerUser();
            Lab lab = DomainLabFactory.buildAiLab();
            creator.assignLab(lab);
            Vote savedVote = DomainVoteFactory.buildValidVote();
            LocalDateTime deadline = LocalDateTime.now().plusDays(7);

            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(creator));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.of(lab));
            when(voteRepositoryPort.save(any(Vote.class))).thenReturn(savedVote);

            // when
            Vote result = service.createVote(
                    "투표 제목",
                    "투표 설명",
                    USER_ID,
                    LAB_ID,
                    deadline,
                    Arrays.asList("선택지1", "선택지2")
            );

            // then
            assertThat(result).isNotNull();
            verify(userRepositoryPort).findById(USER_ID);
            verify(labRepositoryPort).findById(LAB_ID);
            verify(voteRepositoryPort).save(any(Vote.class));
        }

        @Test
        @DisplayName("설명 없이 투표를 생성한다")
        void createVoteWithoutDescription() {
            // given
            User creator = DomainUserFactory.buildLabLeaderUser();
            Lab lab = DomainLabFactory.buildAiLab();
            creator.assignLab(lab);
            Vote savedVote = DomainVoteFactory.buildValidVote();
            LocalDateTime deadline = LocalDateTime.now().plusDays(7);

            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(creator));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.of(lab));
            when(voteRepositoryPort.save(any(Vote.class))).thenReturn(savedVote);

            // when
            Vote result = service.createVote(
                    "투표 제목",
                    USER_ID,
                    LAB_ID,
                    deadline,
                    Arrays.asList("선택지1", "선택지2")
            );

            // then
            assertThat(result).isNotNull();
            verify(userRepositoryPort).findById(USER_ID);
            verify(labRepositoryPort).findById(LAB_ID);
            verify(voteRepositoryPort).save(any(Vote.class));
        }

        @Test
        @DisplayName("존재하지 않는 생성자로 생성 시 UserNotFoundException을 던진다")
        void createVoteUserNotFound() {
            // given
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createVote(
                    "투표 제목", "투표 설명", USER_ID, LAB_ID, 
                    LocalDateTime.now().plusDays(7), Arrays.asList("선택지1", "선택지2")
            ))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(USER_ID);
            verifyNoInteractions(labRepositoryPort);
            verifyNoInteractions(voteRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 랩실로 생성 시 LabNotFoundException을 던진다")
        void createVoteLabNotFound() {
            // given
            User creator = DomainUserFactory.buildLabManagerUser();
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(creator));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createVote(
                    "투표 제목", "투표 설명", USER_ID, LAB_ID,
                    LocalDateTime.now().plusDays(7), Arrays.asList("선택지1", "선택지2")
            ))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(USER_ID);
            verify(labRepositoryPort).findById(LAB_ID);
            verifyNoInteractions(voteRepositoryPort);
        }

        @Test
        @DisplayName("권한이 없는 사용자가 생성 시 VotePermissionException을 던진다")
        void createVotePermissionDenied() {
            // given
            User creator = DomainUserFactory.buildStudentUser(); // 권한 없는 역할
            Lab lab = DomainLabFactory.buildAiLab();
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(creator));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.of(lab));

            // when & then
            assertThatThrownBy(() -> service.createVote(
                    "투표 제목", "투표 설명", USER_ID, LAB_ID,
                    LocalDateTime.now().plusDays(7), Arrays.asList("선택지1", "선택지2")
            ))
                    .isInstanceOf(VotePermissionException.class)
                    .satisfies(ex -> {
                        VotePermissionException e = (VotePermissionException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED);
                    });

            verify(userRepositoryPort).findById(USER_ID);
            verify(labRepositoryPort).findById(LAB_ID);
            verifyNoInteractions(voteRepositoryPort);
        }
    }

    @Nested
    @DisplayName("deleteVote 메서드는")
    class DeleteVoteTests {

        @Test
        @DisplayName("참여자가 없는 투표를 삭제한다")
        void deleteVoteSuccess() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote(); // totalVotes = 0
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));

            // when
            service.deleteVote(VOTE_ID);

            // then
            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(voteParticipationRepositoryPort).deleteByVoteId(VOTE_ID);
            verify(voteOptionRepositoryPort).deleteByVoteId(VOTE_ID);
            verify(voteRepositoryPort).deleteById(VOTE_ID);
        }

        @Test
        @DisplayName("존재하지 않는 투표 삭제 시 VoteNotFoundException을 던진다")
        void deleteVoteNotFound() {
            // given
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.deleteVote(VOTE_ID))
                    .isInstanceOf(VoteNotFoundException.class)
                    .satisfies(ex -> {
                        VoteNotFoundException e = (VoteNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verifyNoInteractions(voteParticipationRepositoryPort);
            verifyNoInteractions(voteOptionRepositoryPort);
            verify(voteRepositoryPort, never()).deleteById(any());
        }

        @Test
        @DisplayName("참여자가 있는 투표 삭제 시 VoteValidationException을 던진다")
        void deleteVoteWithParticipants() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            vote.incrementTotalVotes(); // 참여자 있음으로 설정
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));

            // when & then
            assertThatThrownBy(() -> service.deleteVote(VOTE_ID))
                    .isInstanceOf(VoteValidationException.class)
                    .satisfies(ex -> {
                        VoteValidationException e = (VoteValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_ALREADY_PARTICIPATED);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verifyNoInteractions(voteParticipationRepositoryPort);
            verifyNoInteractions(voteOptionRepositoryPort);
            verify(voteRepositoryPort, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("closeVote 메서드는")
    class CloseVoteTests {

        @Test
        @DisplayName("활성 투표를 종료한다")
        void closeVoteSuccess() {
            // given
            Vote vote = DomainVoteFactory.buildActiveVote();
            Vote closedVote = DomainVoteFactory.buildClosedVote();
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(voteRepositoryPort.save(any(Vote.class))).thenReturn(closedVote);

            // when
            Vote result = service.closeVote(VOTE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(VoteStatus.CLOSED);
            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(voteRepositoryPort).save(vote);
        }

        @Test
        @DisplayName("존재하지 않는 투표 종료 시 VoteNotFoundException을 던진다")
        void closeVoteNotFound() {
            // given
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.closeVote(VOTE_ID))
                    .isInstanceOf(VoteNotFoundException.class)
                    .satisfies(ex -> {
                        VoteNotFoundException e = (VoteNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(voteRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancelVote 메서드는")
    class CancelVoteTests {

        @Test
        @DisplayName("활성 투표를 취소한다")
        void cancelVoteSuccess() {
            // given
            Vote vote = DomainVoteFactory.buildActiveVote();
            Vote canceledVote = DomainVoteFactory.buildCanceledVote();
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(voteRepositoryPort.save(any(Vote.class))).thenReturn(canceledVote);

            // when
            Vote result = service.cancelVote(VOTE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(VoteStatus.CANCELED);
            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(voteRepositoryPort).save(vote);
        }

        @Test
        @DisplayName("존재하지 않는 투표 취소 시 VoteNotFoundException을 던진다")
        void cancelVoteNotFound() {
            // given
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.cancelVote(VOTE_ID))
                    .isInstanceOf(VoteNotFoundException.class)
                    .satisfies(ex -> {
                        VoteNotFoundException e = (VoteNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(voteRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("participateInVote 메서드는")
    class ParticipateInVoteTests {

        @Test
        @DisplayName("유효한 투표에 참여한다")
        void participateInVoteSuccess() {
            // given
            Lab lab = DomainLabFactory.buildAiLab();
            User participant = DomainUserFactory.buildLabMemberWithLab(lab);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);
            VoteOption option = DomainVoteFactory.buildVoteOptionWithVote(vote);
            VoteParticipation savedParticipation = DomainVoteFactory.buildValidVoteParticipation();

            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(participant));
            when(voteOptionRepositoryPort.findById(OPTION_ID)).thenReturn(Optional.of(option));
            when(voteParticipationRepositoryPort.existsByVoteIdAndUserId(VOTE_ID, USER_ID)).thenReturn(false);
            when(voteRepositoryPort.save(any(Vote.class))).thenReturn(vote);
            when(voteParticipationRepositoryPort.save(any(VoteParticipation.class))).thenReturn(savedParticipation);

            // when
            VoteParticipation result = service.participateInVote(VOTE_ID, USER_ID, OPTION_ID);

            // then
            assertThat(result).isNotNull();
            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(userRepositoryPort).findById(USER_ID);
            verify(voteOptionRepositoryPort).findById(OPTION_ID);
            verify(voteParticipationRepositoryPort).existsByVoteIdAndUserId(VOTE_ID, USER_ID);
            verify(voteRepositoryPort).save(vote);
            verify(voteParticipationRepositoryPort).save(any(VoteParticipation.class));
        }

        @Test
        @DisplayName("존재하지 않는 투표 참여 시 VoteNotFoundException을 던진다")
        void participateInVoteNotFound() {
            // given
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.participateInVote(VOTE_ID, USER_ID, OPTION_ID))
                    .isInstanceOf(VoteNotFoundException.class)
                    .satisfies(ex -> {
                        VoteNotFoundException e = (VoteNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verifyNoInteractions(userRepositoryPort);
            verifyNoInteractions(voteOptionRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 참여 시 UserNotFoundException을 던진다")
        void participateInVoteUserNotFound() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.participateInVote(VOTE_ID, USER_ID, OPTION_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(userRepositoryPort).findById(USER_ID);
            verifyNoInteractions(voteOptionRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 선택지 참여 시 VoteNotFoundException을 던진다")
        void participateInVoteOptionNotFound() {
            // given
            Vote vote = DomainVoteFactory.buildValidVote();
            User participant = DomainUserFactory.buildLabMemberUser();
            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(participant));
            when(voteOptionRepositoryPort.findById(OPTION_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.participateInVote(VOTE_ID, USER_ID, OPTION_ID))
                    .isInstanceOf(VoteNotFoundException.class)
                    .satisfies(ex -> {
                        VoteNotFoundException e = (VoteNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_OPTION_NOT_FOUND);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(userRepositoryPort).findById(USER_ID);
            verify(voteOptionRepositoryPort).findById(OPTION_ID);
            verifyNoInteractions(voteParticipationRepositoryPort);
        }

        @Test
        @DisplayName("이미 참여한 투표에 재참여 시 VoteValidationException을 던진다")
        void participateInVoteAlreadyParticipated() {
            // given
            Lab lab = DomainLabFactory.buildAiLab();
            User participant = DomainUserFactory.buildLabMemberWithLab(lab);
            Vote vote = DomainVoteFactory.buildVoteWithLab(lab);
            VoteOption option = DomainVoteFactory.buildVoteOptionWithVote(vote);

            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(participant));
            when(voteOptionRepositoryPort.findById(OPTION_ID)).thenReturn(Optional.of(option));
            when(voteParticipationRepositoryPort.existsByVoteIdAndUserId(VOTE_ID, USER_ID)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> service.participateInVote(VOTE_ID, USER_ID, OPTION_ID))
                    .isInstanceOf(VoteValidationException.class)
                    .satisfies(ex -> {
                        VoteValidationException e = (VoteValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_ALREADY_PARTICIPATED);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(userRepositoryPort).findById(USER_ID);
            verify(voteOptionRepositoryPort).findById(OPTION_ID);
            verify(voteParticipationRepositoryPort).existsByVoteIdAndUserId(VOTE_ID, USER_ID);
            verify(voteParticipationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("종료된 투표 참여 시 VoteValidationException을 던진다")
        void participateInClosedVote() {
            // given
            Lab lab = DomainLabFactory.buildAiLab();
            User participant = DomainUserFactory.buildLabMemberWithLab(lab);
            Vote vote = DomainVoteFactory.buildClosedVote();
            VoteOption option = DomainVoteFactory.buildVoteOptionWithVote(vote);

            when(voteRepositoryPort.findById(VOTE_ID)).thenReturn(Optional.of(vote));
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(participant));
            when(voteOptionRepositoryPort.findById(OPTION_ID)).thenReturn(Optional.of(option));
            when(voteParticipationRepositoryPort.existsByVoteIdAndUserId(VOTE_ID, USER_ID)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> service.participateInVote(VOTE_ID, USER_ID, OPTION_ID))
                    .isInstanceOf(VotePermissionException.class)
                    .satisfies(ex -> {
                        VotePermissionException e = (VotePermissionException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED);
                    });

            verify(voteRepositoryPort).findById(VOTE_ID);
            verify(userRepositoryPort).findById(USER_ID);
            verify(voteOptionRepositoryPort).findById(OPTION_ID);
            verify(voteParticipationRepositoryPort, never()).existsByVoteIdAndUserId(VOTE_ID, USER_ID);
            verify(voteParticipationRepositoryPort, never()).save(any());
        }
    }
}
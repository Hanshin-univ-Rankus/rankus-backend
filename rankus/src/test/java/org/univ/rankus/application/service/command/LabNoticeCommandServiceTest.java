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
import org.univ.rankus.application.port.out.LabNoticeRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LabNoticeCommandServiceTest {

    @Mock
    private LabNoticeRepositoryPort labNoticeRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private LabNoticeCommandService service;

    private static final Long NOTICE_ID = 123L;
    private static final Long LAB_ID = 1L;
    private static final Long USER_ID = 42L;

    @Nested
    @DisplayName("createNotice 메서드는")
    class CreateNoticeTests {

        @Test
        @DisplayName("유효한 정보로 공지사항을 생성한다")
        void createNoticeSuccess() {
            // given
            User author = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildAiLab();
            LabNotice savedNotice = createMockNotice(NoticeType.NORMAL, false);

            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(author));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.of(lab));
            when(labNoticeRepositoryPort.save(any(LabNotice.class))).thenReturn(savedNotice);

            // when
            LabNotice result = service.createNotice(
                    "공지사항 제목",
                    "공지사항 내용",
                    USER_ID,
                    LAB_ID,
                    NoticeType.NORMAL,
                    false
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("공지사항 제목");
            assertThat(result.getType()).isEqualTo(NoticeType.NORMAL);
            assertThat(result.isPinned()).isFalse();

            verify(userRepositoryPort).findById(USER_ID);
            verify(labRepositoryPort).findById(LAB_ID);
            verify(labNoticeRepositoryPort).save(any(LabNotice.class));
        }

        @Test
        @DisplayName("존재하지 않는 작성자로 생성 시 UserNotFoundException을 던진다")
        void createNoticeUserNotFound() {
            // given
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createNotice(
                    "제목", "내용", USER_ID, LAB_ID, NoticeType.NORMAL, false
            ))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(USER_ID);
            verifyNoInteractions(labRepositoryPort);
            verifyNoInteractions(labNoticeRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 랩실로 생성 시 LabNotFoundException을 던진다")
        void createNoticeLabNotFound() {
            // given
            User author = DomainUserFactory.buildStudentUser();
            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(author));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.createNotice(
                    "제목", "내용", USER_ID, LAB_ID, NoticeType.NORMAL, false
            ))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(USER_ID);
            verify(labRepositoryPort).findById(LAB_ID);
            verifyNoInteractions(labNoticeRepositoryPort);
        }

        @Test
        @DisplayName("기본값으로 공지사항을 생성한다")
        void createNoticeWithDefaults() {
            // given
            User author = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildAiLab();
            LabNotice savedNotice = createMockNotice(NoticeType.NORMAL, false);

            when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(author));
            when(labRepositoryPort.findById(LAB_ID)).thenReturn(Optional.of(lab));
            when(labNoticeRepositoryPort.save(any(LabNotice.class))).thenReturn(savedNotice);

            // when
            LabNotice result = service.createNotice("제목", "내용", USER_ID, LAB_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(NoticeType.NORMAL);
            assertThat(result.isPinned()).isFalse();

            verify(labNoticeRepositoryPort).save(any(LabNotice.class));
        }
    }

    @Nested
    @DisplayName("updateNotice 메서드는")
    class UpdateNoticeTests {

        @Test
        @DisplayName("유효한 정보로 공지사항을 수정한다")
        void updateNoticeSuccess() {
            // given
            LabNotice existingNotice = createMockNotice(NoticeType.NORMAL, false);
            LabNotice updatedNotice = createMockNotice(NoticeType.URGENT, true);

            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.of(existingNotice));
            when(labNoticeRepositoryPort.save(existingNotice)).thenReturn(updatedNotice);

            // when
            LabNotice result = service.updateNotice(
                    NOTICE_ID,
                    "수정된 제목",
                    "수정된 내용",
                    NoticeType.URGENT,
                    true
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(NoticeType.URGENT);
            assertThat(result.isPinned()).isTrue();

            verify(labNoticeRepositoryPort).findById(NOTICE_ID);
            verify(labNoticeRepositoryPort).save(existingNotice);
            verify(existingNotice).updateTitle("수정된 제목");
            verify(existingNotice).updateContent("수정된 내용");
            verify(existingNotice).updateType(NoticeType.URGENT);
            verify(existingNotice).pin();
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 수정 시 NoticeNotFoundException을 던진다")
        void updateNoticeNotFound() {
            // given
            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.updateNotice(
                    NOTICE_ID, "제목", "내용", NoticeType.NORMAL, false
            ))
                    .isInstanceOf(NoticeNotFoundException.class);

            verify(labNoticeRepositoryPort).findById(NOTICE_ID);
            verify(labNoticeRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("고정 해제로 공지사항을 수정한다")
        void updateNoticeUnpin() {
            // given
            LabNotice existingNotice = createMockNotice(NoticeType.NORMAL, true);
            LabNotice updatedNotice = createMockNotice(NoticeType.NORMAL, false);

            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.of(existingNotice));
            when(labNoticeRepositoryPort.save(existingNotice)).thenReturn(updatedNotice);

            // when
            LabNotice result = service.updateNotice(
                    NOTICE_ID,
                    "제목",
                    "내용",
                    NoticeType.NORMAL,
                    false
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.isPinned()).isFalse();

            verify(existingNotice).unpin();
        }
    }

    @Nested
    @DisplayName("deleteNotice 메서드는")
    class DeleteNoticeTests {

        @Test
        @DisplayName("존재하는 공지사항을 삭제한다")
        void deleteNoticeSuccess() {
            // given
            when(labNoticeRepositoryPort.existsById(NOTICE_ID)).thenReturn(true);

            // when
            service.deleteNotice(NOTICE_ID);

            // then
            verify(labNoticeRepositoryPort).existsById(NOTICE_ID);
            verify(labNoticeRepositoryPort).deleteById(NOTICE_ID);
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 삭제 시 NoticeNotFoundException을 던진다")
        void deleteNoticeNotFound() {
            // given
            when(labNoticeRepositoryPort.existsById(NOTICE_ID)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> service.deleteNotice(NOTICE_ID))
                    .isInstanceOf(NoticeNotFoundException.class);

            verify(labNoticeRepositoryPort).existsById(NOTICE_ID);
            verify(labNoticeRepositoryPort, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("togglePinNotice 메서드는")
    class TogglePinNoticeTests {

        @Test
        @DisplayName("공지사항 고정 상태를 토글한다")
        void togglePinNoticeSuccess() {
            // given
            LabNotice existingNotice = createMockNotice(NoticeType.NORMAL, false);
            LabNotice toggledNotice = createMockNotice(NoticeType.NORMAL, true);

            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.of(existingNotice));
            when(labNoticeRepositoryPort.save(existingNotice)).thenReturn(toggledNotice);

            // when
            LabNotice result = service.togglePinNotice(NOTICE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isPinned()).isTrue();

            verify(labNoticeRepositoryPort).findById(NOTICE_ID);
            verify(existingNotice).togglePin();
            verify(labNoticeRepositoryPort).save(existingNotice);
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 토글 시 NoticeNotFoundException을 던진다")
        void togglePinNoticeNotFound() {
            // given
            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.togglePinNotice(NOTICE_ID))
                    .isInstanceOf(NoticeNotFoundException.class);

            verify(labNoticeRepositoryPort).findById(NOTICE_ID);
            verify(labNoticeRepositoryPort, never()).save(any());
        }
    }

    // Helper method
    private LabNotice createMockNotice(NoticeType type, boolean pinned) {
        LabNotice notice = mock(LabNotice.class);
        when(notice.getId()).thenReturn(NOTICE_ID);
        when(notice.getTitle()).thenReturn("공지사항 제목");
        when(notice.getContent()).thenReturn("공지사항 내용");
        when(notice.getType()).thenReturn(type);
        when(notice.isPinned()).thenReturn(pinned);
        return notice;
    }
}
package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.application.port.out.LabNoticeRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LabNoticeQueryServiceTest {

    @Mock
    private LabNoticeRepositoryPort labNoticeRepositoryPort;

    @InjectMocks
    private LabNoticeQueryService service;

    private static final Long NOTICE_ID = 123L;
    private static final Long LAB_ID = 1L;

    @Nested
    @DisplayName("getNoticeById 메서드는")
    class GetNoticeByIdTests {

        @Test
        @DisplayName("존재하는 ID로 공지사항을 조회한다")
        void getNoticeByIdSuccess() {
            // given
            LabNotice mockNotice = createMockNotice(NoticeType.NORMAL, false);
            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.of(mockNotice));

            // when
            LabNotice result = service.getNoticeById(NOTICE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(NOTICE_ID);
            assertThat(result.getType()).isEqualTo(NoticeType.NORMAL);

            verify(labNoticeRepositoryPort).findById(NOTICE_ID);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 NoticeNotFoundException을 던진다")
        void getNoticeByIdNotFound() {
            // given
            when(labNoticeRepositoryPort.findById(NOTICE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getNoticeById(NOTICE_ID))
                    .isInstanceOf(NoticeNotFoundException.class);

            verify(labNoticeRepositoryPort).findById(NOTICE_ID);
        }
    }

    @Nested
    @DisplayName("getNoticesByLabId 메서드는")
    class GetNoticesByLabIdTests {

        @Test
        @DisplayName("랩실별 공지사항 목록을 조회한다")
        void getNoticesByLabIdSuccess() {
            // given
            List<LabNotice> mockNotices = Arrays.asList(
                    createMockNotice(NoticeType.NORMAL, false),
                    createMockNotice(NoticeType.URGENT, true)
            );
            when(labNoticeRepositoryPort.findByLabIdOrderByIsPinnedDescCreatedAtDesc(LAB_ID))
                    .thenReturn(mockNotices);

            // when
            List<LabNotice> result = service.getNoticesByLabId(LAB_ID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getType()).isEqualTo(NoticeType.NORMAL);
            assertThat(result.get(1).getType()).isEqualTo(NoticeType.URGENT);

            verify(labNoticeRepositoryPort).findByLabIdOrderByIsPinnedDescCreatedAtDesc(LAB_ID);
        }

        @Test
        @DisplayName("랩실별 공지사항을 페이징으로 조회한다")
        void getNoticesByLabIdWithPaging() {
            // given
            List<LabNotice> mockNotices = Arrays.asList(
                    createMockNotice(NoticeType.NORMAL, false),
                    createMockNotice(NoticeType.URGENT, true)
            );
            Pageable pageable = PageRequest.of(0, 10);
            Page<LabNotice> mockPage = new PageImpl<>(mockNotices, pageable, mockNotices.size());

            when(labNoticeRepositoryPort.findByLabIdOrderByIsPinnedDescCreatedAtDesc(LAB_ID, pageable))
                    .thenReturn(mockPage);

            // when
            Page<LabNotice> result = service.getNoticesByLabId(LAB_ID, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);

            verify(labNoticeRepositoryPort).findByLabIdOrderByIsPinnedDescCreatedAtDesc(LAB_ID, pageable);
        }
    }

    @Nested
    @DisplayName("getNoticesByLabIdAndType 메서드는")
    class GetNoticesByLabIdAndTypeTests {

        @Test
        @DisplayName("랩실별 특정 타입 공지사항을 조회한다")
        void getNoticesByLabIdAndTypeSuccess() {
            // given
            List<LabNotice> urgentNotices = List.of(createMockNotice(NoticeType.URGENT, true));
            when(labNoticeRepositoryPort.findByLabIdAndType(LAB_ID, NoticeType.URGENT))
                    .thenReturn(urgentNotices);

            // when
            List<LabNotice> result = service.getNoticesByLabIdAndType(LAB_ID, NoticeType.URGENT);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(NoticeType.URGENT);

            verify(labNoticeRepositoryPort).findByLabIdAndType(LAB_ID, NoticeType.URGENT);
        }
    }

    @Nested
    @DisplayName("getPinnedNoticesByLabId 메서드는")
    class GetPinnedNoticesByLabIdTests {

        @Test
        @DisplayName("랩실별 고정 공지사항을 조회한다")
        void getPinnedNoticesByLabIdSuccess() {
            // given
            List<LabNotice> pinnedNotices = Arrays.asList(
                    createMockNotice(NoticeType.NORMAL, true),
                    createMockNotice(NoticeType.URGENT, true)
            );
            when(labNoticeRepositoryPort.findPinnedNoticesByLabId(LAB_ID))
                    .thenReturn(pinnedNotices);

            // when
            List<LabNotice> result = service.getPinnedNoticesByLabId(LAB_ID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allSatisfy(notice -> 
                    assertThat(notice.isPinned()).isTrue()
            );

            verify(labNoticeRepositoryPort).findPinnedNoticesByLabId(LAB_ID);
        }
    }

    @Nested
    @DisplayName("countNoticesByLabId 메서드는")
    class CountNoticesByLabIdTests {

        @Test
        @DisplayName("랩실별 공지사항 개수를 조회한다")
        void countNoticesByLabIdSuccess() {
            // given
            long expectedCount = 5L;
            when(labNoticeRepositoryPort.countByLabId(LAB_ID)).thenReturn(expectedCount);

            // when
            long result = service.countNoticesByLabId(LAB_ID);

            // then
            assertThat(result).isEqualTo(expectedCount);

            verify(labNoticeRepositoryPort).countByLabId(LAB_ID);
        }
    }

    @Nested
    @DisplayName("countNoticesByLabIdAndType 메서드는")
    class CountNoticesByLabIdAndTypeTests {

        @Test
        @DisplayName("랩실별 특정 타입 공지사항 개수를 조회한다")
        void countNoticesByLabIdAndTypeSuccess() {
            // given
            long expectedCount = 3L;
            when(labNoticeRepositoryPort.countByLabIdAndType(LAB_ID, NoticeType.URGENT))
                    .thenReturn(expectedCount);

            // when
            long result = service.countNoticesByLabIdAndType(LAB_ID, NoticeType.URGENT);

            // then
            assertThat(result).isEqualTo(expectedCount);

            verify(labNoticeRepositoryPort).countByLabIdAndType(LAB_ID, NoticeType.URGENT);
        }
    }

    // Helper method
    private LabNotice createMockNotice(NoticeType type, boolean pinned) {
        LabNotice notice = mock(LabNotice.class);
        User author = DomainUserFactory.buildStudentUser();
        Lab lab = DomainLabFactory.buildAiLab();

        when(notice.getId()).thenReturn(NOTICE_ID);
        when(notice.getTitle()).thenReturn("공지사항 제목");
        when(notice.getContent()).thenReturn("공지사항 내용");
        when(notice.getType()).thenReturn(type);
        when(notice.isPinned()).thenReturn(pinned);
        when(notice.getAuthor()).thenReturn(author);
        when(notice.getLab()).thenReturn(lab);

        return notice;
    }
}
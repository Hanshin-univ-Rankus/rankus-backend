package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.request.LabNoticeCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabNoticeUpdateRequestDto;
import org.univ.rankus.application.port.in.command.LabNoticeCommandUseCase;
import org.univ.rankus.application.port.in.query.LabNoticeQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.lab.notice.NoticeType;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeErrorCode;
import org.univ.rankus.domain.model.lab.notice.exception.NoticeNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabNoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabNoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabNoticeCommandUseCase commandUseCase;

    @MockitoBean
    private LabNoticeQueryUseCase queryUseCase;

    private static final Long LAB_ID = 1L;
    private static final Long NOTICE_ID = 123L;
    private static final Long USER_ID = 42L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/notices")
    class GetLabNoticesTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 페이징된 공지사항 목록")
        void getLabNoticesSuccess() throws Exception {
            // given
            List<LabNotice> notices = createMockNotices();
            Page<LabNotice> noticePage = new PageImpl<>(notices, PageRequest.of(0, 20), notices.size());
            given(queryUseCase.getNoticesByLabId(eq(LAB_ID), any(Pageable.class)))
                    .willReturn(noticePage);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/notices", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공지사항 목록 조회 성공"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/notices/all")
    class GetAllLabNoticesTests {

        @Test
        @DisplayName("전체 공지사항 조회 → 200 OK")
        void getAllLabNoticesSuccess() throws Exception {
            // given
            List<LabNotice> notices = createMockNotices();
            given(queryUseCase.getNoticesByLabId(LAB_ID)).willReturn(notices);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/notices/all", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("전체 공지사항 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/notices/type/{type}")
    class GetNoticesByTypeTests {

        @Test
        @DisplayName("타입별 공지사항 조회 → 200 OK")
        void getNoticesByTypeSuccess() throws Exception {
            // given
            List<LabNotice> urgentNotices = List.of(createMockNotice(NoticeType.URGENT));
            given(queryUseCase.getNoticesByLabIdAndType(LAB_ID, NoticeType.URGENT))
                    .willReturn(urgentNotices);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/notices/type/{type}", LAB_ID, "URGENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("긴급 공지사항 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/notices/pinned")
    class GetPinnedNoticesTests {

        @Test
        @DisplayName("고정 공지사항 조회 → 200 OK")
        void getPinnedNoticesSuccess() throws Exception {
            // given
            List<LabNotice> pinnedNotices = List.of(createMockPinnedNotice());
            given(queryUseCase.getPinnedNoticesByLabId(LAB_ID)).willReturn(pinnedNotices);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/notices/pinned", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("고정 공지사항 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/notices/{noticeId}")
    class GetNoticeTests {

        @Test
        @DisplayName("공지사항 상세 조회 → 200 OK")
        void getNoticeSuccess() throws Exception {
            // given
            LabNotice mockNotice = createMockNotice(NoticeType.NORMAL);
            given(queryUseCase.getNoticeById(NOTICE_ID)).willReturn(mockNotice);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/notices/{noticeId}", LAB_ID, NOTICE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공지사항 상세 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(NOTICE_ID))
                    .andExpect(jsonPath("$.data.title").value("테스트 공지사항"))
                    .andExpect(jsonPath("$.data.type").value("NORMAL"));
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 조회 → 404 Not Found")
        void getNoticeNotFound() throws Exception {
            // given
            given(queryUseCase.getNoticeById(NOTICE_ID))
                    .willThrow(new NoticeNotFoundException());

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/notices/{noticeId}", LAB_ID, NOTICE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LNT_404"))
                    .andExpect(jsonPath("$.message").value("공지사항을 찾을 수 없습니다"));
        }
    }

    @Nested
    @DisplayName("POST /api/labs/{labId}/notices")
    class CreateNoticeTests {

        @Test
        @DisplayName("공지사항 생성 → 201 Created")
        void createNoticeSuccess() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LabNoticeCreateRequestDto request = LabNoticeCreateRequestDto.builder()
                    .title("새 공지사항")
                    .content("공지사항 내용입니다.")
                    .type(NoticeType.NORMAL)
                    .pinned(false)
                    .build();

            LabNotice createdNotice = createMockNotice(NoticeType.NORMAL);
            given(commandUseCase.createNotice(
                    eq("새 공지사항"),
                    eq("공지사항 내용입니다."),
                    eq(USER_ID),
                    eq(LAB_ID),
                    eq(NoticeType.NORMAL),
                    eq(false)
            )).willReturn(createdNotice);

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/notices", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("공지사항이 성공적으로 생성되었습니다"))
                    .andExpect(jsonPath("$.data.title").value("테스트 공지사항"));
        }

        @Test
        @DisplayName("잘못된 입력으로 공지사항 생성 → 400 Bad Request")
        void createNoticeValidationError() throws Exception {
            // given
            LabNoticeCreateRequestDto request = LabNoticeCreateRequestDto.builder()
                    .title("") // 빈 제목
                    .content("공지사항 내용입니다.")
                    .type(NoticeType.NORMAL)
                    .pinned(false)
                    .build();

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/notices", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"));
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/notices/{noticeId}")
    class UpdateNoticeTests {

        @Test
        @DisplayName("공지사항 수정 → 200 OK")
        void updateNoticeSuccess() throws Exception {
            // given
            LabNoticeUpdateRequestDto request = LabNoticeUpdateRequestDto.builder()
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .type(NoticeType.URGENT)
                    .pinned(true)
                    .build();

            LabNotice updatedNotice = createMockNotice(NoticeType.URGENT);
            given(commandUseCase.updateNotice(
                    eq(NOTICE_ID),
                    eq("수정된 제목"),
                    eq("수정된 내용"),
                    eq(NoticeType.URGENT),
                    eq(true)
            )).willReturn(updatedNotice);

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/notices/{noticeId}", LAB_ID, NOTICE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공지사항이 성공적으로 수정되었습니다"))
                    .andExpect(jsonPath("$.data.title").value("테스트 공지사항"));
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 수정 → 404 Not Found")
        void updateNoticeNotFound() throws Exception {
            // given
            LabNoticeUpdateRequestDto request = LabNoticeUpdateRequestDto.builder()
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .type(NoticeType.URGENT)
                    .pinned(true)
                    .build();

            given(commandUseCase.updateNotice(
                    eq(NOTICE_ID),
                    anyString(),
                    anyString(),
                    any(NoticeType.class),
                    anyBoolean()
            )).willThrow(new NoticeNotFoundException());

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/notices/{noticeId}", LAB_ID, NOTICE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LNT_404"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/labs/{labId}/notices/{noticeId}/pin")
    class TogglePinNoticeTests {

        @Test
        @DisplayName("공지사항 고정 토글 → 200 OK")
        void togglePinNoticeSuccess() throws Exception {
            // given
            LabNotice pinnedNotice = createMockPinnedNotice();
            given(commandUseCase.togglePinNotice(NOTICE_ID)).willReturn(pinnedNotice);

            // when & then
            mockMvc.perform(patch("/api/labs/{labId}/notices/{noticeId}/pin", LAB_ID, NOTICE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공지사항이 고정되었습니다"))
                    .andExpect(jsonPath("$.data.pinned").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/notices/{noticeId}")
    class DeleteNoticeTests {

        @Test
        @DisplayName("공지사항 삭제 → 200 OK")
        void deleteNoticeSuccess() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/labs/{labId}/notices/{noticeId}", LAB_ID, NOTICE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공지사항이 성공적으로 삭제되었습니다"));
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 삭제 → 404 Not Found")
        void deleteNoticeNotFound() throws Exception {
            // given
            doThrow(new NoticeNotFoundException())
                    .when(commandUseCase).deleteNotice(NOTICE_ID);

            // when & then
            mockMvc.perform(delete("/api/labs/{labId}/notices/{noticeId}", LAB_ID, NOTICE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LNT_404"));
        }
    }

    // Helper methods
    private List<LabNotice> createMockNotices() {
        return Arrays.asList(
                createMockNotice(NoticeType.NORMAL),
                createMockNotice(NoticeType.URGENT)
        );
    }

    private LabNotice createMockNotice(NoticeType type) {
        LabNotice notice = mock(LabNotice.class);
        User author = DomainUserFactory.buildStudentUser();
        Lab lab = DomainLabFactory.buildAiLab();

        given(notice.getId()).willReturn(NOTICE_ID);
        given(notice.getTitle()).willReturn("테스트 공지사항");
        given(notice.getContent()).willReturn("테스트 공지사항 내용");
        given(notice.getType()).willReturn(type);
        given(notice.isPinned()).willReturn(false);
        given(notice.getAuthor()).willReturn(author);
        given(notice.getLab()).willReturn(lab);

        return notice;
    }

    private LabNotice createMockPinnedNotice() {
        LabNotice notice = createMockNotice(NoticeType.NORMAL);
        given(notice.isPinned()).willReturn(true);
        return notice;
    }
}
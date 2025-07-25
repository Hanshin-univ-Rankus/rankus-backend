package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.dto.response.LabDashboardResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;
import org.univ.rankus.application.port.in.query.*;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.notice.LabNotice;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.vote.Vote;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 랩실 대시보드 조회 서비스
 * 여러 도메인의 데이터를 통합하여 대시보드 정보를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabDashboardQueryService implements LabDashboardQueryUseCase {

    private final LabPromotionQueryUseCase labPromotionQueryUseCase;
    private final LabNoticeQueryUseCase labNoticeQueryUseCase;
    private final VoteQueryUseCase voteQueryUseCase;
    private final LabResourceQueryUseCase labResourceQueryUseCase;
    private final CalendarEventQueryUseCase calendarEventQueryUseCase;
    private final UserRepositoryPort userRepositoryPort;

    private static final int PREVIEW_LIMIT = 3;

    @Override
    public LabDashboardResponseDto getDashboardByLabId(Long labId) {
        // 1. 랩실 기본 정보 조회
        Lab lab = labPromotionQueryUseCase.getLabById(labId);

        // 2. 각 위젯 데이터 병렬 조회
        List<LabDashboardResponseDto.DashboardPreviewItem> notices = getLatestNotices(labId);
        List<LabDashboardResponseDto.DashboardPreviewItem> votes = getActiveVotes(labId);
        List<LabDashboardResponseDto.DashboardPreviewItem> resources = getLatestResources(labId);
        List<LabDashboardResponseDto.DashboardPreviewItem> schedules = getUpcomingSchedules(labId);
        List<LabDashboardResponseDto.DashboardPreviewItem> members = getLabMembers(lab);

        // 3. 통합 응답 생성
        return LabDashboardResponseDto.create(lab, notices, votes, resources, schedules, members);
    }

    /**
     * 최신 공지사항 3개 조회
     */
    private List<LabDashboardResponseDto.DashboardPreviewItem> getLatestNotices(Long labId) {
        try {
            Pageable pageable = PageRequest.of(0, PREVIEW_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<LabNotice> noticePage = labNoticeQueryUseCase.getNoticesByLabId(labId, pageable);

            return noticePage.getContent().stream()
                    .map(notice -> LabDashboardResponseDto.DashboardPreviewItem.createNotice(
                            notice.getId(),
                            notice.getTitle(),
                            notice.getCreatedAt().toString()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 현재 진행 중인 투표 3개 조회
     */
    private List<LabDashboardResponseDto.DashboardPreviewItem> getActiveVotes(Long labId) {
        try {
            List<Vote> activeVotes = voteQueryUseCase.findActiveVotesByLabId(labId);

            return activeVotes.stream()
                    .limit(PREVIEW_LIMIT)
                    .map(vote -> LabDashboardResponseDto.DashboardPreviewItem.createVote(
                            vote.getId(),
                            vote.getTitle(),
                            vote.getCreatedAt().toString(),
                            vote.getStatus().toString()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 최근 자료 3개 조회 (공개 자료만)
     */
    private List<LabDashboardResponseDto.DashboardPreviewItem> getLatestResources(Long labId) {
        try {
            Pageable pageable = PageRequest.of(0, PREVIEW_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
            // 임시 사용자 ID (시스템 사용자)로 조회 - 공개 자료만 표시됨
            Page<LabResourceResponseDto> resourcePage = labResourceQueryUseCase.getLabResources(
                    labId, null, null, pageable, 1L
            );

            return resourcePage.getContent().stream()
                    .filter(LabResourceResponseDto::getIsPublic)
                    .map(resource -> LabDashboardResponseDto.DashboardPreviewItem.createResource(
                            resource.getId(),
                            resource.getTitle(),
                            resource.getCreatedAt().toString()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 다가오는 일정 3개 조회
     */
    private List<LabDashboardResponseDto.DashboardPreviewItem> getUpcomingSchedules(Long labId) {
        try {
            List<CalendarEvent> events = calendarEventQueryUseCase.getEventsByLabId(labId);

            return events.stream()
                    .sorted((e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate()))
                    .limit(PREVIEW_LIMIT)
                    .map(event -> LabDashboardResponseDto.DashboardPreviewItem.createSchedule(
                            event.getId(),
                            event.getTitle(),
                            event.getCreatedAt().toString()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 랩실 멤버 3개 조회 (최근 가입순)
     */
    private List<LabDashboardResponseDto.DashboardPreviewItem> getLabMembers(Lab lab) {
        try {
            List<User> members = userRepositoryPort.findByLab(lab);

            return members.stream()
                    .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                    .limit(PREVIEW_LIMIT)
                    .map(user -> LabDashboardResponseDto.DashboardPreviewItem.createMember(
                            user.getId(),
                            user.getName(),
                            user.getCreatedAt().toString(),
                            user.getRole().toString()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
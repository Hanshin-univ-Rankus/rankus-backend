package org.univ.rankus.application.service.lab.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;
import org.univ.rankus.application.port.in.lab.member.GetLabMembersQuery;
import org.univ.rankus.application.port.in.query.GetLabMemberActivityQuery;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.util.List;

/**
 * 랩실 멤버 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLabMembersService implements GetLabMembersQuery {

    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final GetLabMemberActivityQuery getLabMemberActivityQuery;

    @Override
    public List<User> getLabMembers(Long labId, Long requesterId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2. 요청자 권한 확인
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3. 랩실 멤버 조회 권한 확인
        if (!canViewLabMembers(requester, lab)) {
            throw new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED);
        }

        // 4. 랩실 멤버 조회
        return userRepositoryPort.findByLab(lab);
    }

    @Override
    public User getLabMember(Long labId, Long memberId, Long requesterId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2. 요청자 권한 확인
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3. 랩실 멤버 조회 권한 확인
        if (!canViewLabMembers(requester, lab)) {
            throw new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED);
        }

        // 4. 멤버 조회
        User member = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 5. 해당 멤버가 실제로 그 랩실에 속하는지 확인
        if (member.getLab() == null || !member.getLab().equals(lab)) {
            throw new LabValidationException(LabErrorCode.LAB_MEMBER_NOT_FOUND);
        }

        return member;
    }

    @Override
    public LabMemberDetailResponse getLabMemberDetail(Long labId, Long memberId, Long requesterId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2. 요청자 권한 확인
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3. 랩실 멤버 조회 권한 확인
        if (!canViewLabMembers(requester, lab)) {
            throw new LabPermissionException(LabErrorCode.LAB_MEMBER_VIEW_PERMISSION_DENIED);
        }

        // 4. 멤버 조회
        User member = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 5. 해당 멤버가 실제로 그 랩실에 속하는지 확인
        if (member.getLab() == null || !member.getLab().equals(lab)) {
            throw new LabValidationException(LabErrorCode.LAB_MEMBER_NOT_FOUND);
        }

        // 6. 상세 정보 노출 여부 확인
        boolean showDetailedInfo = shouldShowDetailedInfo(requester, member);

        if (showDetailedInfo) {
            // 7. 활동 통계 조회
            LabMemberDetailResponse.LabActivityStats stats = getLabMemberActivityQuery
                    .getLabMemberActivityStats(labId, memberId);

            return LabMemberDetailResponse.fromUserDetailed(member, stats);
        } else {
            return LabMemberDetailResponse.fromUserBasic(member);
        }
    }

    /**
     * 랩실 멤버 조회 권한 확인
     * - ADMIN, PROFESSOR: 모든 랩실 조회 가능
     * - LAB_MEMBER 이상: 소속 랩실만 조회 가능
     */
    private boolean canViewLabMembers(User user, Lab lab) {
        // 기존 출석 조회 권한과 동일한 로직 사용
        return user.canViewLabAttendance(lab);
    }

    /**
     * 상세 정보 노출 여부 결정
     * - ADMIN, PROFESSOR: 모든 정보 노출
     * - LAB_LEADER, LAB_MANAGER: 소속 랩실 멤버에 대해서만 상세 정보 노출
     */
    private boolean shouldShowDetailedInfo(User requester, User member) {
        Role role = requester.getRole();

        // ADMIN, PROFESSOR는 모든 정보 조회 가능
        if (role == Role.ADMIN || role == Role.PROFESSOR) {
            return true;
        }

        // LAB_LEADER, LAB_MANAGER는 소속 랩실 멤버에 대해서만 상세 정보 조회 가능
        if (role == Role.LAB_LEADER || role == Role.LAB_MANAGER) {
            return requester.getLab() != null
                    && member.getLab() != null
                    && requester.getLab().equals(member.getLab());
        }

        return false;
    }
}
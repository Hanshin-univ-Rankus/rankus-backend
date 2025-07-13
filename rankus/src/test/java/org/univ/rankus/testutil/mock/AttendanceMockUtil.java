package org.univ.rankus.testutil.mock;

import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Attendance 관련 공통 테스트 Mock 유틸리티
 * - Service 테스트에서 반복되는 Mock 설정을 재사용
 * - 프로젝트 스타일에 맞는 Mock 패턴 제공
 */
public class AttendanceMockUtil {

    /**
     * 기본 테스트 상수들 - DomainAttendanceFactory와 일치시킴
     */
    public static final Long DEFAULT_RECORD_ID = 1L;
    public static final Long DEFAULT_SESSION_ID = DomainAttendanceFactory.DEFAULT_LAB_ID;  // 1L로 통일
    public static final Long DEFAULT_USER_ID = DomainAttendanceFactory.DEFAULT_USER_ID;    // 2L로 통일
    public static final Long DEFAULT_LAB_ID = DomainAttendanceFactory.DEFAULT_LAB_ID;      // 1L로 통일
    public static final String DEFAULT_REASON = "테스트 수정 사유";

    /**
     * AttendanceRecord 존재 상황을 모킹합니다.
     * 
     * @param recordRepo AttendanceRecordRepositoryPort mock
     * @param recordId 출석 기록 ID
     * @return 모킹된 AttendanceRecord
     */
    public static AttendanceRecord mockExistingRecord(AttendanceRecordRepositoryPort recordRepo, Long recordId) {
        // ID가 설정된 AttendanceRecord 사용
        AttendanceRecord record = DomainAttendanceFactory.buildValidRecordWithId(recordId);
        when(recordRepo.findById(recordId)).thenReturn(Optional.of(record));
        
        // save 메서드도 함께 모킹 (상태 변경 시 필요)
        when(recordRepo.save(any(AttendanceRecord.class))).thenReturn(record);
        
        return record;
    }

    /**
     * AttendanceRecord 미존재 상황을 모킹합니다.
     */
    public static void mockRecordNotFound(AttendanceRecordRepositoryPort recordRepo, Long recordId) {
        when(recordRepo.findById(recordId)).thenReturn(Optional.empty());
    }

    /**
     * AttendanceSession 존재 상황을 모킹합니다.
     * 
     * @param sessionRepo AttendanceSessionRepositoryPort mock
     * @param sessionId 세션 ID
     * @return 모킹된 AttendanceSession
     */
    public static AttendanceSession mockExistingSession(AttendanceSessionRepositoryPort sessionRepo, Long sessionId) {
        AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(sessionId);
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.of(mockSession));
        
        // save 메서드도 함께 모킹
        when(sessionRepo.save(any(AttendanceSession.class))).thenReturn(mockSession);
        
        return mockSession;
    }

    /**
     * User 존재 상황을 모킹합니다.
     * 
     * @param userRepo UserRepositoryPort mock  
     * @param userId 사용자 ID
     * @return 모킹된 User
     */
    public static User mockExistingUser(UserRepositoryPort userRepo, Long userId) {
        User mockUser = DomainUserFactory.buildLabManagerUser(); // 출석 관리 권한 있는 사용자
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        return mockUser;
    }

    /**
     * User 미존재 상황을 모킹합니다.
     */
    public static void mockUserNotFound(UserRepositoryPort userRepo, Long userId) {
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
    }

    /**
     * Lab 존재 상황을 모킹합니다.
     * 
     * @param labRepo LabRepositoryPort mock
     * @param labId 랩실 ID
     * @return 모킹된 Lab
     */
    public static Lab mockExistingLab(LabRepositoryPort labRepo, Long labId) {
        Lab mockLab = DomainLabFactory.buildValidLabWithId(labId);
        when(labRepo.findById(labId)).thenReturn(Optional.of(mockLab));
        return mockLab;
    }

    /**
     * 출석 기록 관련 전체 의존성 체인을 한번에 모킹합니다.
     * AttendanceRecord → AttendanceSession → Lab, User 관계
     * 
     * @param recordRepo AttendanceRecordRepositoryPort mock
     * @param sessionRepo AttendanceSessionRepositoryPort mock
     * @param userRepo UserRepositoryPort mock
     * @param labRepo LabRepositoryPort mock
     */
    public static MockChainResult mockFullAttendanceChain(
            AttendanceRecordRepositoryPort recordRepo,
            AttendanceSessionRepositoryPort sessionRepo, 
            UserRepositoryPort userRepo,
            LabRepositoryPort labRepo) {
        
        return mockFullAttendanceChain(recordRepo, sessionRepo, userRepo, labRepo, AttendanceStatus.PRESENT);
    }
    
    /**
     * 특정 초기 상태로 출석 기록 관련 전체 의존성 체인을 한번에 모킹합니다.
     * 
     * @param recordRepo AttendanceRecordRepositoryPort mock
     * @param sessionRepo AttendanceSessionRepositoryPort mock
     * @param userRepo UserRepositoryPort mock
     * @param labRepo LabRepositoryPort mock
     * @param initialStatus 출석 기록 초기 상태
     */
    public static MockChainResult mockFullAttendanceChain(
            AttendanceRecordRepositoryPort recordRepo,
            AttendanceSessionRepositoryPort sessionRepo, 
            UserRepositoryPort userRepo,
            LabRepositoryPort labRepo,
            AttendanceStatus initialStatus) {
        
        // 1. Lab 생성 (ID 설정)
        Lab lab = DomainLabFactory.buildValidLabWithId(DEFAULT_LAB_ID);
        
        // 2. 해당 Lab에 할당된 User 생성 (권한 확보)
        User user = DomainUserFactory.buildLabManagerWithLab(lab);
        
        // 3. AttendanceSession 생성 (동일한 lab 사용, ID 설정)
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithLab(lab);
        org.springframework.test.util.ReflectionTestUtils.setField(session, "sessionId", DEFAULT_SESSION_ID);
        
        // 4. AttendanceRecord 생성 (session 사용, 초기 상태 조정)
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSession(session);
        org.springframework.test.util.ReflectionTestUtils.setField(record, "recordId", DEFAULT_RECORD_ID);
        // 초기 상태가 PRESENT가 아닌 경우 상태 조정
        if (initialStatus != AttendanceStatus.PRESENT) {
            // ReflectionTestUtils로 상태 강제 변경 (테스트용)
            org.springframework.test.util.ReflectionTestUtils.setField(record, "status", initialStatus);
            org.springframework.test.util.ReflectionTestUtils.setField(record, "isManuallyAdjusted", false);
        }
        
        // 5. Repository mock 설정
        when(recordRepo.findById(DEFAULT_RECORD_ID)).thenReturn(Optional.of(record));
        when(recordRepo.save(any(AttendanceRecord.class))).thenReturn(record);
        
        when(sessionRepo.findById(anyLong())).thenReturn(Optional.of(session));
        when(sessionRepo.save(any(AttendanceSession.class))).thenReturn(session);
        
        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        
        when(labRepo.findById(anyLong())).thenReturn(Optional.of(lab));
        
        return new MockChainResult(record, session, user, lab);
    }

    /**
     * AttendanceSession 미존재 상황을 모킹합니다.
     */
    public static void mockSessionNotFound(AttendanceSessionRepositoryPort sessionRepo, Long sessionId) {
        when(sessionRepo.findById(sessionId)).thenReturn(Optional.empty());
    }

    /**
     * Lab 미존재 상황을 모킹합니다.
     */
    public static void mockLabNotFound(LabRepositoryPort labRepo, Long labId) {
        when(labRepo.findById(labId)).thenReturn(Optional.empty());
    }

    /**
     * Mock 체인 결과를 담는 데이터 클래스
     */
    public static class MockChainResult {
        public final AttendanceRecord record;
        public final AttendanceSession session;
        public final User user;
        public final Lab lab;

        public MockChainResult(AttendanceRecord record, AttendanceSession session, User user, Lab lab) {
            this.record = record;
            this.session = session;
            this.user = user;
            this.lab = lab;
        }
    }
}
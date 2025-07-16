package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.time.LocalDateTime;

/**
 * IntegrationAttendanceFactory - Repository/통합 테스트 전용 Attendance 테스트 팩토리
 * - DomainAttendanceFactory.build*() 를 호출하여 도메인 객체 생성 후, 저장(persist) 기능만 제공합니다.
 */
public final class IntegrationAttendanceFactory {
    private IntegrationAttendanceFactory() {
    }

    // AttendanceSession 관련 persist 메서드들
    public static AttendanceSession persistValidSession(AttendanceSessionRepositoryPort repo) {
        AttendanceSession session = DomainAttendanceFactory.buildValidSession();
        return repo.save(session);
    }

    public static AttendanceSession persistValidSession(TestEntityManager em) {
        AttendanceSession session = DomainAttendanceFactory.buildValidSession();
        em.persist(session);
        em.flush();
        return session;
    }

    public static AttendanceSession persistSessionWithLab(AttendanceSessionRepositoryPort repo, Lab lab) {
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithLab(lab);
        return repo.save(session);
    }

    public static AttendanceSession persistSessionWithLab(TestEntityManager em, Lab lab) {
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithLab(lab);
        em.persist(session);
        em.flush();
        return session;
    }

    public static AttendanceSession persistSessionWithTitle(AttendanceSessionRepositoryPort repo, String title) {
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithTitle(title);
        return repo.save(session);
    }

    public static AttendanceSession persistSessionWithTitle(TestEntityManager em, String title) {
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithTitle(title);
        em.persist(session);
        em.flush();
        return session;
    }

    public static AttendanceSession persistSessionWithCreator(AttendanceSessionRepositoryPort repo, Long createdBy) {
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithCreator(createdBy);
        return repo.save(session);
    }

    public static AttendanceSession persistSessionWithCreator(TestEntityManager em, Long createdBy) {
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithCreator(createdBy);
        em.persist(session);
        em.flush();
        return session;
    }

    // AttendanceRecord 관련 persist 메서드들
    public static AttendanceRecord persistValidRecord(AttendanceRecordRepositoryPort repo) {
        AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
        return repo.save(record);
    }

    public static AttendanceRecord persistValidRecord(TestEntityManager em) {
        AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
        em.persist(record);
        em.flush();
        return record;
    }

    public static AttendanceRecord persistRecordWithSession(AttendanceRecordRepositoryPort repo, AttendanceSession session) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSession(session);
        return repo.save(record);
    }

    public static AttendanceRecord persistRecordWithSession(TestEntityManager em, AttendanceSession session) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSession(session);
        em.persist(record);
        em.flush();
        return record;
    }

    public static AttendanceRecord persistRecordWithUser(AttendanceRecordRepositoryPort repo, Long userId) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithUser(userId);
        return repo.save(record);
    }

    public static AttendanceRecord persistRecordWithUser(TestEntityManager em, Long userId) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithUser(userId);
        em.persist(record);
        em.flush();
        return record;
    }

    public static AttendanceRecord persistRecordWithTime(AttendanceRecordRepositoryPort repo, LocalDateTime checkedAt) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithTime(checkedAt);
        return repo.save(record);
    }

    public static AttendanceRecord persistRecordWithTime(TestEntityManager em, LocalDateTime checkedAt) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithTime(checkedAt);
        em.persist(record);
        em.flush();
        return record;
    }

    public static AttendanceRecord persistRecordWithSessionAndUser(AttendanceRecordRepositoryPort repo, AttendanceSession session, Long userId) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSessionAndUser(session, userId);
        return repo.save(record);
    }

    public static AttendanceRecord persistRecordWithSessionAndUser(TestEntityManager em, AttendanceSession session, Long userId) {
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSessionAndUser(session, userId);
        em.persist(record);
        em.flush();
        return record;
    }

    // 복합 객체 생성 헬퍼 메서드들
    public static SessionWithRecords persistSessionWithRecords(AttendanceSessionRepositoryPort sessionRepo,
                                                               AttendanceRecordRepositoryPort recordRepo,
                                                               Lab lab, int recordCount) {
        AttendanceSession session = persistSessionWithLab(sessionRepo, lab);

        AttendanceRecord[] records = new AttendanceRecord[recordCount];
        for (int i = 0; i < recordCount; i++) {
            Long userId = 100L + i; // 테스트용 사용자 ID
            records[i] = persistRecordWithSessionAndUser(recordRepo, session, userId);
        }

        return new SessionWithRecords(session, records);
    }

    public static SessionWithRecords persistSessionWithRecords(TestEntityManager em,
                                                               Lab lab, int recordCount) {
        AttendanceSession session = persistSessionWithLab(em, lab);

        AttendanceRecord[] records = new AttendanceRecord[recordCount];
        for (int i = 0; i < recordCount; i++) {
            Long userId = 100L + i; // 테스트용 사용자 ID
            records[i] = persistRecordWithSessionAndUser(em, session, userId);
        }

        return new SessionWithRecords(session, records);
    }

    // 결과를 담는 데이터 클래스
    public static class SessionWithRecords {
        public final AttendanceSession session;
        public final AttendanceRecord[] records;

        public SessionWithRecords(AttendanceSession session, AttendanceRecord[] records) {
            this.session = session;
            this.records = records;
        }
    }
}
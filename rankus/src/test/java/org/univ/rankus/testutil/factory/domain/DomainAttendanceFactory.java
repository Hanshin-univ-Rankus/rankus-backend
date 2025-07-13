package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.time.LocalDateTime;

/**
 * DomainAttendanceFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 Attendance 관련 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainAttendanceFactory {

    private DomainAttendanceFactory() {
    }

    // 일관된 기본값 사용
    public static final Long DEFAULT_LAB_ID = 1L;
    public static final Long DEFAULT_CREATOR_ID = 1L;
    public static final Long DEFAULT_USER_ID = 2L;
    public static final String DEFAULT_TITLE = "테스트 출석";
    public static final Integer DEFAULT_QR_VALIDITY = 5;

    // AttendanceSession 생성 메서드들
    public static AttendanceSession buildValidSession() {
        return AttendanceSession.create(DEFAULT_LAB_ID, DEFAULT_CREATOR_ID, DEFAULT_TITLE, DEFAULT_QR_VALIDITY);
    }

    public static AttendanceSession buildValidSessionWithId(Long sessionId) {
        AttendanceSession session = buildValidSession();
        ReflectionTestUtils.setField(session, "sessionId", sessionId);
        return session;
    }

    public static AttendanceSession buildSessionWithLab(Lab lab) {
        AttendanceSession session = AttendanceSession.create(lab.getId(), DEFAULT_CREATOR_ID, DEFAULT_TITLE, DEFAULT_QR_VALIDITY);
        ReflectionTestUtils.setField(session, "sessionId", DEFAULT_LAB_ID);
        return session;
    }

    public static AttendanceSession buildSessionWithTitle(String title) {
        return AttendanceSession.create(DEFAULT_LAB_ID, DEFAULT_CREATOR_ID, title, DEFAULT_QR_VALIDITY);
    }

    public static AttendanceSession buildSessionWithValidityMinutes(Integer validityMinutes) {
        return AttendanceSession.create(DEFAULT_LAB_ID, DEFAULT_CREATOR_ID, DEFAULT_TITLE, validityMinutes);
    }

    public static AttendanceSession buildSessionWithCreator(Long createdBy) {
        return AttendanceSession.create(DEFAULT_LAB_ID, createdBy, DEFAULT_TITLE, DEFAULT_QR_VALIDITY);
    }

    public static AttendanceSession buildValidSessionWithCreator(Long createdBy) {
        return AttendanceSession.create(DEFAULT_LAB_ID, createdBy, DEFAULT_TITLE, DEFAULT_QR_VALIDITY);
    }

    // AttendanceRecord 생성 메서드들
    public static AttendanceRecord buildValidRecord() {
        AttendanceSession session = buildValidSessionWithId(DEFAULT_LAB_ID);
        return AttendanceRecord.create(session, DEFAULT_USER_ID, LocalDateTime.now());
    }

    public static AttendanceRecord buildValidRecordWithId(Long recordId) {
        AttendanceRecord record = buildValidRecord();
        ReflectionTestUtils.setField(record, "recordId", recordId);
        return record;
    }

    public static AttendanceRecord buildRecordWithSession(AttendanceSession session) {
        return AttendanceRecord.create(session, DEFAULT_USER_ID, LocalDateTime.now());
    }

    public static AttendanceRecord buildRecordWithUser(Long userId) {
        AttendanceSession session = buildValidSessionWithId(DEFAULT_LAB_ID);
        return AttendanceRecord.create(session, userId, LocalDateTime.now());
    }

    public static AttendanceRecord buildRecordWithTime(LocalDateTime checkedAt) {
        AttendanceSession session = buildValidSessionWithId(DEFAULT_LAB_ID);
        return AttendanceRecord.create(session, DEFAULT_USER_ID, checkedAt);
    }

    public static AttendanceRecord buildRecordWithSessionAndUser(AttendanceSession session, Long userId) {
        return AttendanceRecord.create(session, userId, LocalDateTime.now());
    }

    public static AttendanceRecord buildAbsentRecord() {
        AttendanceRecord record = buildValidRecord();
        record.markAsAbsent(DEFAULT_CREATOR_ID, "테스트용 결석 상태");
        return record;
    }

    public static AttendanceRecord buildLateRecord() {
        AttendanceRecord record = buildValidRecord();
        record.markAsLate(DEFAULT_CREATOR_ID, "테스트용 지각 상태");
        return record;
    }

    public static AttendanceRecord buildAbsentRecordWithId(Long recordId) {
        AttendanceRecord record = buildAbsentRecord();
        ReflectionTestUtils.setField(record, "recordId", recordId);
        return record;
    }

    public static AttendanceRecord buildLateRecordWithId(Long recordId) {
        AttendanceRecord record = buildLateRecord();
        ReflectionTestUtils.setField(record, "recordId", recordId);
        return record;
    }

    // QRToken 생성 메서드들
    public static QRToken buildValidToken() {
        return QRToken.create(DEFAULT_LAB_ID, DEFAULT_LAB_ID, DEFAULT_QR_VALIDITY);
    }

    public static QRToken buildTokenWithLabId(Long labId) {
        return QRToken.create(labId, DEFAULT_LAB_ID, DEFAULT_QR_VALIDITY);
    }

    public static QRToken buildTokenWithSessionId(Long sessionId) {
        return QRToken.create(DEFAULT_LAB_ID, sessionId, DEFAULT_QR_VALIDITY);
    }

    public static QRToken buildTokenWithValidityMinutes(Integer validityMinutes) {
        return QRToken.create(DEFAULT_LAB_ID, DEFAULT_LAB_ID, validityMinutes);
    }

    public static QRToken buildTokenWithLabAndSession(Long labId, Long sessionId) {
        return QRToken.create(labId, sessionId, DEFAULT_QR_VALIDITY);
    }

    // 빌더 패턴 지원
    public static AttendanceSessionBuilder sessionBuilder() {
        return new AttendanceSessionBuilder();
    }

    public static AttendanceRecordBuilder recordBuilder() {
        return new AttendanceRecordBuilder();
    }

    public static QRTokenBuilder tokenBuilder() {
        return new QRTokenBuilder();
    }

    // AttendanceSession 빌더
    public static class AttendanceSessionBuilder {
        private Long labId = DEFAULT_LAB_ID;
        private Long createdBy = DEFAULT_CREATOR_ID;
        private String title = DEFAULT_TITLE;
        private Integer qrValidityMinutes = DEFAULT_QR_VALIDITY;

        public AttendanceSessionBuilder labId(Long labId) {
            this.labId = labId;
            return this;
        }

        public AttendanceSessionBuilder createdBy(Long createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public AttendanceSessionBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AttendanceSessionBuilder qrValidityMinutes(Integer qrValidityMinutes) {
            this.qrValidityMinutes = qrValidityMinutes;
            return this;
        }

        public AttendanceSession build() {
            return AttendanceSession.create(labId, createdBy, title, qrValidityMinutes);
        }
    }

    // AttendanceRecord 빌더
    public static class AttendanceRecordBuilder {
        private AttendanceSession session = buildValidSessionWithId(DEFAULT_LAB_ID);
        private Long userId = DEFAULT_USER_ID;
        private LocalDateTime checkedAt = LocalDateTime.now();

        public AttendanceRecordBuilder session(AttendanceSession session) {
            this.session = session;
            return this;
        }

        public AttendanceRecordBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AttendanceRecordBuilder checkedAt(LocalDateTime checkedAt) {
            this.checkedAt = checkedAt;
            return this;
        }

        public AttendanceRecord build() {
            return AttendanceRecord.create(session, userId, checkedAt);
        }
    }

    // QRToken 빌더
    public static class QRTokenBuilder {
        private Long labId = DEFAULT_LAB_ID;
        private Long sessionId = DEFAULT_LAB_ID;
        private Integer validityMinutes = DEFAULT_QR_VALIDITY;

        public QRTokenBuilder labId(Long labId) {
            this.labId = labId;
            return this;
        }

        public QRTokenBuilder sessionId(Long sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public QRTokenBuilder validityMinutes(Integer validityMinutes) {
            this.validityMinutes = validityMinutes;
            return this;
        }

        public QRToken build() {
            return QRToken.create(labId, sessionId, validityMinutes);
        }
    }
}
-- 1. labs
INSERT INTO labs (id, name, category, description, ranking, professor_name, created_at, updated_at)
VALUES (1, 'AI랩', 'AI', '인공지능 랩', 1, null, NOW(), NOW()),
       (2, 'DB랩', 'DB', '데이터베이스 랩', 2, null, NOW(), NOW());

-- 2. users
INSERT INTO users (id, name, email, password_hash, role, lab_id, student_number, phone_number, grade, enrollment_status,
                   created_at, updated_at)
VALUES (1, '학생1', 'user1@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'STUDENT', NULL,
        '20201001', '010-1234-5678', 3, 'ENROLLED', NOW(), NOW()),
       (2, '랩장1', 'leader1@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'LAB_LEADER',
        1, '20191002', '010-2345-6789', 4, 'ENROLLED', NOW(), NOW()),
       (3, '교수', 'prof@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'PROFESSOR', 2,
        '19951003', '010-3456-7890', 8, 'ENROLLED', NOW(), NOW()),
       (4, '학생2', 'user2@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'STUDENT', NULL,
        '20211004', '010-4567-8901', 2, 'ENROLLED', NOW(), NOW()),
       (5, '학생3', 'user3@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'STUDENT', 1,
        '20201005', '010-5678-9012', 3, 'ON_LEAVE', NOW(), NOW()),
       (6, '랩장2', 'leader2@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'LAB_LEADER',
        2, '20181006', '010-6789-0123', 5, 'ENROLLED', NOW(), NOW()),
       (7, '관리자', 'admin@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'ADMIN', NULL,
        '20151007', '010-7890-1234', 6, 'ENROLLED', NOW(), NOW());

-- 3. lab_images
INSERT INTO lab_images (id, lab_id, image_url, type)
VALUES (1, 1, 'https://example.com/lab1_img1.png', 'REPRESENTATIVE'),
       (2, 1, 'https://example.com/lab1_img2.png', 'ADDITIONAL'),
       (3, 2, 'https://example.com/lab2_img1.png', 'REPRESENTATIVE');

-- 4. interviews (면접 일정)
INSERT INTO interviews (id, lab_id, start_date, end_date, duration_minutes, max_applicants_per_slot, status, created_at,
                        updated_at)
VALUES (1, 1, '2025-07-15', '2025-07-16', 30, 2, 'ACTIVE', NOW(), NOW()),
       (2, 2, '2025-07-20', '2025-07-21', 45, 1, 'ACTIVE', NOW(), NOW());

-- 5. interview_slots (면접 슬롯)
INSERT INTO interview_slots (id, interview_id, start_time, end_time, max_applicants, current_applicants, status,
                             created_at, updated_at)
VALUES (1, 1, '2025-07-15 09:00:00', '2025-07-15 09:30:00', 2, 1, 'AVAILABLE', NOW(), NOW()),
       (2, 1, '2025-07-15 10:00:00', '2025-07-15 10:30:00', 2, 0, 'AVAILABLE', NOW(), NOW()),
       (3, 1, '2025-07-15 14:00:00', '2025-07-15 14:30:00', 2, 2, 'FULL', NOW(), NOW()),
       (4, 2, '2025-07-20 13:00:00', '2025-07-20 13:45:00', 1, 0, 'AVAILABLE', NOW(), NOW()),
       (5, 2, '2025-07-20 15:00:00', '2025-07-20 15:45:00', 1, 1, 'FULL', NOW(), NOW());

-- 6. lab_applications (새로운 스키마에 맞춘 지원서)
INSERT INTO lab_applications (id, lab_id, user_id, interview_slot_id, status, created_at, updated_at)
VALUES (1, 1, 1, 1, 'PENDING', NOW(), NOW()),
       (2, 1, 4, 3, 'APPROVED', NOW(), NOW()),
       (3, 1, 5, 3, 'PENDING', NOW(), NOW()),
       (4, 2, 1, 5, 'PENDING', NOW(), NOW());

-- 5. lab_notices
INSERT INTO lab_notices (id, title, content, type, pinned, author_id, lab_id, created_at, updated_at)
VALUES (1, 'AI랩 정기 미팅 안내', '매주 월요일 오후 2시에 정기 미팅을 진행합니다. 모든 랩원은 필수 참석 바랍니다.', 'NORMAL', false, 2, 1, NOW(), NOW()),
       (2, '[긴급] 프로젝트 발표 일정 변경', '다음 주 금요일로 예정된 프로젝트 발표가 화요일로 변경되었습니다. 준비 부탁드립니다.', 'URGENT', true, 2, 1, NOW(), NOW()),
       (3, '새로운 연구 주제 모집', 'GPT 관련 새로운 연구 주제를 모집합니다. 관심 있는 분은 연락 바랍니다.', 'NORMAL', false, 2, 1, NOW(), NOW()),
       (4, 'DB랩 세미나 일정', '이번 달 세미나는 매주 수요일 오후 3시에 진행됩니다.', 'NORMAL', false, 3, 2, NOW(), NOW()),
       (5, '[중요] 서버 점검 안내', '내일 오후 6시부터 자정까지 서버 점검이 있습니다. 작업 저장 후 로그아웃 바랍니다.', 'URGENT', true, 6, 2, NOW(), NOW()),
       (6, '데이터베이스 최적화 스터디', '매주 목요일 저녁 7시에 데이터베이스 최적화 스터디를 진행합니다.', 'NORMAL', false, 3, 2, NOW(), NOW());

-- 8. attendance_sessions (출석 세션)
INSERT INTO attendance_sessions (session_id, lab_id, created_by, title, start_time, end_time, qr_validity_minutes, status, created_at, updated_at)
VALUES (1, 1, 2, 'AI랩 정기 미팅', '2025-07-18 14:00:00', '2025-07-18 16:00:00', 5, 'COMPLETED', NOW(), NOW()),
       (2, 1, 2, '연구 진행 상황 점검', '2025-07-19 10:00:00', NULL, 3, 'ACTIVE', NOW(), NOW()),
       (3, 2, 6, 'DB랩 세미나', '2025-07-17 15:00:00', '2025-07-17 17:00:00', 5, 'COMPLETED', NOW(), NOW()),
       (4, 2, 3, '교수님과의 면담', '2025-07-18 13:00:00', NULL, 10, 'ACTIVE', NOW(), NOW()),
       (5, 1, 2, '프로젝트 발표회', '2025-07-16 09:00:00', '2025-07-16 09:30:00', 5, 'CANCELLED', NOW(), NOW());

-- 9. attendance_records (출석 기록)
INSERT INTO attendance_records (record_id, session_id, user_id, checked_at, status, is_manually_adjusted, adjustment_reason, adjusted_by, adjusted_at, created_at, updated_at)
VALUES (1, 1, 5, '2025-07-18 14:02:00', 'PRESENT', false, NULL, NULL, NULL, NOW(), NOW()),
       (2, 1, 1, '2025-07-18 14:08:00', 'LATE', false, NULL, NULL, NULL, NOW(), NOW()),
       (3, 3, 6, '2025-07-17 15:00:00', 'PRESENT', false, NULL, NULL, NULL, NOW(), NOW()),
       (4, 3, 3, '2025-07-17 15:00:00', 'PRESENT', false, NULL, NULL, NULL, NOW(), NOW()),
       (5, 1, 4, '2025-07-18 14:00:00', 'ABSENT', true, '사전 휴가 신청', 2, NOW(), NOW(), NOW());

-- 10. score_submissions (점수 신청)
INSERT INTO score_submissions (id, user_id, lab_id, category, achievement_description, achievement_date, proof_file_url, application_reason, related_link, status, visibility, approved_by, approved_at, submitted_at, expires_at, correction_used, correction_count, rejection_reason, created_at, updated_at)
VALUES (1, 1, 1, 'ACADEMIC_ACHIEVEMENT', '인공지능 과목 A+ 성적 취득', '2025-06-15', 'https://example.com/proof1.pdf', '학업 성과를 인정받고 싶습니다', 'https://portal.example.com/grades', 'APPROVED', 'PUBLIC', 2, NOW(), DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 5 MONTH), false, 0, NULL, NOW(), NOW()),
       (2, 5, 1, 'CONTEST_INTERNAL_WINNER', '2025 교내 AI 해커톤 대상', '2025-05-20', 'https://example.com/proof2.pdf', '대회 수상으로 랩실 기여', 'https://hackathon.example.com/results', 'APPROVED', 'LAB_ONLY', 2, NOW(), DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_ADD(NOW(), INTERVAL 4 MONTH), false, 0, NULL, NOW(), NOW()),
       (3, 4, 2, 'CERTIFICATION_NATIONAL', '정보처리기사 자격증 취득', '2025-04-10', 'https://example.com/proof3.pdf', NULL, NULL, 'PENDING', 'PRIVATE', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 WEEK), DATE_ADD(NOW(), INTERVAL 5 MONTH), false, 0, NULL, NOW(), NOW()),
       (4, 1, 1, 'RESEARCH_GENERAL_PAPER', '머신러닝 관련 논문 게재', '2025-03-15', 'https://example.com/proof4.pdf', '연구 성과 공유', 'https://journal.example.com/paper123', 'REJECTED', 'PUBLIC', 2, NOW(), DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_ADD(NOW(), INTERVAL 3 MONTH), false, 0, '증빙 자료 부족', NOW(), NOW());

-- 11. calendar_events (캘린더 이벤트)
INSERT INTO calendar_events (id, lab_id, type, title, description, event_date, start_time, end_time, interview_id, created_at, updated_at)
VALUES (1, 1, 'SCHEDULE', 'AI랩 정기 미팅', '매주 월요일 정기 미팅입니다.', '2025-07-21', NULL, NULL, NULL, NOW(), NOW()),
       (2, 1, 'SCHEDULE', '연구 성과 발표', '이번 달 연구 성과를 발표하는 시간입니다.', '2025-07-25', NULL, NULL, NULL, NOW(), NOW()),
       (3, 2, 'SCHEDULE', 'DB랩 세미나', '데이터베이스 최적화 주제 세미나', '2025-07-24', NULL, NULL, NULL, NOW(), NOW()),
       (4, 1, 'INTERVIEW', 'AI랩 면접', '2025년 하반기 AI랩 면접 일정', '2025-07-15', '09:00:00', '17:00:00', 1, NOW(), NOW()),
       (5, 2, 'INTERVIEW', 'DB랩 면접', '2025년 하반기 DB랩 면접 일정', '2025-07-20', '13:00:00', '16:00:00', 2, NOW(), NOW());

-- 12. lab_creation_requests (랩실 생성 신청)
INSERT INTO lab_creation_requests (id, requested_lab_name, requested_category, requested_description, requester_id, status, processed_at, processed_by_id, rejection_reason, created_at, updated_at)
VALUES (1, '로봇공학랩', 'ROBOTICS', '로봇공학 연구를 위한 랩실입니다.', 1, 'APPROVED', NOW(), 7, NULL, DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),
       (2, '블록체인랩', 'COMPUTER_SCIENCE', '블록체인 기술 연구 및 개발', 4, 'PENDING', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()),
       (3, '보안랩', 'SECURITY', '정보보안 및 사이버보안 연구', 5, 'REJECTED', NOW(), 3, '기존 보안 랩실이 이미 존재합니다.', DATE_SUB(NOW(), INTERVAL 3 WEEK), NOW()),
       (4, '게임개발랩', 'GAME', '게임 개발 및 엔진 연구', 1, 'APPROVED', NOW(), 7, NULL, DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW());

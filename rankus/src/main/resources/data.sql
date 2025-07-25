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
                             version, created_at, updated_at)
VALUES (1, 1, '2025-07-15 09:00:00', '2025-07-15 09:30:00', 2, 1, 'AVAILABLE', 0, NOW(), NOW()),
       (2, 1, '2025-07-15 10:00:00', '2025-07-15 10:30:00', 2, 0, 'AVAILABLE', 0, NOW(), NOW()),
       (3, 1, '2025-07-15 14:00:00', '2025-07-15 14:30:00', 2, 2, 'FULL', 0, NOW(), NOW()),
       (4, 2, '2025-07-20 13:00:00', '2025-07-20 13:45:00', 1, 0, 'AVAILABLE', 0, NOW(), NOW()),
       (5, 2, '2025-07-20 15:00:00', '2025-07-20 15:45:00', 1, 1, 'FULL', 0, NOW(), NOW());

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
INSERT INTO attendance_sessions (session_id, lab_id, created_by, title, start_time, end_time, qr_validity_minutes,
                                 status, created_at, updated_at)
VALUES (1, 1, 2, 'AI랩 정기 미팅', '2025-07-18 14:00:00', '2025-07-18 16:00:00', 5, 'COMPLETED', NOW(), NOW()),
       (2, 1, 2, '연구 진행 상황 점검', '2025-07-19 10:00:00', NULL, 3, 'ACTIVE', NOW(), NOW()),
       (3, 2, 6, 'DB랩 세미나', '2025-07-17 15:00:00', '2025-07-17 17:00:00', 5, 'COMPLETED', NOW(), NOW()),
       (4, 2, 3, '교수님과의 면담', '2025-07-18 13:00:00', NULL, 10, 'ACTIVE', NOW(), NOW()),
       (5, 1, 2, '프로젝트 발표회', '2025-07-16 09:00:00', '2025-07-16 09:30:00', 5, 'CANCELLED', NOW(), NOW());

-- 9. attendance_records (출석 기록)
INSERT INTO attendance_records (record_id, session_id, user_id, checked_at, status, is_manually_adjusted,
                                adjustment_reason, adjusted_by, adjusted_at, created_at, updated_at)
VALUES (1, 1, 5, '2025-07-18 14:02:00', 'PRESENT', false, NULL, NULL, NULL, NOW(), NOW()),
       (2, 1, 1, '2025-07-18 14:08:00', 'LATE', false, NULL, NULL, NULL, NOW(), NOW()),
       (3, 3, 6, '2025-07-17 15:00:00', 'PRESENT', false, NULL, NULL, NULL, NOW(), NOW()),
       (4, 3, 3, '2025-07-17 15:00:00', 'PRESENT', false, NULL, NULL, NULL, NOW(), NOW()),
       (5, 1, 4, '2025-07-18 14:00:00', 'ABSENT', true, '사전 휴가 신청', 2, NOW(), NOW(), NOW());

-- 10. score_submissions (점수 신청)
INSERT INTO score_submissions (id, user_id, lab_id, category, achievement_description, achievement_date, proof_file_url,
                               application_reason, related_link, status, visibility, approved_by, approved_at,
                               submitted_at, expires_at, correction_used, correction_count, rejection_reason,
                               created_at, updated_at)
VALUES (1, 1, 1, 'ACADEMIC_ACHIEVEMENT', '인공지능 과목 A+ 성적 취득', '2025-06-15', 'https://example.com/proof1.pdf',
        '학업 성과를 인정받고 싶습니다', 'https://portal.example.com/grades', 'APPROVED', 'PUBLIC', 2, NOW(),
        DATE_SUB(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 5 MONTH), false, 0, NULL, NOW(), NOW()),
       (2, 5, 1, 'CONTEST_INTERNAL_WINNER', '2025 교내 AI 해커톤 대상', '2025-05-20', 'https://example.com/proof2.pdf',
        '대회 수상으로 랩실 기여', 'https://hackathon.example.com/results', 'APPROVED', 'LAB_ONLY', 2, NOW(),
        DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_ADD(NOW(), INTERVAL 4 MONTH), false, 0, NULL, NOW(), NOW()),
       (3, 4, 2, 'CERTIFICATION_NATIONAL', '정보처리기사 자격증 취득', '2025-04-10', 'https://example.com/proof3.pdf', NULL, NULL,
        'PENDING', 'PRIVATE', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 WEEK), DATE_ADD(NOW(), INTERVAL 5 MONTH), false, 0,
        NULL, NOW(), NOW()),
       (4, 1, 1, 'RESEARCH_GENERAL_PAPER', '머신러닝 관련 논문 게재', '2025-03-15', 'https://example.com/proof4.pdf', '연구 성과 공유',
        'https://journal.example.com/paper123', 'REJECTED', 'PUBLIC', 2, NOW(), DATE_SUB(NOW(), INTERVAL 3 MONTH),
        DATE_ADD(NOW(), INTERVAL 3 MONTH), false, 0, '증빙 자료 부족', NOW(), NOW());

-- 11. calendar_events (캘린더 이벤트)
INSERT INTO calendar_events (id, lab_id, type, title, description, event_date, start_time, end_time, interview_id,
                             created_at, updated_at)
VALUES (1, 1, 'SCHEDULE', 'AI랩 정기 미팅', '매주 월요일 정기 미팅입니다.', '2025-07-21', NULL, NULL, NULL, NOW(), NOW()),
       (2, 1, 'SCHEDULE', '연구 성과 발표', '이번 달 연구 성과를 발표하는 시간입니다.', '2025-07-25', NULL, NULL, NULL, NOW(), NOW()),
       (3, 2, 'SCHEDULE', 'DB랩 세미나', '데이터베이스 최적화 주제 세미나', '2025-07-24', NULL, NULL, NULL, NOW(), NOW()),
       (4, 1, 'INTERVIEW', 'AI랩 면접', '2025년 하반기 AI랩 면접 일정', '2025-07-15', '09:00:00', '17:00:00', 1, NOW(), NOW()),
       (5, 2, 'INTERVIEW', 'DB랩 면접', '2025년 하반기 DB랩 면접 일정', '2025-07-20', '13:00:00', '16:00:00', 2, NOW(), NOW());

-- 12. lab_creation_requests (랩실 생성 신청)
INSERT INTO lab_creation_requests (id, requested_lab_name, requested_category, requested_description, requester_id,
                                   status, processed_at, processed_by_id, rejection_reason, version, created_at,
                                   updated_at)
VALUES (1, '로봇공학랩', 'ROBOTICS', '로봇공학 연구를 위한 랩실입니다.', 1, 'APPROVED', NOW(), 7, NULL, 0,
        DATE_SUB(NOW(), INTERVAL 1 MONTH),
        NOW()),
       (2, '블록체인랩', 'COMPUTER_SCIENCE', '블록체인 기술 연구 및 개발', 4, 'PENDING', NULL, NULL, NULL, 0,
        DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()),
       (3, '보안랩', 'SECURITY', '정보보안 및 사이버보안 연구', 5, 'REJECTED', NOW(), 3, '기존 보안 랩실이 이미 존재합니다.', 0,
        DATE_SUB(NOW(), INTERVAL 3 WEEK), NOW()),
       (4, '게임개발랩', 'GAME', '게임 개발 및 엔진 연구', 1, 'APPROVED', NOW(), 7, NULL, 0, DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW());

-- 13. votes (투표)
INSERT INTO votes (id, creator_id, lab_id, title, description, status, deadline, total_votes, created_at, updated_at)
VALUES (1, 2, 1, '2025년 하반기 연구 주제 선정', 'AI랩의 하반기 연구 주제를 결정하는 투표입니다. 각자의 관심 분야와 연구 가능성을 고려하여 투표해주세요.', 'ACTIVE',
        '2025-08-01 23:59:59', 0, NOW(), NOW()),
       (2, 3, 2, '신규 서버 장비 구매 우선순위', '랩실 확장을 위해 신규 서버 장비 구매가 결정되었습니다. 우선순위를 정하기 위한 투표입니다.', 'ACTIVE',
        '2025-07-25 18:00:00', 0, NOW(), NOW()),
       (3, 2, 1, '정기 미팅 시간 조정', '랩원들의 스케줄 변경으로 인해 정기 미팅 시간을 조정하고자 합니다. 가장 적합한 시간대를 선택해주세요.', 'ACTIVE',
        '2025-07-22 12:00:00', 0, NOW(), NOW()),
       (4, 2, 1, '여름휴가 기간 선택', 'AI랩 여름휴가 기간을 정하기 위한 투표였습니다.', 'CLOSED', '2025-07-10 23:59:59', 4,
        DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW()),
       (5, 6, 2, '연구실 청소 일정 결정', 'DB랩 정기 청소 일정을 정하기 위한 투표였습니다.', 'CLOSED', '2025-07-05 18:00:00', 3,
        DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()),
       (6, 2, 1, '외부 세미나 참석 여부', '외부 AI 세미나 참석 여부를 묻는 투표였으나, 예산 문제로 취소되었습니다.', 'CANCELED', '2025-07-20 23:59:59', 0,
        DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

-- 14. vote_options (투표 선택지)
INSERT INTO vote_options (id, vote_id, option_text, option_order, vote_count, created_at, updated_at)
VALUES
-- 투표 1: 2025년 하반기 연구 주제 선정 (ACTIVE)
(1, 1, '자연어 처리 및 언어 모델 연구', 1, 0, NOW(), NOW()),
(2, 1, '컴퓨터 비전 및 이미지 인식', 2, 0, NOW(), NOW()),
(3, 1, '강화학습 및 게임 AI', 3, 0, NOW(), NOW()),
(4, 1, '추천 시스템 및 데이터 마이닝', 4, 0, NOW(), NOW()),

-- 투표 2: 신규 서버 장비 구매 우선순위 (ACTIVE)
(5, 2, 'GPU 서버 (NVIDIA RTX 4090)', 1, 0, NOW(), NOW()),
(6, 2, '고성능 워크스테이션', 2, 0, NOW(), NOW()),
(7, 2, '스토리지 서버 확장', 3, 0, NOW(), NOW()),

-- 투표 3: 정기 미팅 시간 조정 (ACTIVE)
(8, 3, '월요일 오후 2시', 1, 0, NOW(), NOW()),
(9, 3, '화요일 오후 3시', 2, 0, NOW(), NOW()),
(10, 3, '수요일 오후 4시', 3, 0, NOW(), NOW()),

-- 투표 4: 여름휴가 기간 선택 (CLOSED)
(11, 4, '7월 마지막 주 (7/28-8/1)', 1, 2, DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW()),
(12, 4, '8월 첫째 주 (8/4-8/8)', 2, 2, DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW()),

-- 투표 5: 연구실 청소 일정 결정 (CLOSED)
(13, 5, '매주 금요일 오후', 1, 1, DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()),
(14, 5, '격주 토요일 오전', 2, 2, DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()),

-- 투표 6: 외부 세미나 참석 여부 (CANCELED)
(15, 6, '전체 참석', 1, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(16, 6, '선별적 참석', 2, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(17, 6, '불참', 3, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

-- 15. vote_participations (투표 참여 기록)
INSERT INTO vote_participations (id, vote_id, user_id, selected_option_id, created_at, updated_at)
VALUES
-- 투표 4: 여름휴가 기간 선택 (CLOSED) - 4명 참여
(1, 4, 2, 11, DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW()), -- 랩장1 → 7월 마지막 주
(2, 4, 5, 11, DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW()), -- 학생3 → 7월 마지막 주
(3, 4, 1, 12, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),  -- 학생1 → 8월 첫째 주
(4, 4, 4, 12, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),  -- 학생2 → 8월 첫째 주

-- 투표 5: 연구실 청소 일정 결정 (CLOSED) - 3명 참여
(5, 5, 6, 14, DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()), -- 랩장2 → 격주 토요일 오전
(6, 5, 3, 14, DATE_SUB(NOW(), INTERVAL 13 DAY), NOW()), -- 교수 → 격주 토요일 오전
(7, 5, 1, 13, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW());
-- 학생1 → 매주 금요일 오후

-- 16. lab_resources (랩실 자료)
INSERT INTO lab_resources (id, lab_id, uploader_id, title, description, file_name, file_url, file_size, category,
                           is_public, download_count, version, created_at, updated_at)
VALUES
-- AI랩(1) 자료들
(1, 1, 2, '머신러닝 기초 강의자료', '2024년 상반기 머신러닝 기초 강의 슬라이드입니다. 선형회귀부터 신경망까지 포함되어 있습니다.',
 '머신러닝_기초_2024상반기.pdf', 'https://example.com/files/ml_basics_2024_1.pdf', 5242880, 'LECTURE_NOTE', true, 15, 0,
 DATE_SUB(NOW(), INTERVAL 3 MONTH), NOW()),

(2, 1, 2, 'PyTorch 실습 가이드', 'PyTorch를 활용한 딥러닝 모델 구현 실습 가이드입니다. CNN, RNN 예제 코드가 포함되어 있습니다.',
 'pytorch_실습가이드_v2.1.pdf', 'https://example.com/files/pytorch_guide_v2.1.pdf', 8388608, 'REFERENCE', true, 28, 0,
 DATE_SUB(NOW(), INTERVAL 2 MONTH), NOW()),

(3, 1, 5, '자연어처리 과제 제출물', 'BERT 모델을 활용한 감정 분석 과제 최종 제출물입니다.',
 'NLP_과제_감정분석_BERT.zip', 'https://example.com/files/nlp_assignment_bert.zip', 15728640, 'ASSIGNMENT', false, 3, 0,
 DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),

(4, 1, 2, 'TensorFlow 2.0 설치 가이드', 'GPU 환경에서 TensorFlow 2.0 설치 및 환경 설정 가이드입니다.',
 'TensorFlow_2.0_설치가이드.docx', 'https://example.com/files/tf_install_guide.docx', 2097152, 'DOCUMENT', true, 42, 0,
 DATE_SUB(NOW(), INTERVAL 4 MONTH), NOW()),

(5, 1, 2, '연구 논문 템플릿', 'AI랩 연구 논문 작성을 위한 LaTeX 템플릿입니다. IEEE 형식을 따릅니다.',
 'AI_Lab_Paper_Template.zip', 'https://example.com/files/paper_template.zip', 1048576, 'RESEARCH', true, 12, 0,
 DATE_SUB(NOW(), INTERVAL 5 MONTH), NOW()),

(6, 1, 5, 'Anaconda 환경 설정', '머신러닝 프로젝트를 위한 Anaconda 가상환경 설정 방법입니다.',
 'anaconda_환경설정_가이드.pdf', 'https://example.com/files/anaconda_setup.pdf', 3145728, 'SOFTWARE', true, 35, 0,
 DATE_SUB(NOW(), INTERVAL 6 WEEK), NOW()),

-- DB랩(2) 자료들  
(7, 2, 3, 'MySQL 고급 최적화 기법', '대용량 데이터베이스 최적화를 위한 인덱싱, 쿼리 튜닝 기법을 다룹니다.',
 'MySQL_고급최적화_2024.pdf', 'https://example.com/files/mysql_optimization_2024.pdf', 12582912, 'LECTURE_NOTE', true, 67, 0,
 DATE_SUB(NOW(), INTERVAL 2 MONTH), NOW()),

(8, 2, 6, 'NoSQL 데이터베이스 비교 분석', 'MongoDB, Redis, Cassandra 등 주요 NoSQL 데이터베이스 특징 비교 자료입니다.',
 'NoSQL_비교분석_보고서.pptx', 'https://example.com/files/nosql_comparison.pptx', 7340032, 'RESEARCH', true, 24, 0,
 DATE_SUB(NOW(), INTERVAL 1 MONTH), NOW()),

(9, 2, 6, '데이터베이스 설계 과제', '온라인 쇼핑몰 데이터베이스 설계 과제 및 ERD 작성 가이드입니다.',
 'DB설계과제_쇼핑몰ERD.pdf', 'https://example.com/files/db_design_assignment.pdf', 4194304, 'ASSIGNMENT', false, 8, 0,
 DATE_SUB(NOW(), INTERVAL 3 WEEK), NOW()),

(10, 2, 3, 'PostgreSQL 실습 매뉴얼', 'PostgreSQL 고급 기능 활용을 위한 실습 매뉴얼입니다. JSON, 전문검색 등을 포함합니다.',
 'PostgreSQL_실습매뉴얼_v3.2.pdf', 'https://example.com/files/postgresql_manual_v3.2.pdf', 9437184, 'REFERENCE', true, 18, 0,
 DATE_SUB(NOW(), INTERVAL 5 WEEK), NOW()),

(11, 2, 6, 'Redis 캐싱 전략', '웹 애플리케이션에서 Redis를 활용한 효과적인 캐싱 전략과 구현 방법입니다.',
 'Redis_캐싱전략_구현가이드.md', 'https://example.com/files/redis_caching_strategy.md', 524288, 'DOCUMENT', true, 31, 0,
 DATE_SUB(NOW(), INTERVAL 4 WEEK), NOW()),

(12, 2, 3, 'DB 백업 및 복구 스크립트', '자동화된 데이터베이스 백업 및 복구를 위한 쉘 스크립트 모음입니다.',
 'DB_백업복구_스크립트.tar.gz', 'https://example.com/files/db_backup_scripts.tar.gz', 2621440, 'SOFTWARE', false, 5, 0,
 DATE_SUB(NOW(), INTERVAL 2 WEEK), NOW()),

-- 추가 다양한 자료들
(13, 1, 2, '딥러닝 논문 리뷰 모음', '2024년 상반기 주요 딥러닝 논문 리뷰 발표 자료 모음입니다.',
 '딥러닝논문리뷰_2024상반기.pptx', 'https://example.com/files/dl_paper_reviews_2024.pptx', 18874368, 'RESEARCH', true, 22, 0,
 DATE_SUB(NOW(), INTERVAL 1 WEEK), NOW()),

(14, 2, 6, '빅데이터 처리 프레임워크', 'Spark, Hadoop을 활용한 빅데이터 처리 방법론과 실습 예제입니다.',
 'BigData_Processing_Framework.zip', 'https://example.com/files/bigdata_framework.zip', 25165824, 'REFERENCE', true, 9,
 0,
 DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

(15, 1, 5, '컴퓨터 비전 프로젝트', 'OpenCV와 YOLO를 활용한 객체 인식 프로젝트 소스코드입니다.',
 'CV_프로젝트_객체인식_YOLO.zip', 'https://example.com/files/cv_project_yolo.zip', 31457280, 'ASSIGNMENT', false, 1, 0,
 NOW(), NOW());

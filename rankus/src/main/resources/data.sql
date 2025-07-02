-- 1. labs
INSERT INTO labs (id, name, category, description, ranking, professor_name, created_at, updated_at)
VALUES (1, 'AI랩', 'AI', '인공지능 랩', 1, null, NOW(), NOW()),
       (2, 'DB랩', 'DB', '데이터베이스 랩', 2, null, NOW(), NOW());

-- 2. users
INSERT INTO users (id, name, email, password_hash, role, lab_id, created_at, updated_at)
VALUES (1, '학생1', 'user1@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'STUDENT', NULL,
        NOW(), NOW()),
       (2, '랩장1', 'leader1@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy.', 'LAB_LEADER',
        1, NOW(), NOW()),
       (3, '교수', 'prof@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'PROFESSOR', 2,
        NOW(), NOW()),
       (4, '학생2', 'user2@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'STUDENT', NULL,
        NOW(), NOW()),
       (5, '학생3', 'user3@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'STUDENT', 1,
        NOW(), NOW()),
       (6, '랩장2', 'leader2@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'LAB_LEADER',
        2, NOW(), NOW()),
       (7, '관리자', 'admin@example.com', '$2a$10$7dAhkArjo9wKCC.ndBZ/lOUYslJUualWzJiBk6yxAMiWaX0InFWAy', 'ADMIN', NULL,
        NOW(), NOW());

-- 3. lab_images
INSERT INTO lab_images (id, lab_id, image_url, type)
VALUES (1, 1, 'https://example.com/lab1_img1.png', 'REPRESENTATIVE'),
       (2, 1, 'https://example.com/lab1_img2.png', 'ADDITIONAL'),
       (3, 2, 'https://example.com/lab2_img1.png', 'REPRESENTATIVE');

-- 4. lab_applications
INSERT INTO lab_applications (id, lab_id, user_id, interview_time, status, created_at, updated_at)
VALUES (1, 1, 1, '2025-07-01 13:00:00', 'PENDING', NOW(), NOW()),
       (2, 1, 4, '2025-07-01 15:00:00', 'APPROVED', NOW(), NOW()),
       (3, 2, 5, '2025-07-01 16:00:00', 'PENDING', NOW(), NOW());

-- 5. lab_notices
INSERT INTO lab_notices (id, title, content, type, pinned, author_id, lab_id, created_at, updated_at)
VALUES (1, 'AI랩 정기 미팅 안내', '매주 월요일 오후 2시에 정기 미팅을 진행합니다. 모든 랩원은 필수 참석 바랍니다.', 'NORMAL', false, 2, 1, NOW(), NOW()),
       (2, '[긴급] 프로젝트 발표 일정 변경', '다음 주 금요일로 예정된 프로젝트 발표가 화요일로 변경되었습니다. 준비 부탁드립니다.', 'URGENT', true, 2, 1, NOW(), NOW()),
       (3, '새로운 연구 주제 모집', 'GPT 관련 새로운 연구 주제를 모집합니다. 관심 있는 분은 연락 바랍니다.', 'NORMAL', false, 2, 1, NOW(), NOW()),
       (4, 'DB랩 세미나 일정', '이번 달 세미나는 매주 수요일 오후 3시에 진행됩니다.', 'NORMAL', false, 3, 2, NOW(), NOW()),
       (5, '[중요] 서버 점검 안내', '내일 오후 6시부터 자정까지 서버 점검이 있습니다. 작업 저장 후 로그아웃 바랍니다.', 'URGENT', true, 6, 2, NOW(), NOW()),
       (6, '데이터베이스 최적화 스터디', '매주 목요일 저녁 7시에 데이터베이스 최적화 스터디를 진행합니다.', 'NORMAL', false, 3, 2, NOW(), NOW());

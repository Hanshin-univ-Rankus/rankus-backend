-- 1. labs
INSERT INTO labs (id, name, category, description, ranking, professor_name, created_at, updated_at)
VALUES
    (1, 'AI랩', 'AI', '인공지능 랩', 1, '김교수', NOW(), NOW()),
    (2, 'DB랩', 'DB', '데이터베이스 랩', 2, '이교수', NOW(), NOW());

-- 2. users
INSERT INTO users (id, name, email, password_hash, role, lab_id, created_at, updated_at)
VALUES
    (1, '학생1', 'user1@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'STUDENT', NULL, NOW(), NOW()),
    (2, '랩장1', 'leader1@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'LAB_LEADER', 1, NOW(), NOW()),
    (3, '교수', 'prof@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'PROFESSOR', 2, NOW(), NOW()),
    (4, '학생2', 'user2@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'STUDENT', NULL, NOW(), NOW()),
    (5, '학생3', 'user3@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'STUDENT', 1, NOW(), NOW()),
    (6, '랩장2', 'leader2@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'LAB_LEADER', 2, NOW(), NOW()),
    (7, '관리자', 'admin@example.com', '$2a$10$hCPmBsUxHI.Q0Z2hVKN5QOQGfDzfWkZ0vbUAn5O7gWyPXCxvOAKZ.', 'ADMIN', NULL, NOW(), NOW());

-- 3. lab_images
INSERT INTO lab_images (id, lab_id, image_url, type)
VALUES
    (1, 1, 'https://example.com/lab1_img1.png', 'REPRESENTATIVE'),
    (2, 1, 'https://example.com/lab1_img2.png', 'ADDITIONAL'),
    (3, 2, 'https://example.com/lab2_img1.png', 'REPRESENTATIVE');

-- 4. lab_applications
INSERT INTO lab_applications (id, lab_id, user_id, interview_time, status, created_at, updated_at)
VALUES
    (1, 1, 1, '2025-07-01 13:00:00', 'PENDING', NOW(), NOW()),
    (2, 1, 4, '2025-07-01 15:00:00', 'APPROVED', NOW(), NOW()),
    (3, 2, 5, '2025-07-01 16:00:00', 'PENDING', NOW(), NOW());

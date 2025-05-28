-- ==================================================
-- 1) LABS 테이블: 랩실 샘플 데이터
-- ==================================================
INSERT INTO labs (name, department, field, description, ranking, professor, created_at, updated_at) VALUES
                                                                                                        ('AI 연구실', '컴퓨터공학과', 'AI', '머신러닝·딥러닝 연구', 1, '홍석진', NOW(), NOW()),
                                                                                                        ('웹서비스랩', '소프트웨어학과', 'WEB', '풀스택 개발 실습', 2, '김민수', NOW(), NOW()),
                                                                                                        ('데이터융합랩', '정보통계학과', 'DB', '빅데이터 분석·시각화 연구', 3, '이영희', NOW(), NOW());

-- ==================================================
-- 2) LAB_IMAGES 테이블: 랩실별 이미지 샘플
-- ==================================================
INSERT INTO lab_images (lab_id, image_url, type, created_at, updated_at) VALUES
                                                                             (1, 'https://example.com/ai_thumb.jpg', 'REPRESENTATIVE', NOW(), NOW()),
                                                                             (1, 'https://example.com/ai_thumb.jpg', 'ADDITIONAL', NOW(), NOW()),
                                                                             (2, 'https://example.com/web_thumb.jpg', 'REPRESENTATIVE', NOW(), NOW()),
                                                                             (3, 'https://example.com/data_thumb.jpg', 'REPRESENTATIVE', NOW(), NOW());
-- ==================================================
-- 3) LAB_APPLICATIONS 테이블: 신청 정보 샘플
-- ==================================================
INSERT INTO lab_applications
(lab_id, user_id, status, interview_time, created_at, updated_at)
VALUES
    (1, 1, 'PENDING',    NOW(), NOW(), NOW()),
    (2, 2, 'APPROVED',   NOW(), NOW(), NOW());
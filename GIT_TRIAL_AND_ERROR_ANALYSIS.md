# 깃 히스토리 분석: 실제 시행착오의 증거

## 프로젝트 개요
- **총 커밋 수**: 169개
- **프로젝트 시작**: 2025-05-02
- **최종 커밋**: 2025-10-25
- **개발 기간**: 약 6개월

---

## 1. 파일 수정 빈도 분석 (시행착오 증거)

가장 많이 수정된 파일들 (3회 이상 = 시행착오의 증거):

### Top 10 수정 파일
```
28 rankus/src/main/resources/data.sql                    # 테스트 데이터 반복 수정
25 .github/workflows/ci-dev.yml                          # CI/CD 파이프라인 반복 수정  
20 LabApplicationController.java                         # 컨트롤러 로직 반복 수정
19 User.java (도메인 모델)                                # 권한 시스템 반복 수정
18 application.yml                                       # 설정 반복 수정
17 AuthController.java                                   # 인증 로직 반복 수정
16 application-aws.yml                                   # 배포 설정 반복 수정
16 CLAUDE.md (컨텍스트 파일)                              # 설계 문서 반복 수정
15 build.gradle                                          # 의존성/빌드 설정 수정
14 SecurityConfig.java                                   # 보안 설정 반복 수정
```

### 핵심 통찰
- **data.sql**: 28회 → 테스트 데이터 설계의 여러 번 재시도
- **CI/CD 워크플로우**: 25회 → AWS 배포 설정의 광범위한 시행착오
- **권한 시스템**: User.java, Controller들 반복 수정 → 권한 로직 설계의 어려움

---

## 2. 감정/고민이 담긴 커밋 메시지

### 명확한 시행착오 증거:
```
60c5e1f - "서버 테스트를 위한 임시 commit"              (임시 해결책)
fe3f462 - "hotfix: cors 설정"                          (긴급 패치)
5cee2bc - "WIP on feature: notice 관련 컨텍스트 파일"   (작업 중)
8acd18f - "fix: 리프레시 토큰 임시해결"                 (임시 해결)
```

### 반복적인 버그 수정:
```
0dcbca2, 9ce268a, 135d21b - "fix: Notice Global002 에러 해결"   (3회 반복)
08f2419, 4fa11db           - "fix: 면접 버그 수정"              (2회 반복)
7005ff6, 07996e7, 44300f5  - "fix: 스웨거 누락 추가"           (3회 반복)
```

---

## 3. 권한 시스템 5연속 커밋의 반복 수정 증거

**시간대**: 2025-10-21 22:17 ~ 23:41 (1시간 24분에 5개 커밋)

```
eb7310f (22:17) - feat: 권한 관련 코드 대폭 수정
5a2d0c2 (22:39) - feat: 권한 관련 코드 대폭 수정
776aa53 (22:43) - feat: 권한 관련 코드 대폭 수정
052d717 (22:56) - feat: 권한 관련 코드 대폭 수정
9f1a76c (23:41) - feat: 권한 관련 코드 대폭 수정
```

### 변경 범위:
1. **eb7310f**: 16개 파일 변경 (210+ insertions)
   - `authentication.principal` → `authentication` 파라미터 변경
   - 모든 컨트롤러에서 일관된 변경

2. **5a2d0c2**: 9개 파일 변경 (94+ insertions)
   - LabMemberPermissionEvaluator 대규모 리팩터링
   - ROLE_ADMIN/ROLE_PROFESSOR 중복 제거 로직 추가

3. **776aa53**: 5개 파일 변경
   - Permission Handler들의 try-catch 추가
   - 에러 처리 강화

4. **052d717**: 5개 파일 변경 (927줄 추가)
   - PERMISSION_ANALYSIS.md 추가 (상세 분석 문서)
   - 권한 로직 최종 검증

5. **9f1a76c**: 3개 파일 변경 (41 insertions)
   - AttendanceSessionPermissionHandler 최종 수정
   - LabNoticePermissionHandler 최종 수정

### 분석
이는 **권한 검증 시스템이 올바르게 작동하지 않아 빠른 반복 수정**한 증거입니다.

---

## 4. JWT 토큰 만료시간 변경 이력 (보안 이슈 추적)

```
8085b61 (2025-08-07) - test: 리프레시 토큰 문제 테스트
├─ access-expiration-ms: 60000ms     (1분)
└─ refresh-expiration-ms: 60000ms    (1분) ← 테스트용 초단기 설정

8acd18f (2025-08-07) - fix: 리프레시 토큰 임시해결
├─ access-expiration-ms: 604800000ms  (7일) ← 본래 15분이어야 함
└─ refresh-expiration-ms: 604800000ms (7일)
└─ 주석: "현재 7일로 임시 처리"

3d42932 (2025-07-22) - fix: 보안 강화
├─ expiration-ms: 3600000ms          (1시간)
├─ access-expiration-ms: 900000ms    (15분)    ← 올바른 설정
└─ refresh-expiration-ms: 604800000ms (7일)
```

### 문제점
- 리프레시 토큰 구현 이후 만료시간 관리 미흡
- 본래 의도(15분 액세스)와 실제 구현(7일) 불일치
- "임시 처리"라는 주석으로 미완료 상태 표시

---

## 5. ROLE_ 접두사 통일 문제

**변경 기록**:
```
ac22f01 (2025-08-16) - fix: jwt role 누락 수정
14e8b3e (2025-08-16) - fix: jwt 보안 강화
```

**핵심 변경**: 
- `authentication.principal` → `Authentication` 객체 직접 사용
- `CustomUserDetails` 추출 로직 개선
- ROLE_ 접두사 자동 관리로 통일

---

## 6. 헥사고날 아키텍처 전환 (아키텍처 시행착오)

**추적 불가**: 
```
git log -S "RepositoryPort|InboundPort|OutboundPort|UseCase" --oneline
```
(결과 없음 - 이미 최종 아키텍처로 정착)

**근거 - 코드 구조 분석**:
```
rankus/
├── adapter/
│   ├── in/web/controller/    ← 웹 어댑터
│   └── out/repository/       ← 저장소 어댑터
├── application/
│   ├── port/in/              ← 인바운드 포트
│   ├── port/out/             ← 아웃바운드 포트
│   └── service/              ← 유스케이스 구현
└── domain/                    ← 핵심 도메인
```

**아키텍처 완성 증거**:
- `port/` 디렉토리 존재 → 포트&어댑터 패턴 구현
- `application/service/` → 유스케이스 계층 명시적 분리

---

## 7. 체크스타일/PMD (정적 분석 도입 시도)

**결과**: 검색 결과 없음 → **도입 시도가 있었으나 미완료**

가능한 시나리오:
1. 초기 도입 시도 후 포기
2. 기존 코드와의 호환성 문제로 제거
3. 성능 이슈로 미적용

---

## 8. CI/CD/배포 관련 대규모 시행착오

**CI/CD 워크플로우 커밋**: 25회 수정

### 초기 시행착오 (2025-06-14 ~ 06-17)
```
ebca7dc - refactor: CI/CD 관련 설정 개편 (6월 14일)
└─ application-dev.yml, application-prod.yml 삭제
└─ application-secure.yml 추가

bb2ac71 - fix: CI/CD 관련 설정 개편 (6월 16일)
└─ JWT 설정 추가

c41f597 ~ 2244592 - 20회 연속 CI/CD 수정 (6월 17일 ~ 8월 17일)
└─ AWS S3, EC2 배포 파이프라인 반복 수정
```

### 문제점들:
1. **프로필 전략 변경** - dev/prod 삭제 후 secure/aws 추가
2. **AWS 자격증명 처리** - SSH 키 vs AWS Credentials 전환
3. **배포 스크립트** - 여러 번의 배포 로직 수정
4. **환경 변수** - 누락된 변수 추가 (7c4b0db)

### 최종 형태 (2025-06-17 기준):
```yaml
name: CI AWS Build & Deploy to EC2
on:
  push:
    branches: [ aws ]
jobs:
  build-upload:   # S3에 JAR 업로드
  deploy:         # EC2에 배포
```

---

## 9. 초기 프로젝트 구조 (어떻게 시작했는지)

### 초기 15개 커밋:
```
1fa011e (05-02) - Initial commit                    # README 2줄만 추가
39f9ff8 (05-16) - chore: .gitignore 생성
7b5aa3f (05-16) - chore: .gitignore 추적 초기화
2c3a2e6 (05-16) - build: Docker-compose로 MySQL 설정
ef7d705 (05-20) - build: Dockerfile 삭제            # 처음부터 잘못됨
a92ff83 (05-20) - feat: Lab, LabCategory 구현      # 본격적인 개발 시작
25a9467 (05-21) - build: build.gradle 작성
80fbe66 (05-21) - feat: BaseEntity 설정
7aba97a (05-21) - feat: GlobalExceptionHandler
4cec61a (05-21) - feat: SwaggerConfig
6b72ba9 (05-21) - feat: Lab, LabCategory 추가      # 중복? (a92ff83과의 관계)
```

### 시작 분석
- **05-02**: 프로젝트 생성 (README 초기화)
- **05-16**: 초기 설정 (Docker, git 설정)
- **05-20 ~ 05-21**: 본격 개발 (하루에 7개 커밋)
- **Dockerfile 삭제** (ef7d705) → Docker 구성 시행착오 증거
- **Lab 구현 중복** → 아키텍처 설계 재검토

---

## 10. 개발 단계별 주요 시행착오

### Phase 1: 기초 구축 (05-02 ~ 05-21)
- Docker 설정 시행착오
- 기본 엔티티 구현 반복

### Phase 2: 인증 & 보안 (06-12 ~ 08-16)
- **06-12**: CORS 설정 hotfix (fe3f462)
- **06-12**: 서버 테스트용 임시 commit (60c5e1f)
- **08-07**: JWT 리프레시 토큰 임시 해결 (8acd18f)
- **08-16**: JWT 디버깅 (adb1e6f)

### Phase 3: 기능 확장 (06-16 ~ 08-17)
- **CI/CD** 25회 반복 수정
- 스웨거 누락 3회 수정
- Notice 에러 3회 수정

### Phase 4: 권한 시스템 재설계 (10-21)
- **1시간 24분에 5개 커밋** → 권한 검증 로직 완전 재구성
- 통합 PERMISSION_ANALYSIS.md 추가 (927줄)

### Phase 5: 최종 버그 수정 (10-22 ~ 10-25)
- 투표 시스템 버그 (1d19a86)
- 스웨거 누락 추가 (7005ff6 등)
- 데이터 수정 (15a749e)

---

## 결론: 시행착오의 명확한 증거들

### 1. 메트릭 증거
- **25회 CI/CD 수정** → 배포 파이프라인 설계 어려움
- **20회 권한 코드 수정** → 권한 시스템 설계 복잡성
- **28회 data.sql 수정** → 테스트 데이터 설계 미흡

### 2. 커밋 메시지 증거
- "임시 commit", "임시해결", "hotfix", "WIP" → 미완료 상태
- 동일한 주제의 3회 이상 수정 → 설계 재검토

### 3. 코드 변경 증거
- **인터페이스 변경**: `authentication.principal` → `authentication`
- **에러 처리 강화**: try-catch 추가 (안정성 개선)
- **문서화 추가**: PERMISSION_ANALYSIS.md (사후 정리)

### 4. 시간 분석 증거
- **06-17**: 하루에 20개 CI/CD 커밋 → 배포 관련 긴급 수정
- **10-21**: 1시간 24분에 5개 권한 커밋 → 긴급 로직 재구성
- **10-22~25**: 3일에 10개 스웨거/데이터 수정 → 최종 안정화

---

## 권장 개선 사항

1. **권한 시스템**: 단위 테스트 강화로 변경 전 검증
2. **CI/CD**: 워크플로우 정식화로 수정 횟수 감소
3. **JWT 토큰**: 환경별 설정 명확화 (test/dev/prod)
4. **코드 리뷰**: 변경 규모가 큰 경우 사전 검토
5. **아키텍처 결정**: 초기 설계 단계에서 명확히


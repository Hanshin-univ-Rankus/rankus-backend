# 컨텍스트 파일 업데이트 체크리스트 (AI 코딩용)

## 🎯 기능 개발 완료 후 필수 체크

### ✅ 새 도메인 추가시 (필수 체크)

- [ ] **`ai-essentials.md`**: 네이밍 규칙 테이블에 새 도메인 예시 추가
- [ ] **`ai-essentials.md`**: ErrorCode 패턴 테이블에 Prefix 및 사용 범위 추가
- [ ] **`ai-essentials.md`**: 권한 패턴에 새 PermissionHandler 추가 (랩실 소속 도메인인 경우)
- [ ] **`test-patterns.md`**: ErrorCode 매핑 주석에 새 도메인 ErrorCode 범위 추가
- [ ] **`code-templates.md`**: 특수 패턴이 있는 경우 해당 템플릿 활용 여부 확인
- [ ] **`http-matrix.md`**: 권한 매트릭스 및 API 엔드포인트 추가
- [ ] **`error-codes.md`**: Prefix 할당 및 ErrorCode 예시 추가
- [ ] **`domain/CLAUDE.md`**: Aggregate 구조 및 Enum 업데이트
- [ ] **`application/CLAUDE.md`**: UseCase 구현 현황 테이블 업데이트
- [ ] **`adapter/CLAUDE.md`**: Controller 패턴 및 보안 설정 추가

### ✅ 기능 수정시 (변경사항 반영)

- [ ] **`ai-essentials.md`**: ErrorCode 패턴이나 권한 패턴 변경시 업데이트
- [ ] **`test-patterns.md`**: ErrorCode 매핑 변경시 주석 업데이트
- [ ] **`http-matrix.md`**: API 엔드포인트 URL/권한 변경사항 반영
- [ ] **`error-codes.md`**: ErrorCode 변경사항 반영
- [ ] **`domain/CLAUDE.md`**: 도메인 Enum 값 변경사항 반영

### ✅ 권한 시스템 변경시

- [ ] **`ai-essentials.md`**: 권한 패턴 섹션에 새 패턴 추가
- [ ] **`http-matrix.md`**: 권한 매트릭스 및 @PreAuthorize 패턴 업데이트
- [ ] **`adapter/CLAUDE.md`**: Controller 보안 패턴 업데이트

### ✅ 새로운 아키텍처 패턴 도입시

- [ ] **`code-templates.md`**: 새 패턴의 템플릿 추가 (상태 관리, 연관관계 등)
- [ ] **`test-patterns.md`**: 새 패턴의 테스트 방법 추가
- [ ] **`ai-essentials.md`**: 코딩 패턴 섹션에 새 패턴 추가

## 🔧 컨텍스트 파일 최적화 원칙

### AI 친화적 작성

- **50줄 이하** 유지
- **테이블 중심** 구성
- **코드 템플릿** 포함
- **중복 설명** 제거

### 필수 포함 요소

- **즉시 복사 가능한** 코드 블록
- **핵심 패턴만** 추출
- **실제 구현과 일치**하는 예시

## 📊 체크리스트 사용법

1. **개발 완료 후** 즉시 체크리스트 실행
2. **각 파일별로** 변경사항 확인 및 업데이트
3. **실제 구현과 문서** 일치 여부 검증
4. **AI 코딩 최적화** 원칙 준수 확인

## 🔍 문서-코드 일치성 검증 (필수)

### ✅ ErrorCode 일치성 검증

- [ ] **실제 ErrorCode 클래스**와 **error-codes.md**의 Prefix 일치 확인
- [ ] **ai-essentials.md**의 ErrorCode 패턴 테이블 업데이트
- [ ] **새 ErrorCode 추가시** 예시 섹션에 주요 코드 추가

### ✅ 권한 패턴 일치성 검증

- [ ] **Controller의 @PreAuthorize** 패턴이 **ai-essentials.md**와 일치
- [ ] **http-matrix.md**의 권한 매트릭스 업데이트
- [ ] **도메인별 권한 패턴 일관성** 확인

### ✅ 네이밍 규칙 일치성 검증

- [ ] **실제 클래스명**과 **ai-essentials.md** 네이밍 테이블 일치
- [ ] **새 도메인 추가시** 네이밍 예시 테이블 업데이트
- [ ] **Prefix 패턴 통일성** 확인

## 🚨 발견된 문제점 재발 방지

### ErrorCode 불일치 방지

- **문제**: `error-codes.md`에 "미할당" 상태였으나 실제로는 구현됨
- **방지책**: 새 ErrorCode 구현 시 즉시 문서 업데이트

### Prefix 패턴 불일치 방지

- **문제**: 문서에 "RNK", 실제 코드에 "RANKING_" 사용
- **방지책**: 구현 전 문서에서 Prefix 패턴 확인 후 일치시켜 구현

## ⚠️ 주의사항

- 컨텍스트 파일 업데이트 **절대 생략 금지**
- 문서와 실제 코드 **불일치 방지**
- AI가 빠르게 참조할 수 있도록 **간결성 유지**
- **개발 완료 즉시** 문서 업데이트 (나중에 하면 잊어버림)

**업데이트**: 2025-01-09 | **94줄** | 문서-코드 일치성 검증 프로세스 강화
# AI 개발 필수 가이드 (코딩 최적화)

## 🎯 핵심 아키텍처

```
Controller → {Domain}CommandUseCase → {Domain}CommandService → {Domain}RepositoryPort
Controller → {Domain}QueryUseCase → {Domain}QueryService → {Domain}RepositoryPort
```

## 📛 네이밍 규칙

| 타입         | 패턴                                | 예시                                                        |
|------------|-----------------------------------|-----------------------------------------------------------|
| Entity     | `{Domain}`                        | `User`, `Lab`, `LabApplication`, `LabNotice`, `Interview` |
| Service    | `{Domain}{Command\|Query}Service` | `UserCommandService`, `InterviewQueryService`             |
| Controller | `{Domain}Controller`              | `LabNoticeController`, `InterviewController`              |
| UseCase    | `{Domain}{Command\|Query}UseCase` | `LabApplicationCommandUseCase`, `InterviewCommandUseCase` |
| DTO        | `{Domain}{Action}RequestDto`      | `UserCreateRequestDto`, `InterviewSlotCreateRequestDto`   |
| ErrorCode  | `{Domain}ErrorCode`               | `NoticeErrorCode`, `InterviewErrorCode`                   |

## 🔧 코딩 패턴

### Service 반환값

```java
// ✅ 올바른 패턴: Entity 반환
@Transactional
public User createUser(UserCreateRequestDto request) {
    return userRepository.save(User.create(request.getName()));
}

// ❌ 잘못된 패턴: DTO 반환
public UserResponseDto createUser(...) { /* DTO 반환 금지 */ }
```

### Controller DTO 변환

```java
// ✅ Controller에서 변환
@PostMapping
public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto request) {
    User user = userCommandService.createUser(request);
    return ResponseEntity.status(CREATED).body(ApiResponse.created(UserResponseDto.from(user)));
}
```

### 권한 패턴

```java
// 인증만
@PreAuthorize("isAuthenticated()")

// 소유권 체크
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, 'LabNotice', 'UPDATE')")

// 랩실 권한 - Notice
@PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_NOTICES')")

// 랩실 권한 - Interview
@PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_INTERVIEWS')")

// 랩실 권한 - Application
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'Lab', 'MANAGE_APPLICATIONS')")
```

## 📝 ErrorCode 패턴

| Prefix | 도메인                | 현재 사용   |
|--------|--------------------|---------|
| `USER` | User               | 001~010 |
| `LAB`  | Lab                | 001~015 |
| `LAP`  | LabApplication     | 001~012 |
| `LIM`  | LabImage           | 001~008 |
| `LCR`  | LabCreationRequest | 001~010 |
| `LNT`  | LabNotice          | 001~007 |
| `INT`  | Interview          | 001~034 |

```java
// ErrorCode 템플릿
{FIELD}_REQUIRED("{PREFIX}_001", BAD_REQUEST, "{필드}는 필수입니다"),
{DOMAIN}_NOT_FOUND("{PREFIX}_404", NOT_FOUND, "{도메인}을 찾을 수 없습니다"),
```

## 🔍 테스트 품질 체크리스트

### 코드 작성 후 확인사항

- [ ] Factory는 외부 상태에 의존하지 않는가?
- [ ] DTO 변환에서 연관 객체 null 처리 포함되었는가?
- [ ] 테스트간 상태 격리가 보장되는가?
- [ ] Mock 설정이 완전한가?

### 안전한 설계 패턴

```java
// ✅ 상태 독립적 Factory
public static Entity create() {
    return new Entity(defaultValidData); // 외부 상태 비의존
}

// ✅ 방어적 DTO 변환
.relation(entity.getRelation() != null ? 
    RelationDto.from(entity.getRelation()) : null)

// ✅ 테스트별 상태 조정
@Test void test() {
    Entity entity = Factory.create();
    // 테스트에 필요한 상태만 조정
}
```

## ✅ 컨텍스트 파일 업데이트 의무사항

### 🚨 개발 완료 후 필수 수행

새 기능 개발 또는 기존 기능 수정 후 **반드시** 다음 파일들을 업데이트해야 함:

1. **error-codes.md**: 새 ErrorCode 추가/수정
2. **test-patterns.md**: 도메인별 테스트 패턴 추가
3. **http-matrix.md**: API 엔드포인트 및 권한 매트릭스 업데이트
4. **application/CLAUDE.md**: UseCase 구현 현황 업데이트
5. **adapter/CLAUDE.md**: Controller 보안 패턴 업데이트

### 📋 체크리스트 활용

개발 완료 후 `@core/context-update-checklist.md` 파일의 체크리스트를 사용하여 누락없이 문서 업데이트 수행

### ⚠️ 중요 원칙

- **문서와 코드 불일치** 절대 금지
- **AI 최적화**: 50줄 이하, 테이블 중심, 코드 템플릿 포함
- **즉시 업데이트**: 개발 완료 즉시 문서 업데이트

**업데이트**: 2025-07-06 | **65줄** | 테스트 품질 체크리스트 추가
# 공통 네이밍 컨벤션

## 📛 클래스 네이밍

| 타입         | 패턴                                                     | 예시                                                                 |
|------------|--------------------------------------------------------|--------------------------------------------------------------------|
| Entity     | `{Domain}`                                             | `User`, `Lab`, `LabApplication`                                    |
| DTO        | `{Domain}{Action}RequestDto` / `{Domain}ResponseDto`   | `UserCreateRequestDto`, `UserResponseDto`, `LabCreationRequestDto` |
| Service    | `{Domain}{Command\|Query}Service`                      | `UserCommandService`, `LabQueryService`                            |
| UseCase    | `{Domain}{Command\|Query}UseCase`                      | `UserCommandUseCase`, `LabQueryUseCase`                            |
| Controller | `{Domain}Controller`                                   | `UserController`, `LabController`                                  |
| Repository | `{Domain}RepositoryPort` / `{Domain}RepositoryAdapter` | `UserRepositoryPort`                                               |
| Exception  | `{Domain}{Specific}Exception`                          | `UserNotFoundException`, `LabValidationException`                  |
| ErrorCode  | `{Domain}ErrorCode`                                    | `UserErrorCode`, `LabErrorCode`                                    |

## 🔤 메서드 네이밍

### Service 메서드

| 작업   | 패턴                            | 예시                                       |
|------|-------------------------------|------------------------------------------|
| 생성   | `create{Domain}()`            | `createUser()`, `createLab()`            |
| 조회   | `find{Domain}By{Condition}()` | `findUserById()`, `findLabsByCategory()` |
| 수정   | `update{Domain}()`            | `updateUser()`, `updateLabInfo()`        |
| 삭제   | `delete{Domain}()`            | `deleteUser()`, `deleteLab()`            |
| 상태변경 | `{action}{Domain}()`          | `approveApplication()`, `activateUser()` |

### Repository 메서드

| 작업    | 패턴                                     | 예시                                  |
|-------|----------------------------------------|-------------------------------------|
| 기본    | `save()`, `findById()`, `deleteById()` | 표준 CRUD                             |
| 조건 조회 | `findBy{Property}()`                   | `findByEmail()`, `findByCategory()` |
| 존재 확인 | `existsBy{Property}()`                 | `existsByEmail()`, `existsByName()` |
| 개수 조회 | `countBy{Property}()`                  | `countByLabId()`, `countByStatus()` |

### Controller 메서드

| HTTP   | 패턴                                            | 예시                                     |
|--------|-----------------------------------------------|----------------------------------------|
| GET    | `get{Resource}()` / `get{Resource}List()`     | `getUser()`, `getUserList()`           |
| POST   | `create{Resource}()`                          | `createUser()`, `applyToLab()`         |
| PUT    | `update{Resource}()` / `{action}{Resource}()` | `updateUser()`, `approveApplication()` |
| DELETE | `delete{Resource}()`                          | `deleteUser()`, `cancelApplication()`  |

### DTO 네이밍 특수 규칙

| 케이스        | 일반 패턴                                | 실제 사용                         | 사유     |
|------------|--------------------------------------|-------------------------------|--------|
| 단일 생성 DTO  | `{Domain}CreateRequestDto`           | `{Domain}CreateRequestDto`    | 표준 패턴  |
| 도메인명이 긴 경우 | `LabCreationRequestCreateRequestDto` | `LabCreationRequestDto`       | 중복 제거  |
| 특수 액션 DTO  | `{Domain}{Action}RequestDto`         | `LabCreationRequestRejectDto` | 액션 명확화 |

## 🗂️ 패키지 네이밍

| 계층          | 패턴                                 | 예시                                                         |
|-------------|------------------------------------|------------------------------------------------------------|
| Domain      | `domain.model.{domain}`            | `domain.model.user`, `domain.model.lab`                    |
| Application | `application.service.{type}`       | `application.service.command`, `application.service.query` |
| Adapter     | `adapter.{direction}.{technology}` | `adapter.in.web`, `adapter.out.persistence`                |
| Config      | `config`                           | `config.SecurityConfig`                                    |

## 📄 파일 네이밍

| 타입     | 패턴                | 예시                |
|--------|-------------------|-------------------|
| 메인 가이드 | `CLAUDE.md`       | 각 계층의 메인 가이드      |
| 컨벤션    | `CONVENTIONS.md`  | 구체적인 코딩 규칙        |
| 테스트    | `{ClassName}Test` | `UserServiceTest` |

## 🎯 상수 네이밍

| 타입        | 패턴                    | 예시                                       |
|-----------|-----------------------|------------------------------------------|
| ErrorCode | `{PREFIX}_{CODE}`     | `USER_001`, `LAB_404`, `LCR_009`         |
| Enum      | `UPPER_SNAKE_CASE`    | `LAB_MEMBER`, `APPLICATION_APPROVED`     |
| Security  | `{MODULE}_{CONSTANT}` | `JWT_SECRET_KEY`, `CORS_ALLOWED_ORIGINS` |

## 📊 테이블 및 컬럼 네이밍

| 타입  | 패턴                      | 예시                                                                 |
|-----|-------------------------|--------------------------------------------------------------------|
| 테이블 | `snake_case`            | `users`, `lab_applications`, `lab_images`, `lab_creation_requests` |
| 컬럼  | `snake_case`            | `created_at`, `user_id`, `lab_id`                                  |
| FK  | `{referenced_table}_id` | `user_id`, `lab_id`                                                |

## ⚠️ 예외 처리 네이밍 규칙

### 예외 클래스 네이밍

| 예외 타입   | 패턴                            | 예시                                                  |
|---------|-------------------------------|-----------------------------------------------------|
| 검증 예외   | `{Domain}ValidationException` | `UserValidationException`, `LabValidationException` |
| 조회 실패   | `{Domain}NotFoundException`   | `UserNotFoundException`, `LabNotFoundException`     |
| 비즈니스 규칙 | `{Domain}BusinessException`   | `UserBusinessException`, `LabBusinessException`     |

### ErrorCode 네이밍 규칙

| 도메인                | Prefix | 코드 범위   | 예시                     |
|--------------------|--------|---------|------------------------|
| User               | `USER` | 001~099 | `USER_001`, `USER_404` |
| Lab                | `LAB`  | 001~099 | `LAB_001`, `LAB_404`   |
| LabApplication     | `LAP`  | 001~099 | `LAP_001`, `LAP_404`   |
| LabImage           | `LIM`  | 001~099 | `LIM_001`, `LIM_404`   |
| LabCreationRequest | `LCR`  | 001~099 | `LCR_001`, `LCR_404`   |

### HTTP 상태별 코드 할당

| 상태 코드 | 코드 범위   | 용도         | 예시                                  |
|-------|---------|------------|-------------------------------------|
| 400   | 001~099 | 입력값 검증 오류  | `USER_001` (NAME_REQUIRED)          |
| 401   | 401     | 인증 실패      | `USER_401` (INVALID_CREDENTIALS)    |
| 403   | 403     | 권한 부족      | `LAB_403` (INSUFFICIENT_PERMISSION) |
| 404   | 404     | 리소스 없음     | `USER_404` (USER_NOT_FOUND)         |
| 409   | 409     | 충돌/중복      | `USER_409` (EMAIL_DUPLICATED)       |
| 422   | 422     | 비즈니스 규칙 위반 | `LAP_422` (CANNOT_CHANGE_STATUS)    |

### ErrorCode 상수 네이밍

| 오류 타입    | 패턴                                 | 예시                                      |
|----------|------------------------------------|-----------------------------------------|
| 필수값 누락   | `{FIELD}_REQUIRED`                 | `NAME_REQUIRED`, `EMAIL_REQUIRED`       |
| 길이 초과    | `{FIELD}_TOO_LONG`                 | `NAME_TOO_LONG`, `DESCRIPTION_TOO_LONG` |
| 형식 오류    | `{FIELD}_INVALID_FORMAT`           | `EMAIL_INVALID_FORMAT`                  |
| 중복 오류    | `{FIELD}_DUPLICATED`               | `EMAIL_DUPLICATED`                      |
| 조회 실패    | `{DOMAIN}_NOT_FOUND`               | `USER_NOT_FOUND`, `LAB_NOT_FOUND`       |
| 권한 부족    | `INSUFFICIENT_PERMISSION_{ACTION}` | `INSUFFICIENT_PERMISSION_FOR_APPROVAL`  |
| 상태 변경 불가 | `CANNOT_CHANGE_STATUS`             | `CANNOT_CHANGE_STATUS_AFTER_DECISION`   |

### 메시지 작성 규칙

| 규칙   | 설명               | 예시                       |
|------|------------------|--------------------------|
| 언어   | 한국어              | "사용자를 찾을 수 없습니다."        |
| 종결어미 | 명사형 종결 ("~습니다.") | "이름은 필수입니다."             |
| 구체성  | 구체적 정보 포함        | "최대 10자까지 가능합니다."        |
| 일관성  | 동일 도메인 내 일관된 톤   | "랩실 이름은...", "랩실 설명은..." |

### 파일 위치 규칙

```
domain/model/{domain}/exception/
├── {Domain}ValidationException.java
├── {Domain}NotFoundException.java
├── {Domain}BusinessException.java (선택)
└── {Domain}ErrorCode.java
```

### 예외 발생 시점 규칙

| 예외 타입               | 발생 위치                 | 책임         |
|---------------------|-----------------------|------------|
| ValidationException | Domain Entity 생성/수정 시 | 도메인 검증 로직  |
| NotFoundException   | Repository 조회 실패 시    | 애플리케이션 서비스 |
| BusinessException   | 비즈니스 규칙 위반 시          | 도메인 로직     |
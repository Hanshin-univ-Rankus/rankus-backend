# 공통 네이밍 컨벤션

## 📛 클래스 네이밍

| 타입 | 패턴 | 예시 |
|------|------|------|
| Entity | `{Domain}` | `User`, `Lab`, `LabApplication` |
| DTO | `{Domain}{Action}RequestDto` / `{Domain}ResponseDto` | `UserCreateRequestDto`, `UserResponseDto` |
| Service | `{Domain}{Command\|Query}Service` | `UserCommandService`, `LabQueryService` |
| UseCase | `{Domain}{Command\|Query}UseCase` | `UserCommandUseCase`, `LabQueryUseCase` |
| Controller | `{Domain}Controller` | `UserController`, `LabController` |
| Repository | `{Domain}RepositoryPort` / `{Domain}RepositoryAdapter` | `UserRepositoryPort` |
| Exception | `{Domain}{Specific}Exception` | `UserNotFoundException`, `LabValidationException` |
| ErrorCode | `{Domain}ErrorCode` | `UserErrorCode`, `LabErrorCode` |

## 🔤 메서드 네이밍

### Service 메서드
| 작업 | 패턴 | 예시 |
|------|------|------|
| 생성 | `create{Domain}()` | `createUser()`, `createLab()` |
| 조회 | `find{Domain}By{Condition}()` | `findUserById()`, `findLabsByCategory()` |
| 수정 | `update{Domain}()` | `updateUser()`, `updateLabInfo()` |
| 삭제 | `delete{Domain}()` | `deleteUser()`, `deleteLab()` |
| 상태변경 | `{action}{Domain}()` | `approveApplication()`, `activateUser()` |

### Repository 메서드
| 작업 | 패턴 | 예시 |
|------|------|------|
| 기본 | `save()`, `findById()`, `deleteById()` | 표준 CRUD |
| 조건 조회 | `findBy{Property}()` | `findByEmail()`, `findByCategory()` |
| 존재 확인 | `existsBy{Property}()` | `existsByEmail()`, `existsByName()` |
| 개수 조회 | `countBy{Property}()` | `countByLabId()`, `countByStatus()` |

### Controller 메서드
| HTTP | 패턴 | 예시 |
|------|------|------|
| GET | `get{Resource}()` / `get{Resource}List()` | `getUser()`, `getUserList()` |
| POST | `create{Resource}()` | `createUser()`, `applyToLab()` |
| PUT | `update{Resource}()` / `{action}{Resource}()` | `updateUser()`, `approveApplication()` |
| DELETE | `delete{Resource}()` | `deleteUser()`, `cancelApplication()` |

## 🗂️ 패키지 네이밍

| 계층 | 패턴 | 예시 |
|------|------|------|
| Domain | `domain.model.{domain}` | `domain.model.user`, `domain.model.lab` |
| Application | `application.service.{type}` | `application.service.command`, `application.service.query` |
| Adapter | `adapter.{direction}.{technology}` | `adapter.in.web`, `adapter.out.persistence` |
| Config | `config` | `config.SecurityConfig` |

## 📄 파일 네이밍

| 타입 | 패턴 | 예시 |
|------|------|------|
| 메인 가이드 | `CLAUDE.md` | 각 계층의 메인 가이드 |
| 컨벤션 | `CONVENTIONS.md` | 구체적인 코딩 규칙 |
| 테스트 | `{ClassName}Test` | `UserServiceTest` |

## 🎯 상수 네이밍

| 타입 | 패턴 | 예시 |
|------|------|------|
| ErrorCode | `UPPER_SNAKE_CASE` | `USER_NOT_FOUND`, `INVALID_PASSWORD` |
| Enum | `UPPER_SNAKE_CASE` | `LAB_MEMBER`, `APPLICATION_APPROVED` |
| Security | `{MODULE}_{CONSTANT}` | `JWT_SECRET_KEY`, `CORS_ALLOWED_ORIGINS` |

## 📊 테이블 및 컬럼 네이밍

| 타입 | 패턴 | 예시 |
|------|------|------|
| 테이블 | `snake_case` | `users`, `lab_applications`, `lab_images` |
| 컬럼 | `snake_case` | `created_at`, `user_id`, `lab_id` |
| FK | `{referenced_table}_id` | `user_id`, `lab_id` |
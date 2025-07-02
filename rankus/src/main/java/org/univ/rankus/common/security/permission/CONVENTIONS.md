# Permission 컨벤션

## 📛 네이밍

| 구분     | 패턴                           | 예시                                 |
|--------|------------------------------|------------------------------------|
| 평가자    | `UnifiedPermissionEvaluator` | Spring Security 통합                 |
| 핸들러    | `{Domain}PermissionHandler`  | `LabNoticePermissionHandler`       |
| 인터페이스  | `DomainPermissionEvaluator`  | 도메인별 구현 계약                         |
| 권한 문자열 | `UPPER_CASE`                 | `VIEW`, `CREATE`, `MANAGE_NOTICES` |

## 🏗️ 구조 패턴

### 도메인 권한 핸들러 템플릿

```java
@Component
@RequiredArgsConstructor
public class {Domain}PermissionHandler implements

DomainPermissionEvaluator {

    private final {
        Domain
    } QueryUseCase queryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType () {
        return "{Domain}";
    }

    @Override
    public boolean hasPermission (Object principalObj, Serializable targetId, String permission){
        // 구현: 타입 검증 → 사용자/리소스 조회 → 권한 체크
        return switch (permission) {
            case "VIEW" -> canView(user, resource);
            case "CREATE", "UPDATE", "DELETE" -> canManage(user, resource);
            default -> false;
        };
    }
}
```

## 📋 구현된 권한 핸들러

| 핸들러                                   | 대상       | 주요 권한                         | 특별 기능         |
|---------------------------------------|----------|-------------------------------|---------------|
| `LabApplicationPermissionHandler`     | 지원서      | VIEW, APPROVE, DELETE         | 소유권 기반 DELETE |
| `LabImagePermissionHandler`           | 이미지      | CREATE, DELETE                | 랩실 관리자만       |
| `LabCreationRequestPermissionHandler` | 랩실 생성 요청 | VIEW, DELETE, APPROVE, REJECT | 관리자 DELETE 추가 |
| `LabNoticePermissionHandler`          | 공지사항     | VIEW, CREATE, UPDATE, DELETE  | Lab 단위 권한 체크  |

## 🔐 권한 매트릭스

### LabNoticePermissionHandler

| 권한     | 대상      | 조건                         |
|--------|---------|----------------------------|
| VIEW   | 공지사항 조회 | 랩실 멤버 + 교수 + 관리자           |
| CREATE | 공지사항 생성 | 랩실 관리자 + 교수 + 관리자          |
| UPDATE | 공지사항 수정 | 작성자 본인 + 랩실 관리자 + 교수 + 관리자 |
| DELETE | 공지사항 삭제 | 작성자 본인 + 랩실 관리자 + 교수 + 관리자 |

#### 특별 메서드: Lab 단위 권한 체크

```java
// 랩실별 공지사항 목록 조회용
public boolean hasPermissionForLab(Object principalObj, Serializable labId, String permission) {
    return switch (permission) {
        case "VIEW_NOTICES" -> user.canViewLabNotices(lab);
        case "MANAGE_NOTICES" -> user.canManageLabNotices(lab);
        default -> false;
    };
}
```

## 🎯 PreAuthorize 패턴

### 표준 패턴

| 패턴                                                                                       | 사용 케이스         |
|------------------------------------------------------------------------------------------|----------------|
| `@PreAuthorize("isAuthenticated()")`                                                     | 기본 인증 확인       |
| `@PreAuthorize("hasRole('ADMIN')")`                                                      | 역할 기반 권한       |
| `@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(..., #id, 'Domain', 'VIEW')")` | 리소스별 권한 확인     |
| `@PreAuthorize("hasRole('ADMIN') or @handler.hasPermissionForLab(..., #labId, 'VIEW')")` | 복합 조건 (Notice) |

### Notice 전용 패턴

```java
// 개별 공지사항 권한 체크
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'VIEW')")

// 랩실별 공지사항 권한 체크  
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
```

## 🧪 테스트 패턴

### 권한 핸들러 테스트 구조

```java
@ExtendWith(MockitoExtension.class) class {Domain}

PermissionHandlerTest {

    @Mock private {
        Domain
    } QueryUseCase queryUseCase;
    @Mock private UserQueryUseCase userQueryUseCase;
    @InjectMocks private {
        Domain
    } PermissionHandler permissionHandler;

    @Test
    void 소유자는_리소스를_볼_수_있다 () {
        // given: 소유자 + 리소스 설정
        // when: hasPermission 호출
        // then: true 반환 확인
    }

    @Test
    void 권한_없는_사용자는_리소스를_볼_수_없다 () {
        // given: 일반 사용자 + 타인 리소스
        // when: hasPermission 호출  
        // then: false 반환 확인
    }
}
```

## 🎯 핵심 규칙

1. **명확한 권한**: 도메인별 구체적 권한 정의
2. **소유권 우선**: 리소스 소유자 권한 최우선 고려
3. **역할 기반**: 관리자/교수 권한 적절히 분배
4. **성능 고려**: 불필요한 DB 조회 최소화
5. **테스트 필수**: 모든 권한 시나리오 테스트 작성
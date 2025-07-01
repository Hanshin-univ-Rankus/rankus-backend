# Permission 컨벤션

## 클래스 네이밍

- 평가자: `UnifiedPermissionEvaluator`
- 핸들러: `{Domain}PermissionHandler`
- 인터페이스: `DomainPermissionEvaluator`

## 통합 권한 평가자 구조

```java
@Component
@RequiredArgsConstructor
public class UnifiedPermissionEvaluator implements PermissionEvaluator {
    
    private final Map<String, DomainPermissionEvaluator> permissionHandlers;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetId,
                                Object targetType, Object permission) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String targetTypeName = (String) targetType;
        String permissionName = (String) permission;
        Long resourceId = (Long) targetId;
        
        DomainPermissionEvaluator handler = permissionHandlers.get(targetTypeName.toLowerCase() + "PermissionHandler");
        
        if (handler == null) {
            return false;
        }
        
        return handler.hasPermission(authentication, resourceId, permissionName);
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                String targetType, Object permission) {
        return hasPermission(authentication, (Object) targetId, targetType, permission);
    }
}
```

## 도메인 권한 평가자 인터페이스

```java
public interface DomainPermissionEvaluator {
    boolean hasPermission(Authentication authentication, Long resourceId, String permission);
    boolean hasPermission(Authentication authentication, Object resource, String permission);
}
```

## 도메인별 권한 핸들러 구조

```java
@Component
@RequiredArgsConstructor
public class LabApplicationPermissionHandler implements DomainPermissionEvaluator {
    
    private final LabApplicationRepositoryPort labApplicationRepositoryPort;
    
    @Override
    public boolean hasPermission(Authentication authentication, Long applicationId, String permission) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();
        
        switch (permission.toUpperCase()) {
            case "VIEW":
                return canView(currentUser, applicationId);
            case "APPROVE":
                return canApprove(currentUser, applicationId);
            case "DELETE":
                return canDelete(currentUser, applicationId);
            default:
                return false;
        }
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Object resource, String permission) {
        if (resource instanceof LabApplication) {
            return hasPermission(authentication, ((LabApplication) resource).getId(), permission);
        }
        return false;
    }
    
    private boolean canView(User user, Long applicationId) {
        LabApplication application = getApplicationOrThrow(applicationId);
        
        // 본인 지원서는 항상 볼 수 있음
        if (application.isOwnedBy(user)) {
            return true;
        }
        
        // 해당 랩실의 관리자는 볼 수 있음
        return user.canManageLabApplications(application.getLab()) || 
               user.getRole() == Role.ADMIN;
    }
    
    private boolean canApprove(User user, Long applicationId) {
        LabApplication application = getApplicationOrThrow(applicationId);
        
        // 랩실 관리자만 승인 가능
        return user.canManageLabApplications(application.getLab()) || 
               user.getRole() == Role.ADMIN;
    }
    
    private boolean canDelete(User user, Long applicationId) {
        LabApplication application = getApplicationOrThrow(applicationId);
        
        // 본인 지원서만 취소 가능 (또는 관리자)
        return application.isOwnedBy(user) || user.getRole() == Role.ADMIN;
    }
    
    private LabApplication getApplicationOrThrow(Long applicationId) {
        return labApplicationRepositoryPort.findById(applicationId)
            .orElseThrow(() -> new LabApplicationNotFoundException(applicationId));
    }
}
```

## 구현된 도메인 권한 핸들러

### LabApplicationPermissionHandler

- **대상**: 랩실 지원서 관련 권한
- **권한**: VIEW, APPROVE, REJECT, DELETE
- **소유권 검증**: 지원자 본인만 DELETE 가능
- **관리자 권한**: 랩실 관리자는 VIEW, APPROVE, REJECT 가능

### LabImagePermissionHandler

- **대상**: 랩실 이미지 관련 권한
- **권한**: CREATE, DELETE
- **관리자 권한**: 랩실 관리자만 생성/삭제 가능

### LabCreationRequestPermissionHandler

- **대상**: 랩실 생성 신청 관련 권한
- **권한**: VIEW, DELETE, APPROVE, REJECT
- **DELETE 권한**: 신청자 본인 + 관리자 (2025.07 정책 변경: 관리자 운영 편의성 향상)
- **조회 권한**: 신청자 본인 + 관리자/교수
- **승인/거절**: 관리자/교수만 가능

```java
@Component
@RequiredArgsConstructor
public class LabCreationRequestPermissionHandler implements DomainPermissionEvaluator {

    private final LabCreationRequestQueryUseCase queryUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    public String targetType() {
        return "LabCreationRequest";
    }

    @Override
    public boolean hasPermission(Object principalObj, Serializable targetId, String permission) {
        if (!(principalObj instanceof CustomUserDetails) || !(targetId instanceof Long)) {
            return false;
        }

        Long userId = ((CustomUserDetails) principalObj).getUserId();
        User user = userQueryUseCase.getUserById(userId);
        Long requestId = (Long) targetId;
        LabCreationRequest request = queryUseCase.getLabCreationRequestById(requestId);

        return switch (permission) {
            case "DELETE" -> request.isOwnedBy(userId) || user.getRole() == Role.ADMIN;
            case "VIEW" -> request.isOwnedBy(userId) || isAdminOrProfessor(user);
            case "APPROVE", "REJECT" -> isAdminOrProfessor(user);
            default -> false;
        };
    }

    private boolean isAdminOrProfessor(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.PROFESSOR;
    }
}
```

## 권한 종류 정의

```java
public enum PermissionType {
    
    // 기본 CRUD 권한
    CREATE("CREATE"),
    VIEW("VIEW"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    
    // 비즈니스 특화 권한
    APPROVE("APPROVE"),
    REJECT("REJECT"),
    MANAGE("MANAGE"),
    
    // 관리 권한
    ADMIN("ADMIN");
    
    private final String code;
    
    PermissionType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
```

## PreAuthorize 사용 패턴

```java
// 기본 권한 검사
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #resourceId, 'LabApplication', 'VIEW')")

// 역할 기반 검사
@PreAuthorize("hasRole('ADMIN') or hasRole('LAB_LEADER')")

// 복합 조건
@PreAuthorize("isAuthenticated() and (@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'Lab', 'MANAGE') or hasRole('ADMIN'))")

// 메서드 결과 기반 검사
@PostAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, returnObject.id, 'LabApplication', 'VIEW')")
```

## 권한 캐싱 패턴

```java
@Component
@RequiredArgsConstructor
public class CachedPermissionEvaluator implements DomainPermissionEvaluator {
    
    private final DomainPermissionEvaluator delegate;
    
    @Cacheable(value = "permissions", key = "#authentication.name + ':' + #resourceId + ':' + #permission")
    @Override
    public boolean hasPermission(Authentication authentication, Long resourceId, String permission) {
        return delegate.hasPermission(authentication, resourceId, permission);
    }
    
    @CacheEvict(value = "permissions", key = "#authentication.name + ':' + #resourceId + ':*'")
    public void evictPermissionCache(Authentication authentication, Long resourceId) {
        // 권한 캐시 무효화
    }
}
```

## 테스트 패턴

```java
@ExtendWith(MockitoExtension.class)
class LabApplicationPermissionHandlerTest {
    
    @Mock
    private LabApplicationRepositoryPort labApplicationRepositoryPort;
    
    @InjectMocks
    private LabApplicationPermissionHandler permissionHandler;
    
    @Test
    void 지원자는_본인_지원서를_볼_수_있다() {
        // given
        User applicant = createUser("applicant@example.com", Role.STUDENT);
        LabApplication application = createApplication(applicant);
        Authentication auth = createAuthentication(applicant);
        
        when(labApplicationRepositoryPort.findById(1L)).thenReturn(Optional.of(application));
        
        // when
        boolean result = permissionHandler.hasPermission(auth, 1L, "VIEW");
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    void 일반_사용자는_다른_사용자_지원서를_볼_수_없다() {
        // given
        User applicant = createUser("applicant@example.com", Role.STUDENT);
        User other = createUser("other@example.com", Role.STUDENT);
        LabApplication application = createApplication(applicant);
        Authentication auth = createAuthentication(other);
        
        when(labApplicationRepositoryPort.findById(1L)).thenReturn(Optional.of(application));
        
        // when
        boolean result = permissionHandler.hasPermission(auth, 1L, "VIEW");
        
        // then
        assertThat(result).isFalse();
    }
}
```
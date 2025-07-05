# HTTP 상태코드 및 권한 매트릭스 (AI 코딩용)

## 📊 HTTP 상태코드

| 코드 | 상황 | ResponseEntity 패턴 | ApiResponse 메서드 |
|-----|------|-------------------|-------------------|
| 200 | 조회/수정 | `ResponseEntity.ok()` | `ApiResponse.success()` |
| 201 | 생성 | `ResponseEntity.status(CREATED)` | `ApiResponse.created()` |
| 204 | 삭제 | `ResponseEntity.noContent()` | `ApiResponse.deleted()` |
| 400 | 잘못된 요청 | 검증 실패 | `INVALID_INPUT` |
| 403 | 권한 없음 | 접근 권한 부족 | `ACCESS_DENIED` |
| 404 | 리소스 없음 | 엔티티 조회 실패 | `{DOMAIN}_NOT_FOUND` |

## 🔐 권한 매트릭스

| 리소스 | 액션 | STUDENT | LAB_MEMBER | LAB_MANAGER | LAB_LEADER | ADMIN |
|-------|------|---------|------------|-------------|------------|-------|
| **User** | VIEW/UPDATE | Own | Own | Own | Own | All |
| **Lab** | VIEW | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Lab** | CREATE/UPDATE | ❌ | ❌ | Lab | Lab | All |
| **LabApplication** | VIEW | Own | Own+Lab | Lab | Lab | All |
| **LabApplication** | APPROVE/REJECT | ❌ | ❌ | ❌ | Lab | All |
| **LabNotice** | VIEW | Lab | Lab | Lab | Lab | All |
| **LabNotice** | CREATE | ❌ | Lab | Lab | Lab | All |
| **LabNotice** | UPDATE/DELETE | ❌ | Own | Own | Own | All |

## 🎯 @PreAuthorize 패턴

```java
// 인증만
@PreAuthorize("isAuthenticated()")

// 역할 기반
@PreAuthorize("hasRole('ADMIN')")

// 소유권 체크
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, 'Domain', 'ACTION')")

// Notice 권한
@PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
```

## 📝 Notice API 엔드포인트

| HTTP | 엔드포인트 | 권한 | 용도 |
|------|----------|------|------|
| GET | `/api/labs/{labId}/notices` | VIEW_NOTICES | 목록 조회 |
| GET | `/api/labs/{labId}/notices/{noticeId}` | LabNotice.VIEW | 상세 조회 |
| POST | `/api/labs/{labId}/notices` | MANAGE_NOTICES | 생성 |
| PUT | `/api/labs/{labId}/notices/{noticeId}` | LabNotice.UPDATE | 수정 |
| DELETE | `/api/labs/{labId}/notices/{noticeId}` | LabNotice.DELETE | 삭제 |

**업데이트**: 2025-01-05 | **50줄** | AI 코딩 최적화
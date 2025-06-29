# Repository Port 컨벤션

## 인터페이스 네이밍
- 패턴: `{Domain}RepositoryPort`
- 예시: `UserRepositoryPort`, `LabRepositoryPort`

## 표준 구조
```java
public interface {Domain}RepositoryPort {
    
    // 기본 CRUD
    {Domain} save({Domain} {domain});
    Optional<{Domain}> findById(Long id);
    List<{Domain}> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    
    // 도메인 특화 메서드
    Optional<{Domain}> findBy{Property}({PropertyType} {property});
    List<{Domain}> findBy{Condition}({ConditionType} condition);
    boolean existsBy{Property}({PropertyType} {property});
    long countBy{Condition}({ConditionType} condition);
    
    // 페이징
    Page<{Domain}> findAll(Pageable pageable);
    Page<{Domain}> findBy{Condition}({ConditionType} condition, Pageable pageable);
}
```

## 메서드 네이밍 패턴
- 저장: `save({Domain}) -> {Domain}`
- 조회: `findBy{Property}({Type}) -> Optional<{Domain}>`
- 목록조회: `findBy{Condition}({Type}) -> List<{Domain}>`
- 존재확인: `existsBy{Property}({Type}) -> boolean`
- 개수: `countBy{Condition}({Type}) -> long`
- 삭제: `deleteBy{Property}({Type}) -> void`

## 기본 메서드 (필수)
```java
// 모든 Repository Port가 포함해야 하는 기본 메서드
{Domain} save({Domain} {domain});
Optional<{Domain}> findById(Long id);
void deleteById(Long id);
boolean existsById(Long id);
```

## 반환 타입 규칙
- 단일 조회: `Optional<{Domain}>` (null 안전성)
- 목록 조회: `List<{Domain}>` (빈 리스트 반환)
- 저장: `{Domain}` (저장된 엔티티 반환)
- 존재 확인: `boolean`
- 페이징: `Page<{Domain}>`

## 도메인별 특화 메서드 예시
```java
// UserRepositoryPort
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
List<User> findByLabId(Long labId);

// LabRepositoryPort  
List<Lab> findByCategory(LabCategory category);
List<Lab> findByRankingGreaterThan(Integer ranking);

// LabApplicationRepositoryPort
List<LabApplication> findByLabIdAndStatus(Long labId, ApplicationStatus status);
boolean existsByLabIdAndUserId(Long labId, Long userId);
```

## 페이징 지원
- 기본: `Page<{Domain}> findAll(Pageable pageable)`
- 조건부: `Page<{Domain}> findBy{Condition}({Type} condition, Pageable pageable)`

## 파라미터 규칙
1. 엔티티 타입 그대로 사용 (DTO 변환은 상위 계층에서)
2. 조건은 구체적인 타입 사용
3. ID는 `Long` 타입 사용

## 예외 처리
- Repository Port에서는 예외를 정의하지 않음
- 구현체(Adapter)에서 적절한 예외로 변환
# Query UseCase 컨벤션

## 인터페이스 네이밍

- 패턴: `{Domain}QueryUseCase`
- 예시: `UserQueryUseCase`, `LabPromotionQueryUseCase`

## 표준 구조

```java
public interface {Domain}QueryUseCase {
    
    // 단일 조회
    {Domain}ResponseDto find{Domain}ById(Long id);
    
    // 목록 조회
    List<{Domain}ResponseDto> findAll{Domain}s();
    
    // 조건 조회
    List<{Domain}ResponseDto> find{Domain}sBy{Condition}({ConditionType} condition);
    
    // 페이징 조회
    PageResponse<{Domain}ResponseDto> find{Domain}s(Pageable pageable);
    
    // 검색
    List<{Domain}ResponseDto> search{Domain}s(String keyword);
}
```

## 메서드 네이밍 패턴

- 단일 조회: `find{Domain}ById(Long) -> {ResponseDto}`
- 전체 조회: `findAll{Domain}s() -> List<{ResponseDto}>`
- 조건 조회: `find{Domain}sBy{Property}({Type}) -> List<{ResponseDto}>`
- 존재 확인: `exists{Domain}By{Property}({Type}) -> boolean`
- 개수 조회: `count{Domain}sBy{Condition}({Type}) -> long`

## 파라미터 규칙

1. ID 조회: `Long id` (필수)
2. 조건 조회: 구체적인 타입 사용
3. 페이징: `Pageable pageable`
4. 검색: `String keyword` 또는 전용 DTO

## 반환 타입 규칙

- 단일: `{Domain}ResponseDto`
- 목록: `List<{Domain}ResponseDto>`
- 페이징: `PageResponse<{Domain}ResponseDto>`
- 존재여부: `boolean`
- 개수: `long`

## JavaDoc 패턴

```java
/**
 * {설명}
 * 
 * @param {매개변수} {설명}
 * @return {반환값 설명}
 * @throws {Domain}NotFoundException {조건}
 */
```

## 예외 명세

- `{Domain}NotFoundException` - 조회 실패
- `InvalidParameterException` - 잘못된 파라미터

## 캐싱 고려사항

- 자주 조회되는 데이터의 캐싱 전략
- 캐시 무효화 시점 고려
# Query Service 컨벤션

## 클래스 네이밍

- 패턴: `{Domain}QueryService`
- 예시: `UserQueryService`, `LabPromotionQueryService`

## 표준 구조

```java
@Service
@RequiredArgsConstructor
public class {Domain}QueryService implements {Domain}QueryUseCase {
    
    private final {Domain}RepositoryPort {domain}RepositoryPort;
    
    @Override
    @Transactional(readOnly = true)
    public {Domain} find{Domain}ById(Long id) {
        return {domain}RepositoryPort.findById(id)
            .orElseThrow(() -> new {Domain}NotFoundException());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<{Domain}> findAll{Domain}s() {
        return {domain}RepositoryPort.findAll();
    }
}
```

## 메서드 패턴

- 단일 조회: `find{Domain}ById(Long) -> {Domain}`
- 목록 조회: `findAll{Domain}s() -> List<{Domain}>`
- 조건 조회: `find{Domain}sBy{Condition}({Params}) -> List<{Domain}>`
- 페이징 조회: `find{Domain}s(Pageable) -> Page<{Domain}>`

## 반환값 규칙

- **Domain Entity 반환**: 모든 public 메서드는 Domain Entity 또는 Value Object를 반환한다
- **DTO 변환 위치**: Controller 계층에서 Entity → DTO 변환을 담당한다
- **비즈니스 로직**: 도메인 객체 중심으로 처리

## 구현 규칙

1. 모든 메서드에 `@Override` 어노테이션
2. Repository에서 조회 후 Domain Entity 반환
3. 예외는 즉시 발생 (orElseThrow 사용)
4. 비즈니스 로직은 Domain Entity로 처리

## 트랜잭션

- 각 메서드에 `@Transactional(readOnly = true)` 어노테이션
- 읽기 전용 트랜잭션으로 성능 최적화
- 클래스 레벨 `@Transactional` 어노테이션 사용 금지

## 예외 처리

- 조회 실패 시 `{Domain}NotFoundException` 발생
- Repository 예외는 그대로 전파

## 성능 최적화

1. 필요한 데이터만 조회 (Projection 활용)
2. N+1 문제 방지 (Fetch Join 고려)
3. 캐싱 적용 가능 지점 식별

## 테스트 패턴

- 클래스명: `{Domain}QueryServiceTest`
- Mock 사용: Repository Port를 Mock
- 검증: DTO 변환 정확성 및 예외 처리
# Query Service 컨벤션

## 클래스 네이밍
- 패턴: `{Domain}QueryService`
- 예시: `UserQueryService`, `LabPromotionQueryService`

## 표준 구조
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class {Domain}QueryService implements {Domain}QueryUseCase {
    
    private final {Domain}RepositoryPort {domain}RepositoryPort;
    
    @Override
    public {Domain}ResponseDto find{Domain}ById(Long id) {
        {Domain} {domain} = {domain}RepositoryPort.findById(id)
            .orElseThrow(() -> new {Domain}NotFoundException());
        return {Domain}ResponseDto.from({domain});
    }
    
    @Override
    public List<{Domain}ResponseDto> findAll{Domain}s() {
        List<{Domain}> {domain}s = {domain}RepositoryPort.findAll();
        return {domain}s.stream()
            .map({Domain}ResponseDto::from)
            .collect(Collectors.toList());
    }
}
```

## 메서드 패턴
- 단일 조회: `find{Domain}ById(Long) -> {ResponseDto}`
- 목록 조회: `findAll{Domain}s() -> List<{ResponseDto}>`
- 조건 조회: `find{Domain}sBy{Condition}({Params}) -> List<{ResponseDto}>`
- 페이징 조회: `find{Domain}s(Pageable) -> Page<{ResponseDto}>`

## 구현 규칙
1. 모든 메서드에 `@Override` 어노테이션
2. Repository에서 조회 후 DTO 변환
3. 예외는 즉시 발생 (orElseThrow 사용)
4. Stream API 활용한 변환

## 트랜잭션
- 클래스 레벨: `@Transactional(readOnly = true)`
- 읽기 전용 트랜잭션으로 성능 최적화

## DTO 변환
- `{Domain}ResponseDto.from()` 정적 메서드 사용
- 리스트 변환 시 Stream API 활용
- 페이징 응답 시 `PageResponse.of()` 사용

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
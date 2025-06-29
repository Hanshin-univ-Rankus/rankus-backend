# Command Service 컨벤션

## 클래스 네이밍
- 패턴: `{Domain}CommandService`
- 예시: `UserCommandService`, `LabApplicationCommandService`

## 표준 구조
```java
@Service
@Transactional
@RequiredArgsConstructor
public class {Domain}CommandService implements {Domain}CommandUseCase {
    
    private final {Domain}RepositoryPort {domain}RepositoryPort;
    
    @Override
    public {Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request) {
        // 1. 도메인 객체 생성
        // 2. 비즈니스 규칙 검증  
        // 3. 영속화
        // 4. 응답 DTO 변환
    }
    
    @Override
    public {Domain}ResponseDto update{Domain}(Long id, {Domain}UpdateRequestDto request) {
        // 1. 엔티티 조회
        // 2. 도메인 로직 실행
        // 3. 영속화
        // 4. 응답 DTO 변환
    }
    
    @Override
    public void delete{Domain}(Long id) {
        // 1. 엔티티 조회
        // 2. 삭제 가능 여부 검증
        // 3. 삭제 실행
    }
}
```

## 메서드 패턴
- 생성: `create{Domain}({CreateRequestDto}) -> {ResponseDto}`
- 수정: `update{Domain}(Long, {UpdateRequestDto}) -> {ResponseDto}`
- 삭제: `delete{Domain}(Long) -> void`
- 상태변경: `{action}{Domain}(Long) -> {ResponseDto}`

## 구현 규칙
1. 모든 메서드에 `@Override` 어노테이션
2. 도메인 객체 중심의 로직 구현
3. 검증은 도메인 계층에 위임
4. Repository Port를 통한 영속화

## 트랜잭션
- 클래스 레벨: `@Transactional`
- 각 메서드는 하나의 트랜잭션 단위

## 예외 처리
- 도메인 예외는 그대로 전파
- Repository에서 발생하는 예외 처리
- 비즈니스 로직 예외를 적절한 도메인 예외로 변환

## 테스트 패턴
- 클래스명: `{Domain}CommandServiceTest`
- Mock 사용: Repository Port들을 Mock
- 검증: 도메인 로직 실행 및 Repository 호출 확인
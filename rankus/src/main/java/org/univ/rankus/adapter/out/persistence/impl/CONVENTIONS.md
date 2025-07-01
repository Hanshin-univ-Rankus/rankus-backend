# Repository Adapter 컨벤션

## 클래스 네이밍

- 패턴: `{Domain}RepositoryAdapter`
- 예시: `UserRepositoryAdapter`, `LabRepositoryAdapter`

## 표준 구조

```java
@Component
@RequiredArgsConstructor
public class {Domain}RepositoryAdapter implements {Domain}RepositoryPort {
    
    private final SpringData{Domain}Repository springData{Domain}Repository;
    
    @Override
    public {Domain} save({Domain} {domain}) {
        return springData{Domain}Repository.save({domain});
    }
    
    @Override
    public Optional<{Domain}> findById(Long id) {
        return springData{Domain}Repository.findById(id);
    }
    
    @Override
    public List<{Domain}> findAll() {
        return springData{Domain}Repository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        springData{Domain}Repository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return springData{Domain}Repository.existsById(id);
    }
}
```

## 구현 규칙

1. `@Component` 어노테이션으로 Spring Bean 등록
2. `@RequiredArgsConstructor`로 의존성 주입
3. 모든 메서드에 `@Override` 어노테이션
4. JPA Repository에 직접 위임

## 메서드 패턴

- 기본 CRUD: JPA Repository 메서드 그대로 위임
- 커스텀 쿼리: JPA Repository에서 구현 후 위임
- 복잡한 조회: QueryDSL 또는 JPQL 활용

## 예외 처리

```java
@Override
public {Domain} save({Domain} {domain}) {
    try {
        return springData{Domain}Repository.save({domain});
    } catch (DataIntegrityViolationException e) {
        throw new {Domain}DataIntegrityException("데이터 무결성 위반", e);
    }
}
```

## 페이징 처리

```java
@Override
public Page<{Domain}> findAll(Pageable pageable) {
    return springData{Domain}Repository.findAll(pageable);
}

@Override  
public Page<{Domain}> findBy{Condition}({ConditionType} condition, Pageable pageable) {
    return springData{Domain}Repository.findBy{Condition}(condition, pageable);
}
```

## 복잡한 쿼리 위임

```java
@Override
public List<{Domain}> findBy{ComplexCondition}({Params}) {
    return springData{Domain}Repository.findBy{ComplexCondition}({params});
}
```

## 트랜잭션 처리

- Adapter에서는 트랜잭션 어노테이션 사용하지 않음
- Service 계층에서 트랜잭션 관리
- 읽기 전용은 Repository 레벨에서 최적화 가능

## 캐싱 적용

```java
@Override
@Cacheable(value = "{domain}Cache", key = "#id")
public Optional<{Domain}> findById(Long id) {
    return springData{Domain}Repository.findById(id);
}

@Override
@CacheEvict(value = "{domain}Cache", key = "#domain.id")
public {Domain} save({Domain} {domain}) {
    return springData{Domain}Repository.save({domain});
}
```

## 테스트 패턴

```java
@DataJpaTest
class {Domain}RepositoryAdapterTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private SpringData{Domain}Repository springData{Domain}Repository;
    
    private {Domain}RepositoryAdapter {domain}RepositoryAdapter;
    
    @BeforeEach
    void setUp() {
        {domain}RepositoryAdapter = new {Domain}RepositoryAdapter(springData{Domain}Repository);
    }
    
    @Test
    void 저장_후_조회_가능() {
        // given
        {Domain} {domain} = create{Domain}();
        
        // when
        {Domain} saved = {domain}RepositoryAdapter.save({domain});
        Optional<{Domain}> found = {domain}RepositoryAdapter.findById(saved.getId());
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }
}
```
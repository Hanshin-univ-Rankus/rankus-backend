# JPA Repository 컨벤션

## 인터페이스 네이밍

- 패턴: `SpringData{Domain}Repository`
- 예시: `SpringDataUserRepository`, `SpringDataLabRepository`

## 표준 구조

```java
public interface SpringData{Domain}Repository extends JpaRepository<{Domain}, Long> {
    
    // 기본 메서드는 JpaRepository에서 제공
    
    // 단순 조건 쿼리 (Query Method)
    Optional<{Domain}> findBy{Property}({PropertyType} {property});
    List<{Domain}> findBy{Property}({PropertyType} {property});
    boolean existsBy{Property}({PropertyType} {property});
    long countBy{Property}({PropertyType} {property});
    void deleteBy{Property}({PropertyType} {property});
    
    // 복잡한 조건 쿼리
    List<{Domain}> findBy{Property1}And{Property2}({Type1} {property1}, {Type2} {property2});
    List<{Domain}> findBy{Property}In(List<{PropertyType}> {property}List);
    List<{Domain}> findBy{Property}Between({PropertyType} start, {PropertyType} end);
    
    // 정렬 쿼리
    List<{Domain}> findBy{Property}OrderBy{SortProperty}Asc({PropertyType} {property});
    List<{Domain}> findBy{Property}OrderBy{SortProperty}Desc({PropertyType} {property});
    
    // 페이징 쿼리
    Page<{Domain}> findBy{Property}({PropertyType} {property}, Pageable pageable);
    
    // JPQL 쿼리
    @Query("SELECT d FROM {Domain} d WHERE d.{property} = :{property}")
    List<{Domain}> findBy{Property}WithJpql(@Param("{property}") {PropertyType} {property});
    
    // Native Query
    @Query(value = "SELECT * FROM {table_name} WHERE {column} = :{property}", nativeQuery = true)
    List<{Domain}> findBy{Property}WithNative(@Param("{property}") {PropertyType} {property});
}
```

## Query Method 네이밍 패턴

- 조회: `findBy{Property}`, `findBy{Property1}And{Property2}`
- 존재확인: `existsBy{Property}`
- 개수: `countBy{Property}`
- 삭제: `deleteBy{Property}` (주의: 트랜잭션 필요)

## 조건 키워드

- `And`, `Or`: 논리 연산
- `Is`, `Equals`: 같음 (생략 가능)
- `IsNot`, `Not`: 다름
- `IsNull`, `Null`: null 값
- `IsNotNull`, `NotNull`: null이 아닌 값
- `Like`, `NotLike`: 문자열 패턴
- `StartingWith`, `EndingWith`, `Containing`: 문자열 포함
- `In`, `NotIn`: 목록 포함/제외
- `GreaterThan`, `LessThan`: 크기 비교
- `Between`: 범위
- `Before`, `After`: 날짜 비교

## 정렬 키워드

- `OrderBy{Property}Asc`: 오름차순
- `OrderBy{Property}Desc`: 내림차순
- `OrderBy{Property1}Asc{Property2}Desc`: 다중 정렬

## JPQL 사용 패턴

```java
// 기본 JPQL
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmailWithJpql(@Param("email") String email);

// 조인 쿼리
@Query("SELECT u FROM User u JOIN FETCH u.lab WHERE u.id = :id")
Optional<User> findByIdWithLab(@Param("id") Long id);

// 집계 쿼리
@Query("SELECT COUNT(u) FROM User u WHERE u.lab.id = :labId")
long countUsersByLabId(@Param("labId") Long labId);

// 조건부 쿼리
@Query("SELECT l FROM Lab l WHERE (:category IS NULL OR l.category = :category)")
List<Lab> findByOptionalCategory(@Param("category") LabCategory category);
```

## Native Query 사용 패턴

```java
// 복잡한 집계
@Query(value = """
    SELECT l.*, COUNT(la.id) as application_count 
    FROM lab l 
    LEFT JOIN lab_application la ON l.id = la.lab_id 
    GROUP BY l.id
    """, nativeQuery = true)
List<Object[]> findLabsWithApplicationCount();

// 성능 최적화가 필요한 쿼리
@Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
Optional<User> findByEmailOptimized(@Param("email") String email);
```

## 페이징 처리

```java
// 기본 페이징
Page<User> findAll(Pageable pageable);

// 조건부 페이징
Page<User> findByLabId(Long labId, Pageable pageable);

// JPQL 페이징
@Query("SELECT u FROM User u WHERE u.lab.id = :labId ORDER BY u.createdAt DESC")
Page<User> findByLabIdOrderByCreatedAtDesc(@Param("labId") Long labId, Pageable pageable);
```

## 수정/삭제 쿼리

```java
// 수정 쿼리
@Modifying
@Query("UPDATE User u SET u.role = :role WHERE u.id = :id")
int updateUserRole(@Param("id") Long id, @Param("role") Role role);

// 삭제 쿼리  
@Modifying
@Query("DELETE FROM LabApplication la WHERE la.lab.id = :labId")
int deleteApplicationsByLabId(@Param("labId") Long labId);
```

## 성능 최적화

```java
// Fetch Join으로 N+1 문제 해결
@Query("SELECT u FROM User u JOIN FETCH u.lab WHERE u.role = :role")
List<User> findByRoleWithLab(@Param("role") Role role);

// Projection 사용
@Query("SELECT new com.example.dto.UserSummary(u.id, u.name, u.email) FROM User u")
List<UserSummary> findUserSummaries();

// 읽기 전용 최적화
@Query("SELECT u FROM User u WHERE u.id = :id")
@QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
Optional<User> findByIdReadOnly(@Param("id") Long id);
```

## 테스트 패턴

```java
@DataJpaTest
class SpringDataUserRepositoryTest {
    
    @Autowired
    private SpringDataUserRepository userRepository;
    
    @Autowired 
    private TestEntityManager entityManager;
    
    @Test
    void 이메일로_사용자_조회() {
        // given
        User user = createUser("test@example.com");
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
```
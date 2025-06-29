# CustomUser 컨벤션

## 클래스 네이밍
- UserDetails 구현: `CustomUserDetails`
- UserDetailsService 구현: `CustomUserDetailsService`

## CustomUserDetails 구조
```java
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
    
    @Override
    public String getPassword() {
        return user.getPassword().getValue();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // 도메인 객체 접근
    public User getUser() {
        return user;
    }
    
    // 편의 메서드
    public Long getUserId() {
        return user.getId();
    }
    
    public Role getRole() {
        return user.getRole();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
}
```

## CustomUserDetailsService 구조
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepositoryPort userRepositoryPort;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
            
        return new CustomUserDetails(user);
    }
    
    // 추가 로딩 메서드
    public CustomUserDetails loadUserById(Long userId) {
        User user = userRepositoryPort.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));
            
        return new CustomUserDetails(user);
    }
}
```

## 권한 매핑 규칙
```java
// Role enum → Spring Security Authority 변환
public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    
    // 기본 역할 권한
    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    
    // 계층적 권한 (필요시)
    switch (user.getRole()) {
        case ADMIN:
            authorities.add(new SimpleGrantedAuthority("ROLE_LAB_LEADER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_LAB_MANAGER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_LAB_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            break;
        case LAB_LEADER:
            authorities.add(new SimpleGrantedAuthority("ROLE_LAB_MANAGER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_LAB_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            break;
        case LAB_MANAGER:
            authorities.add(new SimpleGrantedAuthority("ROLE_LAB_MEMBER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            break;
        case LAB_MEMBER:
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            break;
    }
    
    return authorities;
}
```

## 인증 컨텍스트 유틸리티
```java
public final class AuthenticationUtils {
    
    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }
    
    public static Optional<User> getCurrentUser() {
        return getCurrentUserDetails().map(CustomUserDetails::getUser);
    }
    
    public static Long getCurrentUserId() {
        return getCurrentUser()
            .map(User::getId)
            .orElseThrow(() -> new AuthenticationException("인증된 사용자가 없습니다"));
    }
    
    public static boolean isCurrentUser(Long userId) {
        return getCurrentUser()
            .map(user -> user.getId().equals(userId))
            .orElse(false);
    }
    
    private AuthenticationUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
```

## Controller에서 사용자 정보 주입
```java
@RestController
public class UserController {
    
    // @AuthenticationPrincipal 사용
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        User currentUser = userDetails.getUser();
        UserResponseDto response = UserResponseDto.from(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // SecurityContext에서 직접 가져오기
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserProfile() {
        User currentUser = AuthenticationUtils.getCurrentUser()
            .orElseThrow(() -> new AuthenticationException("인증이 필요합니다"));
            
        UserResponseDto response = UserResponseDto.from(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

## 사용자 정보 캐싱
```java
@Service
@RequiredArgsConstructor
public class CachedUserDetailsService implements UserDetailsService {
    
    private final UserRepositoryPort userRepositoryPort;
    
    @Override
    @Cacheable(value = "userDetails", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
            
        return new CustomUserDetails(user);
    }
    
    @CacheEvict(value = "userDetails", key = "#email")
    public void evictUserFromCache(String email) {
        // 캐시에서 사용자 정보 제거
    }
}
```

## 테스트 지원
```java
public final class CustomUserDetailsTestUtils {
    
    public static CustomUserDetails createUserDetails(Role role) {
        User user = User.create("test", "test@example.com", "password", role);
        user.setId(1L);
        return new CustomUserDetails(user);
    }
    
    public static Authentication createAuthentication(Role role) {
        CustomUserDetails userDetails = createUserDetails(role);
        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    }
    
    @TestConfiguration
    public static class TestConfig {
        
        @Bean
        @Primary
        public CustomUserDetailsService mockUserDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }
    }
    
    private CustomUserDetailsTestUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
```

## 테스트 패턴
```java
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    
    @Mock
    private UserRepositoryPort userRepositoryPort;
    
    @InjectMocks
    private CustomUserDetailsService userDetailsService;
    
    @Test
    void 이메일로_사용자_로딩() {
        // given
        String email = "test@example.com";
        User user = User.create("test", email, "password", Role.STUDENT);
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        
        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        
        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
    }
    
    @Test
    void 존재하지_않는_사용자_로딩시_예외() {
        // given
        String email = "nonexistent@example.com";
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
```
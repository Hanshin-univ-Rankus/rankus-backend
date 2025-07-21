# JWT 컨벤션

## 클래스 네이밍

- 제공자: `JwtTokenProvider`
- 필터: `JwtAuthenticationFilter`
- 진입점: `JwtAuthenticationEntryPoint`

## JwtTokenProvider 구조

```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final SecurityProperties securityProperties;
    
    // 토큰 생성
    public String createToken(String email, Role role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role.name());
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + securityProperties.getJwt().getExpirationTime());
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    // 토큰에서 역할 추출
    public Role getRole(String token) {
        String roleString = getClaimsFromToken(token).get("role", String.class);
        return Role.valueOf(roleString);
    }
    
    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // Claims 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    // 서명 키 생성
    private Key getSigningKey() {
        byte[] keyBytes = securityProperties.getJwt().getSecretKey().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

## JwtAuthenticationFilter 구조

```java

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                authenticateUser(token);
            }
        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token) {
        String email = jwtTokenProvider.getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return bearerToken.substring(SecurityConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}
```

## JWT 클레임 구조

```java
// 표준 클레임
{
  "sub": "user@example.com",        // 사용자 이메일 (subject)
  "role": "STUDENT",                // 사용자 역할
  "iat": 1234567890,                // 발급 시간 (issued at)
  "exp": 1234654290                 // 만료 시간 (expiration)
}
```

## 토큰 생성 패턴

```java
// 기본 토큰 생성
public String createToken(String email, Role role) {
    return createTokenWithExpiration(email, role, securityProperties.getJwt().getExpirationTime());
}

// 커스텀 만료 시간
public String createTokenWithExpiration(String email, Role role, long expirationTime) {
    // 구현
}

// 리프레시 토큰 생성  
public String createRefreshToken(String email) {
    // 더 긴 만료 시간으로 생성
}
```

## 토큰 검증 패턴

```java
public TokenValidationResult validateTokenWithDetails(String token) {
    try {
        Claims claims = getClaimsFromToken(token);
        return TokenValidationResult.valid(claims);
    } catch (ExpiredJwtException e) {
        return TokenValidationResult.expired();
    } catch (MalformedJwtException e) {
        return TokenValidationResult.malformed();
    } catch (SecurityException e) {
        return TokenValidationResult.invalid();
    }
}

public enum TokenValidationResult {
    VALID, EXPIRED, MALFORMED, INVALID
}
```

## 예외 처리

```java
// 토큰 관련 예외
public enum JwtErrorCode implements ErrorCode {
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다"),
    TOKEN_INVALID(401, "유효하지 않은 토큰입니다"),
    TOKEN_MALFORMED(401, "토큰 형식이 올바르지 않습니다"),
    TOKEN_SIGNATURE_INVALID(401, "토큰 서명이 유효하지 않습니다");
}
```

## 테스트 패턴

```java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    @Mock
    private SecurityProperties securityProperties;
    
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;
    
    @BeforeEach
    void setUp() {
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecretKey("test-secret-key-that-is-long-enough-for-hmac-sha256");
        jwt.setExpirationTime(86400000L); // 24시간
        
        when(securityProperties.getJwt()).thenReturn(jwt);
    }
    
    @Test
    void 토큰_생성_및_검증() {
        // given
        String email = "test@example.com";
        Role role = Role.STUDENT;
        
        // when
        String token = jwtTokenProvider.createToken(email, role);
        
        // then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo(role);
    }
}
```
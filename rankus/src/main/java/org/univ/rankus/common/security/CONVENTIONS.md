# Security 공통 컨벤션

## 클래스 네이밍
- 상수: `SecurityConstants`
- 핸들러: `{Type}Handler` - `CustomAccessDeniedHandler`
- 인증관련: `Custom{Type}` - `CustomUserDetails`
- 권한 핸들러: `{Domain}PermissionHandler` - `LabApplicationPermissionHandler`
- 통합 평가자: `UnifiedPermissionEvaluator`

## SecurityConstants 구조
```java
public final class SecurityConstants {
    
    // JWT 관련 상수
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // 공개 엔드포인트
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**",
        "/api/labs",
        "/api/labs/*",
        "/api/labs/*/images",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };
    
    // 권한 상수
    public static final String ROLE_PREFIX = "ROLE_";
    
    // 지원되는 권한 타입
    public static final String[] SUPPORTED_PERMISSIONS = {
        "VIEW", "CREATE", "UPDATE", "DELETE", "MANAGE", "APPROVE", "REJECT"
    };
    
    public static final String[] RESOURCE_TYPES = {
        "Lab", "LabApplication", "LabImage", "User"
    };
    
    private SecurityConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
```

## 예외 핸들러 패턴
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        String currentUser = getCurrentUser();
        
        // 보안 로그 - 권한 부족 접근 시도 추적
        log.warn("접근 권한 거부 - URI: {} {}, User: {}, IP: {}, UserAgent: {}, Exception: {}", 
                method, requestURI, currentUser, clientIp, userAgent, accessDeniedException.getMessage());
        
        // 컨텍스트 기반 에러 메시지 결정
        String errorMessage = determineErrorMessage(accessDeniedException, request);
        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.ACCESS_DENIED, errorMessage, requestURI);
        
        // 응답 설정 및 보안 헤더 추가
        response.setStatus(GlobalErrorCode.ACCESS_DENIED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        addSecurityHeaders(response);
        
        // 안전한 JSON 응답 작성
        try {
            objectMapper.writeValue(response.getWriter(), errorResponse);
        } catch (IOException e) {
            log.error("접근 거부 응답 작성 중 오류 발생", e);
            response.getWriter().write("{\"error\":\"Access denied\"}");
        }
    }
    
    private String determineErrorMessage(AccessDeniedException accessDeniedException, HttpServletRequest request) {
        // 엔드포인트별 맞춤 메시지 로직
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if (requestURI.contains("/api/labs/") && requestURI.contains("/applications")) {
            return switch (method) {
                case "POST" -> "랩실 지원 권한이 없습니다";
                case "PUT" -> "지원서 승인 권한이 없습니다"; 
                case "DELETE" -> "지원서 삭제 권한이 없습니다";
                case "GET" -> "지원서 조회 권한이 없습니다";
                default -> "접근 권한이 없습니다";
            };
        }
        // ... 추가 엔드포인트별 로직
        
        return "접근 권한이 없습니다";
    }
    
    private String getCurrentUser() {
        // SecurityContext에서 현재 사용자 추출
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        // 프록시 고려한 실제 클라이언트 IP 추출
    }
    
    private void addSecurityHeaders(HttpServletResponse response) {
        // 보안 헤더 추가
    }
}
```

## 인증 진입점 패턴
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        // 보안 로그 - 인증 실패 추적
        log.warn("인증 실패 - URI: {} {}, IP: {}, UserAgent: {}, Exception: {}", 
                method, requestURI, clientIp, userAgent, authException.getMessage());
        
        // 예외 유형별 맞춤 메시지
        String errorMessage = determineErrorMessage(authException, request);
        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.UNAUTHORIZED, errorMessage, requestURI);
        
        // 응답 설정 및 보안 헤더 추가
        response.setStatus(GlobalErrorCode.UNAUTHORIZED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        addSecurityHeaders(response);
        
        // 안전한 JSON 응답 작성
        try {
            objectMapper.writeValue(response.getWriter(), errorResponse);
        } catch (IOException e) {
            log.error("인증 실패 응답 작성 중 오류 발생", e);
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
        }
    }
    
    private String determineErrorMessage(AuthenticationException authException, HttpServletRequest request) {
        // JWT 토큰 존재 여부 확인
        if (request.getHeader("Authorization") != null && 
            request.getHeader("Authorization").startsWith("Bearer ")) {
            return "유효하지 않은 인증 토큰입니다";
        }
        
        // 예외 타입별 메시지 결정
        return switch (authException.getClass().getSimpleName()) {
            case "BadCredentialsException" -> "인증 정보가 올바르지 않습니다";
            case "DisabledException" -> "비활성화된 계정입니다";
            case "AccountExpiredException" -> "만료된 계정입니다";
            case "CredentialsExpiredException" -> "인증 정보가 만료되었습니다";
            case "LockedException" -> "잠긴 계정입니다";
            default -> "인증이 필요합니다";
        };
    }
}
```

## 보안 유틸리티 패턴
```java
public final class SecurityUtils {
    
    public static Optional<String> getCurrentUsername() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return Optional.of(userDetails.getUsername());
        }
        
        return Optional.empty();
    }
    
    public static Optional<CustomUserDetails> getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }
    
    public static boolean hasRole(Role role) {
        return getCurrentUser()
            .map(user -> user.getUser().getRole() == role)
            .orElse(false);
    }
    
    private SecurityUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
```

## 설정 상수 관리
```java
@ConfigurationProperties(prefix = "security")
@Component
@Data
public class SecurityProperties {
    
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    
    @Data
    public static class Jwt {
        private String secretKey;
        private long expirationTime;
    }
    
    @Data
    public static class Cors {
        private String[] allowedOrigins;
        private String[] allowedMethods;
        private boolean allowCredentials;
    }
}
```

## 테스트 지원 유틸리티
```java
public final class SecurityTestUtils {
    
    public static Authentication createAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    }
    
    public static Authentication createAuthenticationWithRole(Role role) {
        User user = User.create("test", "test@example.com", "password", role);
        return createAuthentication(user);
    }
    
    private SecurityTestUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
```


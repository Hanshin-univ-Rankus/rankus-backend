# Config Layer 가이드

> Spring Boot 애플리케이션의 설정과 Bean 구성을 담당하는 계층

## ⚙️ Config Layer 개요

### 핵심 책임
- **Spring Bean 설정**: 애플리케이션 컨텍스트에 필요한 Bean 등록
- **보안 설정**: Spring Security 및 인증/인가 구성
- **외부 연동 설정**: 데이터베이스, API 문서 등 외부 시스템 연동
- **환경별 설정**: 프로파일별 다른 설정 적용

### 설계 원칙
- **관심사 분리**: 기능별로 설정 클래스 분리
- **환경 독립성**: 프로파일별 다른 설정 지원
- **보안 우선**: 민감 정보는 환경변수로 관리
- **확장 가능성**: 새로운 설정 추가 용이성

## 📁 Config 클래스 구성

### SecurityConfig
**Spring Security 메인 설정**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!default")  // default 프로파일에서는 비활성화
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
```

### DevSecurityConfig
**개발 환경용 보안 설정 (보안 비활성화)**

```java
@Configuration
@EnableWebSecurity
@Profile("default")  // default 프로파일에서만 활성화
public class DevSecurityConfig {
    
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> 
                authorize.anyRequest().permitAll())  // 모든 요청 허용
            .headers(headers -> 
                headers.frameOptions().disable());  // H2 Console 사용 가능
            
        return http.build();
    }
}
```

### MethodSecurityConfig
**메서드 레벨 보안 설정**

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(unifiedPermissionEvaluator());
        return handler;
    }
    
    @Bean
    public UnifiedPermissionEvaluator unifiedPermissionEvaluator() {
        return new UnifiedPermissionEvaluator(
            labApplicationPermissionHandler(),
            labImagePermissionHandler()
        );
    }
    
    @Bean
    public LabApplicationPermissionHandler labApplicationPermissionHandler() {
        return new LabApplicationPermissionHandler();
    }
    
    @Bean
    public LabImagePermissionHandler labImagePermissionHandler() {
        return new LabImagePermissionHandler();
    }
}
```

### SwaggerConfig
**API 문서 설정**

```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Rankus API",
        version = "1.0",
        description = "대학 랩실 관리 플랫폼 API 문서"
    )
)
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Rankus API")
                .description("대학 랩실 관리 플랫폼 API 문서")
                .version("1.0"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요")));
    }
}
```

### CorsConfig
**CORS 설정**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")  // 개발 환경에서는 모든 도메인 허용
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### DomainConfig
**도메인 및 애플리케이션 Bean 설정**

```java
@Configuration
@EnableJpaAuditing  // BaseTimeEntity 자동 시간 설정 활성화
public class DomainConfig {
    
    // 도메인 서비스 Bean 등록 (필요시)
    
    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
```

### JacksonConfig
**JSON 직렬화/역직렬화 설정**

```java
@Configuration
public class JacksonConfig {
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
}
```

### WebConfig
**웹 관련 설정**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new PageableHandlerMethodArgumentResolver());
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
    }
    
    private ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```

## 🌍 환경별 설정 관리

### application.yml (기본 설정)
```yaml
spring:
  application:
    name: rankus
  
  profiles:
    active: default
    
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        
  datasource:
    url: jdbc:mysql://localhost:3306/rankus
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret-key: "your-secret-key-for-development-environment-only"
  expiration-time: 86400000  # 24시간

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### application-secure.yml (로컬 보안 테스트용)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret-key: ${JWT_SECRET_KEY}
  expiration-time: ${JWT_EXPIRATION_TIME:86400000}

logging:
  level:
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
```

### application-aws.yml (배포용)
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret-key: ${JWT_SECRET_KEY}
  expiration-time: ${JWT_EXPIRATION_TIME:86400000}

logging:
  level:
    root: INFO
    org.univ.rankus: DEBUG
```

## 🔐 보안 설정 세부사항

### JWT 설정
```java
@ConfigurationProperties(prefix = "jwt")
@Component
@Data
public class JwtProperties {
    
    private String secretKey;
    private long expirationTime;
    
    @PostConstruct
    public void validateProperties() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters");
        }
    }
}
```

### 보안 헤더 설정
```java
@Configuration
public class SecurityHeaderConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilter() {
        FilterRegistrationBean<SecurityHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeaderFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}

public class SecurityHeaderFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 보안 헤더 추가
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        chain.doFilter(request, response);
    }
}
```

## 📊 데이터베이스 설정

### JPA 설정 최적화
```java
@Configuration
public class JpaConfig {
    
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (properties) -> {
            // 배치 크기 설정
            properties.put("hibernate.jdbc.batch_size", 20);
            properties.put("hibernate.order_inserts", true);
            properties.put("hibernate.order_updates", true);
            
            // 2차 캐시 설정 (필요시)
            properties.put("hibernate.cache.use_second_level_cache", false);
            
            // SQL 통계 (개발 환경에서만)
            if (isDevProfile()) {
                properties.put("hibernate.generate_statistics", true);
            }
        };
    }
    
    private boolean isDevProfile() {
        // 프로파일 확인 로직
        return Arrays.asList(environment.getActiveProfiles()).contains("default");
    }
}
```

### 트랜잭션 관리 설정
```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
```

## 🧪 Config 테스트

### 보안 설정 테스트
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityConfigTest {
    
    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }
    
    @Test
    void 인증_없이_보호된_엔드포인트_접근시_401_반환() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void 공개_엔드포인트는_인증_없이_접근_가능() throws Exception {
        mockMvc.perform(get("/api/labs"))
            .andExpect(status().isOk());
    }
}
```

### JWT 설정 테스트
```java
@SpringBootTest
class JwtConfigTest {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Test
    void JWT_토큰을_올바르게_생성하고_검증한다() {
        // given
        String email = "test@example.com";
        Role role = Role.STUDENT;
        
        // when
        String token = jwtTokenProvider.createToken(email, role);
        
        // then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo(email);
    }
}
```

## 🎯 설정 모범 사례

### 환경변수 관리
1. **개발 환경**: `.env` 파일 또는 IDE 설정
2. **테스트 환경**: `application-test.yml`
3. **운영 환경**: 시스템 환경변수 또는 AWS Parameter Store

### 보안 고려사항
1. **JWT Secret**: 32자 이상의 강력한 키 사용
2. **비밀번호**: BCrypt 암호화 적용
3. **CORS**: 운영 환경에서는 특정 도메인만 허용
4. **HTTPS**: 운영 환경에서는 HTTPS 강제

### 성능 최적화
1. **Connection Pool**: HikariCP 설정 최적화
2. **JPA Batch**: 배치 Insert/Update 활성화
3. **캐싱**: 필요한 경우 2차 캐시 적용
4. **Lazy Loading**: 기본적으로 지연 로딩 사용

## 📋 Config 상세 가이드

설정 클래스의 구체적인 구현 방법과 컨벤션은 다음을 참조하세요:

- **Config 컨벤션**: @CONVENTIONS.md
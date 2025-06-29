# Config 클래스 컨벤션

## 클래스 네이밍
- 패턴: `{Domain}Config` 또는 `{Feature}Config`
- 예시: `SecurityConfig`, `JacksonConfig`, `SwaggerConfig`

## 기본 구조
```java
@Configuration
@EnableSomeFeature  // 기능 활성화 어노테이션
@RequiredArgsConstructor
public class {Feature}Config {
    
    // 설정 프로퍼티 주입
    private final {Feature}Properties {feature}Properties;
    
    @Bean
    @ConditionalOnMissingBean
    public {Type} {beanName}() {
        return new {Type}({parameters});
    }
    
    @Bean
    @Profile("!test")  // 프로파일 조건
    public {Type} {productionBean}() {
        return new {Type}({productionParameters});
    }
}
```

## SecurityConfig 패턴
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!default")
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 데이터베이스 Config 패턴
```java
@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class DatabaseConfig {
    
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (properties) -> {
            properties.put("hibernate.jdbc.batch_size", 20);
            properties.put("hibernate.order_inserts", true);
            properties.put("hibernate.order_updates", true);
        };
    }
    
    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
```

## API 문서 Config 패턴
```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Rankus API",
        version = "1.0",
        description = "API 문서"
    )
)
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(createApiInfo())
            .addSecurityItem(createSecurityRequirement())
            .components(createComponents());
    }
    
    private Info createApiInfo() {
        return new Info()
            .title("Rankus API")
            .description("대학 랩실 관리 플랫폼 API")
            .version("1.0");
    }
    
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
    
    private Components createComponents() {
        return new Components()
            .addSecuritySchemes("bearerAuth", createSecurityScheme());
    }
    
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");
    }
}
```

## CORS Config 패턴
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns(getAllowedOrigins())
            .allowedMethods(getAllowedMethods())
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
    
    private String[] getAllowedOrigins() {
        if (isProductionProfile()) {
            return new String[]{"https://yourdomain.com"};
        }
        return new String[]{"*"};
    }
    
    private String[] getAllowedMethods() {
        return new String[]{"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};
    }
    
    private boolean isProductionProfile() {
        // 프로파일 확인 로직
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
```

## 직렬화 Config 패턴
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
    
    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))
            .timeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
```

## 프로파일별 설정 패턴
```java
// 개발 환경용
@Configuration
@Profile("default")
public class DevSecurityConfig {
    
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .build();
    }
}

// 운영 환경용
@Configuration
@Profile("prod")
public class ProdSecurityConfig {
    
    @Bean
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
            .build();
    }
}
```

## 조건부 Bean 생성
```java
@Configuration
public class ConditionalConfig {
    
    @Bean
    @ConditionalOnProperty(value = "app.caching.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
    
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConditionalOnClass(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate() {
        return new RedisTemplate<>();
    }
}
```

## Properties 클래스 패턴
```java
@ConfigurationProperties(prefix = "app.feature")
@Component
@Data
@Validated
public class FeatureProperties {
    
    @NotBlank
    private String name;
    
    @Min(1)
    @Max(100)
    private int maxSize = 10;
    
    private boolean enabled = true;
    
    private List<String> allowedValues = new ArrayList<>();
    
    private Duration timeout = Duration.ofSeconds(30);
    
    @PostConstruct
    public void validate() {
        if (enabled && allowedValues.isEmpty()) {
            throw new IllegalArgumentException("Allowed values must not be empty when enabled");
        }
    }
}
```

## 메서드 Security Config
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
        return new UnifiedPermissionEvaluator();
    }
}
```

## 테스트 Config 패턴
```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    }
    
    @Bean
    @Primary
    public JwtTokenProvider mockJwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }
}
```

## Bean 순서 및 의존성
```java
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(JpaRepositoriesAutoConfiguration.class)
public class DatabaseConfig {
    
    @Bean
    @DependsOn("dataSource")
    public CustomRepository customRepository() {
        return new CustomRepository();
    }
    
    @Bean
    @Order(1)
    public HighPriorityBean highPriorityBean() {
        return new HighPriorityBean();
    }
}
```

## 테스트 패턴
```java
@SpringBootTest
@TestPropertySource(properties = {
    "app.feature.enabled=true",
    "app.feature.max-size=50"
})
class ConfigTest {
    
    @Autowired
    private FeatureProperties featureProperties;
    
    @Test
    void 설정값이_올바르게_로딩된다() {
        assertThat(featureProperties.isEnabled()).isTrue();
        assertThat(featureProperties.getMaxSize()).isEqualTo(50);
    }
}
```
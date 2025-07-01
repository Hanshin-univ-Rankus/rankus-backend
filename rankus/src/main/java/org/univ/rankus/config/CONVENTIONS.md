# Config 클래스 컨벤션

> 🛠️ **Config 가이드**: @config/CLAUDE.md  
> 🎯 **네이밍 패턴**: @core/conventions.md#config-네이밍  
> 📋 **표준 템플릿**: @core/patterns.md

## 🏷️ 네이밍 매트릭스

| 타입         | 패턴                    | 예시                                 |
|------------|-----------------------|------------------------------------|
| 도메인 Config | `{Domain}Config`      | `SecurityConfig`, `DatabaseConfig` |
| 기능 Config  | `{Feature}Config`     | `JacksonConfig`, `SwaggerConfig`   |
| Properties | `{Feature}Properties` | `JwtProperties`, `CorsProperties`  |

## 🏗️ 기본 템플릿

> 📋 **표준 템플릿**: @core/patterns.md#config-클래스

## 🌍 프로파일별 설정

### 매트릭스

| 프로파일    | Config             | 설정       |
|---------|--------------------|----------|
| default | DevSecurityConfig  | 모든 요청 허용 |
| prod    | ProdSecurityConfig | HTTPS 강제 |

```java
// 개발용
@Configuration @Profile("default")
public class DevSecurityConfig {
    @Bean SecurityFilterChain devFilterChain(HttpSecurity http) { /* 모든 요청 허용 */ }
}

// 운영용  
@Configuration @Profile("prod")
public class ProdSecurityConfig {
    @Bean SecurityFilterChain prodFilterChain(HttpSecurity http) { /* HTTPS 강제 */ }
}
```

## 🔄 조건부 Bean 매트릭스

| 조건 어노테이션                    | 용도        | 예시            |
|-----------------------------|-----------|---------------|
| `@ConditionalOnProperty`    | 설정값 기반    | 캐싱 활성화        |
| `@ConditionalOnMissingBean` | Bean 없을 때 | 기본 DataSource |
| `@ConditionalOnClass`       | 클래스 존재시   | Redis 설정      |

```java
@Configuration
public class ConditionalConfig {
    @Bean @ConditionalOnProperty("app.caching.enabled")
    CacheManager cacheManager() { return new ConcurrentMapCacheManager(); }
    
    @Bean @ConditionalOnMissingBean(DataSource.class)
    DataSource defaultDataSource() { return DataSourceBuilder.create().build(); }
}
```

## ⚙️ Properties 클래스

> 📋 **표준 템플릿**: @core/patterns.md#properties-클래스

### 고급 Config 패턴

| Config               | 역할               | 템플릿 참조            |
|----------------------|------------------|-------------------|
| MethodSecurityConfig | @PreAuthorize 지원 | @core/patterns.md |
| TestConfig           | 테스트용 Bean        | @core/patterns.md |
| DatabaseConfig       | JPA Auditing     | @core/patterns.md |

## 🔗 Bean 순서 제어

### 의존성 매트릭스

| 어노테이션                 | 용도           | 예시            |
|-----------------------|--------------|---------------|
| `@AutoConfigureAfter` | 특정 Config 이후 | DataSource 이후 |
| `@DependsOn`          | Bean 의존성     | dataSource 의존 |
| `@Order`              | 실행 순서        | 우선순위 설정       |

```java
@Configuration @AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class DatabaseConfig {
    @Bean @DependsOn("dataSource") CustomRepository customRepository() { return new CustomRepository(); }
    @Bean @Order(1) HighPriorityBean highPriorityBean() { return new HighPriorityBean(); }
}
```

## 🧪 테스트 패턴

> 🧪 **Config 테스트**: @core/testing.md#config-테스트  
> 📋 **테스트 템플릿**: @core/patterns.md#config-테스트
package org.univ.rankus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.univ.rankus.adapter.out.security.JwtAuthenticationFilter;
import org.univ.rankus.adapter.out.security.JwtTokenProvider;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 1) CSRF 비활성화 (REST API 에서는 토큰으로 보호)
        http.csrf(AbstractHttpConfigurer::disable);

        // 2) 세션을 사용하지 않음 (Stateless)
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        // 3) 인증·인가 설정
        http.authorizeHttpRequests(authz -> authz
                // 인증 없이 허용할 경로
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**")
                .permitAll()
                // ✅ 랩실 API 허용 (GET만) - 단, applications 하위 경로는 제외
                .requestMatchers(HttpMethod.GET, "/api/labs", "/api/labs/", "/api/labs/{labId}")
                .permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest()
                .authenticated()
        );

        // 4) JWT 인증 필터 등록
        http.addFilterBefore(
                new JwtAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        // 5) 예외 처리: 인증 실패(401), 인가 실패(403) 등의 기본 핸들러 사용
        http.exceptionHandling(Customizer.withDefaults());

        return http.build();
    }

    // 6) 비밀번호 암호화에 사용할 PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 7) AuthenticationManager 빈 등록 (필요 시 직접 주입 가능
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
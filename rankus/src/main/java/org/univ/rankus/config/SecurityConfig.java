package org.univ.rankus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.univ.rankus.common.security.customUser.CustomUserDetailsService;
import org.univ.rankus.common.security.jwt.JwtAuthenticationEntryPoint;
import org.univ.rankus.common.security.jwt.JwtAuthenticationFilter;
import org.univ.rankus.common.security.jwt.JwtTokenProvider;

/**
 * Spring Security의 전반적인 보안 설정을 담당하는 클래스입니다.
 * - JWT 기반 인증/인가
 * - 세션 미사용(Stateless)
 * - URL별 접근 권한 설정
 * - 커스텀 PermissionEvaluator 적용
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 토큰 관련 기능 제공
    private final JwtTokenProvider tokenProvider;
    // 인증 실패 시 처리 핸들러
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    // 사용자 정보 조회 서비스
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * HTTP 보안 설정을 정의합니다.
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1) CSRF 비활성화 (REST API + JWT 사용)
                .csrf(csrf -> csrf.disable())

                // 2) 세션을 사용하지 않음 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3) 인증·인가 예외 처리 지정
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                // 4) URL별 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 회원가입, 로그인 API, 랩실 조회, 이미지 조회는 인증 없이 접근 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/labs").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 5) JWT 인증 필터 추가 (기존 UsernamePasswordAuthenticationFilter 앞에 위치)
                .addFilterBefore(
                        new JwtAuthenticationFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )

                .cors(cors -> cors
                        .configurationSource(request -> {
                            var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                            corsConfig.setAllowedOriginPatterns(java.util.List.of("*"));
                            corsConfig.setAllowedMethods(java.util.List.of("*"));
                            corsConfig.setAllowedHeaders(java.util.List.of("*"));
                            corsConfig.setAllowCredentials(true);
                            return corsConfig;
                        })
                )

        ;

        return http.build();
    }

    /**
     * AuthenticationManager 빈 등록
     * @param http HttpSecurity 객체
     * @return AuthenticationManager
     * @throws Exception 예외 발생 시
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    /**
     * 비밀번호 암호화에 사용할 PasswordEncoder 빈 등록 (BCrypt 사용)
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

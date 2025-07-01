package org.univ.rankus.adapter.in.web.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.univ.rankus.adapter.in.web.controller.AuthController;
import org.univ.rankus.adapter.in.web.controller.UserController;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.CustomAccessDeniedHandler;
import org.univ.rankus.common.security.jwt.JwtAuthenticationEntryPoint;
import org.univ.rankus.common.security.jwt.JwtAuthenticationFilter;
import org.univ.rankus.common.security.jwt.JwtTokenProvider;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.common.security.permission.UnifiedPermissionEvaluator;
import org.univ.rankus.config.CorsConfig;
import org.univ.rankus.config.MethodSecurityConfig;
import org.univ.rankus.config.SecurityConfig;
import org.univ.rankus.domain.model.user.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class, UserController.class})
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, MethodSecurityConfig.class})
// Default profile now uses JWT authentication
class JwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private UserQueryUseCase userQueryUseCase;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;

    @MockitoBean
    private JwtAuthenticationEntryPoint authEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private CorsConfig corsConfig;

    @Test
    @DisplayName("로그인 후 JWT로 보호된 엔드포인트에 접근하면 200 OK 반환")
    void accessWithValidToken() throws Exception {
        // 1) 로그인 모킹
        String email = "user@example.com";
        String rawPassword = "password";
        String fakeToken = "fake-jwt-token";

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .name("테스트사용자")
                .email(email)
                .build();

        AuthResponseDto authDto = AuthResponseDto.builder()
                .token(fakeToken)
                .user(userDto)
                .build();

        UserLoginRequestDto loginRequest = UserLoginRequestDto.builder()
                .email(email)
                .password(rawPassword)
                .build();
        given(authUseCase.login(any(UserLoginRequestDto.class))).willReturn(authDto);

        // 로그인 응답 사용자 모킹
        User mockUser = org.mockito.Mockito.mock(User.class);
        given(userQueryUseCase.getUserByEmail(email)).willReturn(mockUser);
        // getUserById 메서드 모킹 추가
        given(userQueryUseCase.getUserById(1L)).willReturn(mockUser);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getName()).willReturn("Tester");
        given(mockUser.getEmail()).willReturn(email);
        given(mockUser.getRole()).willReturn(org.univ.rankus.domain.model.user.Role.ADMIN);

        // JWT 필터 모킹: 토큰 유효, Authentication 생성
        given(tokenProvider.validateToken(fakeToken)).willReturn(true);
        CustomUserDetails principal = org.mockito.Mockito.mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(1L);
        given(principal.getUsername()).willReturn(email);

        // doReturn 사용하여 타입 불일치 문제 해결
        org.mockito.Mockito.doReturn(
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")
                )
        ).when(principal).getAuthorities();

        // TestingAuthenticationToken에 principal과 권한 전달
        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities =
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")
                );
        TestingAuthenticationToken auth = new TestingAuthenticationToken(principal, null, authorities);
        given(tokenProvider.getAuthentication(fakeToken, userDetailsService))
                .willReturn(auth);

        // 2) 로그인 요청
        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new java.util.HashMap<String, String>() {{
                                            put("email", email);
                                            put("password", rawPassword);
                                        }}
                                ))
                )
                .andExpect(status().isOk())
                .andReturn();

        // 3) 토큰 추출
        String responseStr = loginResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseStr);
        String token = root.path("data").path("token").asText();

        // 4) 보호된 엔드포인트 호출: /api/users/me
        MvcResult userMeResult = mockMvc.perform(
                        get("/api/users/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // 응답 내용 확인용 출력
        System.out.println("User ME Response: " + userMeResult.getResponse().getContentAsString());

        // 응답 검증
        mockMvc.perform(
                        get("/api/users/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("사용자 정보 조회 성공"));
    }

    @Test
    @DisplayName("잘못된 토큰으로 보호된 엔드포인트에 접근하면 401 Unauthorized")
    void accessWithInvalidToken() throws Exception {
        String invalidToken = "invalid-token";
        given(tokenProvider.validateToken(invalidToken)).willReturn(false);

        mockMvc.perform(
                        get("/api/users/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }
}

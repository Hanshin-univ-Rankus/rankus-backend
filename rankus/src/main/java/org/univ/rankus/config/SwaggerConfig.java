package org.univ.rankus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI rankusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rankus Lab Management API")
                        .version("v1.0.0")
                        .description("""
                                # Rankus - 대학 랩실 통합 관리 플랫폼 API
                                                                
                                랩실 홍보, 지원 관리, 운영 효율화, 동기 부여를 위한 종합적인 REST API를 제공합니다.
                                                                
                                ## 주요 기능
                                - 🔐 **사용자 인증**: JWT 기반 회원가입/로그인
                                - 🏢 **랩실 관리**: 랩실 생성, 홍보, 멤버 관리
                                - 📝 **지원 시스템**: 랩실 지원, 면접 관리
                                - 📊 **랭킹 시스템**: 점수 관리 및 랭킹 조회
                                - 📅 **출석 관리**: QR 기반 출석 체크
                                - 🗳️ **투표 시스템**: 랩실 내 의사결정 도구
                                - 📢 **공지사항**: 랩실 공지사항 관리
                                - 📆 **캘린더**: 일정 및 면접 관리
                                                                
                                ## 인증 방법
                                1. `/api/auth/login`으로 로그인하여 액세스 토큰(15분)과 리프레시 토큰(7일)을 획득합니다.
                                2. API 요청 시 헤더에 `Authorization: Bearer {accessToken}`을 추가하여 인증합니다.
                                3. 액세스 토큰 만료 시(401 에러), `/api/auth/refresh`를 호출하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다. (토큰 순환)
                                4. 로그아웃 시 `/api/auth/logout`을 호출하여 서버에 저장된 토큰을 모두 무효화합니다.
                                """)
                        .contact(new Contact()
                                .name("Rankus Team")
                                .email("admin@rankus.com")
                                .url("https://github.com/rankus-team"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("http://3.34.229.56:8080")
                                .description("개발 서버"),
                        new Server()
                                .url("https://api.rankus.com")
                                .description("프로덕션 서버")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Rankus 프로젝트 문서")
                        .url("https://github.com/rankus-team/rankus/wiki"))
                .tags(List.of(
                        new Tag().name("Auth").description("🔐 인증 관리 (회원가입, 로그인, 토큰 갱신, 로그아웃)"),
                        new Tag().name("User").description("👤 사용자 정보 관리"),
                        new Tag().name("LabPromotion").description("🏢 랩실 홍보 및 조회"),
                        new Tag().name("LabCreationRequest").description("🏗️ 랩실 생성 신청"),
                        new Tag().name("LabApplication").description("📝 랩실 지원 관리"),
                        new Tag().name("LabNotice").description("📢 랩실 공지사항"),
                        new Tag().name("LabImage").description("🖼️ 랩실 이미지 관리"),
                        new Tag().name("LabResource").description("📚 랩실 자료실 관리"),
                        new Tag().name("LabDashboard").description("📊 랩실 대시보드"),
                        new Tag().name("Interview").description("🎯 면접 관리"),
                        new Tag().name("Calendar Schedule").description("📅 일반 일정 관리"),
                        new Tag().name("Calendar Interview").description("📆 캘린더 면접 일정 조회"),
                        new Tag().name("AttendanceSession").description("✅ 출석 세션 관리"),
                        new Tag().name("AttendanceRecord").description("📋 출석 기록 관리"),
                        new Tag().name("Ranking").description("🏆 랭킹 조회"),
                        new Tag().name("ScoreSubmission").description("📊 점수 신청 관리"),
                        new Tag().name("Vote").description("🗳️ 투표 시스템"),
                        new Tag().name("FileUpload").description("📁 파일 업로드")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.")));
    }
}

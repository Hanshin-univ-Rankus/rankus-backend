package org.univ.rankus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI rankusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rankus Lab Management API")
                        .version("v1.0.0")
                        .description("교내 랩실 홍보·운영을 위한 REST API 문서")
                        .contact(new Contact()
                                .name("Rankus Team")
                                .email("admin@rankus.com")))
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

package org.univ.rankus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                        .description("교내 랩실 홍보·운영을 위한 REST API 문서"));
    }
}

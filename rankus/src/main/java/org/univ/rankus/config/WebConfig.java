package org.univ.rankus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .defaultContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
            .mediaType("json", new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
    }
}

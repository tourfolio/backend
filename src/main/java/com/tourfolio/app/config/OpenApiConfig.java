package com.tourfolio.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("https://tourfolio.kr");
        server.setDescription("운영 서버");

        return new OpenAPI().servers(List.of(server));
    }
}
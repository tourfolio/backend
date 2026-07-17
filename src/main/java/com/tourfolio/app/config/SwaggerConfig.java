package com.tourfolio.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server-url:http://localhost:8000}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI tourfolioOpenAPI() {
        Server server = new Server();
        server.setUrl(swaggerServerUrl);
        server.setDescription("Tourfolio API Server");

        Contact contact = new Contact();
        contact.setEmail("tourfolio@example.com");
        contact.setName("Tourfolio Team");

        Info info = new Info()
                .title("투어폴리오(Tourfolio) 백엔드 통합 API 명세서")
                .version("1.0")
                .contact(contact)
                .description("2026 관광데이터 활용 공모전 - 팀해달별 탐색 및 가상 주식 투자 시스템 코어 인프라 명세서");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }

    @Bean
    public GroupedOpenApi totalApi() {
        return GroupedOpenApi.builder()
                .group("01. 투어폴리오 백엔드 전체 API 마스터 풀")
                .pathsToMatch("/api/**")
                .build();
    }
}

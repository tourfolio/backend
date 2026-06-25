package com.tourfolio.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tourfolioOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8000");
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("tourfolio@example.com");
        contact.setName("Tourfolio Team");

        Info info = new Info()
                .title("투어폴리오(Tourfolio) 코어 백엔드 인프라 API")
                .version("1.0")
                .contact(contact)
                .description("2026 관광데이터 활용 공모전 - 팀해달별 가상 투자 및 관광지 탐색 통합 API 문서");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }

    @Bean
    public GroupedOpenApi touristSpotsApi() {
        // 기존의 관광지 발견/탐색 API 그룹 레이어 유지
        return GroupedOpenApi.builder()
                .group("01. 관광지 발견 및 탐색 파트")
                .pathsToMatch("/api/tourist-spots/**")
                .build();
    }

    @Bean
    public GroupedOpenApi stocksApi() {

        return GroupedOpenApi.builder()
                .group("02. 가상 투자 및 주식 정산 파트")
                .pathsToMatch("/api/v1/stocks/**")
                .build();
    }
}
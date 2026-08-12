package com.tourfolio.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "TokenAuth";

    @Value("${swagger.server-url:https://tourfolio.kr}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI tourfolioOpenAPI() {
        // 1. 운영 서버 (HTTPS)
        Server prodServer = new Server();
        prodServer.setUrl(swaggerServerUrl);
        prodServer.setDescription("Production Server (HTTPS)");

        // 2. 로컬 개발 서버 (HTTP) - 필요 시 스웨거 UI 상단 드롭다운에서 변경 가능
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setEmail("tourfolio@example.com");
        contact.setName("Tourfolio Team");

        Info info = new Info()
                .title("투어폴리오(Tourfolio) 백엔드 통합 API 명세서")
                .version("1.0")
                .contact(contact)
                .description("2026 관광데이터 활용 공모전 - 팀해달별 탐색 및 가상 주식 투자 시스템 코어 인프라 명세서");

        // 자물쇠 버튼 클릭 시 나올 인증 방식 정의
        // Authorization 헤더에 "TOKEN_{uuid}_{userId}" 형식 값을 그대로 입력받음 (Bearer 아님)
        SecurityScheme tokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("형식 그대로 입력: TOKEN_{uuid}_{userId} (예: TOKEN_abc123-def456_1)");

        return new OpenAPI()
                .info(info)
                .servers(List.of(prodServer, localServer))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, tokenScheme))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public GroupedOpenApi totalApi() {
        return GroupedOpenApi.builder()
                .group("01. 투어폴리오 백엔드 전체 API 마스터 풀")
                .pathsToMatch("/api/**")
                .build();
    }
}
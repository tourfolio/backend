package com.tourfolio.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
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
                .title("Tourfolio API")
                .version("1.0")
                .contact(contact)
                .description("Tourfolio 관광 공모전 앱 API 문서");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}

package com.wordcrush.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wordCrushOpenApi() {
        return new OpenAPI().info(new Info()
                .title("WordCrush Backend API")
                .version("1.0.0")
                .description("Android WordCrush app backend compatible with existing client contracts.")
                .contact(new Contact().name("WordCrush")));
    }
}

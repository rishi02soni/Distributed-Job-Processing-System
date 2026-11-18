package com.jobprocessing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Distributed Job Processing System API")
                        .version("1.0.0")
                        .description("Production-style distributed asynchronous job processing platform with Redis Streams and MongoDB")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org"))
                        .contact(new Contact().name("Engineering Team").email("engineering@jobds.local")));
    }
}

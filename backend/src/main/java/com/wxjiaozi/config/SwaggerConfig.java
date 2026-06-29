package com.wxjiaozi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("高校教资题库API")
                        .version("1.0.0")
                        .description("刷题小程序+管理后台接口文档")
                        .contact(new Contact()
                                .name("wxjiaozi")
                                .email("support@wxjiaozi.com")));
    }
}

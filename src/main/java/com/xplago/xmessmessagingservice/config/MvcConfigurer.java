package com.xplago.xmessmessagingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /*registry.addMapping("/chats/**").allowedOrigins("*").allowedMethods("*");
        registry.addMapping("/messages/**").allowedOrigins("*").allowedMethods("*");
        registry.addMapping("/document/**").allowedOrigins("*").allowedMethods("*");
        registry.addMapping("/socket/**").allowedOrigins("*").allowedMethods("*").allowCredentials(false);*/
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://xmess.jelastic.regruhosting.ru")
                .maxAge(3600)
                .allowedHeaders("Accept", "Content-Type", "Origin",
                        "Authorization", "X-Auth-Token")
                .exposedHeaders("X-Auth-Token", "Authorization")
                .allowedMethods("POST", "GET", "DELETE", "PUT", "OPTIONS");
    }
}

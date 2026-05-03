package com.eventbooking.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // Required for null-safety
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // This allows all endpoints to be accessed from any origin
        registry.addMapping("/**")
                .allowedOrigins("*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
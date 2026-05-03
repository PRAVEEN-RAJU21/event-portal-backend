package com.eventbooking.backend.config; 

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // Added to fix the @NonNull warning
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    // Added @NonNull here to satisfy the inherited method requirements
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                // Using patterns is safer for cloud-to-cloud connections
                .allowedOriginPatterns(
                    "http://localhost:5173", 
                    "https://event-portal-frontend-six.vercel.app"
                ) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
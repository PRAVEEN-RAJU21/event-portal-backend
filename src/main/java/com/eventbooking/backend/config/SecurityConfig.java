package com.eventbooking.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for API compatibility
            .csrf(csrf -> csrf.disable())
            
            // 2. Use the CORS settings from your WebConfig.java
            .cors(Customizer.withDefaults())

            // 3. Define access rules
            .authorizeHttpRequests(auth -> auth
                // 🔓 Public Gate: Allow scanning and verification without login
                .requestMatchers("/api/public/**").permitAll()
                
                // 🔓 Allow health check and event listings
                .requestMatchers("/api/health", "/api/events").permitAll()
                
                // 🔓 Allow booking for now (can be restricted later)
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
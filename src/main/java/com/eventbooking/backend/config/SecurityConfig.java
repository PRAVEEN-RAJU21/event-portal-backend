package com.eventbooking.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for API compatibility (required for POST requests like Login/Booking)
            .csrf(csrf -> csrf.disable())
            
            // 2. Use the CORS settings from your WebConfig.java
            .cors(Customizer.withDefaults())

            // 3. Define access rules
            .authorizeHttpRequests(auth -> auth
                // 🔓 Public Gates: No login required for these
                .requestMatchers("/api/public/**", "/api/events", "/api/health").permitAll()
                
                // 🔓 Explicitly allow the Admin Login path
                .requestMatchers("/api/admin/login").permitAll() 
                
                // 🔓 Allow booking (public-facing features)
                .requestMatchers("/api/book", "/api/bookings").permitAll()
                
                // 🔓 THE KEY FIX: Allow the dashboard to fetch stats, table data, and export CSV
                .requestMatchers("/api/admin/stats", "/api/admin/bookings", "/api/admin/export").permitAll()
                
                // 🔒 Lock all other internal management routes
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
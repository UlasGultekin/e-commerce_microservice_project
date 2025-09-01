package com.mikro.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                // Read operations: authenticated
                .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                // Write operations: only SHOP_OWNER
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SHOP_OWNER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SHOP_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SHOP_OWNER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}



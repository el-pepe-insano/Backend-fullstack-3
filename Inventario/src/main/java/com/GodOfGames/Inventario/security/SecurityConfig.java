package com.GodOfGames.Inventario.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserContextFilter userContextFilter;

    // CADENA 1: documentacion/infra â€” sin filtros, maxima prioridad
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/v3/api-docs",
                "/swagger-resources/**",
                "/webjars/**",
                "/actuator/**",
                "/error",
                "/uploads/**"
            )
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }

    // CADENA 2: /api/productos â€” lectura publica, escritura solo Admin
    @Bean
    @Order(2)
    public SecurityFilterChain privateFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Catalogo de productos: lectura libre (vitrina publica de la tienda)
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                // Administracion de productos: solo Admin
                .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("Admin")
                .requestMatchers(HttpMethod.PATCH, "/api/productos/**").hasRole("Admin")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("Admin")
                .requestMatchers(HttpMethod.PUT, "/api/productos/*/stock").hasRole("Admin")
                // Reserva de stock durante el checkout: cualquier usuario autenticado
                .requestMatchers(HttpMethod.POST, "/api/productos/*/reservar").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(userContextFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
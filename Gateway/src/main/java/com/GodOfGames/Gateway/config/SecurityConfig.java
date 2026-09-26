package com.GodOfGames.Gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Deshabilita la autorizacion automatica de Spring Security (que exige token
 * valido en TODAS las rutas apenas detecta un ReactiveJwtDecoder en el
 * classpath). La autenticacion/autorizacion real la hace JwtAuthFilter
 * (GlobalFilter con Ordered=-1), que ya conoce las rutas publicas y los roles
 * protegidos. Esta clase solo evita que Spring Security se entrometa.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}
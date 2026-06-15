package com.GodOfGames.Gateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    // Rutas que NO requieren token
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/usuarios/login",
        "/api/usuarios/registro",
        "/api/usuarios/cambiar-contrasena",
        "/api/usuarios/recuperar-contrasena",
        "/actuator/health",
        "/actuator/info",
        "/api/productos",
        "/api/productos/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Si es ruta pública, dejamos pasar sin validar token
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Verificamos que exista el header Authorization
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Agregamos el usuario al header para que los microservicios lo puedan usar
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.headers(h -> {
                        h.set("X-User-Id", claims.getSubject());
                        h.set("X-User-Role", claims.get("rol", String.class));
                    }))
                    .build();

            return chain.filter(modifiedExchange);

        } catch (JwtException | IllegalArgumentException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
    return PUBLIC_PATHS.stream().anyMatch(publicPath -> {
        if (publicPath.endsWith("/**")) {
            String prefix = publicPath.substring(0, publicPath.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(publicPath);
    });
}

    @Override
    public int getOrder() {
        return -1; // Ejecutar antes que cualquier otro filtro
    }
}
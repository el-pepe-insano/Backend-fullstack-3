package com.GodOfGames.Gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final ReactiveJwtDecoder jwtDecoder;

    public JwtAuthFilter(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    // Rutas que NO requieren token
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/usuarios/login",
        "/api/usuarios/registro",
        "/api/usuarios/cambiar-contrasena",
        "/api/usuarios/recuperar-contrasena",
        "/actuator/health",
        "/actuator/info",
        "/api/productos",
        "/api/productos/**",
        "/uploads/productos/**"
    );

    // Rutas que ademas de token valido requieren un rol especifico
    // (el rol viene del claim "roles" del token de Entra ID -> App roles configurados en la app registration)
    private static final Map<String, String> ROLE_PROTECTED_PATHS = Map.of(
        "/api/productos/admin/**", "Admin",
        "/api/pedidos/admin/**", "Admin",
        "/api/usuarios/admin/**", "Admin"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Si es ruta publica, dejamos pasar sin validar token
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Verificamos que exista el header Authorization
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        // jwtDecoder.decode() valida firma (JWKS de Entra ID), issuer, expiracion y audience
        return jwtDecoder.decode(token)
                .flatMap(jwt -> authorizeAndForward(exchange, chain, path, jwt))
                .onErrorResume(JwtException.class, e -> unauthorized(exchange));
    }

    private Mono<Void> authorizeAndForward(ServerWebExchange exchange, GatewayFilterChain chain, String path, Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        String primaryRole = (roles != null && !roles.isEmpty()) ? roles.get(0) : null;

        String requiredRole = requiredRoleForPath(path);
        if (requiredRole != null && (roles == null || !roles.contains(requiredRole))) {
            return forbidden(exchange);
        }

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set("X-User-Id", jwt.getSubject());
                    if (primaryRole != null) {
                        h.set("X-User-Role", primaryRole);
                    }
                    Object preferredUsername = jwt.getClaims().get("preferred_username");
                    if (preferredUsername != null) {
                        h.set("X-User-Email", preferredUsername.toString());
                    }
                }))
                .build();

        return chain.filter(modifiedExchange);
    }

    private String requiredRoleForPath(String path) {
        return ROLE_PROTECTED_PATHS.entrySet().stream()
                .filter(entry -> matchesPath(path, entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> matchesPath(path, publicPath));
    }

    private boolean matchesPath(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Ejecutar antes que cualquier otro filtro
    }
}

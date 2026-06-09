package com.GodOfGames.Inventario.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    // CORRECCIÓN: Agrega un valor por defecto para evitar NPE si el Secret no llega
    @Value("${jwt.secret:SECRET_NO_CONFIGURADO}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                // CORRECCIÓN: Loguear el error real para diagnóstico futuro
                logger.warn("JWT inválido o expirado en ruta " + request.getRequestURI() + ": " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.contains("/v3/api-docs") ||
               path.contains("/swagger-ui") ||
               path.contains("/webjars") ||
               path.contains("/actuator"); // CORRECCIÓN: actuator también debe ser libre para los probes
    }
}
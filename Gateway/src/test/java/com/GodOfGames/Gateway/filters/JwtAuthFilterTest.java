package com.GodOfGames.Gateway.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private GatewayFilterChain chain;
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        jwtDecoder = mock(ReactiveJwtDecoder.class);
        jwtAuthFilter = new JwtAuthFilter(jwtDecoder);
        chain = mock(GatewayFilterChain.class);

        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private Jwt jwtValido(String usuario, String rol) {
        return new Jwt(
                "token-de-prueba",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", usuario,
                        "cognito:groups", List.of(rol),
                        "username", usuario + "@godofgames.com",
                        "token_use", "access"
                )
        );
    }

    @Test
    void filter_rutaPublicaExacta_dejaPasarSinValidar() {
        MockServerHttpRequest request =
                MockServerHttpRequest.post("/api/usuarios/login").build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void filter_rutaPublicaWildcard_dejaPasarSinValidar() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/productos/5").build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
    }

    @Test
    void filter_sinHeaderAuthorization_retorna401() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/pedidos").build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse().getStatusCode()
        );

        verify(chain, never()).filter(any());
    }

    @Test
    void filter_headerSinBearer_retorna401() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, "Basic algo")
                        .build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse().getStatusCode()
        );

        verify(chain, never()).filter(any());
    }

    @Test
    void filter_tokenValido_agregaHeadersYContinua() {
        when(jwtDecoder.decode(eq("token-entra-valido")))
                .thenReturn(
                        Mono.just(jwtValido("user1", "Cliente"))
                );

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/pedidos")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token-entra-valido"
                        )
                        .build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_tokenInvalido_retorna401() {
        when(jwtDecoder.decode(eq("token-corrupto-invalido")))
                .thenReturn(
                        Mono.error(
                                new BadJwtException("token invalido")
                        )
                );

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/pedidos")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token-corrupto-invalido"
                        )
                        .build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse().getStatusCode()
        );

        verify(chain, never()).filter(any());
    }

    @Test
    void filter_rolNoAutorizado_retorna403() {
        when(jwtDecoder.decode(eq("token-sin-rol-admin")))
                .thenReturn(
                        Mono.just(jwtValido("user1", "Cliente"))
                );

        MockServerHttpRequest request =
                MockServerHttpRequest.get(
                        "/api/productos/admin/reportes"
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer token-sin-rol-admin"
                )
                .build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(
                HttpStatus.FORBIDDEN,
                exchange.getResponse().getStatusCode()
        );

        verify(chain, never()).filter(any());
    }

    @Test
    void getOrder_retornaMenosUno() {
        assertEquals(-1, jwtAuthFilter.getOrder());
    }
}
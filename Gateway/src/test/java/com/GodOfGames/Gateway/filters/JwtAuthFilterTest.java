package com.GodOfGames.Gateway.filters;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private static final String SECRET = "requiemElMejorJuegoDeGodOfGames2026SecretKeyParaJWT12345678901234";

    private JwtAuthFilter jwtAuthFilter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter();
        ReflectionTestUtils.setField(jwtAuthFilter, "secretKey", SECRET);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String tokenValido(String usuario, String rol) {
        return Jwts.builder()
                .setSubject(usuario)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void filter_rutaPublicaExacta_dejaPasarSinValidar() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/usuarios/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_rutaPublicaWildcard_dejaPasarSinValidar() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/productos/5").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
    }

    @Test
    void filter_sinHeaderAuthorization_retorna401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pedidos").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_headerSinBearer_retorna401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pedidos")
                .header(HttpHeaders.AUTHORIZATION, "Basic algo")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_tokenValido_agregaHeadersYContinua() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pedidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido("user1", "CLIENTE"))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_tokenInvalido_retorna401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pedidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-corrupto-invalido")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void getOrder_retornaMenosUno() {
        assertEquals(-1, jwtAuthFilter.getOrder());
    }
}
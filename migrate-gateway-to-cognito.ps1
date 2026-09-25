# ============================================================
# Migra el Gateway de Microsoft Entra ID a AWS Cognito
# Corre esto DESDE LA RAIZ del repo del backend
# (C:\Users\Acer\Projects\Backend-fullstack-3)
# ============================================================

$ErrorActionPreference = "Stop"
[Environment]::CurrentDirectory = (Get-Location).Path
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

Write-Host "Eliminando archivos de Entra ID..." -ForegroundColor Yellow
Remove-Item -Force -ErrorAction SilentlyContinue "Gateway/src/main/java/com/GodOfGames/Gateway/config/EntraIdJwtDecoderConfig.java"
Remove-Item -Force -ErrorAction SilentlyContinue "Gateway/src/main/java/com/GodOfGames/Gateway/config/AudienceValidator.java"

Write-Host "Escribiendo Gateway/src/main/java/com/GodOfGames/Gateway/config/CognitoClientIdValidator.java..." -ForegroundColor Cyan
$content = @'
package com.GodOfGames.Gateway.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Los access tokens de Cognito NO traen el claim "aud" (a diferencia de Entra ID):
 * traen "token_use" = "access" y "client_id" con el App Client que lo emitio.
 * Esta clase reemplaza a la validacion de audience para adaptarse a ese formato.
 */
public class CognitoClientIdValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR = new OAuth2Error(
            "invalid_token",
            "El token no es un access token valido de este User Pool / App Client de Cognito",
            null
    );

    private final String expectedClientId;

    public CognitoClientIdValidator(String expectedClientId) {
        this.expectedClientId = expectedClientId;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tokenUse = token.getClaimAsString("token_use");
        String clientId = token.getClaimAsString("client_id");

        boolean esAccessToken = "access".equals(tokenUse);
        boolean clientIdValido = expectedClientId.equals(clientId);

        if (esAccessToken && clientIdValido) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(ERROR);
    }
}
'@
[System.IO.File]::WriteAllText("Gateway/src/main/java/com/GodOfGames/Gateway/config/CognitoClientIdValidator.java", $content, $utf8NoBom)

Write-Host "Escribiendo Gateway/src/main/java/com/GodOfGames/Gateway/config/CognitoJwtDecoderConfig.java..." -ForegroundColor Cyan
$content = @'
package com.GodOfGames.Gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.util.List;

/**
 * Construye el ReactiveJwtDecoder que valida access tokens emitidos por un
 * User Pool de AWS Cognito:
 * - Firma: contra las claves publicas (JWKS) del User Pool
 * - Issuer: que el token venga del User Pool configurado
 * - Client ID: que el token haya sido emitido para nuestro App Client (ver CognitoClientIdValidator)
 */
@Configuration
public class CognitoJwtDecoderConfig {

    @Value("${aws.cognito.region}")
    private String region;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        String issuer = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
        String jwkSetUri = issuer + "/.well-known/jwks.json";

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> clientIdValidator = new CognitoClientIdValidator(clientId);
        OAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(
                List.of(defaultValidators, clientIdValidator)
        );

        decoder.setJwtValidator(combined);
        return decoder;
    }
}
'@
[System.IO.File]::WriteAllText("Gateway/src/main/java/com/GodOfGames/Gateway/config/CognitoJwtDecoderConfig.java", $content, $utf8NoBom)

Write-Host "Escribiendo Gateway/src/main/java/com/GodOfGames/Gateway/filters/JwtAuthFilter.java..." -ForegroundColor Cyan
$content = @'
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
    // (el rol viene del claim "cognito:groups" del token de Cognito -> Grupos configurados en el User Pool)
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

        // jwtDecoder.decode() valida firma (JWKS de Cognito), issuer, expiracion y client_id
        return jwtDecoder.decode(token)
                .flatMap(jwt -> authorizeAndForward(exchange, chain, path, jwt))
                .onErrorResume(JwtException.class, e -> unauthorized(exchange));
    }

    private Mono<Void> authorizeAndForward(ServerWebExchange exchange, GatewayFilterChain chain, String path, Jwt jwt) {
        // Cognito manda los grupos del usuario en el claim "cognito:groups" (no "roles").
        List<String> roles = jwt.getClaimAsStringList("cognito:groups");
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
                    // Los access tokens de Cognito no traen el correo; traen "username"
                    // (que coincide con el correo si el User Pool usa email como sign-in).
                    Object username = jwt.getClaims().get("username");
                    if (username != null) {
                        h.set("X-User-Email", username.toString());
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
'@
[System.IO.File]::WriteAllText("Gateway/src/main/java/com/GodOfGames/Gateway/filters/JwtAuthFilter.java", $content, $utf8NoBom)

Write-Host "Escribiendo Gateway/src/main/resources/application.properties..." -ForegroundColor Cyan
$content = @'
spring.application.name=Gateway
server.port=8080

# JWT propio (ya no se usa en el Gateway, se deja por si algun otro lugar lo referencia)
jwt.secret=requiemElMejorJuegoDeGodOfGames2026SecretKeyParaJWT12345678901234

# AWS Cognito - datos del User Pool / App Client
# Se cargan por variable de entorno para no commitear IDs
aws.cognito.region=${AWS_COGNITO_REGION}
aws.cognito.user-pool-id=${AWS_COGNITO_USER_POOL_ID}
aws.cognito.client-id=${AWS_COGNITO_CLIENT_ID}

# Rutas publicas primero
spring.cloud.gateway.routes[0].id=usuarios-auth
spring.cloud.gateway.routes[0].uri=http://usuarios-service:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/usuarios/login,/api/usuarios/registro

spring.cloud.gateway.routes[4].id=inventario-uploads
spring.cloud.gateway.routes[4].uri=http://inventario-service:8082
spring.cloud.gateway.routes[4].predicates[0]=Path=/uploads/productos/**

# Ruta OPTIONS global para preflight CORS
spring.cloud.gateway.routes[5].id=options-preflight
spring.cloud.gateway.routes[5].uri=http://usuarios-service:8081
spring.cloud.gateway.routes[5].predicates[0]=Path=/**
spring.cloud.gateway.routes[5].predicates[1]=Method=OPTIONS
spring.cloud.gateway.routes[5].filters[0]=SetStatus=200

# Rutas privadas
spring.cloud.gateway.routes[1].id=usuarios-service
spring.cloud.gateway.routes[1].uri=http://usuarios-service:8081
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/usuarios/**

spring.cloud.gateway.routes[2].id=inventario-service
spring.cloud.gateway.routes[2].uri=http://inventario-service:8082
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/productos/**

spring.cloud.gateway.routes[3].id=pedidos-service
spring.cloud.gateway.routes[3].uri=http://pedidos-service:8083
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/v1/pedidos/**

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# Logging
logging.level.org.springframework.cloud.gateway=INFO

# Compatibilidad
spring.cloud.compatibility-verifier.enabled=false'@
[System.IO.File]::WriteAllText("Gateway/src/main/resources/application.properties", $content, $utf8NoBom)

Write-Host "Escribiendo Gateway/k8s/gateway-k8s.yaml..." -ForegroundColor Cyan
$content = @'
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: gateway
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
      - name: gateway-app
        image: ryuko67/godofgames-gateway:v4
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
        env:
        - name: JWT_SECRET
          value: "requiemElMejorJuegoDeGodOfGames2026SecretKeyParaJWT12345678901234"
        - name: AWS_COGNITO_REGION
          valueFrom:
            secretKeyRef:
              name: cognito-config
              key: region
        - name: AWS_COGNITO_USER_POOL_ID
          valueFrom:
            secretKeyRef:
              name: cognito-config
              key: user-pool-id
        - name: AWS_COGNITO_CLIENT_ID
          valueFrom:
            secretKeyRef:
              name: cognito-config
              key: client-id
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 15
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
spec:
  selector:
    app: gateway
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: LoadBalancer'@
[System.IO.File]::WriteAllText("Gateway/k8s/gateway-k8s.yaml", $content, $utf8NoBom)

Write-Host "Escribiendo Gateway/src/test/java/com/GodOfGames/Gateway/filters/JwtAuthFilterTest.java..." -ForegroundColor Cyan
$content = @'
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
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/usuarios/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
        verifyNoInteractions(jwtDecoder);
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
        when(jwtDecoder.decode(eq("token-entra-valido")))
                .thenReturn(Mono.just(jwtValido("user1", "Cliente")));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pedidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-entra-valido")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_tokenInvalido_retorna401() {
        when(jwtDecoder.decode(eq("token-corrupto-invalido")))
                .thenReturn(Mono.error(new BadJwtException("token invalido")));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/pedidos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-corrupto-invalido")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_rolNoAutorizado_retorna403() {
        when(jwtDecoder.decode(eq("token-sin-rol-admin")))
                .thenReturn(Mono.just(jwtValido("user1", "Cliente")));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/productos/admin/reportes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-sin-rol-admin")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void getOrder_retornaMenosUno() {
        assertEquals(-1, jwtAuthFilter.getOrder());
    }
}
'@
[System.IO.File]::WriteAllText("Gateway/src/test/java/com/GodOfGames/Gateway/filters/JwtAuthFilterTest.java", $content, $utf8NoBom)

Write-Host "Escribiendo Gateway/pom.xml..." -ForegroundColor Cyan
$content = @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.3</version>
        <relativePath/>
    </parent>

    <groupId>com.GodOfGames</groupId>
    <artifactId>Gateway</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Gateway</name>
    <description>API Gateway para GodOfGames</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.5</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Cloud Gateway (reactivo con WebFlux) -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <!-- Actuator para health probes de Kubernetes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- JWT propio (legacy, se deja por compatibilidad si algo mas lo usa) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Validacion de tokens JWT emitidos por el IDaaS (AWS Cognito): issuer, client_id y firma via JWKS -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
'@
[System.IO.File]::WriteAllText("Gateway/pom.xml", $content, $utf8NoBom)

Write-Host ""
Write-Host "Listo. Gateway migrado a AWS Cognito." -ForegroundColor Green
Write-Host "Falta completar AWS_COGNITO_REGION / AWS_COGNITO_USER_POOL_ID / AWS_COGNITO_CLIENT_ID" -ForegroundColor Yellow
Write-Host ""
git status

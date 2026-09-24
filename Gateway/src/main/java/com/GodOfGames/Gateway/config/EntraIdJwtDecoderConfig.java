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
 * Construye el ReactiveJwtDecoder que valida tokens emitidos por Microsoft Entra ID:
 * - Firma: contra las claves publicas (JWKS) del tenant
 * - Issuer: que el token venga del tenant configurado
 * - Audience: que el token haya sido emitido para este API (Client ID del backend)
 */
@Configuration
public class EntraIdJwtDecoderConfig {

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        String issuer = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
        String jwkSetUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(clientId);
        OAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(
                List.of(defaultValidators, audienceValidator)
        );

        decoder.setJwtValidator(combined);
        return decoder;
    }
}

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
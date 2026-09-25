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
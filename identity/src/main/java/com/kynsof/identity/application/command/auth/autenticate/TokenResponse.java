package com.kynsof.identity.application.command.auth.autenticate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenResponse implements IResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("not-before-policy")
    private int notBeforePolicy;

    @JsonProperty("session_state")
    private String sessionState;

    private String scope;
    
    // Campos para manejo de errores
    private String error;
    
    @JsonProperty("error_description")
    private String errorDescription;
    
    // Campo para verificar si es un error de autenticación
    private boolean authenticated = true;
    
    // Se asegura que scanAvailable no aparezca al serializar el objeto a JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean scanAvailable;
    
    // Nuevos campos para formato de error estandarizado
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String errorField = "email/password";

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String errorMessage = "The username or password is incorrect. Please try again.";

    // Campos para Two-Factor Authentication
    @JsonProperty("two_factor_required")
    private boolean twoFactorRequired = false;

    @JsonProperty("user_id")
    private String userId;
}

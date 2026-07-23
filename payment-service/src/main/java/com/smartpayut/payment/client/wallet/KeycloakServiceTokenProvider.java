package com.smartpayut.payment.client.wallet;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.smartpayut.payment.exception.ExternalServiceException;

@Component
public class KeycloakServiceTokenProvider implements ServiceTokenProvider {

    private final RestClient restClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final String configuredToken;

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public KeycloakServiceTokenProvider(
            RestClient.Builder builder,
            @Value("${services.keycloak.token-uri}") String tokenUri,
            @Value("${services.keycloak.client-id}") String clientId,
            @Value("${services.keycloak.client-secret}") String clientSecret,
            @Value("${services.wallet.service-token:}") String configuredToken) {
        this.restClient = builder.build();
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.configuredToken = configuredToken;
    }

    @Override
    public synchronized String getToken() {
        if (!configuredToken.isBlank()) {
            return configuredToken;
        }
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(10))) {
            return cachedToken;
        }
        if (clientSecret.isBlank()) {
            throw new ExternalServiceException(
                    "Payment no tiene configurado un token de servicio para invocar Wallet.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        try {
            TokenResponse response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new ExternalServiceException("Keycloak no devolvió un token de servicio.");
            }
            cachedToken = response.accessToken();
            expiresAt = Instant.now().plusSeconds(response.expiresIn());
            return cachedToken;
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException("No fue posible obtener el token de servicio.", exception);
        }
    }

    private record TokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
            @com.fasterxml.jackson.annotation.JsonProperty("expires_in") long expiresIn,
            Map<String, Object> ignored) {
    }
}

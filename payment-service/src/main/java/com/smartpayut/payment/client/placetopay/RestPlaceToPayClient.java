package com.smartpayut.payment.client.placetopay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartpayut.payment.exception.ExternalServiceException;

@Component
public class RestPlaceToPayClient implements PlaceToPayClient {

    private final RestClient restClient;
    private final String login;
    private final String secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public RestPlaceToPayClient(
            RestClient.Builder builder,
            @Value("${payment.placetopay.base-url}") String baseUrl,
            @Value("${payment.placetopay.login}") String login,
            @Value("${payment.placetopay.secret-key}") String secretKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.login = login;
        this.secretKey = secretKey;
    }

    @Override
    public SessionResult createSession(SessionCommand command) {
        verifyCredentials();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("auth", createAuth());
        request.put("payment", Map.of(
                "reference", command.reference(),
                "description", command.description(),
                "amount", Map.of("currency", "USD", "total", command.amount())));
        request.put("expiration", OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(command.expirationMinutes()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        request.put("returnUrl", command.returnUrl());
        request.put("cancelUrl", command.cancelUrl());
        request.put("ipAddress", command.ipAddress());
        request.put("userAgent", command.userAgent());

        try {
            JsonNode response = restClient.post().uri("/api/session").body(request).retrieve().body(JsonNode.class);
            if (response == null || !response.hasNonNull("requestId") || !response.hasNonNull("processUrl")) {
                throw new ExternalServiceException("PlaceToPay no devolvió una sesión válida.");
            }
            JsonNode status = response.path("status");
            return new SessionResult(
                    response.path("requestId").asLong(),
                    response.path("processUrl").asText(),
                    status.path("status").asText("PENDING"),
                    status.path("message").asText("Sesión creada."));
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException("No fue posible crear la sesión PlaceToPay Sandbox.", exception);
        }
    }

    @Override
    public SessionStatus querySession(long requestId) {
        verifyCredentials();
        try {
            JsonNode response = restClient.post()
                    .uri("/api/session/{requestId}", requestId)
                    .body(Map.of("auth", createAuth()))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new ExternalServiceException("PlaceToPay no devolvió el estado de la sesión.");
            }
            JsonNode status = response.path("status");
            return new SessionStatus(status.path("status").asText(), status.path("message").asText());
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException("No fue posible consultar la sesión PlaceToPay.", exception);
        }
    }

    @Override
    public boolean isValidCallback(long requestId, String status, OffsetDateTime date, String signature) {
        String source = requestId + status + date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + secretKey;
        byte[] expected = sha256(source.getBytes(StandardCharsets.UTF_8));
        byte[] received;
        try {
            received = Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return MessageDigest.isEqual(expected, received);
    }

    private Map<String, String> createAuth() {
        byte[] nonceBytes = new byte[16];
        secureRandom.nextBytes(nonceBytes);
        String nonce = Base64.getEncoder().encodeToString(nonceBytes);
        String seed = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        byte[] suffix = (seed + secretKey).getBytes(StandardCharsets.UTF_8);
        byte[] keyMaterial = new byte[nonceBytes.length + suffix.length];
        System.arraycopy(nonceBytes, 0, keyMaterial, 0, nonceBytes.length);
        System.arraycopy(suffix, 0, keyMaterial, nonceBytes.length, suffix.length);
        String tranKey = Base64.getEncoder().encodeToString(sha256(keyMaterial));
        return Map.of("login", login, "tranKey", tranKey, "nonce", nonce, "seed", seed);
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }

    private void verifyCredentials() {
        if (login.isBlank() || secretKey.isBlank()) {
            throw new ExternalServiceException("Las credenciales de PlaceToPay Sandbox no están configuradas.");
        }
    }
}

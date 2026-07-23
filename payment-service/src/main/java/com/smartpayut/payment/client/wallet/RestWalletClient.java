package com.smartpayut.payment.client.wallet;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.smartpayut.payment.dto.wallet.WalletApiResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementRequest;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.exception.ExternalServiceException;

@Component
public class RestWalletClient implements WalletClient {

    private final RestClient restClient;
    private final ServiceTokenProvider tokenProvider;

    public RestWalletClient(
            RestClient.Builder builder,
            ServiceTokenProvider tokenProvider,
            @Value("${services.wallet.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.tokenProvider = tokenProvider;
    }

    @Override
    public WalletResponse getCurrentWallet(String userBearerToken) {
        return execute(() -> restClient.get()
                .uri("/api/wallets/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(userBearerToken))
                .retrieve()
                .body(new ParameterizedTypeReference<WalletApiResponse<WalletResponse>>() {
                }), "No fue posible consultar la billetera del usuario.");
    }

    @Override
    public WalletMovementResponse debit(WalletMovementRequest request) {
        return movement("/internal/wallets/debit", request);
    }

    @Override
    public WalletMovementResponse credit(WalletMovementRequest request) {
        return movement("/internal/wallets/credit", request);
    }

    @Override
    public WalletMovementResponse refund(WalletMovementRequest request) {
        return movement("/internal/wallets/refund", request);
    }

    @Override
    public WalletResponse getByUserId(UUID userId) {
        return execute(() -> restClient.get()
                .uri("/internal/wallets/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenProvider.getToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<WalletApiResponse<WalletResponse>>() {
                }), "No fue posible consultar la billetera en Wallet.");
    }

    private WalletMovementResponse movement(String path, WalletMovementRequest request) {
        return execute(() -> restClient.post()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenProvider.getToken()))
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<WalletApiResponse<WalletMovementResponse>>() {
                }), "Wallet rechazó la operación de saldo.");
    }

    private <T> T execute(ResponseSupplier<T> supplier, String errorMessage) {
        try {
            WalletApiResponse<T> response = supplier.get();
            if (response == null || !response.success() || response.data() == null) {
                String detail = response == null ? "Sin respuesta." : response.message();
                throw new ExternalServiceException(errorMessage + " " + detail);
            }
            return response.data();
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException(errorMessage, exception);
        }
    }

    private String bearer(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    @FunctionalInterface
    private interface ResponseSupplier<T> {

        WalletApiResponse<T> get();
    }
}

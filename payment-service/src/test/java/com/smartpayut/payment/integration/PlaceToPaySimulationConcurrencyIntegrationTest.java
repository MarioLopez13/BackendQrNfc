package com.smartpayut.payment.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpayut.payment.client.placetopay.PlaceToPayClient;
import com.smartpayut.payment.client.wallet.WalletClient;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.messaging.publisher.PaymentEventPublisher;
import com.smartpayut.payment.repository.PaymentOutboxEventRepository;
import com.smartpayut.payment.repository.PaymentRepository;
import com.smartpayut.payment.service.PaymentEventStateService;
import com.smartpayut.payment.service.PlaceToPayService;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        PlaceToPayService.class,
        PaymentMapper.class,
        PaymentEventPublisher.class,
        PaymentEventStateService.class,
        PlaceToPaySimulationConcurrencyIntegrationTest.ObjectMapperConfiguration.class
})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "payment.placetopay.default-expiration-minutes=30",
        "payment.placetopay.simulation-enabled=true",
        "payment.placetopay.simulation-process-url-base=http://localhost:5174/#/top-up/simulated"
})
class PlaceToPaySimulationConcurrencyIntegrationTest {

    @Autowired
    private PlaceToPayService service;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOutboxEventRepository outboxRepository;

    @MockBean
    private WalletClient walletClient;

    @MockBean
    private PlaceToPayClient placeToPayClient;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void simultaneousConfirmationsCreditWalletAndCreateOutboxOnlyOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Payment payment = new Payment(
                userId,
                walletId,
                PaymentMethod.PLACETOPAY,
                new BigDecimal("3.00"),
                "concurrent-" + UUID.randomUUID(),
                "WALLET",
                "Recarga de saldo");
        payment.assignPlaceToPaySession(
                445566L,
                "http://localhost:5174/#/top-up/simulated/" + payment.getId(),
                "TOPUP-" + payment.getId());
        paymentRepository.saveAndFlush(payment);

        CountDownLatch callersReady = new CountDownLatch(2);
        AtomicInteger credits = new AtomicInteger();
        when(walletClient.getCurrentWallet("Bearer token")).thenAnswer(invocation -> {
            callersReady.countDown();
            assertThat(callersReady.await(5, TimeUnit.SECONDS)).isTrue();
            return new WalletResponse(walletId, userId, BigDecimal.ZERO, "USD", "ACTIVE");
        });
        when(walletClient.credit(any())).thenAnswer(invocation -> {
            credits.incrementAndGet();
            return new WalletMovementResponse(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("3.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("3.00"),
                    payment.getId().toString());
        });

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<PaymentResponse>> results = List.of(
                    executor.submit(() -> service.confirm(payment.getId(), "Bearer token")),
                    executor.submit(() -> service.confirm(payment.getId(), "Bearer token")));

            assertThat(results.get(0).get(10, TimeUnit.SECONDS).paymentStatus())
                    .isEqualTo(PaymentStatus.COMPLETED);
            assertThat(results.get(1).get(10, TimeUnit.SECONDS).paymentStatus())
                    .isEqualTo(PaymentStatus.COMPLETED);
        }

        assertThat(credits).hasValue(1);
        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
        assertThat(outboxRepository.findByPaymentIdAndEventType(payment.getId(), "topup.completed"))
                .isPresent();
        assertThat(outboxRepository.count()).isOne();
    }

    @TestConfiguration
    static class ObjectMapperConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }
}

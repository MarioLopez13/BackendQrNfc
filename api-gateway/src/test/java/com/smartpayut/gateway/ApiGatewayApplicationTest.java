package com.smartpayut.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void contextLoads() {
    }

    @Test
    void publishesAllMicroserviceRoutes() {
        Set<String> routeIds = routeDefinitionLocator.getRouteDefinitions()
                .map(definition -> definition.getId())
                .collectList()
                .map(Set::copyOf)
                .block();

        assertThat(routeIds).containsExactlyInAnyOrder(
                "identity-service",
                "wallet-service",
                "wallet-mobile-compatibility",
                "payment-service",
                "placetopay-compatibility",
                "transaction-service",
                "notification-service");
    }
}

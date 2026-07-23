package com.smartpayut.wallet.messaging;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartpayut.wallet.event.IdentityUserCreatedEvent;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class IdentityEventContractTest {
    @Test
    void deserializaContratoRealIdentity() throws Exception {
        UUID event = UUID.randomUUID(), user = UUID.randomUUID(), keycloak = UUID.randomUUID();
        String json = "{\"eventId\":\"" + event
                + "\",\"eventType\":\"identity.user.created\",\"eventVersion\":1,\"occurredAt\":\"2026-07-22T10:00:00-05:00\",\"userId\":\""
                + user + "\",\"keycloakId\":\"" + keycloak
                + "\",\"userName\":\"mario\",\"email\":\"m@test.com\",\"name\":\"Mario\",\"lastName\":\"López\",\"status\":\"ACTIVE\"}";
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        IdentityUserCreatedEvent e = mapper.readValue(json, IdentityUserCreatedEvent.class);
        assertEquals(event, e.eventId());
        assertEquals(user, e.userId());
        assertEquals(keycloak, e.keycloakId());
    }
}

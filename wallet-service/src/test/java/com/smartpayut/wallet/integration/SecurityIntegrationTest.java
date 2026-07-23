package com.smartpayut.wallet.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void accesoSinJwt() throws Exception {
        mvc.perform(get("/api/wallets/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void jwtValidoLlegaAlCasoDeUso() throws Exception {
        mvc.perform(get("/api/wallets/me").with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioNoConsultaWalletAjena() throws Exception {
        mvc.perform(get("/internal/wallets/users/" + UUID.randomUUID()).with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointInternoProtegido() throws Exception {
        mvc.perform(post("/internal/wallets/debit").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }
}

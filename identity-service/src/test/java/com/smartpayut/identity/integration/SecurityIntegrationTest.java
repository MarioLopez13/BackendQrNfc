package com.smartpayut.identity.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void accesoSinJwt() throws Exception {
        mvc.perform(post("/api/users/search").contentType("application/json").content("{\"page\":0,\"pageSize\":20}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accesoConRolInsuficiente() throws Exception {
        mvc.perform(post("/api/users/search").with(jwt().authorities(() -> "ROLE_USER")).contentType("application/json")
                .content("{\"page\":0,\"pageSize\":20}")).andExpect(status().isForbidden());
    }
}

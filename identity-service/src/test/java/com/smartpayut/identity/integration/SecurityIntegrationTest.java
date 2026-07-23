package com.smartpayut.identity.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    void operadorNoPuedeCrearUsuarioAdministrativamente() throws Exception {
        mvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_OPERATOR")).with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "userName": "new.user",
                          "email": "new.user@example.com",
                          "name": "New",
                          "lastName": "User",
                          "password": "Password123!"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void operadorNoPuedeEliminarUsuarioAdministrativamente() throws Exception {
        mvc.perform(delete("/api/users/{id}", java.util.UUID.randomUUID())
                .with(jwt().authorities(() -> "ROLE_OPERATOR"))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeAccederALaCreacionAdministrativa() throws Exception {
        mvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_ADMIN")).with(csrf())
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminPuedeAccederALaEliminacionAdministrativa() throws Exception {
        mvc.perform(delete("/api/users/{id}", java.util.UUID.randomUUID())
                .with(jwt().authorities(() -> "ROLE_ADMIN"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void resumenSinJwtEsRechazado() throws Exception {
        mvc.perform(get("/api/users/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioNoPuedeConsultarResumen() throws Exception {
        mvc.perform(get("/api/users/summary").with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void operadorPuedeConsultarResumen() throws Exception {
        mvc.perform(get("/api/users/summary").with(jwt().authorities(() -> "ROLE_OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resumen de usuarios obtenido correctamente"));
    }

    @Test
    void adminPuedeConsultarResumen() throws Exception {
        mvc.perform(get("/api/users/summary").with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.activeUsers").isNumber())
                .andExpect(jsonPath("$.data.inactiveUsers").isNumber())
                .andExpect(jsonPath("$.data.deletedUsers").isNumber());
    }
}

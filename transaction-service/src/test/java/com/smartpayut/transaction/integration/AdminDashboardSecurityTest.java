package com.smartpayut.transaction.integration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.smartpayut.transaction.controller.AdminTransactionController;
import com.smartpayut.transaction.dto.response.DashboardSummaryResponse;
import com.smartpayut.transaction.dto.response.PageResponse;
import com.smartpayut.transaction.security.TransactionSecurityConfig;
import com.smartpayut.transaction.service.DashboardQueryService;
import com.smartpayut.transaction.service.TransactionQueryService;

@WebMvcTest(AdminTransactionController.class)
@Import(TransactionSecurityConfig.class)
class AdminDashboardSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TransactionQueryService transactionQueryService;

    @MockBean
    private DashboardQueryService dashboardQueryService;

    @BeforeEach
    void configureSummary() {
        when(transactionQueryService.all(0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        when(dashboardQueryService.summary(anyInt())).thenAnswer(invocation -> {
            int days = invocation.getArgument(0);
            if (days < 1 || days > 90) {
                throw new IllegalArgumentException("days debe estar entre 1 y 90.");
            }
            return new DashboardSummaryResponse(
                    0,
                    0,
                    0,
                    0,
                    0,
                    BigDecimal.ZERO,
                    Map.of(),
                    List.of());
        });
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mvc.perform(get("/api/admin/transactions/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUserRole() throws Exception {
        mvc.perform(get("/api/admin/transactions/dashboard")
                .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsOperatorRole() throws Exception {
        mvc.perform(get("/api/admin/transactions/dashboard")
                .with(jwt().authorities(() -> "ROLE_OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void allowsAdminRoleAndUsesSevenDaysByDefault() throws Exception {
        mvc.perform(get("/api/admin/transactions/dashboard")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resumen del Dashboard obtenido correctamente"));

        verify(dashboardQueryService).summary(7);
    }

    @Test
    void adminTransactionEndpointStillReturnsTheGlobalHistory() throws Exception {
        mvc.perform(get("/api/admin/transactions")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(transactionQueryService).all(0, 20);
    }

    @Test
    void rejectsDaysBelowMinimum() throws Exception {
        mvc.perform(get("/api/admin/transactions/dashboard")
                .param("days", "0")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("days debe estar entre 1 y 90."));
    }

    @Test
    void rejectsDaysAboveMaximum() throws Exception {
        mvc.perform(get("/api/admin/transactions/dashboard")
                .param("days", "91")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("days debe estar entre 1 y 90."));
    }
}

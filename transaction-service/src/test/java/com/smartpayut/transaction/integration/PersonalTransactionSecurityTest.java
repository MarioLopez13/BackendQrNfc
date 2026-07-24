package com.smartpayut.transaction.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.smartpayut.transaction.controller.TransactionController;
import com.smartpayut.transaction.dto.response.PageResponse;
import com.smartpayut.transaction.dto.response.TransactionResponse;
import com.smartpayut.transaction.security.CurrentUserIdResolver;
import com.smartpayut.transaction.security.TransactionSecurityConfig;
import com.smartpayut.transaction.service.TransactionQueryService;

@WebMvcTest(TransactionController.class)
@Import({TransactionSecurityConfig.class, CurrentUserIdResolver.class})
class PersonalTransactionSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TransactionQueryService queryService;

    @Test
    void userIdClaimDefinesThePersonalHistory() throws Exception {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        when(queryService.mine(eq(userA), anyInt(), anyInt())).thenReturn(emptyPage());
        when(queryService.mine(eq(userB), anyInt(), anyInt())).thenReturn(emptyPage());

        mvc.perform(get("/api/transactions/me")
                .with(jwt().jwt(token -> token
                        .subject(UUID.randomUUID().toString())
                        .claim("user_id", userA.toString()))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/transactions/me")
                .with(jwt().jwt(token -> token
                        .subject(UUID.randomUUID().toString())
                        .claim("user_id", userB.toString()))))
                .andExpect(status().isOk());

        verify(queryService).mine(userA, 0, 20);
        verify(queryService).mine(userB, 0, 20);
        verify(queryService, never()).all(anyInt(), anyInt());
    }

    @Test
    void spoofedUserHeaderCannotOverrideTheJwtClaim() throws Exception {
        UUID authenticatedUser = UUID.randomUUID();
        UUID spoofedUser = UUID.randomUUID();
        when(queryService.mine(eq(authenticatedUser), anyInt(), anyInt())).thenReturn(emptyPage());

        mvc.perform(get("/api/transactions/me")
                .header("X-User-Id", spoofedUser)
                .with(jwt().jwt(token -> token
                        .subject(UUID.randomUUID().toString())
                        .claim("user_id", authenticatedUser.toString()))))
                .andExpect(status().isOk());

        verify(queryService).mine(authenticatedUser, 0, 20);
        verify(queryService, never()).mine(eq(spoofedUser), anyInt(), anyInt());
        verify(queryService, never()).all(anyInt(), anyInt());
    }

    @Test
    void authenticatedSubjectIsASafeCompatibilityFallback() throws Exception {
        UUID subject = UUID.randomUUID();
        when(queryService.mine(eq(subject), anyInt(), anyInt())).thenReturn(emptyPage());

        mvc.perform(get("/api/transactions/me")
                .with(jwt().jwt(token -> token.subject(subject.toString()))))
                .andExpect(status().isOk());

        verify(queryService).mine(subject, 0, 20);
        verify(queryService, never()).all(anyInt(), anyInt());
    }

    @Test
    void invalidJwtIdentifierNeverReturnsAllTransactions() throws Exception {
        mvc.perform(get("/api/transactions/me")
                .with(jwt().jwt(token -> token.subject("not-a-uuid"))))
                .andExpect(status().isBadRequest());

        verify(queryService, never()).mine(any(), anyInt(), anyInt());
        verify(queryService, never()).all(anyInt(), anyInt());
    }

    private PageResponse<TransactionResponse> emptyPage() {
        return new PageResponse<>(List.of(), 0, 20, 0, 0);
    }
}

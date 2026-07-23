package com.smartpayut.identity.service;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.domain.enumeration.UserStatus;
import com.smartpayut.identity.dto.request.UserSearchRequest;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserQueryServiceTest {
    UserAccountRepository accounts = mock(UserAccountRepository.class);
    UserProfileRepository profiles = mock(UserProfileRepository.class);
    UserQueryService service = new UserQueryService(accounts, profiles, new UserAccountMapper());

    UserAccount user() {
        UUID k = UUID.randomUUID();
        return new UserAccount(UUID.randomUUID(), k, "mario", "mario@test.com", "Mario", "Lopez");
    }

    @Test
    void consultaPorId() {
        var u = user();
        when(accounts.findById(u.getId())).thenReturn(Optional.of(u));
        assertEquals(u.getId(), service.byId(u.getId()).id());
    }

    @Test
    void consultaMeUsaSubjectKeycloak() {
        var u = user();
        when(accounts.findByKeycloakId(u.getKeycloakId())).thenReturn(Optional.of(u));
        assertEquals(u.getId(), service.me(u.getKeycloakId().toString()).id());
    }

    @Test
    void busquedaPaginada() {
        var u = user();
        when(accounts.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(u), PageRequest.of(0, 20), 1));
        var result = service.search(new UserSearchRequest(List.of(), "mario", 0, 20));
        assertEquals(1, result.totalCount());
        assertEquals("mario", result.items().getFirst().userName());
    }

    @Test
    void resumenCuentaTodosLosEstados() {
        when(accounts.count()).thenReturn(100L);
        when(accounts.countByStatus(UserStatus.ACTIVE)).thenReturn(80L);
        when(accounts.countByStatus(UserStatus.INACTIVE)).thenReturn(15L);
        when(accounts.countByStatus(UserStatus.DELETED)).thenReturn(5L);

        var result = service.summary();

        assertEquals(100L, result.totalUsers());
        assertEquals(80L, result.activeUsers());
        assertEquals(15L, result.inactiveUsers());
        assertEquals(5L, result.deletedUsers());
    }
}

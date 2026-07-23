package com.smartpayut.identity.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.smartpayut.identity.domain.entity.UserAccount;
import com.smartpayut.identity.domain.enumeration.UserStatus;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.repository.UserAccountRepository;
import com.smartpayut.identity.service.UserQueryService;

@DataJpaTest
@ActiveProfiles("test")
@Import({ UserQueryService.class, UserAccountMapper.class })
class UserSummaryIntegrationTest {

    @Autowired
    private UserAccountRepository repository;

    @Autowired
    private UserQueryService queryService;

    @Test
    void summarizesActiveInactiveAndDeletedAccounts() {
        repository.save(account("active", UserStatus.ACTIVE));
        repository.save(account("inactive", UserStatus.INACTIVE));
        repository.save(account("deleted", UserStatus.DELETED));

        var summary = queryService.summary();

        assertThat(summary.totalUsers()).isEqualTo(3);
        assertThat(summary.activeUsers()).isEqualTo(1);
        assertThat(summary.inactiveUsers()).isEqualTo(1);
        assertThat(summary.deletedUsers()).isEqualTo(1);
    }

    private UserAccount account(String suffix, UserStatus status) {
        UserAccount account = new UserAccount(
                UUID.randomUUID(),
                UUID.randomUUID(),
                suffix,
                suffix + "@smartpayut.test",
                "Test",
                suffix);
        account.update(null, null, null, status);
        return account;
    }
}

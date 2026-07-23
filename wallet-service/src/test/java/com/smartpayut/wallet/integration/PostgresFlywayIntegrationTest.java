package com.smartpayut.wallet.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class PostgresFlywayIntegrationTest {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migracionesYConstraintsReales() throws Exception {
        assertEquals(4, Flyway.configure().dataSource(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword())
                .locations("classpath:db/migration").load().migrate().migrationsExecuted);
        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
                PreparedStatement p = c.prepareStatement(
                        "insert into wallet(id,user_id,keycloak_id,balance,currency,status,created_at,updated_at,version) values(?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0)")) {
            p.setObject(1, java.util.UUID.randomUUID());
            p.setObject(2, java.util.UUID.randomUUID());
            p.setObject(3, java.util.UUID.randomUUID());
            p.setBigDecimal(4, new java.math.BigDecimal("-1.00"));
            p.setString(5, "USD");
            p.setString(6, "ACTIVE");
            assertThrows(SQLException.class, p::executeUpdate);
        }
    }
}

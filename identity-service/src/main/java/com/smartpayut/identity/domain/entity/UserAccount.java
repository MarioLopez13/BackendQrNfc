package com.smartpayut.identity.domain.entity;

import com.smartpayut.identity.domain.enumeration.UserStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccount {
    @Id
    private UUID id;
    @Column(name = "keycloak_id", nullable = false, unique = true)
    private UUID keycloakId;
    @Column(name = "username", nullable = false, unique = true)
    private String userName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "first_name", nullable = false)
    private String name;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserAccount() {
    }

    public UserAccount(UUID id, UUID keycloakId, String userName, String email, String name, String lastName) {
        this.id = id;
        this.keycloakId = keycloakId;
        this.userName = userName;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        status = UserStatus.ACTIVE;
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    public void update(String name, String lastName, String email, UserStatus status) {
        if (name != null)
            this.name = name;
        if (lastName != null)
            this.lastName = lastName;
        if (email != null)
            this.email = email.toLowerCase();
        if (status != null)
            this.status = status;
        updatedAt = OffsetDateTime.now();
    }

    public void markDeleted() {
        status = UserStatus.DELETED;
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getKeycloakId() {
        return keycloakId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

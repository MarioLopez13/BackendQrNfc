package com.smartpayut.identity.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount userAccount;
    private String phone;
    @Column(name = "avatar_url")
    private String image;
    @Column(name = "preferred_language")
    private String preferredLanguage;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserProfile() {
    }

    public UserProfile(UUID id, UserAccount userAccount) {
        this.id = id;
        this.userAccount = userAccount;
        preferredLanguage = "es";
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    public void update(String phone, String image, String language) {
        if (phone != null)
            this.phone = phone;
        if (image != null)
            this.image = image;
        if (language != null)
            this.preferredLanguage = language;
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }
}

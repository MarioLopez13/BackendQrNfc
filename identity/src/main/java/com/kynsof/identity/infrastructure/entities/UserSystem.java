package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.UserStatus;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.share.core.domain.EUserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
//@Audited
//@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "user_system",
        schema = "identity",
        indexes = {
                @Index(name = "idx_user_system_email", columnList = "email"),
                @Index(name = "idx_user_system_username", columnList = "userName"),
                @Index(name = "idx_user_system_keycloakid", columnList = "keyCloakId"),
                @Index(name = "idx_user_system_status", columnList = "status"),
                @Index(name = "idx_user_system_usertype", columnList = "userType"),
                // Índice compuesto para existsByEmailAndStatus query
                @Index(name = "idx_user_system_email_status", columnList = "email, status")
        }
)
public class UserSystem implements Serializable {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(unique = true)
    private String userName;
    @Column(unique = true)
    private String email;
    private String name;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Enumerated(EnumType.STRING)
    private EUserType userType;
    private String image;

    @Column(nullable = true)
    private UUID selectedBusiness;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserPermissionBusiness> userPermissionBusinesses = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(unique = true)
    private UUID keyCloakId;

    public UserSystem(UserSystemDto dto) {
        this.id = dto.getId();
        this.userName = dto.getUserName();
        this.email = dto.getEmail();
        this.name = dto.getName();
        this.lastName = dto.getLastName();
        this.status = dto.getStatus();
        this.userType = dto.getUserType() != null ? dto.getUserType() : EUserType.UNDEFINED;
        this.image = dto.getImage() != null ? dto.getImage() : null;
        this.selectedBusiness = dto.getSelectedBusiness() != null ? dto.getSelectedBusiness() : null;
        this.keyCloakId = dto.getKeyCloakId();
    }

    public UserSystemDto toAggregate() {
        UserSystemDto dto = new UserSystemDto(this.id, this.userName, this.email, this.name, this.lastName, this.status, this.image);
        dto.setUserType(userType);
        dto.setImage(image);
        dto.setSelectedBusiness(selectedBusiness);
        dto.setCreatedAt(createdAt.toLocalDate());
        dto.setKeyCloakId(keyCloakId);
        return dto;
    }
}

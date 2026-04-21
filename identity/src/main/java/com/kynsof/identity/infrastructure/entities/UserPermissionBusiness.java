package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.UserPermissionBusinessDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_permission_business", schema = "identity")
public class UserPermissionBusiness {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserSystem user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public UserPermissionBusiness(UserPermissionBusinessDto userRoleBusinessDto) {
        this.id = userRoleBusinessDto.getId();
        this.user = new UserSystem(userRoleBusinessDto.getUser());
        this.permission = new Permission(userRoleBusinessDto.getPermission());
        this.business = new Business(userRoleBusinessDto.getBusiness());
    }

    public UserPermissionBusinessDto toAggregate () {
        return new UserPermissionBusinessDto(id, user.toAggregate(), permission.toAggregate(), business.toAggregate());
    }
}

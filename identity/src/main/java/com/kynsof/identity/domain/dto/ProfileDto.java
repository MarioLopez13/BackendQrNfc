package com.kynsof.identity.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ProfileDto implements Serializable {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Set<PermissionDto> permissions = new HashSet<>();
    private LocalDateTime createdAt;

    /**
     * Constructor para crear un nuevo Profile.
     */
    public ProfileDto(UUID id, String code, String name, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * Constructor completo con permisos.
     */
    public ProfileDto(UUID id, String code, String name, String description, Set<PermissionDto> permissions, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.createdAt = createdAt;
    }
}
